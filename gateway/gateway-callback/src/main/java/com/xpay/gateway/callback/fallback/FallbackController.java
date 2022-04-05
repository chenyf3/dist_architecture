package com.xpay.gateway.callback.fallback;

import com.xpay.common.utils.JsonUtil;
import com.xpay.gateway.callback.enums.CompanyEnum;
import com.xpay.gateway.callback.enums.RespCodeEnum;
import com.xpay.gateway.callback.helper.CompanyHelper;
import com.xpay.gateway.callback.params.RequestParam;
import com.xpay.gateway.callback.util.RequestUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * hystrix的熔断、降级时会进入到此处
 * @author chenyf
 */
@RestController
public class FallbackController {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    @Autowired
    CompanyHelper companyHelper;

    /**
     * 有配置Hystrix熔断器时，发生熔断或降级时会进入此方法
     * @param exchange
     * @return
     */
    @RequestMapping("/fallback")
    public Mono<byte[]> fallback(ServerWebExchange exchange) {
        try {
            RequestParam param = RequestUtil.getRequestParam(exchange);
            CompanyEnum company = param != null ? param.getCompany() : null;
            String code = RespCodeEnum.FALLBACK.getValue();
            String msg = "服务繁忙!";

            Map<String, Object> response = companyHelper.buildResponse(company, code, msg);
            byte[] respBodyByte = JsonUtil.toJson(response).getBytes(StandardCharsets.UTF_8);
            return Mono.just(respBodyByte);
        } catch (Throwable e) {
            logger.error("fallback方法执行时出现异常, errorMsg: {}", e.getMessage());
            throw e;
        }
    }
}
