package com.xpay.common.statics.constants.mqdest;

import com.xpay.common.statics.annotations.Queue;

import java.lang.reflect.Field;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * MQ消息目的地，在ActiveMQ中是Queue名称或虚拟队列名称，在RocketMQ中就是Topic名称，可在消息消费端被业务方用以逻辑划分
 */
public class TopicDest {
    //平台商户账务处理
    @Queue(desc = "平台商户账务处理缓冲", group = TopicGroup.ACCOUNT_MCH_GROUP)
    public static final String ACCOUNT_MCH_PROCESS_BUFFER = "accountMch.accountProcessBuffer";
    @Queue(desc = "平台商户加急账务处理", group = TopicGroup.ACCOUNT_MCH_GROUP)
    public static final String ACCOUNT_MCH_URGENT_PROCESS = "accountMch.urgentAccountProcess";
    @Queue(desc = "平台商户定时账务处理", group = TopicGroup.ACCOUNT_MCH_GROUP)
    public static final String ACCOUNT_MCH_SCHEDULE_PROCESS = "accountMch.scheduleAccountProcess";
    @Queue(desc = "平台商户合并账务处理", group = TopicGroup.ACCOUNT_MCH_GROUP)
    public static final String ACCOUNT_MCH_SCHEDULE_MERGE_PROCESS = "accountMch.scheduleMergeProcess";
    @Queue(desc = "平台商户加急账务回调", group = TopicGroup.ACCOUNT_MCH_GROUP)
    public static final String ACCOUNT_MCH_URGENT_CALLBACK = "accountMch.urgentResultCallback";
    @Queue(desc = "平台商户定时账务回调", group = TopicGroup.ACCOUNT_MCH_GROUP)
    public static final String ACCOUNT_MCH_SCHEDULE_CALLBACK = "accountMch.scheduleResultCallback";
    @Queue(desc = "平台商户垫资清零", group = TopicGroup.ACCOUNT_MCH_GROUP)
    public static final String MCH_ADVANCE_CLEAR = "accountMch.advanceClear";
    @Queue(desc = "平台商户账务处理预警", group = TopicGroup.ACCOUNT_MCH_GROUP)
    public static final String ACCOUNT_MCH_SCHEDULE_PROCESS_ALERT = "accountMch.scheduleProcessAlert";

    //通用(公用)模块
    @Queue(desc = "异步邮件", group = TopicGroup.COMMON_GROUP)
    public static final String EMAIL_SEND_ASYNC = "common.emailSendAsync";
    @Queue(desc = "异步短信", group = TopicGroup.COMMON_GROUP)
    public static final String SMS_SEND_ASYNC = "common.smsSendAsync";
    @Queue(desc = "数据迁移", group = TopicGroup.COMMON_GROUP)
    public static final String DATA_MIGRATION = "common.dataMigration";
    @Queue(desc = "上线发布", group = TopicGroup.COMMON_GROUP)
    public final static String DEV_OPS_PUBLISH_PROJECT = "common.devopsPublishProject";
    @Queue(desc = "商户通知回调", group = TopicGroup.COMMON_GROUP)
    public static final String MERCHANT_NOTIFY = "common.merchantNotifyCallback";

    /**
     * key为属性值，value为该属性上@Queue注解的描述内容
     *
     * @return
     */
    public static Map<String, String> toMap() {
        Map<String, String> map = new LinkedHashMap<>();

        Field[] fields = TopicDest.class.getDeclaredFields();
        for (Field field : fields) {
            Queue queue = field.getAnnotation(Queue.class);
            if (queue == null) {
                continue;
            }

            String desc = queue.desc();
            String name;
            try {
                field.setAccessible(true);
                name = String.valueOf(field.get(TopicDest.class));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            map.put(name, desc);
        }
        return map;
    }

    /**
     * 二维map，第一个key为分组名，第二个key为属性值，value为该属性上@Queue注解的描述内容
     *
     * @return
     */
    public static Map<String, Map<String, String>> toGroupMap() {
        Map<String, Map<String, String>> outMap = new LinkedHashMap<>();

        Field[] fields = TopicDest.class.getDeclaredFields();
        for (Field field : fields) {
            Queue queue = field.getAnnotation(Queue.class);
            if (queue == null) {
                continue;
            }

            String group = queue.group();
            String desc = queue.desc();
            String name;
            try {
                field.setAccessible(true);
                name = String.valueOf(field.get(TopicDest.class));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            if (!outMap.containsKey(group)) {
                outMap.put(group, new LinkedHashMap<>());
            }
            outMap.get(group).put(name, desc);
        }
        return outMap;
    }

    public static Map<String, String> getMap(String group) {
        if (group == null) {
            return new LinkedHashMap<>();
        }

        Map<String, String> map = new LinkedHashMap<>();
        Field[] fields = TopicDest.class.getDeclaredFields();
        for (Field field : fields) {
            Queue queue = field.getAnnotation(Queue.class);
            if (queue == null || !group.equals(queue.group())) {
                continue;
            }

            String desc = queue.desc();
            String name;
            try {
                field.setAccessible(true);
                name = String.valueOf(field.get(TopicDest.class));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            map.put(name, desc);
        }
        return map;
    }

    public static String getGroup(String topic) {
        if (topic == null) {
            return "";
        }
        Field[] fields = TopicDest.class.getDeclaredFields();
        for (Field field : fields) {
            Queue queue = field.getAnnotation(Queue.class);
            if (queue == null) {
                continue;
            }
            try {
                field.setAccessible(true);
                String name = String.valueOf(field.get(TopicDest.class));
                if (!topic.equals(name)) {
                    continue;
                } else {
                    return queue.group();
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return "";
    }
}
