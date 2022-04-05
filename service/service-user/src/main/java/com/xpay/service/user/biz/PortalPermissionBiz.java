package com.xpay.service.user.biz;

import com.xpay.common.statics.constants.common.PublicStatus;
import com.xpay.common.statics.enums.merchant.MchTypeEnum;
import com.xpay.common.statics.enums.user.AuthTypeEnum;
import com.xpay.common.statics.enums.user.UserTypeEnum;
import com.xpay.common.statics.enums.user.RevokeAuthTypeEnum;
import com.xpay.common.statics.enums.user.RoleTypeEnum;
import com.xpay.common.statics.exception.BizException;
import com.xpay.common.statics.query.PageQuery;
import com.xpay.common.statics.result.PageResult;
import com.xpay.common.utils.BeanUtil;
import com.xpay.common.utils.StringUtil;
import com.xpay.common.utils.ValidateUtil;
import com.xpay.facade.user.dto.PortalAuthDto;
import com.xpay.facade.user.dto.PortalRoleDto;
import com.xpay.service.user.dao.*;
import com.xpay.service.user.entity.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 商户后台权限管理
 */
@Service
public class PortalPermissionBiz {
    @Autowired
    private PortalRoleUserDao portalRoleUserDao;

    @Autowired
    private PortalAuthDao portalAuthDao;

    @Autowired
    private PortalRoleDao portalRoleDao;

    @Autowired
    private PortalRoleAuthDao portalRoleAuthDao;

    @Autowired
    private PortalUserDao portalUserDao;

    @Autowired
    private RevokePermissionBiz revokePermissionBiz;


