package com.xpay.web.api.common.service;

import com.xpay.common.statics.query.PageQuery;
import com.xpay.common.statics.result.PageResult;
import com.xpay.web.api.common.ddo.vo.AuthVo;
import com.xpay.web.api.common.ddo.vo.RoleVo;

import java.util.List;
import java.util.Map;

public interface RoleService {
    boolean addRole(RoleVo roleDto, String creator);

    boolean editRole(RoleVo roleDto, String modifier, String mchNo);

    RoleVo getRoleById(Long roleId, String mchNo);

    PageResult<List<RoleVo>> listRolePage(Map<String, Object> paramMap, PageQuery pageQuery, String mchNo);

    List<RoleVo> listAllRoles(Map<String, String> paramMap, String mchNo);

    List<AuthVo> listAuthByRoleId(Long roleId, String mchNo);

    List<RoleVo> listRoleByUserId(Long userId, String mchNo);

    boolean assignPermission(Long roleId, List<Long> functionIds, String mchNo);

    boolean deleteRoleAndRelatedById(Long roleId, String mchNo);
}
