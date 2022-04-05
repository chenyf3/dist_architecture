package com.xpay.starter.plugin.config;

import com.xpay.starter.plugin.config.redis.*;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * @author chenyf
 */
@Configuration
@Import({RedisClientConfiguration.class, RedisLockConfiguration.class, RedisLimiterConfiguration.class,
        JedisMultiSourceConfiguration.class, LettuceMultiSourceConfiguration.class, RedissonMultiSourceConfiguration.class
})
public class RedisAutoConfiguration {

}
