package com.xpay.service.user.serviceImpl;

import com.xpay.common.statics.query.PageQuery;
import com.xpay.common.statics.result.PageResult;
import com.xpay.facade.user.dto.PmsUserDto;
import com.xpay.facade.user.service.PmsUserFacade;
import com.xpay.service.user.biz.PmsUserBiz;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;

@DubboService
public class PmsUserFacadeImpl implements PmsUserFacade {
    @Autowired
    PmsUserBiz pmsUserBiz;

    @Override
    public PmsUserDto getUserById(long id) {
        return pmsUserBiz.getUserById(id);
    }

    @Override
    public PmsUserDto getUserByLoginName(String loginName) {
        return pmsUserBiz.getUserByLoginName(loginName);
    }

    @Override
    public void deleteUserById(long id) {
        pmsUserBiz.deleteUserById(id);
    }

    @Override
    public List<PmsUserDto> listUserByRoleId(long roleId) {
        return pmsUserBiz.listUserByRoleId(roleId);
    }

    @Override
    public void createUser(PmsUserDto user) {
        pmsUserBiz.createUser(user);
    }

    @Override
    public void updateUser(PmsUserDto user) {
        pmsUserBiz.updateUser(user);
    }

    @Override
    public boolean validLoginPwd(String loginName, String loginPwd){
        return pmsUserBiz.validLoginPwd(loginName, loginPwd);
    }

    @Override
    public boolean assignRoles(Long userId, List<Long> roleIds, String modifier) {
        return pmsUserBiz.assignRoles(userId, roleIds, modifier);
    }

    @Override
    public void createUserAndAssignRoles(PmsUserDto userDto, List<Long> roleIds) {
        pmsUserBiz.createUserAndAssignRoles(userDto, roleIds);
    }

    @Override
    public void updateUserPwd(Long userId, String newPwd) {
        pmsUserBiz.updateUserLoginPwd(userId, newPwd);
    }

    @Override
    public PageResult<List<PmsUserDto>> listUserPage(Map<String, Object> paramMap, PageQuery pageQuery) {
        return pmsUserBiz.listUserPage(paramMap, pageQuery);
    }
}
