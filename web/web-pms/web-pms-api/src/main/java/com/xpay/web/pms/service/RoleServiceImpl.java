package com.xpay.web.pms.service;

import com.xpay.common.statics.query.PageQuery;
import com.xpay.common.statics.result.PageResult;
import com.xpay.common.utils.BeanUtil;
import com.xpay.facade.user.dto.PmsAuthDto;
import com.xpay.facade.user.dto.PmsRoleDto;
import com.xpay.facade.user.service.PmsPermissionFacade;
import com.xpay.web.api.common.ddo.vo.AuthVo;
import com.xpay.web.api.common.ddo.vo.RoleVo;
import com.xpay.web.api.common.service.RoleService;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class RoleServiceImpl implements RoleService {
    @DubboReference
    PmsPermissionFacade pmsPermissionFacade;

    @Override
    public RoleVo getRoleById(Long id, String mchNo) {
        PmsRoleDto role = pmsPermissionFacade.getRoleById(id);
        return BeanUtil.newAndCopy(role, RoleVo.class);
    }

    @Override
    public PageResult<List<RoleVo>> listRolePage(Map<String, Object> paramMap, PageQuery pageQuery, String mchNo) {
        PageResult<List<PmsRoleDto>> pageResult = pmsPermissionFacade.listRolePage(paramMap, pageQuery);
        return PageResult.newInstance(BeanUtil.newAndCopy(pageResult.getData(), RoleVo.class), pageResult);
    }

    @Override
    public List<RoleVo> listAllRoles(Map<String, String> paramMap, String mchNo) {
        List<PmsRoleDto> roleList = pmsPermissionFacade.listAllRoles();
        return BeanUtil.newAndCopy(roleList, RoleVo.class);
    }

    @Override
    public List<AuthVo> listAuthByRoleId(Long roleId, String mchNo) {
        List<PmsAuthDto> pmsAuthList = pmsPermissionFacade.listAuthByRoleId(roleId);
        return BeanUtil.newAndCopy(pmsAuthList, AuthVo.class);
    }

    @Override
    public List<RoleVo> listRoleByUserId(Long userId, String mchNo){
        List<PmsRoleDto> roleList = pmsPermissionFacade.listRolesByUserId(userId);
        return BeanUtil.newAndCopy(roleList, RoleVo.class);
    }

    @Override
    public boolean assignPermission(Long roleId, List<Long> functionIds, String mchNo) {
        pmsPermissionFacade.assignPermission(roleId, functionIds);
        return true;
    }

    @Override
    public boolean addRole(RoleVo roleVo, String creator) {
        PmsRoleDto pmsRole = BeanUtil.newAndCopy(roleVo, PmsRoleDto.class);
        pmsPermissionFacade.createRole(pmsRole);
        return true;
    }

    @Override
    public boolean editRole(RoleVo roleVo, String modifier, String mchNo) {
        PmsRoleDto pmsRole = BeanUtil.newAndCopy(roleVo, PmsRoleDto.class);
        pmsPermissionFacade.updateRole(pmsRole);
        return true;
    }

    @Override
    public boolean deleteRoleAndRelatedById(Long roleId, String mchNo) {
        pmsPermissionFacade.deleteRoleById(roleId);
        return true;
    }
}
