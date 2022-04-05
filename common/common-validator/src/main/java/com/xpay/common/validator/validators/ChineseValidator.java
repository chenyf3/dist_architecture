package com.xpay.common.validator.validators;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * 中文校验
 *
 */
public class ChineseValidator implements ConstraintValidator<Chinese, String> {

	@Override
	public boolean isValid(String value, ConstraintValidatorContext context) {
		if (value == null) return true;
        for (char c : value.toCharArray()) {
            if (c >= 0x4E00 &&  c <= 0x9FA5 || isChineseChar(c)) return false;// 有一个中文字符就返回
        }
        return true;
	}
	
	/**
     * 判断一个字符串是否含有中文符号
     * @param c
     * @return
     */
   	private static final boolean isChineseChar(char c) {
   	    Character.UnicodeBlock ub = Character.UnicodeBlock.of(c);
   	    if (ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS
   	      || ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS
   	      || ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A
   	      || ub == Character.UnicodeBlock.GENERAL_PUNCTUATION
   	      || ub == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION
   	      || ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS) {
   	     return true;
   	    }
   	    return false;
   	 }

}
