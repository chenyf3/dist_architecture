package com.xpay.gateway.callback.service.impl;

import com.xpay.gateway.callback.enums.CompanyEnum;
import com.xpay.gateway.callback.enums.RespCodeEnum;
import com.xpay.gateway.callback.params.RequestParam;
import com.xpay.gateway.callback.service.CompanyService;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 微信支付使用样例
 */
@Component(CompanyEnum.WECHAT_PAY_BEAN_NAME)
public class WechatPayCompanyServiceImpl implements CompanyService {

    @Override
    public void fillSignInfo(RequestParam requestParam) {
        //TODO
    }

    @Override
    public boolean isSignaturePass(String signature, byte[] content) {
        //TODO
        return false;
    }

    @Override
    public void modifyRequestParam(RequestParam requestParam) {
        //TODO
    }

    @Override
    public Map<String, Object> buildResponse(String code, String msg) {
        //TODO
        boolean isSuccess = RespCodeEnum.SUCCESS.getValue().equals(code);
        Map<String, Object> map = new HashMap<>();
        map.put("return_code", isSuccess ? "SUCCESS" : "FAIL");
        map.put("return_msg", msg);
        return map;
    }

    @Override
    public Map<String, Object> buildResponse(boolean isSuccess) {
        //TODO
        Map<String, Object> map = new HashMap<>();
        map.put("return_code", isSuccess ? "SUCCESS" : "FAIL");
        map.put("return_msg", isSuccess ? "成功" : "失败");
        return map;
    }
}
