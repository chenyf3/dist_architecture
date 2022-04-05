package com.xpay.common.utils;

/**
 * 
 * @author healy
 * 
 */
public class MaskUtil {

	/**
	 * 隐藏手机号
	 * 
	 * @param cellphoneNo
	 * @return
	 */
	public static String maskCellphone(String cellphoneNo) {
		if (StringUtil.isEmpty(cellphoneNo) || cellphoneNo.trim().length() != 11) {
			return cellphoneNo;
		}
		return new StringBuilder().append(cellphoneNo.substring(0, 3)).append("****").append(cellphoneNo.substring(cellphoneNo.length() - 4)).toString();
	}

	/**
	 * 隐藏邮箱信息
	 * 
	 * @param email
	 * @return
	 */
	public static String maskEmail(String email) {
		if(StringUtil.isEmpty(email) || !email.contains("@")){
			return email;
		}
		int idx = email.indexOf("@");
		String prefix = email.substring(0, idx);
		return mask(prefix, 1, prefix.length() > 2 ? 1 : 0) + email.substring(idx); 
	}

	/**
	 * 隐藏卡号信息
	 * 
	 * @param cardNo
	 * @return
	 */
	private static String maskCardNo(String cardNo) {
		if (StringUtil.isEmpty(cardNo) || cardNo.trim().length() <= 8) {
			return cardNo;
		}
		cardNo = cardNo.trim();
		int length = cardNo.length();
		String firstFourNo = cardNo.substring(0, 6);
		String lastFourNo = cardNo.substring(length - 4);
		StringBuffer mask = new StringBuffer("");
		for (int i = 0; i < length - 8; i++) {
			mask.append("*");
		}
		return firstFourNo + mask.toString() + lastFourNo;
	}

	/**
	 * 隐藏身份证号码
	 * 
	 * @param idCardNo
	 * @return
	 */
	public static String maskIDCardNo(String idCardNo) {
		return maskCardNo(idCardNo);
	}

	/**
	 * 隐藏银行卡号码
	 * 
	 * @param bankCardNo
	 * @return
	 */
	public static String maskBankCardNo(String bankCardNo) {
		return maskCardNo(bankCardNo);
	}
	
	/**
	 * 隐藏中间字符
	 * （如mask("1234567890", 4, 2)结果为1234****90）
	 * @param str
	 * @param front 前显示位数
	 * @param tail  后显示位数
	 * @return
	 */
	public static String mask(String str, int front, int tail){
		if (StringUtil.isEmpty(str) || str.length() < front + tail) {
			return str;
		}
		String frontStr = str.substring(0, front);
		String tailStr = str.substring(str.length() - tail);
		StringBuilder maskStr = new StringBuilder();
		int mashLen = str.length() - (front + tail);
		for (int i = 0; i < mashLen; i++) {
			maskStr.append("*");
		}
		return frontStr + maskStr.toString() + tailStr;
	}
	
}
