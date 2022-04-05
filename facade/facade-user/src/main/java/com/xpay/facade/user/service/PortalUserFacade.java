package com.xpay.facade.user.service;

import com.xpay.common.statics.exception.BizException;
import com.xpay.common.statics.query.PageQuery;
import com.xpay.common.statics.result.PageResult;
import com.xpay.facade.user.dto.PortalUserDto;

import java.util.List;
import java.util.Map;

public interface PortalUserFacade {

    void createUser(PortalUserDto user) throws BizException;

    void updateUser(PortalUserDto user) throws BizException;

    void updateUserPwd(Long userId, String newPwd, String modifier) throws BizException;

    boolean validLoginPwd(String loginName, String loginPwd);

    void deleteUserById(long userId) throws BizException;

    void createAdminUserWithAutoAssignRole(PortalUserDto user) throws BizException;

    void assignAdminRoles(Long userId, List<Long> roleIds, String modifier, String remark) throws BizException;

    void assignRoles(Long userId, List<Long> roleIds) throws BizException;

    PortalUserDto getUserById(long userId);

    PortalUserDto getUserByLoginName(String loginName);

    PortalUserDto getAdminUser(String mchNo);

    List<PortalUserDto> listUserByRoleId(long roleId);

    PageResult<List<PortalUserDto>> listUserPage(Map<String, Object> paramMap, PageQuery pageQuery);
}
