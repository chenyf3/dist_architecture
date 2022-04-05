package com.xpay.web.portal.service;

import com.xpay.common.statics.exception.BizException;
import com.xpay.common.utils.BeanUtil;
import com.xpay.facade.user.dto.PortalAuthDto;
import com.xpay.facade.user.service.PortalPermissionFacade;
import com.xpay.web.api.common.ddo.vo.AuthVo;
import com.xpay.web.api.common.service.AuthService;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AuthServiceImpl implements AuthService {
    @DubboReference
    PortalPermissionFacade portalPermissionFacade;

    @Override
    public AuthVo getAuthById(Long id) {
        PortalAuthDto function = portalPermissionFacade.getAuthById(id);
        return BeanUtil.newAndCopy(function, AuthVo.class);
    }

    @Override
    public List<AuthVo> listAllAuth(Map<String, String> paramMap, String mchNo) {
        List<PortalAuthDto> functionList = portalPermissionFacade.listAllAuthByMchNo(mchNo);
        return BeanUtil.newAndCopy(functionList, AuthVo.class);
    }

    @Override
    public boolean addAuth(AuthVo functionDto) {
        throw new BizException(BizException.BIZ_INVALID, "不支持此功能");
    }

    @Override
    public boolean editAuth(AuthVo function) {
        throw new BizException(BizException.BIZ_INVALID, "不支持此功能");
    }

    @Override
    public boolean deleteAuthAndRelated(Long id) {
        throw new BizException(BizException.BIZ_INVALID, "不支持此功能");
    }

    @Override
    public Map<String, String> getAuthMapByUserId(Long userId, String mchNo){
        Map<String, String> functionMap = new HashMap<>();
        List<AuthVo> functionList = listAuthByUserId(userId, mchNo);
        if(functionList != null && ! functionList.isEmpty()){
            for(AuthVo function : functionList){
                functionMap.put(function.getPermissionFlag().trim(), function.getName());
            }
        }
        return functionMap;
    }

    @Override
    public List<AuthVo> listAuthByUserId(Long userId, String mchNo) {
        List<PortalAuthDto> functionList = portalPermissionFacade.listAuthByUserId(userId);
        return BeanUtil.newAndCopy(functionList, AuthVo.class);
    }

    @Override
    public List<AuthVo> listAuthByParentId(Long parentId, String mchNo) {
        List<PortalAuthDto> functionList = portalPermissionFacade.listAuthByParentId(parentId);
        return BeanUtil.newAndCopy(functionList, AuthVo.class);
    }
}
