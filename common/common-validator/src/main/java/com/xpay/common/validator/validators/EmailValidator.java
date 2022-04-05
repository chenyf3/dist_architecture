package com.xpay.common.validator.validators;

import com.xpay.common.validator.util.ValidUtil;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;


/**
 * 电子邮件校验
 *
 */
public class EmailValidator  implements ConstraintValidator<Email, String> {

	@Override
	public boolean isValid(String value, ConstraintValidatorContext context) {
		if (ValidUtil.isEmail(value)) {
			return true;
		}
		return false;
	}

}
