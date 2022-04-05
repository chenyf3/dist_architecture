package com.xpay.service.user.biz;

import com.xpay.common.statics.enums.user.UserStatusEnum;
import com.xpay.common.statics.enums.user.UserTypeEnum;
import com.xpay.common.statics.exception.BizException;
import com.xpay.common.statics.query.PageQuery;
import com.xpay.common.statics.result.PageResult;
import com.xpay.common.utils.BeanUtil;
import com.xpay.common.utils.MD5Util;
import com.xpay.common.utils.StringUtil;
import com.xpay.facade.user.dto.PmsUserDto;
import com.xpay.service.user.dao.PmsRoleUserDao;
import com.xpay.service.user.dao.PmsUserDao;
import com.xpay.service.user.entity.PmsRoleUser;
import com.xpay.service.user.entity.PmsUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 *
 */
@Service
public class PmsUserBiz {
    @Autowired
    private PmsUserDao pmsUserDao;
    @Autowired
    private PmsRoleUserDao pmsRoleUserDao;

    public PmsUserDto getUserById(Long id) {
        PmsUser pmsUser = pmsUserDao.getById(id);
        return BeanUtil.newAndCopy(pmsUser, PmsUserDto.class);
    }

    public PmsUserDto getUserByLoginName(String loginName) {
        if(StringUtil.isEmpty(loginName)){
            return null;
        }
        PmsUser pmsUser = pmsUserDao.findByLoginName(loginName);
        return BeanUtil.newAndCopy(pmsUser, PmsUserDto.class);
    }

    public void updateUser(PmsUserDto pmsUser) {
        PmsUser userOld = pmsUserDao.getById(pmsUser.getId());

        //这几个字段是允许更新的字段，其余字段不允许更新
        userOld.setRemark(pmsUser.getRemark());
        userOld.setStatus(pmsUser.getStatus());
        userOld.setRealName(pmsUser.getRealName());
        userOld.setMobileNo(pmsUser.getMobileNo());
        userOld.setEmail(pmsUser.getEmail());
        userOld.setModifier(pmsUser.getModifier());
        pmsUserDao.updateIfNotNull(userOld);
    }

    public void updateUserLoginPwd(Long userId, String newPwd) {
        if(userId == null){
            throw new BizException(BizException.BIZ_INVALID, "userId不能为空");
        }else if(StringUtil.isEmpty(newPwd)){
            throw new BizException(BizException.BIZ_INVALID, "新密码不能为空");
        }

        PmsUser pmsUser = pmsUserDao.getById(userId);
        if (pmsUser.getLoginPwd().equals(encryptPwd(newPwd))) {
            throw new BizException(BizException.BIZ_INVALID, "新密码不能与旧密码相同");
        }
        pmsUser.setLoginPwd(encryptPwd(newPwd));
        pmsUserDao.update(pmsUser);
    }

    public boolean validLoginPwd(String loginName, String loginPwd){
        PmsUserDto pmsUser = getUserByLoginName(loginName);
        if(pmsUser == null){
            return false;
        }
        return pmsUser.getLoginPwd().equals(encryptPwd(loginPwd));
    }

