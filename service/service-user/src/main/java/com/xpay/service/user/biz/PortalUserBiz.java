package com.xpay.service.user.biz;

import com.xpay.common.statics.constants.common.PublicStatus;
import com.xpay.common.statics.enums.user.UserStatusEnum;
import com.xpay.common.statics.enums.user.UserTypeEnum;
import com.xpay.common.statics.enums.user.RevokeAuthTypeEnum;
import com.xpay.common.statics.enums.user.RoleTypeEnum;
import com.xpay.common.statics.exception.BizException;
import com.xpay.common.statics.query.PageQuery;
import com.xpay.common.statics.result.PageResult;
import com.xpay.common.utils.BeanUtil;
import com.xpay.common.utils.MD5Util;
import com.xpay.common.utils.StringUtil;
import com.xpay.facade.user.dto.PortalUserDto;
import com.xpay.service.user.dao.PortalRoleDao;
import com.xpay.service.user.dao.PortalRoleUserDao;
import com.xpay.service.user.dao.PortalUserDao;
import com.xpay.service.user.entity.PortalRole;
import com.xpay.service.user.entity.PortalRoleUser;
import com.xpay.service.user.entity.PortalUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 商户后台用户管理
 */
@Service
public class PortalUserBiz {
    @Autowired
    private PortalUserDao portalUserDao;
    @Autowired
    private PortalRoleUserDao portalRoleUserDao;
    @Autowired
    private PortalRoleDao portalRoleDao;
    @Autowired
    private RevokePermissionBiz revokePermissionBiz;

    public PortalUserDto getUserById(Long id) {
        PortalUser user = portalUserDao.getById(id);
        return BeanUtil.newAndCopy(user, PortalUserDto.class);
    }

    public PortalUserDto getUserByLoginName(String loginName) {
        PortalUser user = portalUserDao.findByLoginName(loginName);
        return BeanUtil.newAndCopy(user, PortalUserDto.class);
    }

    public PortalUserDto getAdminUser(String mchNo){
        if (StringUtil.isEmpty(mchNo)) {
            return null;
        }

        Map<String, Object> map = new HashMap<>();
        map.put("type", UserTypeEnum.ADMIN.getValue());
        map.put("mchNo", mchNo);
        List<PortalUser> list = portalUserDao.listBy(map);
        if (list == null || list.isEmpty()) {
            return null;
        } else if (list.size() > 1) {
            throw new BizException(BizException.BIZ_INVALID, "当前商户存在多个管理员用户");
        }
        return BeanUtil.newAndCopy(list.get(0), PortalUserDto.class);
    }

