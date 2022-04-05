package com.xpay.common.statics.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 数据库的唯一主键，注解到属性上，表示这个属性就是primary key
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface Queue {
    /**
     * 描述
     * @return
     */
    String desc();

    /**
     * 队列分组（业务线）
     * @return
     */
    String group() default "";

    /**
     * 虚拟队列名(非虚拟队列时不用填)
     * @return
     */
    String vtopic() default "";
}
