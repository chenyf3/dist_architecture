package com.xpay.starter.plugin.config;

import com.xpay.starter.plugin.client.FastdfsClient;
import com.xpay.starter.plugin.properties.FastdfsProperties;
import com.xpay.libs.fdfs.fasfdfs.ClientGlobal;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Author: Cmf
 * Date: 2019/11/13
 * Time: 19:13
 * Description:
 */
@Configuration
@ConditionalOnClass(ClientGlobal.class)
@EnableConfigurationProperties(FastdfsProperties.class)
public class FastdfsAutoConfiguration {

    @Bean
    public FastdfsClient fastdfsClient(FastdfsProperties fastdfsProperties) {
        return new FastdfsClient(fastdfsProperties);//创建客户端对象并初始化
    }
}
