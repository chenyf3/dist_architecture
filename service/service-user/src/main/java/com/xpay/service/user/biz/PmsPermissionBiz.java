package com.xpay.service.user.biz;

import com.xpay.common.statics.enums.user.AuthTypeEnum;
import com.xpay.common.statics.enums.user.UserTypeEnum;
import com.xpay.common.statics.exception.BizException;
import com.xpay.common.statics.query.PageQuery;
import com.xpay.common.statics.result.PageResult;
import com.xpay.common.utils.BeanUtil;
import com.xpay.common.utils.StringUtil;
import com.xpay.common.utils.ValidateUtil;
import com.xpay.facade.user.dto.*;
import com.xpay.service.user.dao.*;
import com.xpay.service.user.entity.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 *
 */
@Service
public class PmsPermissionBiz {
    @Autowired
    private PmsRoleUserDao pmsRoleUserDao;

    @Autowired
    private PmsAuthDao pmsAuthDao;

    @Autowired
    private PmsRoleDao pmsRoleDao;

    @Autowired
    private PmsRoleAuthDao pmsRoleAuthDao;

    @Autowired
    private PmsUserDao pmsUserDao;


    //<editor-fold desc="权限管理">
    /**
     * 创建权限
     * @param pmsAuth
     */
    public void createAuth(PmsAuthDto pmsAuth) {
        String parentNumber = "";
        if (pmsAuth.getParentId() == null || pmsAuth.getParentId() == 0) {
            pmsAuth.setParentId(0L);
            pmsAuth.setAuthType(AuthTypeEnum.MENU_TYPE.getValue());
        } else {
            PmsAuth parent = pmsAuthDao.getById(pmsAuth.getParentId());
            if (parent == null) {
                throw new BizException(BizException.BIZ_INVALID, "父节点不存在");
            } else if (parent.getAuthType() != AuthTypeEnum.MENU_TYPE.getValue()) {
                throw new BizException(BizException.BIZ_INVALID, "父节点必须是菜单类型");
            }
            parentNumber = parent.getNumber();
        }

        String format = "%1$03d";//不足3位数时，前面补0，超过3位数时，直接原样输出
        String number = pmsAuth.getNumber();
        if(StringUtil.isNotEmpty(number)){
            number = number.trim();
            validAuthNumber(number, parentNumber);
            number = number.trim().substring(parentNumber.trim().length());
            number = String.format(format, Integer.parseInt(number));
        }else{
            String brotherNumber = pmsAuthDao.getBrotherMaximalNumber(pmsAuth.getParentId());
            if(StringUtil.isEmpty(brotherNumber)){
                number = "001";
            }else{
                String maxStr = brotherNumber.substring(parentNumber.length());
                Integer maxVal = Integer.parseInt(maxStr) + 1;
                number = String.format(format, maxVal);
            }
        }
        pmsAuth.setNumber(parentNumber + number);
        pmsAuth.setCreateTime(new Date());
        pmsAuth.setVersion(0);
        if(StringUtil.isEmpty(pmsAuth.getName())){
            pmsAuth.setName("");
        }
        if(StringUtil.isEmpty(pmsAuth.getPermissionFlag())){
            pmsAuth.setPermissionFlag("");
        }
        if(StringUtil.isEmpty(pmsAuth.getUrl())){
            pmsAuth.setUrl("");
        }
        if(StringUtil.isEmpty(pmsAuth.getIcon())){
            pmsAuth.setIcon("");
        }

        PmsAuth authDo = BeanUtil.newAndCopy(pmsAuth, PmsAuth.class);
        pmsAuthDao.insert(authDo);
    }

