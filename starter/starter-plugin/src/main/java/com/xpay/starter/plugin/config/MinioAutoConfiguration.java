package com.xpay.starter.plugin.config;

import com.xpay.starter.plugin.annotation.ConditionalOnPropertyNotEmpty;
import com.xpay.starter.plugin.client.MinioClient;
import com.xpay.starter.plugin.client.ObjectStorage;
import com.xpay.starter.plugin.properties.MinioProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@ConditionalOnClass(io.minio.MinioClient.class)
@EnableConfigurationProperties(MinioProperties.class)
@Configuration
public class MinioAutoConfiguration {

    @ConditionalOnPropertyNotEmpty(value = "minio.endpoint")
    @Bean(name = "minioStorage", destroyMethod = "destroy")
    public ObjectStorage minioStorage(MinioProperties properties){
        return new MinioClient(properties);
    }
}
