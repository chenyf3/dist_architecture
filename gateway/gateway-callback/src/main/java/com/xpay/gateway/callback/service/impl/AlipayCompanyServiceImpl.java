package com.xpay.gateway.callback.service.impl;

import com.xpay.gateway.callback.enums.CompanyEnum;
import com.xpay.gateway.callback.params.RequestParam;
import com.xpay.gateway.callback.service.CompanyService;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 支付宝使用样例
 */
@Component(CompanyEnum.ALIPAY_BEAN_NAME)
public class AlipayCompanyServiceImpl implements CompanyService {

    @Override
    public void fillSignInfo(RequestParam requestParam) {
        //TODO
    }

    @Override
    public boolean isSignaturePass(String signature, byte[] content) {
        //TODO

        return true;
    }

    @Override
    public void modifyRequestParam(RequestParam requestParam) {
        //TODO
    }

    @Override
    public Map<String, Object> buildResponse(String code, String msg) {
        //TODO

        Map<String, Object> map = new HashMap<>();
        map.put("code", code);
        map.put("msg", msg);
        return map;
    }

    @Override
    public Map<String, Object> buildResponse(boolean isSuccess) {
        //TODO

        Map<String, Object> map = new HashMap<>();
        map.put("code", isSuccess ? "SUCCESS" : "FAIL");
        map.put("msg", isSuccess ? "成功" : "失败");
        return map;
    }
}
