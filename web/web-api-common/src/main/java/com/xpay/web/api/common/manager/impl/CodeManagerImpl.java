package com.xpay.web.api.common.manager.impl;

import com.xpay.common.utils.StringUtil;
import com.xpay.starter.plugin.client.RedisClient;
import com.xpay.web.api.common.manager.CodeManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CodeManagerImpl implements CodeManager {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private RedisClient redisClient;

    public CodeManagerImpl(RedisClient redisClient){
        this.redisClient = redisClient;
    }

    @Override
    public boolean cacheCode(String codeKey, String code, int expireSec, String limitKey){
        if(StringUtil.isNotEmpty(limitKey)){
            redisClient.set(limitKey, "1", 60);
        }
        return redisClient.set(codeKey, code, expireSec);
    }

    @Override
    public String getCode(String codeKey){
        if(StringUtil.isEmpty(codeKey)){
            return null;
        }
        return redisClient.get(codeKey);
    }

    @Override
    public void deleteCode(String codeKey, String limitKey) {
        try{
            redisClient.del(codeKey);
            if(StringUtil.isNotEmpty(limitKey)){
                redisClient.del(limitKey);
            }
        }catch (Exception e){
            logger.error("删除code时出现异常 codeKey = {}", codeKey, e);
        }
    }

    @Override
    public String getLimitVal(String limitKey) {
        return redisClient.get(limitKey);
    }
}
