package com.xpay.service.message.biz.common;

import com.xpay.common.statics.exception.BizException;
import freemarker.core.ParseException;
import freemarker.template.MalformedTemplateNameException;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.ui.freemarker.FreeMarkerConfigurationFactoryBean;

import java.io.StringWriter;
import java.util.Map;
import java.util.Objects;

@Component
public class TemplateResolver {
    private FreeMarkerConfigurationFactoryBean freeMarkerConfiguration;

    public TemplateResolver(FreeMarkerConfigurationFactoryBean freeMarkerConfiguration){
        this.freeMarkerConfiguration = freeMarkerConfiguration;
    }

    public String resolve(String tplName, Map<String, Object> paramMap) {
        Template template;

        try {
            template = Objects.requireNonNull(freeMarkerConfiguration.getObject()).getTemplate(tplName);
        } catch (TemplateNotFoundException e) {
            throw new BizException(BizException.BIZ_INVALID, e.getTemplateName() + " 模版不存在");
        } catch (MalformedTemplateNameException e) {
            throw new BizException(BizException.BIZ_INVALID, e.getTemplateName() + " 模版名称不合法，" + e.getMalformednessDescription());
        } catch (ParseException e) {
            throw new BizException(BizException.BIZ_INVALID, e.getTemplateName() + "，" + e.getMessage());
        } catch (Exception e) {
            throw new BizException(BizException.BIZ_INVALID, tplName + " 模版解析出现异常，" + e.getMessage());
        }

        try {
            StringWriter result = new StringWriter();
            template.process(paramMap, result);
            return result.toString();
        } catch (TemplateException e) {
            throw new BizException(BizException.BIZ_INVALID, e.getTemplateSourceName() + "，" + e.getMessage());
        } catch (Exception e) {
            throw new BizException(BizException.BIZ_INVALID, tplName + " 模版处理过程出现异常，" + e.getMessage());
        }
    }
}
