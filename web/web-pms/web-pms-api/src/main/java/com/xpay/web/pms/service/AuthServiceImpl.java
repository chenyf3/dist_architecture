package com.xpay.web.pms.service;

import com.xpay.common.utils.BeanUtil;
import com.xpay.facade.user.dto.PmsAuthDto;
import com.xpay.facade.user.service.PmsPermissionFacade;
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
    PmsPermissionFacade pmsPermissionFacade;

    @Override
    public AuthVo getAuthById(Long id) {
        PmsAuthDto function = pmsPermissionFacade.getAuthById(id);
        return BeanUtil.newAndCopy(function, AuthVo.class);
    }

    @Override
    public List<AuthVo> listAllAuth(Map<String, String> paramMap, String mchNo) {
        List<PmsAuthDto> functionList = pmsPermissionFacade.listAllAuth(null);
        return BeanUtil.newAndCopy(functionList, AuthVo.class);
    }

    @Override
    public boolean addAuth(AuthVo function) {
        PmsAuthDto functionDto = BeanUtil.newAndCopy(function, PmsAuthDto.class);
        pmsPermissionFacade.createAuth(functionDto);
        return true;
    }

    @Override
    public boolean editAuth(AuthVo function) {
        PmsAuthDto current = pmsPermissionFacade.getAuthById(function.getId());
        current.setName(function.getName());
        current.setIcon(function.getIcon());
        current.setNumber(function.getNumber());
        current.setUrl(function.getUrl());
        current.setPermissionFlag(function.getPermissionFlag());
        pmsPermissionFacade.updateAuth(current);
        return true;
    }

    @Override
    public boolean deleteAuthAndRelated(Long id) {
        pmsPermissionFacade.deleteAuthById(id);
        return true;
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
        List<PmsAuthDto> authList = pmsPermissionFacade.listAuthByUserId(userId);
        return BeanUtil.newAndCopy(authList, AuthVo.class);
    }

    @Override
    public List<AuthVo> listAuthByParentId(Long parentId, String mchNo) {
        List<PmsAuthDto> authList = pmsPermissionFacade.listAuthByParentId(parentId);
        return BeanUtil.newAndCopy(authList, AuthVo.class);
    }
}
