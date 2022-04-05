package com.xpay.common.validator.validators;

import com.xpay.common.validator.util.ValidUtil;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;


/**
 * 手机号码验证
 *
 */
public class PhoneValidator implements ConstraintValidator<Phone, String> {

	@Override
	public boolean isValid(String value, ConstraintValidatorContext context) {
		if (ValidUtil.isNotEmpty(value)) {
			return ValidUtil.isMobile(value);
		}
		return true;
	}
}
