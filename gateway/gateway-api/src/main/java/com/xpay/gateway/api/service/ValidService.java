package com.xpay.gateway.api.service;

import com.xpay.gateway.api.params.FileUploadParam;
import com.xpay.common.utils.JsonUtil;
import com.xpay.gateway.api.params.RequestParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @description 网关校验失败之后的处理器：如：IP校验失败、签名校验失败 等
 * @author: chenyf
 */
public class ValidService {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * 验签失败之后的处理，如果是发送邮件之类的通知，需注意限制发送通知的频率，如：同一个商户每分钟不超过5次，不然可能会面临邮件轰炸
     * @param requestIp     用户请求的IP
     * @param routeId       定义路由的id
     * @param requestParam  用户的请求数据，当用户没有传入数据时为null
     * @param cause         验签失败的异常，可能为null
     */
    public void afterSignValidFail(String routeId, String requestIp, RequestParam requestParam, Throwable cause){
        logger.error("验签失败 routeId={} requestIp={} RequestParam={}", routeId, requestIp, requestParam.toString(), cause);
    }

    public void afterSignValidFail(String routeId, String requestIp, FileUploadParam uploadParam, Throwable cause){
        logger.error("验签失败 routeId={} requestIp={} FileUploadParam={}", routeId, requestIp, uploadParam.toString(), cause);
    }

    /**
     * IP校验失败之后的处理，如果是发送邮件之类的通知，需注意限制发送通知的频率，如：同一个商户每分钟不超过5次，不然可能会面临邮件轰炸
     * @param routeId       定义路由的id
     * @param requestIp     用户请求的IP
     * @param expectIp      实际要求的IP，可能为null
     * @param requestParam  用户的请求数据，当用户没有传入数据时为null
     */
    public void afterIpValidFail(String routeId, String requestIp, String expectIp, RequestParam requestParam){
        logger.error("IP校验失败 routeId={} requestIp={} expectIp={} RequestParam={}", routeId, requestIp, expectIp, requestParam.toString());
    }
}
