package com.xpay.gateway.callback.helper;

import com.xpay.common.statics.exception.BizException;
import com.xpay.gateway.callback.enums.CompanyEnum;
import com.xpay.gateway.callback.service.CompanyService;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;

import java.util.HashMap;
import java.util.Map;

public class CompanyHelper {
    private ApplicationContext applicationContext;

    public CompanyHelper(ApplicationContext applicationContext){
        this.applicationContext = applicationContext;
    }

    public static CompanyEnum determineCompany(String oriPath){
        return CompanyEnum.getEnumByFirstPath(oriPath);
    }

    public CompanyService getCompanyService(CompanyEnum company) {
        if(company == null) {
            throw new BizException("未预期的回调方：null");
        }

        String beanName = CompanyEnum.getCompanyBeanName(company);
        if (beanName == null) {
            throw new BizException("未预期的回调方：" + company.name());
        }

        try {
            return applicationContext.getBean(beanName, CompanyService.class);
        } catch (NoSuchBeanDefinitionException e) {
            throw new BizException("当前回调方未注册：" + company.name());
        }
    }

    public Map<String, Object> buildResponse(CompanyEnum company, String code, String msg){
        if (company == null) {
            Map<String, Object> resp = new HashMap<>();
            resp.put("code", code);
            resp.put("msg", msg);
            return resp;
        }
        return getCompanyService(company).buildResponse(code, msg);
    }
}
