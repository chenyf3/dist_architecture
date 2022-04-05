package com.xpay.common.validator.validators;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.FIELD, ElementType.ANNOTATION_TYPE })  
@Retention(RetentionPolicy.RUNTIME)  
@Constraint(validatedBy = PhoneValidator.class)
public @interface Phone {
	String message() default "手机号码错误";  
	  
    Class<?>[] groups() default {};  
  
    Class<? extends Payload>[] payload() default {};
}