    /**
     * 更新节点
     * @param pmsAuth
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateAuth(PmsAuthDto pmsAuth) {
        PmsAuth authOld = pmsAuthDao.getById(pmsAuth.getId());
        pmsAuth.setParentId(authOld.getParentId());
        if(StringUtil.isEmpty(pmsAuth.getNumber())){
            pmsAuth.setNumber(authOld.getNumber());
        }else{
            pmsAuth.setNumber(pmsAuth.getNumber().trim());
        }

        List<PmsAuth> needUpdateChildrenNodes = new ArrayList<>();
        if(! pmsAuth.getNumber().equals(authOld.getNumber())){ //如果有改变排序编号
            String parentNumber = "";
            if(authOld.getParentId() > 0){
                PmsAuth parent = pmsAuthDao.getById(authOld.getParentId());
                parentNumber = parent.getNumber();
            }
            validAuthNumber(pmsAuth.getNumber(), parentNumber);

            String numberNew = pmsAuth.getNumber().trim().substring(parentNumber.trim().length());//当前节点的编号
            String format = "%1$03d";//不足3位数时，前面补0，超过3位数时，直接原样输出
            numberNew = parentNumber + String.format(format, Integer.parseInt(numberNew));
            pmsAuth.setNumber(numberNew);

            //替换当前节点的所有子节点的排序编号
            List<PmsAuth> allAuthNodes = pmsAuthDao.listAll("NUMBER ASC");
            replaceChildrenNumber(pmsAuth.getId(), authOld.getNumber(), numberNew, allAuthNodes, needUpdateChildrenNodes);
        }

        pmsAuthDao.updateIfNotNull(BeanUtil.newAndCopy(pmsAuth, PmsAuth.class));
        if(needUpdateChildrenNodes.size() > 0) {
            pmsAuthDao.update(needUpdateChildrenNodes);
        }
    }

    /**
     * 删除权限以及其子权限，同时也会删除与这些权限相关的role_auth映射关联
     * 若存在菜单类的子权限，该方法会抛出异常
     *
     * @param authId .
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteAuthById(Long authId) {
        List<PmsAuth> subAuths = pmsAuthDao.listByParentId(authId);
        boolean isHadSubMenu = subAuths.stream().anyMatch(p -> p.getAuthType() == AuthTypeEnum.MENU_TYPE.getValue());
        if (isHadSubMenu) {
            throw new BizException(BizException.BIZ_INVALID, "当前节点存在子菜单，不能直接删除");
        }
        List<Long> deleteIds = subAuths.stream().map(PmsAuth::getId).collect(Collectors.toList());
        deleteIds.add(authId);
        pmsAuthDao.deleteByIdList(deleteIds);           //删除权限以及其子权限
        pmsRoleAuthDao.deleteByAuthIds(deleteIds);  //删除关联的所有role_auth
    }

    /**
     * 根据userId获取到其拥有的所有权限
     *
     * @param userId .
     * @return
     */
    public List<PmsAuthDto> listAuthByUserId(Long userId) {
        if (userId == null) {
            throw new BizException(BizException.BIZ_INVALID, "userId不能为空");
        }

        PmsUser user = pmsUserDao.getById(userId);
        if (user == null) {
            throw new BizException(BizException.BIZ_INVALID, "用户不存在");
        } else if (user.getType() == UserTypeEnum.ADMIN.getValue()) {
            return listAllAuth(null);
        } else {
            //获取得到所有的roleId
            List<Long> roleIds = pmsRoleUserDao.listByUserId(userId).stream().map(PmsRoleUser::getRoleId).collect(Collectors.toList());
            if (roleIds.size() == 0) {
                return Collections.emptyList();
            }
            List<PmsAuth> pmsAuths = pmsAuthDao.listByRoleIds(roleIds, null);
            return BeanUtil.newAndCopy(pmsAuths, PmsAuthDto.class);
        }
    }

    /**
     * 获取得到所有的权限节点
     *
     * @return .
     */
    public List<PmsAuthDto> listAllAuth(String sortColumn) {
        if(StringUtil.isEmpty(sortColumn)){
            sortColumn = "NUMBER ASC";
        }
        List<PmsAuth> pmsAuths = pmsAuthDao.listAll(sortColumn);
        return BeanUtil.newAndCopy(pmsAuths, PmsAuthDto.class);
    }

    /**
     * 根据角色id取得该角色关联的所有权限节点Id
     * @param roleId
     * @return
     */
    public List<Long> listAuthIdsByRoleId(Long roleId) {
        return listAuthByRoleId(roleId)
                .stream()
                .map(PmsAuthDto::getId)
                .collect(Collectors.toList());
    }

    /**
     * 根据角色id取得该角色关联的所有权限节点
     * @param roleId
     * @return
     */
    public List<PmsAuthDto> listAuthByRoleId(Long roleId) {
        List<PmsAuth> pmsAuths = pmsAuthDao.listByRoleIds(Collections.singletonList(roleId), null);
        return BeanUtil.newAndCopy(pmsAuths, PmsAuthDto.class);
    }

    /**
     * 取得当前节点下的所有子节点
     * @param parentId
     * @return
     */
    public List<PmsAuthDto> listAuthByParentId(Long parentId) {
        List<PmsAuth> pmsAuths = pmsAuthDao.listByParentId(parentId);
        return BeanUtil.newAndCopy(pmsAuths, PmsAuthDto.class);
    }

    public PmsAuthDto getAuthById(Long id) {
        PmsAuth pmsAuth = pmsAuthDao.getById(id);
        return BeanUtil.newAndCopy(pmsAuth, PmsAuthDto.class);
    }

    /**
     * 查询自身节点和其父节点
     * @param id
     * @return
     */
    public PmsAuthDto getSelfAuthAndParentAuth(Long id) {
        PmsAuth pmsAuth = pmsAuthDao.getById(id);
        if (pmsAuth == null) {
            return null;
        }

        PmsAuthDto authDto = BeanUtil.newAndCopy(pmsAuth, PmsAuthDto.class);
        if (authDto.getParentId() > 0) {
            PmsAuth pAuth = pmsAuthDao.getById(pmsAuth.getParentId());
            authDto.setParent(BeanUtil.newAndCopy(pAuth, PmsAuthDto.class));
        }
        return authDto;
    }
    //</editor-fold>


    //<editor-fold desc="角色管理">

