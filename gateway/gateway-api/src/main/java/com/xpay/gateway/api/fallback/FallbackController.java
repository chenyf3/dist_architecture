package com.xpay.gateway.api.fallback;

import com.xpay.common.api.constants.HttpHeaderKey;
import com.xpay.common.api.params.APIParam;
import com.xpay.gateway.api.config.conts.ReqCacheKey;
import com.xpay.gateway.api.helper.RequestHelper;
import com.xpay.gateway.api.params.FileUploadParam;
import com.xpay.gateway.api.params.RequestParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import com.xpay.gateway.api.params.ResponseParam;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

/**
 * hystrix的熔断、降级时会进入到此处
 * @author chenyf
 */
@RestController
public class FallbackController {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    @Autowired
    RequestHelper requestHelper;

    /**
     * 有配置Hystrix熔断器时，发生熔断或降级时会进入此方法
     * @param exchange
     * @return
     */
    @RequestMapping("/fallback")
    public Mono<byte[]> fallback(ServerWebExchange exchange) {
        try {
            RequestParam requestParam = getRequestParam(exchange);
            String mchNo = requestParam == null ? "" : requestParam.getMchNo();
            String signType = requestParam == null ? "" : requestParam.getSignType();
            String version = requestParam == null ? "" : requestParam.getVersion();

            //因为调用后端服务超时的时候也会进入fallback，此时，我们并不知道业务处理结果，所以，统一返回 "结果未知" 的响应信息
            ResponseParam responseParam = ResponseParam.unknown(mchNo);
            responseParam.setSignType(signType);
            responseParam.setRespMsg("服务繁忙，结果未知");

            byte[] respBodyByte = responseParam.toResponseBody().getBytes(StandardCharsets.UTF_8);
            String signature = requestHelper.genSignature(respBodyByte, mchNo, new APIParam(signType, version));
            exchange.getResponse().getHeaders().set(HttpHeaderKey.SIGNATURE_HEADER, signature);
            return Mono.just(respBodyByte);
        } catch (Throwable e) {
            logger.error("fallback方法执行时出现异常, errorMsg: {}", e.getMessage());
            throw e;
        }
    }

    private RequestParam getRequestParam(ServerWebExchange exchange) {
        Object obj = exchange.getAttributes().get(ReqCacheKey.CACHE_REQUEST_BODY_OBJECT_KEY);
        if(obj == null){
            return null;
        }else if(obj instanceof RequestParam){
            ((RequestParam) obj).setData(null); //把data置为null，避免日志打印时出现一堆内容
            return (RequestParam) obj;
        }else{
            return null;
        }
    }
}
