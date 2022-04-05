package com.xpay.web.portal.service;

import com.xpay.common.statics.constants.common.PublicStatus;
import com.xpay.common.statics.enums.user.RoleTypeEnum;
import com.xpay.common.statics.exception.BizException;
import com.xpay.common.statics.query.PageQuery;
import com.xpay.common.statics.result.PageResult;
import com.xpay.common.utils.BeanUtil;
import com.xpay.common.utils.StringUtil;
import com.xpay.facade.user.dto.PortalAuthDto;
import com.xpay.facade.user.dto.PortalRoleDto;
import com.xpay.facade.user.service.PortalPermissionFacade;
import com.xpay.web.api.common.ddo.vo.AuthVo;
import com.xpay.web.api.common.ddo.vo.RoleVo;
import com.xpay.web.api.common.service.RoleService;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class RoleServiceImpl implements RoleService {
    @DubboReference
    PortalPermissionFacade portalPermissionFacade;

    @Override
    public RoleVo getRoleById(Long id, String mchNo) {
        validateMchNo(mchNo);
        PortalRoleDto role = portalPermissionFacade.getRoleById(id);
        if(! mchNo.equals(role.getMchNo())){
            throw new BizException("非法操作，当前角色不属于当前商户！");
        }
        return BeanUtil.newAndCopy(role, RoleVo.class);
    }

    @Override
    public PageResult<List<RoleVo>> listRolePage(Map<String, Object> paramMap, PageQuery pageQuery, String mchNo) {
        validateMchNo(mchNo);

        if(paramMap == null){
            paramMap = new HashMap<>();
        }
        paramMap.put("mchNo", mchNo);

        PageResult<List<PortalRoleDto>> pageResult = portalPermissionFacade.listRolePage(paramMap, pageQuery);
        if (pageResult.getData() == null || pageResult.getData().isEmpty()) {
            return PageResult.newInstance(new ArrayList<>(), pageResult);
        }

        List<RoleVo> roleVoList = BeanUtil.newAndCopy(pageResult.getData(), RoleVo.class);
        return PageResult.newInstance(roleVoList, pageResult);
    }

    @Override
    public List<RoleVo> listAllRoles(Map<String, String> paramMap, String mchNo) {
        validateMchNo(mchNo);
        Map<String, Object> map = new HashMap<>();
        map.put("mchNo", mchNo);
        if(paramMap != null){
            for(Map.Entry<String, String> entry : paramMap.entrySet()){
                map.put(entry.getKey(), entry.getValue());
            }
        }

        List<PortalRoleDto> roleList = portalPermissionFacade.listAllRoles(map);
        return BeanUtil.newAndCopy(roleList, RoleVo.class);
    }

    @Override
    public List<AuthVo> listAuthByRoleId(Long roleId, String mchNo) {
        validateMchNo(mchNo);
        RoleVo role = getRoleById(roleId, mchNo);
        if(! mchNo.equals(role.getMchNo())){
            throw new BizException("非法操作，当前角色不属于当前商户！");
        }

        List<PortalAuthDto> functionList = portalPermissionFacade.listAuthByRoleId(roleId);
        return BeanUtil.newAndCopy(functionList, AuthVo.class);
    }

    @Override
    public List<RoleVo> listRoleByUserId(Long userId, String mchNo){
        validateMchNo(mchNo);
        List<PortalRoleDto> roleList = portalPermissionFacade.listRolesByUserId(userId, mchNo);
        if(roleList == null || roleList.isEmpty()){
            return new ArrayList<>();
        }

        List<RoleVo> voList = new ArrayList<>();
        for(PortalRoleDto role : roleList){
            if(! mchNo.equals(role.getMchNo())){
                throw new BizException("非法操作，当前角色不属于当前商户！");
            }
            RoleVo dto = BeanUtil.newAndCopy(role, RoleVo.class);
            voList.add(dto);
        }
        return voList;
    }

    @Override
    public boolean assignPermission(Long roleId, List<Long> functionIds, String mchNo) {
        validateMchNo(mchNo);
        RoleVo role = getRoleById(roleId, mchNo);
        if(! mchNo.equals(role.getMchNo())){
            throw new BizException("非法操作，当前角色不属于当前商户！");
        }else if(role.getRoleType() == RoleTypeEnum.ADMIN.getValue()){
            throw new BizException("非法操作，不可修改此角色的权限！");
        }

        portalPermissionFacade.assignRolePermission(roleId, functionIds);
        return true;
    }

    @Override
    public boolean addRole(RoleVo roleVo, String creator) {
        validateMchNo(roleVo.getMchNo());
        PortalRoleDto portalRole = BeanUtil.newAndCopy(roleVo, PortalRoleDto.class);
        portalRole.setAutoAssign(PublicStatus.INACTIVE);
        portalPermissionFacade.createRole(portalRole);
        return true;
    }

    @Override
    public boolean editRole(RoleVo roleVo, String modifier, String mchNo) {
        validateMchNo(mchNo);
        RoleVo role = getRoleById(roleVo.getId(), mchNo);
        if(! mchNo.equals(role.getMchNo())){
            throw new BizException("非法操作，当前角色不属于当前商户！");
        }

        PortalRoleDto PortalRole = BeanUtil.newAndCopy(roleVo, PortalRoleDto.class);
        portalPermissionFacade.updateRole(PortalRole);
        return true;
    }

    @Override
    public boolean deleteRoleAndRelatedById(Long roleId, String mchNo) {
        validateMchNo(mchNo);
        RoleVo role = getRoleById(roleId, mchNo);
        if(! mchNo.equals(role.getMchNo())){
            throw new BizException("非法操作，当前角色不属于当前商户！");
        }

        portalPermissionFacade.deleteRoleById(roleId);
        return true;
    }

    private void validateMchNo(String mchNo){
        if(StringUtil.isEmpty(mchNo)){
            throw new BizException("mchNo不能为空");
        }
    }
}
