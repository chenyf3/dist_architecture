package com.xpay.gateway.api.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.codec.HttpMessageReader;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.http.codec.multipart.SynchronossPartHttpMessageReader;
import org.springframework.web.reactive.config.EnableWebFlux;
import org.springframework.web.reactive.config.WebFluxConfigurer;

import java.util.List;

@Configuration
//@EnableWebFlux
public class GatewayWebFluxConfigurer implements WebFluxConfigurer {

//    @Override
//    public void configureHttpMessageCodecs(ServerCodecConfigurer configurer) {
//        List<HttpMessageReader<?>> messageReaders = configurer.getReaders();
//        messageReaders.forEach(messageReader -> {
//            if(messageReader instanceof SynchronossPartHttpMessageReader){
//                SynchronossPartHttpMessageReader partReader = (SynchronossPartHttpMessageReader) messageReader;
//                partReader.setMaxParts(1);//限制只能上传
//                partReader.setMaxDiskUsagePerPart(2 * 1024 * 1024L);//2M
//                partReader.setMaxInMemorySize(1 * 1024 * 1024);
//            }
//        });
//    }
}
