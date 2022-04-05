/*
 * Copyright 2017-2020 The OpenTracing Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.xpay.starter.amq.consume;

import org.springframework.jms.annotation.JmsListenerConfigurer;
import org.springframework.jms.config.JmsListenerEndpointRegistrar;

/**
 * 自定义一个JmsListenerConfigurer配置，就可以改变Endpoint在注册过程中的行为
 */
public class CustomizeJmsListenerConfigurer implements JmsListenerConfigurer {
    private final JmsListenerEndpointRegistryWrapper registry;

    public CustomizeJmsListenerConfigurer(JmsListenerEndpointRegistryWrapper registry) {
        this.registry = registry;
    }

    /**
     * 当前方法在 {@link org.springframework.jms.annotation.JmsListenerAnnotationBeanPostProcessor#afterSingletonsInstantiated} 中会被调用
     * 而此方法的入参就是{@link org.springframework.jms.annotation.JmsListenerAnnotationBeanPostProcessor#registrar}属性，素以，我们就可以
     * 设置registrar的相关属性了，比如：把其 endpointRegistry 属性设置为我们自定义的 JmsListenerEndpointRegistryWrapper
     *
     * @param registrar
     */
    @Override
    public void configureJmsListeners(JmsListenerEndpointRegistrar registrar) {
        //设置注册器
        registry.setRegistrar(registrar);

        //设置自定义的EndpointRegistry，那么在 JmsListenerAnnotationBeanPostProcessor#afterSingletonsInstantiated 方法中便不会使用
        //默认的JmsListenerEndpointRegistry，这个类的实例化是在 JmsBootstrapConfiguration 中配置的
        registrar.setEndpointRegistry(registry);
    }
}
