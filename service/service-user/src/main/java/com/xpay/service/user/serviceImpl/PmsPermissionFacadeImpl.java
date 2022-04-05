package com.xpay.service.user.serviceImpl;

import com.xpay.common.statics.query.PageQuery;
import com.xpay.common.statics.result.PageResult;
import com.xpay.facade.user.dto.PmsAuthDto;
import com.xpay.facade.user.dto.PmsRoleDto;
import com.xpay.facade.user.service.PmsPermissionFacade;
import com.xpay.service.user.biz.PmsPermissionBiz;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;

@DubboService
public class PmsPermissionFacadeImpl implements PmsPermissionFacade {
    @Autowired
    PmsPermissionBiz pmsPermissionBiz;

    @Override
    public void createAuth(PmsAuthDto pmsAuth) {
        pmsPermissionBiz.createAuth(pmsAuth);
    }

    @Override
    public void deleteAuthById(Long authId) {
        pmsPermissionBiz.deleteAuthById(authId);
    }

    @Override
    public void updateAuth(PmsAuthDto pmsAuth) {
        pmsPermissionBiz.updateAuth(pmsAuth);
    }

    @Override
    public List<PmsAuthDto> listAuthByUserId(Long userId) {
        return pmsPermissionBiz.listAuthByUserId(userId);
    }

    @Override
    public List<PmsAuthDto> listAllAuth(String sortColumn) {
        return pmsPermissionBiz.listAllAuth(sortColumn);
    }

    @Override
    public List<Long> listAuthIdsByRoleId(Long roleId) {
        return pmsPermissionBiz.listAuthIdsByRoleId(roleId);
    }

    @Override
    public List<PmsAuthDto> listAuthByRoleId(Long roleId) {
        return pmsPermissionBiz.listAuthByRoleId(roleId);
    }

    @Override
    public List<PmsAuthDto> listAuthByParentId(Long parentId) {
        return pmsPermissionBiz.listAuthByParentId(parentId);
    }

    @Override
    public PmsAuthDto getAuthById(Long id) {
        return pmsPermissionBiz.getAuthById(id);
    }

    @Override
    public PmsAuthDto getSelfAuthAndParentAuth(Long id) {
        return pmsPermissionBiz.getSelfAuthAndParentAuth(id);
    }

    @Override
    public void createRole(PmsRoleDto pmsRole) {
        pmsPermissionBiz.createRole(pmsRole);
    }

    @Override
    public void deleteRoleById(Long id) {
        pmsPermissionBiz.deleteRoleById(id);
    }

    @Override
    public void updateRole(PmsRoleDto pmsRole) {
        pmsPermissionBiz.updateRole(pmsRole);
    }

    @Override
    public List<PmsRoleDto> listAllRoles() {
        return pmsPermissionBiz.listAllRoles();
    }

    @Override
    public PmsRoleDto getRoleById(Long id) {
        return pmsPermissionBiz.getRoleById(id);
    }

    @Override
    public PageResult<List<PmsRoleDto>> listRolePage(Map<String, Object> paramMap, PageQuery pageQuery) {
        return pmsPermissionBiz.listRolePage(paramMap, pageQuery);
    }

    @Override
    public List<PmsRoleDto> listRolesByUserId(Long userId) {
        return pmsPermissionBiz.listRolesByUserId(userId);
    }

    @Override
    public void assignPermission(Long roleId, List<Long> functionIds) {
        pmsPermissionBiz.assignPermission(roleId, functionIds);
    }
}