    @Transactional(rollbackFor = Exception.class)
    public void createUser(PmsUserDto pmsUser) {
        if(StringUtil.isEmpty(pmsUser.getLoginName())){
            throw new BizException(BizException.BIZ_INVALID, "登录名不能为空");
        }else if(StringUtil.isEmpty(pmsUser.getLoginPwd())){
            throw new BizException(BizException.BIZ_INVALID, "登录密码不能为空");
        }else if(StringUtil.isEmpty(pmsUser.getRealName())){
            throw new BizException(BizException.BIZ_INVALID, "姓名不能为空");
        }else if(StringUtil.isEmpty(pmsUser.getMobileNo())){
            throw new BizException(BizException.BIZ_INVALID, "手机号码不能为空");
        }else if(StringUtil.isEmpty(pmsUser.getCreator())){
            throw new BizException(BizException.BIZ_INVALID, "创建人不能为空");
        }

        PmsUserDto userTemp = getUserByLoginName(pmsUser.getLoginName());
        if(userTemp != null){
            throw new BizException(BizException.BIZ_INVALID, "当前登录名已存在！");
        }
        if(StringUtil.isEmpty(pmsUser.getRemark())){
            pmsUser.setRemark("");
        }
        if(StringUtil.isEmpty(pmsUser.getEmail())){
            pmsUser.setEmail("");
        }
        if(pmsUser.getStatus() == null){
            pmsUser.setStatus(UserStatusEnum.ACTIVE.getValue());
        }
        pmsUser.setVersion(0);
        pmsUser.setCreateTime(new Date());
        pmsUser.setModifier(pmsUser.getCreator());
        pmsUser.setType(UserTypeEnum.USER.getValue());//只允许添加普通用户，超级管理员手动执行sql进行初始化
        pmsUser.setLoginPwd(encryptPwd(pmsUser.getLoginPwd()));
        pmsUserDao.insert(BeanUtil.newAndCopy(pmsUser, PmsUser.class));
    }

    @Transactional(rollbackFor = Exception.class)
    public void createUserAndAssignRoles(PmsUserDto user, List<Long> roleIds) {
        List<PmsRoleUser> roleUserList = null;
        if (roleIds != null && roleIds.size() > 0) {
            roleUserList = roleIds.stream().map(p -> {
                PmsRoleUser roleUser = new PmsRoleUser();
                roleUser.setRoleId(p);
                roleUser.setUserId(user.getId());
                return roleUser;
            }).collect(Collectors.toList());
        }

        createUser(user);

        if(roleUserList != null && ! roleUserList.isEmpty()){
            pmsRoleUserDao.insert(roleUserList);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean assignRoles(Long userId, List<Long> roleIds, String modifier){
        PmsUserDto userDto = getUserById(userId);
        if (userDto == null) {
            throw new BizException(BizException.BIZ_INVALID, "操作员不存在！");
        }else if(userDto.getType() == UserTypeEnum.ADMIN.getValue()){
            throw new BizException(BizException.BIZ_INVALID, "当前操作员不允许分配角色！");
        }

        pmsRoleUserDao.deleteByUserId(userDto.getId());
        if (roleIds != null && roleIds.size() > 0) {
            List<PmsRoleUser> roleUserList = roleIds.stream().map(p -> {
                PmsRoleUser roleUser = new PmsRoleUser();
                roleUser.setRoleId(p);
                roleUser.setUserId(userDto.getId());
                return roleUser;
            }).collect(Collectors.toList());
            pmsRoleUserDao.insert(roleUserList);
        }
        return true;
    }

    /**
     * 查询当前角色关联的所有用户
     * @param roleId
     * @return
     */
    public List<PmsUserDto> listUserByRoleId(long roleId) {
        List<PmsUser> pmsUsers = pmsUserDao.listByRoleId(roleId);
        return BeanUtil.newAndCopy(pmsUsers, PmsUserDto.class);
    }

    public PageResult<List<PmsUserDto>> listUserPage(Map<String, Object> paramMap, PageQuery pageQuery) {
        PageResult<List<PmsUser>> result = pmsUserDao.listPage(paramMap, pageQuery);
        return PageResult.newInstance(BeanUtil.newAndCopy(result.getData(), PmsUserDto.class), result);
    }

    /**
     * 删除用户以及当前用户的角色关联记录
     * @param id
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteUserById(long id) {
        PmsUserDto user = getUserById(id);
        if(user == null){
            return;
        }else if(user.getType() == UserTypeEnum.ADMIN.getValue()){
            throw new BizException(BizException.BIZ_INVALID, "当前用户不能删除！");
        }

        pmsUserDao.deleteById(id);
        pmsRoleUserDao.deleteByUserId(id);
    }

    private String encryptPwd(String pwd){
        return MD5Util.getMD5Hex(pwd);
    }
}
