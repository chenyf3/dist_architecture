package com.xpay.libs.id.service;

import com.xpay.libs.id.config.RedisProperties;
import com.xpay.libs.id.common.IdGenException;
import com.xpay.libs.id.generator.zero.ZeroIDGen;
import com.xpay.libs.id.generator.IDGen;
import com.xpay.libs.id.generator.redis.RedisIDGenImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class RedisIdService {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private IDGen idGen;
    private RedisProperties properties;

    public RedisIdService(RedisProperties properties) {
        this.properties = properties;
        init();
    }

    public Long getId(String key) throws IdGenException {
        return idGen.get(key);
    }

    public List<Long> getId(String key, int count) throws IdGenException {
        return idGen.get(key, count);
    }

    public Long getId(String key, long maxValue) throws IdGenException {
        return idGen.get(key, maxValue);
    }

    public void destroy() {
        this.idGen.destroy();
    }

    private void init() {
        boolean flag = properties != null && properties.getEnabled();
        if (flag) {
            idGen = new RedisIDGenImpl(properties);

            if (idGen.init()) {
                logger.info("RedisId Service Init Successfully");
            } else {
                throw new RuntimeException("RedisId Service Init Fail");
            }
        } else {
            idGen = new ZeroIDGen("RedisIdService Use ZeroIDGen!");
        }
    }
}
