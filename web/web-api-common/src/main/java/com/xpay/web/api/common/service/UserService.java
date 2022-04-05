package com.xpay.web.api.common.service;

import com.xpay.common.statics.query.PageQuery;
import com.xpay.common.statics.result.PageResult;
import com.xpay.web.api.common.ddo.dto.LoginDto;
import com.xpay.web.api.common.ddo.vo.UserVo;
import com.xpay.web.api.common.model.UserModel;

import java.util.List;
import java.util.Map;

public interface UserService {
    PageResult<List<UserVo>> listUserPage(Map<String, Object> paramMap, PageQuery pageQuery, String mchNo);

    boolean addUser(UserVo userDto, String creator, String mchNo);

    /**
     * 获取UserDto
     * @param userId
     * @param mchNo
     * @return
     */
    UserVo getUserById(Long userId, String mchNo);

    /**
     * 获取UserModel，实现类中可以考虑使用缓存
     * @param loginName
     * @param mchNo
     * @return
     */
    UserModel getUserModelByLoginName(String loginName, String mchNo);

    UserModel getUserModelByLoginNameCache(String loginName, String mchNo);

    UserVo getAdminUser(String mchNo);

    boolean updateUserStatus(Long userId, Integer newStatus, String modifier, String mchNo);

    boolean updatePassword(Long userId, String newPwd, String modifier, String mchNo);

    boolean validLoginPwd(String loginName, String password);

    boolean updateUser(UserVo userDto, String modifier, String mchNo);

    boolean assignRoles(Long userId, List<Long> roleIds, String modifier, String mchNo);

    boolean deleteUser(Long userId, String modifier, String mchNo);

    void afterLoginSuccess(UserModel userModel);

    void afterLoginPwdError(LoginDto loginDto);

    void afterLogout(UserModel userModel);

    void afterPwdReset(String loginName);
}