    @Transactional(rollbackFor = Exception.class)
    public void createUser(PortalUserDto user) {
        if (user.getMchType() == null) {
            throw new BizException(BizException.PARAM_INVALID, "请设置商户类型！");
        }else if(StringUtil.isEmpty(user.getMchNo())){
            throw new BizException(BizException.PARAM_INVALID, "商户编号不能为空！");
        }else if(StringUtil.isEmpty(user.getLoginName())){
            throw new BizException(BizException.PARAM_INVALID, "登录名不能为空！");
        }else if(StringUtil.isEmpty(user.getLoginPwd())){
            throw new BizException(BizException.PARAM_INVALID, "登陆密码不能为空！");
        }

        // 判断登录名是否重复
        PortalUserDto userTemp = getUserByLoginName(user.getLoginName());
        if(userTemp != null){
            throw new BizException(BizException.PARAM_INVALID, "当前登录名已存在！");
        }

        // 同一个商户的管理员用户只允许有一个
        boolean isAdmin = user.getType() == UserTypeEnum.ADMIN.getValue();
        if (isAdmin) {
            PortalUserDto userDto = getAdminUser(user.getMchNo());
            if (userDto != null) {
                throw new BizException(BizException.BIZ_INVALID, "当前商户已存在管理员用户");
            }
        }

        if(user.getStatus() == null){
            user.setStatus(UserStatusEnum.ACTIVE.getValue());
        }
        if(StringUtil.isEmpty(user.getRemark())){
            user.setRemark("");
        }
        if(StringUtil.isEmpty(user.getEmail())){
            user.setEmail("");
        }
        String loginPwd = encryptPwd(user.getLoginPwd());
        user.setLoginPwd(loginPwd);

        List<PortalRoleUser> roleUsers = null;
        if(user.getRoleIds() != null && ! user.getRoleIds().isEmpty()){
            List<PortalRole> tempList = portalRoleDao.listByIdList(user.getRoleIds());
            if(tempList == null || tempList.size() != user.getRoleIds().size()){
                throw new BizException(BizException.BIZ_INVALID, "部分分配的角色不存在");
            }
            roleUsers = new ArrayList<>();
            for(Long roleId : user.getRoleIds()){
                PortalRoleUser roleUser = new PortalRoleUser();
                roleUser.setUserId(user.getId());
                roleUser.setRoleId(roleId);
                roleUser.setMchNo(user.getMchNo());
                roleUsers.add(roleUser);
            }
        }

        //保存用户以及和角色的关联记录
        portalUserDao.insert(BeanUtil.newAndCopy(user, PortalUser.class));
        if(roleUsers != null){
            portalRoleUserDao.insert(roleUsers);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void createAdminUserWithAutoAssignRole(PortalUserDto user){
        if(user == null){
            throw new BizException(BizException.PARAM_INVALID, "用户对象不能为空");
        }else if(user.getMchType() == null){
            throw new BizException(BizException.PARAM_INVALID, "商户类型不能为空");
        }else if(user.getRoleIds() == null){
            user.setRoleIds(new ArrayList<>());
        }

        List<PortalRole> roleList = portalRoleDao.listAllAdminRoles(user.getMchType());
        List<Long> roleIds = new ArrayList<>();
        for(PortalRole role : roleList){
            if(role.getAutoAssign() == PublicStatus.ACTIVE){
                roleIds.add(role.getId());
            }
        }

        user.getRoleIds().addAll(roleIds);
        user.setType(UserTypeEnum.ADMIN.getValue());//强制设置为管理员

        createUser(user);
    }

    public void updateUser(PortalUserDto userDto) {
        PortalUser userOld = portalUserDao.getById(userDto.getId());
        if(userOld == null){
            throw new BizException(BizException.PARAM_INVALID, "用户不存在");
        }

        //以下字段是允许更新的字段
        userOld.setRealName(userDto.getRealName());
        userOld.setModifier(userDto.getModifier());
        userOld.setRemark(userDto.getRemark());
        userOld.setMobileNo(userDto.getMobileNo());
        userOld.setEmail(userDto.getEmail());
        userOld.setExtraInfo(userDto.getExtraInfo());
        if(userDto.getStatus() != null){
            userOld.setStatus(userDto.getStatus());
        }

        portalUserDao.update(userOld);
    }

    /**
     * 修改用户登录密码
     * @param userId
     * @param newPwd
     * @param modifier
     */
    public void updateUserLoginPwd(Long userId, String newPwd, String modifier) {
        if(userId == null){
            throw new BizException(BizException.BIZ_INVALID, "userId不能为空");
        }else if(StringUtil.isEmpty(newPwd)){
            throw new BizException(BizException.BIZ_INVALID, "新密码不能为空");
        }

        PortalUser user = portalUserDao.getById(userId);
        newPwd = encryptPwd(newPwd);
        if (user.getLoginPwd().equals(newPwd)) {
            throw new BizException(BizException.BIZ_INVALID, "新密码不能与旧密码相同");
        }

        user.setLoginPwd(newPwd);
        user.setModifier(modifier);
        portalUserDao.update(user);
    }

    /**
     * 校验登陆密码是否正确
     * @param loginName
     * @param loginPwd
     * @return
     */
    public boolean validLoginPwd(String loginName, String loginPwd){
        if(StringUtil.isEmpty(loginName)){
            return false;
        }
        PortalUserDto user = getUserByLoginName(loginName);
        if(user == null){
            return false;
        }
        loginPwd = encryptPwd(loginPwd);
        return user.getLoginPwd().equals(loginPwd);
    }

    public List<PortalUserDto> listUserByRoleId(long roleId) {
        List<PortalUser> portalUsers = portalUserDao.listByRoleId(roleId);
        return BeanUtil.newAndCopy(portalUsers, PortalUserDto.class);
    }

    public PageResult<List<PortalUserDto>> listUserPage(Map<String, Object> paramMap, PageQuery pageQuery) {
        PageResult<List<PortalUser>> result = portalUserDao.listPage(paramMap, pageQuery);
        return PageResult.newInstance(BeanUtil.newAndCopy(result.getData(), PortalUserDto.class), result);
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteUserById(long id) {
        PortalUserDto portalUser = getUserById(id);
        if(portalUser == null){
            return;
        }else if(portalUser.getType() == UserTypeEnum.ADMIN.getValue()){
            throw new BizException(BizException.BIZ_INVALID, "当前用户不允许删除");
        }

        portalUserDao.deleteById(id);
        portalRoleUserDao.deleteByUserId(id);
    }

    /**
     * 给普通用户分配角色
     * @param userId
     * @param roleIds
     */
    @Transactional(rollbackFor = Exception.class)
    public void assignRoles(Long userId, List<Long> roleIds){
        if(userId == null){
            throw new BizException(BizException.PARAM_INVALID, "操作员不能为空");
        }else if(roleIds == null){
            roleIds = new ArrayList<>();
        }

        PortalUser user = portalUserDao.getById(userId);
        if(user == null){
            throw new BizException(BizException.PARAM_INVALID, "操作员不存在");
        }else if(user.getType() == UserTypeEnum.ADMIN.getValue()){
            throw new BizException(BizException.PARAM_INVALID, "当前操作员不允许修改角色分配");
        }

        this.reassignRoles(user, user.getMchNo(), roleIds, false);
    }

    /**
     * 给管理员用户分配角色
     * @param userId
     * @param roleIds
     */
    @Transactional(rollbackFor = Exception.class)
    public void assignAdminRoles(Long userId, List<Long> roleIds, String modifier, String remark){
        if(userId == null){
            throw new BizException(BizException.PARAM_INVALID, "用户Id不能为空");
        }else if(roleIds == null){
            roleIds = new ArrayList<>();
        }

        PortalUser user = portalUserDao.getById(userId);
        if(user == null){
            throw new BizException(BizException.PARAM_INVALID, "用户不存在");
        }else if(user.getType() != UserTypeEnum.ADMIN.getValue()){
            throw new BizException(BizException.PARAM_INVALID, "当前用户非管理员角色");
        }

        boolean isNeedRevokePermission = false;//是否需要回收权限
        List<PortalRoleUser> roleUsers = portalRoleUserDao.listByUserId(userId);
        if(roleUsers != null && !roleUsers.isEmpty()){
            for(PortalRoleUser roleUser : roleUsers){
                if(! roleIds.contains(roleUser.getRoleId())){//判断是否有减少关联角色
                    isNeedRevokePermission = true;
                    break;
                }
            }
        }

        reassignRoles(user, user.getMchNo(), roleIds, true);

        if(isNeedRevokePermission){
            revokePermissionBiz.revokeMerchantPermission(RevokeAuthTypeEnum.CANCEL_ROLE_RELATE.getValue(),
                    user.getLoginName(), Collections.singletonList(user.getMchNo()), modifier, remark);
        }
    }

    private void reassignRoles(PortalUser user, String mchNo, List<Long> roleIds, boolean isAdmin){
        if(! roleIds.isEmpty()){
            List<PortalRole> roleList = portalRoleDao.listByIdList(roleIds);
            if(roleList == null || roleList.isEmpty()){
                throw new BizException(BizException.BIZ_INVALID, "当前分配的角色不存在");
            }
            for(PortalRole role : roleList){
                if(isAdmin && role.getRoleType() != RoleTypeEnum.ADMIN.getValue()){
                    throw new BizException(BizException.BIZ_INVALID, "当前用户只能分配管理员角色！");
                }else if(!isAdmin && role.getRoleType() != RoleTypeEnum.USER.getValue()){
                    throw new BizException(BizException.BIZ_INVALID, "当前用户只能分配普通角色！");
                }else if(!user.getMchType().equals(role.getMchType())){
                    throw new BizException(BizException.BIZ_INVALID, "当前用户和角色的商户类型不匹配！ 角色名：" + role.getRoleName());
                }
            }
        }

        Long userId = user.getId();
        List<PortalRoleUser> roleUserList = null;
        if(roleIds != null && roleIds.size() > 0){
            roleUserList = roleIds.stream()
                    .map(p -> {
                        PortalRoleUser roleUser = new PortalRoleUser();
                        roleUser.setRoleId(p);
                        roleUser.setUserId(userId);
                        roleUser.setMchNo(mchNo);
                        return roleUser;
                    }).collect(Collectors.toList());
        }

        portalRoleUserDao.deleteByUserId(userId);
        if(roleUserList != null && !roleUserList.isEmpty()){
            portalRoleUserDao.insert(roleUserList);
        }
    }

    private String encryptPwd(String pwd){
        return MD5Util.getMD5Hex(pwd);
    }
}
