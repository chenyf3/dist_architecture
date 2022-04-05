package com.xpay.common.validator.validators;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.FIELD, ElementType.ANNOTATION_TYPE })  
@Retention(RetentionPolicy.RUNTIME)  
@Constraint(validatedBy = ChineseValidator.class)
public @interface Chinese {
	String message() default "不能含有中文字符";  
	  
    Class<?>[] groups() default {};  
  
    Class<? extends Payload>[] payload() default {};
}