    /**
     * 创建角色
     *
     * @param pmsRole 角色
     */
    public void createRole(PmsRoleDto pmsRole) {
        if (pmsRole == null){
            throw new BizException(BizException.PARAM_INVALID, "角色对象不能为空");
        }else if(StringUtil.isEmpty(pmsRole.getRoleName())){
            throw new BizException(BizException.PARAM_INVALID, "角色名称不能为空");
        }

        pmsRole.setVersion(0);
        pmsRole.setCreateTime(new Date());
        if(StringUtil.isEmpty(pmsRole.getRemark())){
            pmsRole.setRemark("");
        }
        pmsRoleDao.insert(BeanUtil.newAndCopy(pmsRole, PmsRole.class));
    }

    /**
     * 删除角色
     *
     * @param id 角色id
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteRoleById(Long id) {
        if (id == null || pmsRoleDao.getById(id) == null) {
            throw new BizException(BizException.BIZ_INVALID, "该角色不存在");
        }
        // 先删除所有关联权限和关联操作员
        pmsRoleAuthDao.deleteByRoleId(id);
        pmsRoleUserDao.deleteByRoleId(id);
        pmsRoleDao.deleteById(id);
    }

    /**
     * 更新角色
     *
     * @param pmsRole 角色
     */
    public void updateRole(PmsRoleDto pmsRole) {
        if (pmsRole == null) {
            throw new BizException(BizException.BIZ_INVALID, "角色不能为空");
        }else if(StringUtil.isEmpty(pmsRole.getRoleName())){
            throw new BizException(BizException.BIZ_INVALID, "角色名称不能为空");
        }

        PmsRole targetRole = pmsRoleDao.getById(pmsRole.getId());
        if (targetRole == null) {
            throw new BizException(BizException.BIZ_INVALID, "当前角色不存在");
        }
        //为允许修改的字段设值
        targetRole.setRoleName(pmsRole.getRoleName());
        targetRole.setRemark(pmsRole.getRemark());
        pmsRoleDao.update(targetRole);
    }

    /**
     * 查询所有角色
     */
    public List<PmsRoleDto> listAllRoles() {
        List<PmsRole> pmsRoles = pmsRoleDao.listAll();
        return BeanUtil.newAndCopy(pmsRoles, PmsRoleDto.class);
    }

    /**
     * 查询角色列表
     *
     * @param paramMap  查询参数
     * @param pageQuery 分页参数
     */
    public PageResult<List<PmsRoleDto>> listRolePage(Map<String, Object> paramMap, PageQuery pageQuery) {
        PageResult<List<PmsRole>> result = pmsRoleDao.listPage(paramMap, pageQuery);
        return PageResult.newInstance(BeanUtil.newAndCopy(result.getData(), PmsRoleDto.class), result);
    }

    /**
     * 查询操作员的所有角色
     *
     * @param userId 操作员id
     */
    public List<PmsRoleDto> listRolesByUserId(Long userId) {
        List<Long> roleIds = pmsRoleUserDao.listByUserId(userId).stream().map(PmsRoleUser::getRoleId).collect(Collectors.toList());
        if (roleIds.size() == 0) {
            return Collections.emptyList();
        }

        List<PmsRole> pmsRoles = pmsRoleDao.listByIdList(roleIds);
        return BeanUtil.newAndCopy(pmsRoles, PmsRoleDto.class);
    }

    /**
     * 根据id查询
     *
     * @param id 角色id
     */
    public PmsRoleDto getRoleById(Long id) {
        PmsRole pmsRole = pmsRoleDao.getById(id);
        return BeanUtil.newAndCopy(pmsRole, PmsRoleDto.class);
    }

    /**
     * 分配权限
     *
     * @param roleId      角色id
     * @param authIds     权限id列表
     */
    @Transactional(rollbackFor = Exception.class)
    public void assignPermission(Long roleId, List<Long> authIds) {
        if (roleId == null || pmsRoleDao.getById(roleId) == null) {
            throw new BizException(BizException.BIZ_INVALID, "该角色不存在");
        }

        // 先删除所有的菜单权限和功能权限
        pmsRoleAuthDao.deleteByRoleId(roleId);

        //添加菜单权限
        if (authIds != null && !authIds.isEmpty()) {
            List<PmsRoleAuth> roleAuthList = authIds.stream().map(p -> {
                PmsRoleAuth pmsRoleAuth = new PmsRoleAuth();
                pmsRoleAuth.setAuthId(p);
                pmsRoleAuth.setRoleId(roleId);
                return pmsRoleAuth;
            }).collect(Collectors.toList());
            pmsRoleAuthDao.insert(roleAuthList);
        }
    }


    //</editor-fold>

    private void replaceChildrenNumber(Long parentId, String parentNumOld, String parentNumNew, List<PmsAuth> allAuthNodes, List<PmsAuth> childrenNodes){
        if(allAuthNodes == null || allAuthNodes.isEmpty()) {
            return;
        }

        for(PmsAuth pmsAuth : allAuthNodes) {
            if (parentId.equals(pmsAuth.getParentId())) {
                String numberOld = pmsAuth.getNumber();
                String numberNew = numberOld.replace(parentNumOld, parentNumNew);
                pmsAuth.setNumber(numberNew);
                childrenNodes.add(pmsAuth);
                replaceChildrenNumber(pmsAuth.getId(), numberOld, numberNew, allAuthNodes, childrenNodes);
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
