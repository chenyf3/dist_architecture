package com.xpay.common.statics.constants.mqdest;

import com.xpay.common.statics.annotations.Queue;

import java.lang.reflect.Field;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 队列分组（消费分组），属于逻辑上的业务分组，跟ActiveMQ本身无关，如果是RocketMQ，可作为消息的tags
 */
public class TopicGroup {
    @Queue(desc = "通用模块", group = "")//不好归类，和其他流程都涉及到的，例如：商户通知
    public static final String COMMON_GROUP = "common";
    @Queue(desc = "支付相关")
    public static final String PAYMENT_GROUP = "payment";
    @Queue(desc = "出款相关")
    public static final String REMIT_GROUP = "remit";
    @Queue(desc = "退款相关")
    public static final String REFUND_GROUP = "refund";
    @Queue(desc = "平台商户账务相关")
    public static final String ACCOUNT_MCH_GROUP = "accountMch";
    @Queue(desc = "会员相关")
    public static final String MEMBER_GROUP = "member";
    @Queue(desc = "商户清结算")
    public static final String MCH_SETTLE_GROUP = "mchSettle";
    /**
     * key为属性值，value为该属性上@Queue注解的描述内容
     *
     * @return
     */
    public static Map<String, String> toMap() {
        Map<String, String> map = new LinkedHashMap<>();
        Field[] fields = TopicGroup.class.getDeclaredFields();
        for (Field field : fields) {
            Queue queue = field.getAnnotation(Queue.class);
            if (queue == null) {
                continue;
            }

            String desc = queue.desc();
            String name;
            try {
                field.setAccessible(true);
                name = String.valueOf(field.get(TopicGroup.class));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            map.put(name, desc);
        }
        return map;
    }
}
