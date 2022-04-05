package com.xpay.service.sequence.config;

import com.xpay.libs.id.config.RedisProperties;
import com.xpay.libs.id.config.SegmentProperties;
import com.xpay.libs.id.config.SnowFlakeProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "sequence")
public class SeqProperties {
    /**
     * 数据库分段发号的配置
     */
    private SegmentProperties segment = new SegmentProperties();
    /**
     * 雪花算法的配置
     */
    private SnowFlakeProperties snowflake = new SnowFlakeProperties();
    /**
     * redis自增id的配置
     */
    private RedisProperties redisSeq = new RedisProperties();

    public SegmentProperties getSegment() {
        return segment;
    }

    public void setSegment(SegmentProperties segment) {
        this.segment = segment;
    }

    public SnowFlakeProperties getSnowflake() {
        return snowflake;
    }

    public void setSnowflake(SnowFlakeProperties snowflake) {
        this.snowflake = snowflake;
    }

    public RedisProperties getRedisSeq() {
        return redisSeq;
    }

    public void setRedisSeq(RedisProperties redisSeq) {
        this.redisSeq = redisSeq;
    }
}
