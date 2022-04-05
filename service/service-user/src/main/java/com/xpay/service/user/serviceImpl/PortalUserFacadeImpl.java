package com.xpay.service.user.serviceImpl;

import com.xpay.common.statics.exception.BizException;
import com.xpay.common.statics.query.PageQuery;
import com.xpay.common.statics.result.PageResult;
import com.xpay.facade.user.dto.PortalUserDto;
import com.xpay.facade.user.service.PortalUserFacade;
import com.xpay.service.user.biz.PortalUserBiz;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;

@DubboService
public class PortalUserFacadeImpl implements PortalUserFacade {
    @Autowired
    PortalUserBiz portalUserBiz;

    @Override
    public void createUser(PortalUserDto user) throws BizException {
        portalUserBiz.createUser(user);
    }

    @Override
    public void updateUser(PortalUserDto user) throws BizException {
        portalUserBiz.updateUser(user);
    }

    @Override
    public void updateUserPwd(Long userId, String newPwd, String modifier) throws BizException {
        portalUserBiz.updateUserLoginPwd(userId, newPwd, modifier);
    }

    @Override
    public boolean validLoginPwd(String loginName, String loginPwd){
        return portalUserBiz.validLoginPwd(loginName, loginPwd);
    }

    @Override
    public void deleteUserById(long userId) throws BizException {
        portalUserBiz.deleteUserById(userId);
    }

    @Override
    public void createAdminUserWithAutoAssignRole(PortalUserDto user) throws BizException {
        portalUserBiz.createAdminUserWithAutoAssignRole(user);
    }

    @Override
    public void assignAdminRoles(Long userId, List<Long> roleIds, String modifier, String remark) throws BizException {
        portalUserBiz.assignAdminRoles(userId, roleIds, modifier, remark);
    }

    @Override
    public void assignRoles(Long userId, List<Long> roleIds) throws BizException {
        portalUserBiz.assignRoles(userId, roleIds);
    }

    @Override
    public PortalUserDto getUserById(long userId) {
        return portalUserBiz.getUserById(userId);
    }

    @Override
    public PortalUserDto getUserByLoginName(String loginName) {
        return portalUserBiz.getUserByLoginName(loginName);
    }

    @Override
    public PortalUserDto getAdminUser(String mchNo) {
        return portalUserBiz.getAdminUser(mchNo);
    }

    @Override
    public List<PortalUserDto> listUserByRoleId(long roleId) {
        return portalUserBiz.listUserByRoleId(roleId);
    }

    @Override
    public PageResult<List<PortalUserDto>> listUserPage(Map<String, Object> paramMap, PageQuery pageQuery) {
        return portalUserBiz.listUserPage(paramMap, pageQuery);
    }
}
