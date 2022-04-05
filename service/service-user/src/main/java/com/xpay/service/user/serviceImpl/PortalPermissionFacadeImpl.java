package com.xpay.service.user.serviceImpl;

import com.xpay.common.statics.exception.BizException;
import com.xpay.common.statics.query.PageQuery;
import com.xpay.common.statics.result.PageResult;
import com.xpay.facade.user.dto.PortalAuthDto;
import com.xpay.facade.user.dto.PortalRevokeAuthDto;
import com.xpay.facade.user.dto.PortalRoleDto;
import com.xpay.facade.user.service.PortalPermissionFacade;
import com.xpay.service.user.biz.PortalPermissionBiz;
import com.xpay.service.user.biz.RevokePermissionBiz;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;

@DubboService
public class PortalPermissionFacadeImpl implements PortalPermissionFacade {
    @Autowired
    PortalPermissionBiz portalPermissionBiz;
    @Autowired
    RevokePermissionBiz revokePermissionBiz;

    @Override
    public void createAuth(PortalAuthDto portalAuth) throws BizException {
        portalPermissionBiz.createAuth(portalAuth);
    }

    @Override
    public void deleteAuthById(Long authId) throws BizException {
        portalPermissionBiz.deleteAuthById(authId);
    }

    @Override
    public void updateAuth(PortalAuthDto portalAuth) throws BizException {
        portalPermissionBiz.updateAuth(portalAuth);
    }

    @Override
    public PortalAuthDto getAuthById(Long id) {
        return portalPermissionBiz.getAuthById(id);
    }

    @Override
    public PortalAuthDto getAuthWithParentInfo(Long id) {
        return portalPermissionBiz.getSelfAuthAndParentAuth(id);
    }

    @Override
    public List<PortalAuthDto> listAuthByUserId(Long userId) {
        return portalPermissionBiz.listAuthByUserId(userId);
    }

    @Override
    public List<PortalAuthDto> listAllAuth() {
        return portalPermissionBiz.listAllAuth(null);
    }

    @Override
    public List<PortalAuthDto> listAllAuthByMchNo(String mchNo) {
        return portalPermissionBiz.listAllAuthByMchNo(mchNo);
    }

    @Override
    public List<PortalAuthDto> listAuthByRoleId(Long roleId) {
        return portalPermissionBiz.listAuthByRoleId(roleId);
    }

    @Override
    public List<PortalAuthDto> listAuthByParentId(Long parentId) {
        return portalPermissionBiz.listAuthByParentId(parentId);
    }

    @Override
    public List<Long> listAuthIdsByRoleId(Long roleId) {
        return portalPermissionBiz.listAuthIdsByRoleId(roleId);
    }

    @Override
    public void createRole(PortalRoleDto portalRole) throws BizException {
        portalPermissionBiz.createRole(portalRole);
    }

    @Override
    public void deleteRoleById(Long roleId) throws BizException {
        portalPermissionBiz.deleteRoleById(roleId);
    }

    @Override
    public void deleteAdminRoleById(Long roleId, String modifier, String remark) throws BizException {
        portalPermissionBiz.deleteAdminRoleById(roleId, modifier, remark);
    }

    @Override
    public void updateRole(PortalRoleDto portalRole) throws BizException {
        portalPermissionBiz.updateRole(portalRole);
    }

    @Override
    public PortalRoleDto getRoleById(Long roleId) {
        return portalPermissionBiz.getRoleById(roleId);
    }

    @Override
    public List<PortalRoleDto> listAllRoles(Map<String, Object> paramMap) {
        return portalPermissionBiz.listAllRoles(paramMap);
    }

    @Override
    public List<PortalRoleDto> listAllAdminRoles(Integer mchType) {
        return portalPermissionBiz.listAllAdminRoles(mchType);
    }

    @Override
    public List<PortalRoleDto> listRoleByMerchantNo(String merchantNo) {
        return portalPermissionBiz.listRoleByMerchantNo(merchantNo);
    }

    @Override
    public List<PortalRoleDto> listRolesByUserId(Long userId, String mchNo) {
        return portalPermissionBiz.listRolesByUserId(userId, mchNo);
    }

    @Override
    public PageResult<List<PortalRoleDto>> listRolePage(Map<String, Object> paramMap, PageQuery pageQuery) {
        return portalPermissionBiz.listRolePage(paramMap, pageQuery);
    }

    @Override
    public void assignRolePermission(Long roleId, List<Long> authIds) throws BizException {
        portalPermissionBiz.assignRolePermission(roleId, authIds);
    }

    @Override
    public void assignAdminRolePermission(Long roleId, List<Long> authIds, String modifier, String remark) throws BizException {
        portalPermissionBiz.assignAdminRolePermission(roleId, authIds, modifier, remark);
    }

    @Override
    public PageResult<List<PortalRevokeAuthDto>> listRevokeAuthPage(Map<String, Object> paramMap, PageQuery pageQuery) {
        return revokePermissionBiz.listRevokeAuthPage(paramMap, pageQuery);
    }

    @Override
    public boolean doAuthRevoke(Long id) throws BizException {
        return revokePermissionBiz.doAuthRevoke(id);
    }
}
