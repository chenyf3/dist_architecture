package com.xpay.common.validator.validators;

import com.xpay.common.validator.util.ValidUtil;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PwdValidator implements ConstraintValidator<Pwd, String> {

	@Override
	public boolean isValid(String value, ConstraintValidatorContext context) {
		return ValidUtil.validPassword(value);
	}

}
