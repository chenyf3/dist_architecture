package com.xpay.web.api.common.service;

import com.xpay.web.api.common.ddo.vo.AuthVo;

import java.util.List;
import java.util.Map;

public interface AuthService {

    boolean addAuth(AuthVo authVo);

    boolean editAuth(AuthVo authVo);

    Map<String, String> getAuthMapByUserId(Long userId, String mchNo);

    List<AuthVo> listAuthByUserId(Long userId, String mchNo);

    AuthVo getAuthById(Long id);

    List<AuthVo> listAllAuth(Map<String, String> paramMap, String mchNo);

    List<AuthVo> listAuthByParentId(Long parentId, String mchNo);

    /**
     * 删除当前功能，以及与该功能关系的关系表记录
     * @param id
     * @return
     */
    boolean deleteAuthAndRelated(Long id);
}
