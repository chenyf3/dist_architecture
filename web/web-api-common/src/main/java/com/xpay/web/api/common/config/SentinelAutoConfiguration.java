package com.xpay.web.api.common.config;

import com.alibaba.csp.sentinel.adapter.spring.webmvc.callback.BlockExceptionHandler;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.authority.AuthorityException;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeException;
import com.alibaba.csp.sentinel.slots.block.flow.FlowException;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowException;
import com.alibaba.csp.sentinel.slots.system.SystemBlockException;
import com.xpay.common.statics.result.RestResult;
import com.xpay.common.utils.JsonUtil;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@ConditionalOnClass({
        com.alibaba.csp.sentinel.adapter.spring.webmvc.callback.DefaultBlockExceptionHandler.class,
        javax.servlet.http.HttpServletRequest.class
})
@Configuration
public class SentinelAutoConfiguration {

    @Bean
    public BlockExceptionHandler blockExceptionHandler(){
        return new BlockExceptionHandler() {
            @Override
            public void handle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, BlockException e) throws Exception {
                RestResult result;
                if (e instanceof FlowException) {
                    result = RestResult.sysError("请求频繁，系统限流！");
                } else if (e instanceof DegradeException) {
                    result = RestResult.sysError("请求频繁，系统降级！");
                } else if (e instanceof ParamFlowException) {
                    result = RestResult.sysError("请求频繁，热点数据限流！");
                } else if (e instanceof SystemBlockException) {
                    result = RestResult.sysError("系统过载！");
                } else if (e instanceof AuthorityException) {
                    result = RestResult.unAuth("授权不通过");
                } else {
                    result = RestResult.error("非法操作，未预期保护措施！");
                }
                // http状态码
                httpServletResponse.setStatus(500);
                httpServletResponse.setCharacterEncoding("utf-8");
                httpServletResponse.setHeader("Content-Type", "application/json;charset=utf-8");
                httpServletResponse.setContentType("application/json;charset=utf-8");
                httpServletResponse.getWriter().write(JsonUtil.toJson(result));
            }
        };
    }
}
