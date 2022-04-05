package com.xpay.service.sequence.config;

import com.xpay.libs.id.service.RedisIdService;
import com.xpay.libs.id.service.SegmentService;
import com.xpay.libs.id.service.SnowflakeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@EnableConfigurationProperties({SeqProperties.class})
@Configuration
public class SequenceConfig {
    @Autowired
    SeqProperties seqProperties;

    @ConditionalOnProperty(value = "sequence.segment.enabled", havingValue = "true")
    @Bean(destroyMethod = "destroy")
    public SegmentService segmentService(){
        return new SegmentService(seqProperties.getSegment());
    }

    @ConditionalOnProperty(value = "sequence.snowflake.enabled", havingValue = "true")
    @Bean(destroyMethod = "destroy")
    public SnowflakeService snowflakeService(){
        return new SnowflakeService(seqProperties.getSnowflake());
    }

    @ConditionalOnProperty(value = "sequence.redisSeq.enabled", havingValue = "true")
    @Bean(destroyMethod = "destroy")
    public RedisIdService redisIdService(){
        return new RedisIdService(seqProperties.getRedisSeq());
    }
}
