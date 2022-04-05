package com.xpay.common.validator.validators;

import com.xpay.common.validator.util.ValidUtil;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;


/**
 * 金额校验
 *
 */
public class AmountValidator implements ConstraintValidator<Amount, String> {

	@Override
	public boolean isValid(String value, ConstraintValidatorContext context) {
		return ValidUtil.isEmpty(value) || ValidUtil.isAmount(value);
	}

}
