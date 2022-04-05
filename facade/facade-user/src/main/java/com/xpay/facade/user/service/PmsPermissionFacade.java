package com.xpay.facade.user.service;

import com.xpay.common.statics.exception.BizException;
import com.xpay.common.statics.query.PageQuery;
import com.xpay.common.statics.result.PageResult;
import com.xpay.facade.user.dto.PmsAuthDto;
import com.xpay.facade.user.dto.PmsRoleDto;

import java.util.List;
import java.util.Map;

public interface PmsPermissionFacade {

    public void createAuth(PmsAuthDto pmsAuth) throws BizException;

    public void deleteAuthById(Long authId) throws BizException;

    public void updateAuth(PmsAuthDto pmsAuth) throws BizException;

    public List<PmsAuthDto> listAuthByUserId(Long userId);

    public List<PmsAuthDto> listAllAuth(String sortColumn);

    public List<Long> listAuthIdsByRoleId(Long roleId);

    public List<PmsAuthDto> listAuthByRoleId(Long roleId);

    public List<PmsAuthDto> listAuthByParentId(Long parentId);

    public PmsAuthDto getAuthById(Long id);

    public PmsAuthDto getSelfAuthAndParentAuth(Long id);



    public void createRole(PmsRoleDto pmsRole) throws BizException;

    public void deleteRoleById(Long id) throws BizException;

    public void updateRole(PmsRoleDto pmsRole) throws BizException;

    public List<PmsRoleDto> listAllRoles();

    public PmsRoleDto getRoleById(Long id);

    public PageResult<List<PmsRoleDto>> listRolePage(Map<String, Object> paramMap, PageQuery pageQuery);

    public List<PmsRoleDto> listRolesByUserId(Long userId);

    public void assignPermission(Long roleId, List<Long> functionIds) throws BizException;
}
