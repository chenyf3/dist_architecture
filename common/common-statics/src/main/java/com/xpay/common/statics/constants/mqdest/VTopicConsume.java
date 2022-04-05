package com.xpay.common.statics.constants.mqdest;

import com.xpay.common.statics.annotations.Queue;

import java.lang.reflect.Field;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 虚拟队列 (VirtualTopic) 的消费者
 */
public class VTopicConsume {


    /**
     * 二维map，第一个key为虚拟队列的名称，第二个key为属性值，value为该属性上@Queue注解的描述内容
     *
     * @return
     */
    public static Map<String, Map<String, String>> toVTopicMap() {
        Map<String, Map<String, String>> outMap = new LinkedHashMap<>();

        Field[] fields = VTopicConsume.class.getDeclaredFields();
        for (Field field : fields) {
            Queue queue = field.getAnnotation(Queue.class);
            if (queue == null) {
                continue;
            }

            String vtopic = queue.vtopic();
            String desc = queue.desc();
            String name;
            try {
                field.setAccessible(true);
                name = String.valueOf(field.get(VTopicConsume.class));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            if (!outMap.containsKey(vtopic)) {
                outMap.put(vtopic, new LinkedHashMap<>());
            }
            outMap.get(vtopic).put(name, desc);
        }
        return outMap;
    }

    public static Map<String, String> getMap(String vtopic) {
        if (vtopic == null || vtopic.trim().length() <= 0 || !vtopic.startsWith("VirtualTopic.")) {
            return new LinkedHashMap<>();
        }

        Map<String, String> map = new LinkedHashMap<>();
        Field[] fields = VTopicConsume.class.getDeclaredFields();
        for (Field field : fields) {
            Queue queue = field.getAnnotation(Queue.class);
            if (queue == null || !vtopic.equals(queue.vtopic())) {
                continue;
            }

            String desc = queue.desc();
            String name;
            try {
                field.setAccessible(true);
                name = String.valueOf(field.get(VTopicConsume.class));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            map.put(name, desc);
        }
        return map;
    }
}
