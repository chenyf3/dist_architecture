package com.xpay.common.api.config;

import com.alibaba.csp.sentinel.adapter.spring.webmvc.callback.BlockExceptionHandler;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.servlet.Servlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnClass({Servlet.class, DispatcherServlet.class, WebMvcConfigurer.class, BlockExceptionHandler.class})
@Configuration
public class SentinelMvcAutoConfiguration {

    /**
     * webflux模式下，sentinel是通过 {@link com.alibaba.csp.sentinel.adapter.reactor.SentinelReactorSubscriber}来实现流控的，
     * 在webmvc模式下，sentinel是通过 {@link com.alibaba.csp.sentinel.adapter.spring.webmvc.AbstractSentinelInterceptor} 来实现流控的
     * 两者的实现机制是不一样的，在webflux模式下，有 {@link com.alibaba.csp.sentinel.slots.block.BlockException} 抛出时，
     * 是通过webflux的异常处理机制来处理的(即WebExceptionHandler相关子类)，此时，是可以被我们自定义的 {@link com.xpay.common.api.webflux.GlobalExceptionHandler}
     * 所捕获然后进行异常处理的，而在webmvc模式下，在 AbstractSentinelInterceptor 中，是catch住了异常，然后通过
     * {@link com.alibaba.csp.sentinel.adapter.spring.webmvc.config.BaseWebMvcConfig#getBlockExceptionHandler()} 获取到异常
     * 处理器来处理的，所以，在webmvc模式下必须配置一个异常处理器并注册到spring容器中去。
     * @return
     */
    @Bean
    public BlockExceptionHandler blockExceptionHandler(){
        return new BlockExceptionHandler() {
            @Override
            public void handle(HttpServletRequest request, HttpServletResponse response, BlockException e) throws Exception {
                throw e;//直接抛出异常，让全局异常处理器来处理
            }
        };
    }
}
