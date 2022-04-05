package com.xpay.facade.user.service;

import com.xpay.common.statics.exception.BizException;
import com.xpay.common.statics.query.PageQuery;
import com.xpay.common.statics.result.PageResult;
import com.xpay.facade.user.dto.PmsUserDto;

import java.util.List;
import java.util.Map;

public interface PmsUserFacade {

    public PmsUserDto getUserById(long id);

    public PmsUserDto getUserByLoginName(String loginName);

    public void deleteUserById(long id) throws BizException;

    public List<PmsUserDto> listUserByRoleId(long roleId);

    public void createUser(PmsUserDto user) throws BizException;

    public void updateUser(PmsUserDto user) throws BizException;

    public boolean validLoginPwd(String loginName, String loginPwd);

    public boolean assignRoles(Long userId, List<Long> roleIds, String modifier) throws BizException;

    public void createUserAndAssignRoles(PmsUserDto user, List<Long> roleIds) throws BizException;

    public void updateUserPwd(Long userId, String newPwd) throws BizException;

    public PageResult<List<PmsUserDto>> listUserPage(Map<String, Object> paramMap, PageQuery pageQuery);
}
