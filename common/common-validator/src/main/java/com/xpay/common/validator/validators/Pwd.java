package com.xpay.common.validator.validators;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 密码校验
 */
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.ANNOTATION_TYPE })  
@Retention(RetentionPolicy.RUNTIME)  
@Constraint(validatedBy = PwdValidator.class)
public @interface Pwd {
	String message() default "瀵嗙爜蹇呴』鍖呭惈瀛楁瘝涓庢暟瀛�";  
	  
    Class<?>[] groups() default {};  
  
    Class<? extends Payload>[] payload() default {};
}