    //<editor-fold desc="权限点管理">
    public void createAuth(PortalAuthDto portalAuth) {
        String parentNumber = "";
        if (portalAuth.getParentId() == null || portalAuth.getParentId() == 0) {
            portalAuth.setParentId(0L);
            portalAuth.setAuthType(AuthTypeEnum.MENU_TYPE.getValue());
        } else {
            PortalAuth parent = portalAuthDao.getById(portalAuth.getParentId());
            if (parent == null) {
                throw new BizException(BizException.BIZ_INVALID, "父节点不存在");
            } else if (parent.getAuthType() != AuthTypeEnum.MENU_TYPE.getValue()) {
                throw new BizException(BizException.BIZ_INVALID, "父节点必须是菜单类型");
            }
            parentNumber = parent.getNumber();
        }

        String format = "%1$03d";//不足3位数时，前面补0，超过3位数时，直接原样输出
        String number = portalAuth.getNumber();
        if(StringUtil.isNotEmpty(number)){
            number = number.trim();
            validAuthNumber(number, parentNumber);
            number = number.trim().substring(parentNumber.trim().length());
            number = String.format(format, Integer.parseInt(number));
        }else{
            String brotherNumber = portalAuthDao.getBrotherMaximalNumber(portalAuth.getParentId());
            if(StringUtil.isEmpty(brotherNumber)){
                number = "001";
            }else{
                String maxStr = brotherNumber.substring(parentNumber.length());
                Integer maxVal = Integer.parseInt(maxStr) + 1;
                number = String.format(format, maxVal);
            }
        }

        portalAuth.setNumber(parentNumber + number);
        portalAuth.setCreateTime(new Date());
        portalAuth.setVersion(0);
        if(StringUtil.isEmpty(portalAuth.getName())){
            portalAuth.setName("");
        }
        if(StringUtil.isEmpty(portalAuth.getPermissionFlag())){
            portalAuth.setPermissionFlag("");
        }
        if(StringUtil.isEmpty(portalAuth.getUrl())){
            portalAuth.setUrl("");
        }
        if(StringUtil.isEmpty(portalAuth.getIcon())){
            portalAuth.setIcon("");
        }
        portalAuthDao.insert(BeanUtil.newAndCopy(portalAuth, PortalAuth.class));
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateAuth(PortalAuthDto portalAuth) {
        PortalAuth authOld = portalAuthDao.getById(portalAuth.getId());
        portalAuth.setParentId(authOld.getParentId());
        if(StringUtil.isEmpty(portalAuth.getNumber())){
            portalAuth.setNumber(authOld.getNumber());
        }else{
            portalAuth.setNumber(portalAuth.getNumber().trim());
        }

        List<PortalAuth> needUpdateChildrenNodes = new ArrayList<>();
        if(! portalAuth.getNumber().equals(authOld.getNumber())){//如果有改变排序编号
            String parentNumber = "";
            if(authOld.getParentId() > 0){
                PortalAuth parent = portalAuthDao.getById(authOld.getParentId());
                parentNumber = parent.getNumber();
            }
            validAuthNumber(portalAuth.getNumber(), parentNumber);

            String numberNew = portalAuth.getNumber().trim().substring(parentNumber.trim().length());//当前节点的编号
            String format = "%1$03d";//不足3位数时，前面补0，超过3位数时，直接原样输出
            numberNew = parentNumber + String.format(format, Integer.parseInt(numberNew));
            portalAuth.setNumber(numberNew);

            //替换当前节点的所有子节点的排序编号
            List<PortalAuth> allAuthNodes = portalAuthDao.listAll("NUMBER ASC");
            replaceChildrenNumber(portalAuth.getId(), authOld.getNumber(), numberNew, allAuthNodes, needUpdateChildrenNodes);
        }

        portalAuthDao.updateIfNotNull(BeanUtil.newAndCopy(portalAuth, PortalAuth.class));
        if(needUpdateChildrenNodes.size() > 0) {
            portalAuthDao.update(needUpdateChildrenNodes);
        }
    }

    /**
     * 删除权限及其子权限,同时也会删除与这些权限相关的role_auth映射关联
     * 若存在菜单类的子权限，该方法会抛出异常
     *
     * @param authId .
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteAuthById(Long authId) {
        List<PortalAuth> subAuths = portalAuthDao.listByParentId(authId);
        boolean isHadSubMenu = subAuths.stream().anyMatch(p -> p.getAuthType() == AuthTypeEnum.MENU_TYPE.getValue());
        if (isHadSubMenu) {
            throw new BizException(BizException.BIZ_INVALID, "当前节点存在子菜单，不能直接删除");
        }
        List<Long> deleteAuthIds = subAuths.stream().map(PortalAuth::getId).collect(Collectors.toList());
        deleteAuthIds.add(authId);
        portalAuthDao.deleteByIdList(deleteAuthIds);       //删除权限以及其子权限
        portalRoleAuthDao.deleteByAuthIds(deleteAuthIds);  //删除关联的所有role_auth
    }

    /**
     * 查询userId所拥有的所有权限
     * @param userId .
     * @return
     */
    public List<PortalAuthDto> listAuthByUserId(Long userId) {
        if (userId == null) {
            throw new BizException(BizException.BIZ_INVALID, "userId不能为空");
        }
        //获取得到所有的roleId
        List<Long> roleIds = portalRoleUserDao.listByUserId(userId)
                .stream()
                .map(PortalRoleUser::getRoleId)
                .collect(Collectors.toList());
        if (roleIds.size() == 0) {
            return Collections.emptyList();
        }
        List<PortalAuth> auths = portalAuthDao.listByRoleIds(roleIds, null);
        return BeanUtil.newAndCopy(auths, PortalAuthDto.class);
    }

    /**
     * 查询所有权限节点
     * @return .
     */
    public List<PortalAuthDto> listAllAuth(String sortColumn) {
        if(StringUtil.isEmpty(sortColumn)){
            sortColumn = "NUMBER ASC";
        }
        List<PortalAuth> auths = portalAuthDao.listAll(sortColumn);
        return BeanUtil.newAndCopy(auths, PortalAuthDto.class);
    }

    /**
     * 查询某个商户所拥有的所有权限
     * @param mchNo
     * @return
     */
    public List<PortalAuthDto> listAllAuthByMchNo(String mchNo){
        if(StringUtil.isEmpty(mchNo)){
            throw new BizException(BizException.BIZ_INVALID, "商户编号不能为空！");
        }

        //取得当前商户下所有admin类型的用户
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("type", UserTypeEnum.ADMIN.getValue());
        List<Long> userIdList = portalUserDao.listUserIdByMap(mchNo, paramMap);
        if(userIdList == null || userIdList.isEmpty()){
            return new ArrayList<>();
        }

        //取得所有admin类型的用户所关联的角色
        List<Long> roleIdList = new ArrayList<>();
        List<PortalRoleUser> roleUserList = portalRoleUserDao.listByUserIds(userIdList);
        if(roleUserList == null || roleUserList.isEmpty()){
            return new ArrayList<>();
        }else{
            roleUserList.forEach(obj -> roleIdList.add(obj.getRoleId()));
        }

        //根据角色Id取得这些角色所关联的权限节点
        List<PortalAuth> auths = portalAuthDao.listByRoleIds(roleIdList, null);
        return BeanUtil.newAndCopy(auths, PortalAuthDto.class);
    }

    /**
     * 查询当前角色关联的所有权限节点Id
     * @param roleId
     * @return
     */
    public List<Long> listAuthIdsByRoleId(Long roleId) {
        return listAuthByRoleId(roleId)
                .stream()
                .map(PortalAuthDto::getId)
                .collect(Collectors.toList());
    }

    /**
     * 查询当前角色关联的所有权限节点
     * @param roleId
     * @return
     */
    public List<PortalAuthDto> listAuthByRoleId(Long roleId) {
        List<PortalAuth> auths = portalAuthDao.listByRoleIds(Collections.singletonList(roleId), null);
        return BeanUtil.newAndCopy(auths, PortalAuthDto.class);
    }

    /**
     * 查询当前权限节点的所有子节点
     * @param parentId
     * @return
     */
    public List<PortalAuthDto> listAuthByParentId(Long parentId) {
        List<PortalAuth> auths = portalAuthDao.listByParentId(parentId);
        return BeanUtil.newAndCopy(auths, PortalAuthDto.class);
    }

    /**
     * 根据id查询权限
     * @param id
     * @return
     */
    public PortalAuthDto getAuthById(Long id) {
        PortalAuth auth = portalAuthDao.getById(id);
        return BeanUtil.newAndCopy(auth, PortalAuthDto.class);
    }

    /**
     * 查询自身节点和其父节点
     * @param id
     * @return
     */
    public PortalAuthDto getSelfAuthAndParentAuth(Long id) {
        PortalAuth auth = portalAuthDao.getById(id);
        if (auth == null) {
            return null;
        }

        PortalAuthDto authDto = BeanUtil.newAndCopy(auth, PortalAuthDto.class);
        if (authDto.getParentId() > 0){
            PortalAuth pAuth = portalAuthDao.getById(auth.getParentId());
            authDto.setParent(BeanUtil.newAndCopy(pAuth, PortalAuthDto.class));
        }
        return authDto;
    }
    //</editor-fold>


    //<editor-fold desc="角色管理">
    /**
     * 创建角色
     * @param portalRole
     */
    public void createRole(PortalRoleDto portalRole) {
        if(portalRole.getRoleType() == null){
            throw new BizException(BizException.PARAM_INVALID, "角色类型不能为空");
        }else if(RoleTypeEnum.getEnum(portalRole.getRoleType()) == null){
            throw new BizException(BizException.PARAM_INVALID, "未识别的角色类型");
        }else if(StringUtil.isEmpty(portalRole.getRoleName())){
            throw new BizException(BizException.PARAM_INVALID, "角色名称不能为空");
        }else if(portalRole.getMchType() == null){
            throw new BizException(BizException.PARAM_INVALID, "商户类型不能为空");
        }else if(MchTypeEnum.getEnum(portalRole.getMchType()) == null){
            throw new BizException(BizException.PARAM_INVALID, "未识别的商户类型");
        }

        if(portalRole.getRoleType() == RoleTypeEnum.ADMIN.getValue()){
            portalRole.setMchNo("");
        }else if(StringUtil.isEmpty(portalRole.getMchNo())){
            throw new BizException(BizException.PARAM_INVALID, "商户编号不能为空");
        }

        portalRole.setVersion(0);
        portalRole.setCreateTime(new Date());
        if(portalRole.getAutoAssign() == null){
            portalRole.setAutoAssign(PublicStatus.INACTIVE);
        }
        if(StringUtil.isEmpty(portalRole.getRemark())){
            portalRole.setRemark("");
        }

        portalRoleDao.insert(BeanUtil.newAndCopy(portalRole, PortalRole.class));
    }

    /**
     * 删除普通角色
     * @param id
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteRoleById(Long id) {
        if(id == null) {
            throw new BizException(BizException.BIZ_INVALID, "id不能为空");
        }

        PortalRole role = portalRoleDao.getById(id);
        if(role == null){
            throw new BizException(BizException.BIZ_INVALID, "角色不存在");
        }else if(role.getRoleType() == RoleTypeEnum.ADMIN.getValue()){
            throw new BizException(BizException.BIZ_INVALID, "当前角色不能删除");
        }
        portalRoleAuthDao.deleteByRoleId(id);
        portalRoleUserDao.deleteByRoleId(id);
        portalRoleDao.deleteById(id);
    }

    /**
     * 删除管理员角色
     * @param id
     * @param modifier
     * @param remark
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteAdminRoleById(Long id, String modifier, String remark){
        //1.参数校验
        if(id == null){
            throw new BizException(BizException.BIZ_INVALID, "角色id不能为空");
        }
        PortalRole role = portalRoleDao.getById(id);
        if(role == null){
            throw new BizException(BizException.BIZ_INVALID, "角色记录不存在");
        }else if(role.getRoleType() != RoleTypeEnum.ADMIN.getValue()){
            throw new BizException(BizException.BIZ_INVALID, "角色类型不匹配");
        }

        //2.找到所有需要进行权限回收的商户编号
        List<String> revokeMchNos = new ArrayList<>();
        List<Long> userIds = portalRoleUserDao.listUserIdByRoleId(id);
        if(! userIds.isEmpty()){
            revokeMchNos = portalUserDao.listMchNoByUserIdList(userIds);
        }

        //3.删除角色及其关联记录
        portalRoleAuthDao.deleteByRoleId(id);
        portalRoleUserDao.deleteByRoleId(id);
        portalRoleDao.deleteById(id);

        //4.执行权限回收
        if(revokeMchNos != null && !revokeMchNos.isEmpty()){
            revokePermissionBiz.revokeMerchantPermission(RevokeAuthTypeEnum.DELETE_ROLE.getValue(),
                    role.getRoleName(), revokeMchNos, modifier, remark);
        }
    }

    /**
     * 更新角色信息，其中：商户类型、商户编号、角色类型 不能修改
     * @param portalRole
     */
    public void updateRole(PortalRoleDto portalRole) {
        if(portalRole == null){
            throw new BizException(BizException.BIZ_INVALID, "参数不能为空");
        }
        
        PortalRoleDto roleOld = getRoleById(portalRole.getId());
        portalRole.setCreateTime(roleOld.getCreateTime());
        portalRole.setMchType(roleOld.getMchType());
        portalRole.setMchNo(roleOld.getMchNo());
        portalRole.setRoleType(roleOld.getRoleType());
        if(StringUtil.isEmpty(portalRole.getRoleName())){
            portalRole.setRoleName(roleOld.getRoleName());
        }

        portalRoleDao.updateIfNotNull(BeanUtil.newAndCopy(portalRole, PortalRole.class));
    }

    public List<PortalRoleDto> listAllRoles(Map<String, Object> paramMap) {
        List<PortalRole> portalRoles = portalRoleDao.listBy(paramMap);
        return BeanUtil.newAndCopy(portalRoles, PortalRoleDto.class);
    }

    public List<PortalRoleDto> listAllAdminRoles(Integer mchType) {
        List<PortalRole> portalRoles = portalRoleDao.listAllAdminRoles(mchType);
        return BeanUtil.newAndCopy(portalRoles, PortalRoleDto.class);
    }

    /**
     * 查询当前商户下的所有角色(不包含管理员角色)
     * @param merchantNo
     * @return
     */
    public List<PortalRoleDto> listRoleByMerchantNo(String merchantNo) {
        if (StringUtil.isEmpty(merchantNo)) {
            throw new BizException(BizException.BIZ_INVALID, "merchantNo不能为空");
        }
        List<PortalRole> portalRoles = portalRoleDao.listRoleByMerchantNo(merchantNo);
        return BeanUtil.newAndCopy(portalRoles, PortalRoleDto.class);
    }

    public PageResult<List<PortalRoleDto>> listRolePage(Map<String, Object> paramMap, PageQuery pageQuery) {
        PageResult<List<PortalRole>> result = portalRoleDao.listPage(paramMap, pageQuery);
        return PageResult.newInstance(BeanUtil.newAndCopy(result.getData(), PortalRoleDto.class), result) ;
    }

    /**
     *
     * @param userId
     * @param mchNo
     * @return
     */
    public List<PortalRoleDto> listRolesByUserId(Long userId, String mchNo) {
        List<Long> roleIds = portalRoleUserDao.listByUserId(userId)
                .stream()
                .map(PortalRoleUser::getRoleId)
                .collect(Collectors.toList());
        if (roleIds.size() == 0) {
            return Collections.emptyList();
        }

        List<PortalRole> portalRoles = portalRoleDao.listByIdList(roleIds);
        if(StringUtil.isEmpty(mchNo)){
            return BeanUtil.newAndCopy(portalRoles, PortalRoleDto.class);
        }else{
            List<PortalRoleDto> portalRoleList = new ArrayList<>();
            for(PortalRole role : portalRoles){
                if(mchNo.equals(role.getMchNo())){//过滤掉非当前商户下的角色
                    portalRoleList.add(BeanUtil.newAndCopy(role, PortalRoleDto.class));
                }
            }
            return portalRoleList;
        }
    }

    public PortalRoleDto getRoleById(Long id) {
        if(id == null) {
            return null;
        }
        PortalRole role = portalRoleDao.getById(id);
        return BeanUtil.newAndCopy(role, PortalRoleDto.class);
    }

    /**
     * 给普通角色分配权限
     * @param roleId
     * @param authIds
     */
    @Transactional(rollbackFor = Exception.class)
    public void assignRolePermission(Long roleId, List<Long> authIds) {
        //1.参数校验
        if (roleId == null) {
            throw new BizException(BizException.BIZ_INVALID, "roleId不能为空");
        }
        PortalRole portalRole = portalRoleDao.getById(roleId);
        if(portalRole == null){
            throw new BizException(BizException.BIZ_INVALID, "当前角色不存在");
        }else if(portalRole.getRoleType() == RoleTypeEnum.ADMIN.getValue()){
            throw new BizException(BizException.BIZ_INVALID, "角色类型不允许！");
        }

        //2.添加菜单权限
        List<PortalRoleAuth> roleAuthList = null;
        if (authIds != null && !authIds.isEmpty()) {
            roleAuthList = authIds.stream().map(p -> {
                PortalRoleAuth portalRoleAuth = new PortalRoleAuth();
                portalRoleAuth.setAuthId(p);
                portalRoleAuth.setRoleId(roleId);
                portalRoleAuth.setMchNo(portalRole.getMchNo());
                return portalRoleAuth;
            }).collect(Collectors.toList());
        }

        //3.先删除所有的菜单权限和功能权限
        portalRoleAuthDao.deleteByRoleId(roleId);

        //4.再重新插入关联记录
        if(roleAuthList != null && roleAuthList.size() > 0){
            portalRoleAuthDao.insert(roleAuthList);
        }
    }

    /**
     * 给管理员角色分配权限
     * @param roleId
     * @param authIds
     * @param modifier
     * @param remark
     */
    @Transactional(rollbackFor = Exception.class)
    public void assignAdminRolePermission(Long roleId, List<Long> authIds, String modifier, String remark) {
        //1.参数校验
        PortalRole portalRole;
        if (roleId == null) {
            throw new BizException(BizException.BIZ_INVALID, "roleId不能为空");
        }else if((portalRole = portalRoleDao.getById(roleId)) == null){
            throw new BizException(BizException.BIZ_INVALID, "当前角色不存在");
        }else if(portalRole.getRoleType() != RoleTypeEnum.ADMIN.getValue()){
            throw new BizException(BizException.BIZ_INVALID, "当前角色不属于管理员角色");
        }

        //2.封装 角色-权限 的关联记录
        List<PortalRoleAuth> roleAuthList = null;
        String mchNo = portalRole.getMchNo();
        if (authIds != null && !authIds.isEmpty()) {
            roleAuthList = authIds.stream().map(p -> {
                PortalRoleAuth portalRoleAuth = new PortalRoleAuth();
                portalRoleAuth.setAuthId(p);
                portalRoleAuth.setRoleId(roleId);
                portalRoleAuth.setMchNo(mchNo);
                return portalRoleAuth;
            }).collect(Collectors.toList());
        }

        //3.如果当前角色有减少权限，则找到需要执行回收权限的商户编号
        List<String> revokeMchNos = new ArrayList<>();
        List<Long> authIdsOld = portalRoleAuthDao.listAuthIdByRoleIds(Collections.singletonList(roleId));
        if(authIdsOld != null && !authIdsOld.isEmpty()){
            boolean isNeedRevokePermission;
            //旧权限中有，但新权限中已没有的，需要回收权限
            if(authIds == null || authIds.isEmpty()){
                isNeedRevokePermission = true;
            }else{
                isNeedRevokePermission = authIdsOld.stream().anyMatch(funcId -> !authIds.contains(funcId));
            }

            //找到所有需要执行权限回收的商户
            if(isNeedRevokePermission){
                List<Long> userIds = portalRoleUserDao.listUserIdByRoleId(roleId);
                if(! userIds.isEmpty()){
                    revokeMchNos = portalUserDao.listMchNoByUserIdList(userIds);
                }
            }
        }

        //4.先删除当前角色与权限节点的所有关联记录(相当于删除了当前角色的所有权限)
        portalRoleAuthDao.deleteByRoleId(roleId);
        //5.再重新插入关联记录(相当于重新分配了新权限)
        if(roleAuthList != null && roleAuthList.size() > 0){
            portalRoleAuthDao.insert(roleAuthList);
        }
        //6.执行商户权限回收
        if(revokeMchNos != null && !revokeMchNos.isEmpty()){
            revokePermissionBiz.revokeMerchantPermission(RevokeAuthTypeEnum.CHANGE_ROLE_AUTH.getValue(),
                    portalRole.getRoleName(), revokeMchNos, modifier, remark);
        }
    }
    //</editor-fold>


    private void replaceChildrenNumber(Long parentId, String parentNumOld, String parentNumNew, List<PortalAuth> allAuthNodes, List<PortalAuth> childrenNodes){
        if(allAuthNodes == null || allAuthNodes.isEmpty()) {
            return;
        }

        for(PortalAuth portalAuth : allAuthNodes) {
            if (parentId.equals(portalAuth.getParentId())) {
                String numberOld = portalAuth.getNumber();
                String numberNew = numberOld.replace(parentNumOld, parentNumNew);
                portalAuth.setNumber(numberNew);
                childrenNodes.add(portalAuth);
                replaceChildrenNumber(portalAuth.getId(), numberOld, numberNew, allAuthNodes, childrenNodes);
            }
        }
    }

    private void validAuthNumber(String selfNumber, String parentNumber){
        if(! ValidateUtil.isNumericOnly(selfNumber)){
            throw new BizException(BizException.BIZ_INVALID, "排序编号须为纯数字格式");
        }else if(StringUtil.isNotEmpty(parentNumber) && !selfNumber.startsWith(parentNumber)){
            throw new BizException(BizException.BIZ_INVALID, "排序编号须以父节点的排序编号开头");
        }else if(selfNumber.trim().length() <= parentNumber.trim().length()){
            throw new BizException(BizException.BIZ_INVALID, "排序编号长度须比父节点的排序编号长");
        }

        String currNodeNumber = selfNumber.trim().substring(parentNumber.trim().length());
        if(currNodeNumber.length() > 3){
            throw new BizException(BizException.BIZ_INVALID, "子节点的排序编号长度不能超过父节点的排序编号长度3位数");
        }
    }
}
