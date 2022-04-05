package com.xpay.common.validator.validators;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.FIELD, ElementType.ANNOTATION_TYPE })  
@Retention(RetentionPolicy.RUNTIME)  
@Constraint(validatedBy = AmountValidator.class)

/**
 * 校验金额类型，允许为空，不为空时校验规则参考
 *
 * @param null
 * @return
 **/
public @interface Amount {

	String message() default "格式错误";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};


}
