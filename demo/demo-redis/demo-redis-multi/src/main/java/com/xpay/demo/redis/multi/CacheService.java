package com.xpay.demo.redis.multi;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * 测试 @Cacheable 注解的使用
 */
@Service
public class CacheService {

    @Cacheable(value = "demoCache", key = "methodName + '.' + #name")
    public Map<String, String> getMap(String name){
        System.out.println("not use cache, name = " + name);

        Map<String, String> map = new HashMap<>();
        map.put("key_1", "value_1");
        map.put("key_2", "value_2");
        map.put("key_3", "value_3");
        map.put("name", name);
        return map;
    }

    @Cacheable(value = "demoCache2", key = "methodName + '.' + #name")
    public Map<String, String> getMap2(String name){
        System.out.println("not use cache2, name = " + name);

        Map<String, String> map = new HashMap<>();
        map.put("key2_1", "value_1");
        map.put("key2_2", "value_2");
        map.put("key2_3", "value_3");
        map.put("name", name);
        return map;
    }
}
