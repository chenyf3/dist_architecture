package com.xpay.facade.user.service;

import com.xpay.common.statics.exception.BizException;
import com.xpay.common.statics.query.PageQuery;
import com.xpay.common.statics.result.PageResult;
import com.xpay.facade.user.dto.PortalAuthDto;
import com.xpay.facade.user.dto.PortalRevokeAuthDto;
import com.xpay.facade.user.dto.PortalRoleDto;

import java.util.List;
import java.util.Map;

public interface PortalPermissionFacade {

    //<editor-fold desc="功能点管理">
    void createAuth(PortalAuthDto portalAuth) throws BizException;
    /**
     * 删除权限及其子权限,同时也会删除与这些权限相关的role_auth映射关联
     * 若存在菜单类的子权限，该方法会抛出异常
     *
     * @param authId .
     */
    void deleteAuthById(Long authId) throws BizException;

    void updateAuth(PortalAuthDto portalAuth) throws BizException;

    PortalAuthDto getAuthById(Long id);

    PortalAuthDto getAuthWithParentInfo(Long id);

    List<PortalAuthDto> listAuthByUserId(Long userId);

    List<PortalAuthDto> listAllAuth();

    List<PortalAuthDto> listAllAuthByMchNo(String mchNo);

    List<PortalAuthDto> listAuthByRoleId(Long roleId);

    List<PortalAuthDto> listAuthByParentId(Long parentId);

    List<Long> listAuthIdsByRoleId(Long roleId);
    //</editor-fold>


    //<editor-fold desc="角色管理">
    void createRole(PortalRoleDto portalRole) throws BizException;

    void deleteRoleById(Long roleId) throws BizException;

    void deleteAdminRoleById(Long roleId, String modifier, String remark) throws BizException;

    void updateRole(PortalRoleDto portalRole) throws BizException;

    PortalRoleDto getRoleById(Long roleId);

    List<PortalRoleDto> listAllRoles(Map<String, Object> paramMap);

    List<PortalRoleDto> listAllAdminRoles(Integer mchType);

    List<PortalRoleDto> listRoleByMerchantNo(String merchantNo);

    List<PortalRoleDto> listRolesByUserId(Long userId, String mchNo);

    PageResult<List<PortalRoleDto>> listRolePage(Map<String, Object> paramMap, PageQuery pageQuery);

    void assignRolePermission(Long roleId, List<Long> authIds) throws BizException;

    void assignAdminRolePermission(Long roleId, List<Long> authIds, String modifier, String remark) throws BizException;

    PageResult<List<PortalRevokeAuthDto>> listRevokeAuthPage(Map<String, Object> paramMap, PageQuery pageQuery);

    boolean doAuthRevoke(Long id) throws BizException;
    //</editor-fold>
}
