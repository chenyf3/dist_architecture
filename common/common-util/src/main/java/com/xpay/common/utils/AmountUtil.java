package com.xpay.common.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 
 * @author chenyf
 * 
 */
public class AmountUtil {
	private static final char[] RMB_NUMS = "零壹贰叁肆伍陆柒捌玖".toCharArray();
	private static final String[] UNITS = {"元", "角", "分", "整"};
	private static final String[] U1 = {"", "拾", "佰", "仟"};
	private static final String[] U2 = {"", "万", "亿"};

	/**
	 * 判断是否为0
	 * @param amount
	 * @return
	 */
	public static boolean isZero(String amount){
		if (amount == null || amount.trim().length() == 0) {
			return false;
		}

		String[] amountArr = amount.split("\\.");
		for(String str : amountArr){
			for(int i=0; i<str.length(); i++){
				if(str.charAt(i) != '0') {
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * 加法运算
	 * 
	 * @param v1
	 * @param v2
	 * @return
	 */
	public static double add(double v1, double v2) {
		BigDecimal b1 = BigDecimal.valueOf(v1);
		BigDecimal b2 = BigDecimal.valueOf(v2);
		return b1.add(b2).doubleValue();
	}

	/**
	 * 加法运算
	 *
	 * @param v1
	 * @param v2
	 * @return
	 */
	public static double add(double v1, double v2, double... v3) {
		double result = add(v1, v2);
		if(v3 != null && v3.length > 0){
			for(int i=0; i<v3.length; i++){
				result = add(result, v3[i]);
			}
		}
		return result;
	}

	/**
	 * 加法运算
	 *
	 * @param v1
	 * @param v2
	 * @return
	 */
	public static BigDecimal add(BigDecimal v1, BigDecimal v2) {
//		BigDecimal b1 = new BigDecimal(Double.toString(v1.doubleValue()));
//		BigDecimal b2 = new BigDecimal(Double.toString(v2.doubleValue()));
//		return new BigDecimal(Double.toString(b1.add(b2).doubleValue()));
		return v1.add(v2);
	}

	/**
	 * 加法运算
	 * @param v1
	 * @param v2
	 * @param v3
	 * @return
	 */
	public static BigDecimal add(BigDecimal v1, BigDecimal v2, BigDecimal... v3) {
		BigDecimal result = add(v1, v2);
		if(v3 != null && v3.length > 0){
			for(int i=0; i<v3.length; i++){
				result = add(result, v3[i]);
			}
		}
		return result;
	}
	
	/**
	 * 减法运算
	 * 
	 * @param v1 被减数
	 * @param v2 减数
	 * @return
	 */
	public static double sub(double v1, double v2) {
		BigDecimal b1 = BigDecimal.valueOf(v1);
		BigDecimal b2 = BigDecimal.valueOf(v2);
		return b1.subtract(b2).doubleValue();
	}

	/**
	 * 减法运算
	 * @param v1
	 * @param v2
	 * @param v3
	 * @return
	 */
	public static double sub(double v1, double v2, double... v3) {
		double result = sub(v1, v2);
		if(v3 != null && v3.length > 0){
			for(int i=0; i<v3.length; i++){
				result = sub(result, v3[i]);
			}
		}
		return result;
	}

	/**
	 * 减法运算
	 *
	 * @param v1 被减数
	 * @param v2 减数
	 * @return
	 */
	public static BigDecimal sub(BigDecimal v1, BigDecimal v2) {
		return v1.subtract(v2);
	}

	/**
	 * @description v1 减去 v2、v3....的结果，结果精度默认为6
	 * @author: chenyf
	 * @param v1
	 * @param v2
	 * @param v3
	 * @return
	 */
	public static BigDecimal sub(BigDecimal v1, BigDecimal v2, BigDecimal... v3) {
		BigDecimal result = sub(v1, v2);
		if(v3 != null && v3.length > 0){
			for(int i=0; i<v3.length; i++){
				result = sub(result, v3[i]);
			}
		}
		return result;
	}

	/**
	 * 乘法运算
	 * 
	 * @param v1 被乘数
	 * @param v2 乘数
	 * @return
	 */
	public static double mul(double v1, double v2) {
		BigDecimal b1 = BigDecimal.valueOf(v1);
		BigDecimal b2 = BigDecimal.valueOf(v2);
		return b1.multiply(b2).doubleValue();
	}
	
	/**
	 * 
	 * @description: 返回运算数1和运算数2相乘后的结果，结果类型为BigDecimal
	 * @param v1          运算数1，类型为BigDecimal
	 * @param v2          运算数2，类型为BigDecimal
	 * @return       返回运算数1和运算数2相乘后的结果
	 *
	 */
	public static BigDecimal mul(BigDecimal v1, BigDecimal v2) {
		return mul(v1, v2, 2);
	}

	/**
	 *
	 * @description: 返回运算数1和运算数2相乘后的结果，结果类型为BigDecimal
	 * @param v1          运算数1，类型为BigDecimal
	 * @param v2          运算数2，类型为BigDecimal
	 * @param scale  运算精度
	 * @return       返回运算数1和运算数2相乘后的结果
	 */
	public static BigDecimal mul(BigDecimal v1, BigDecimal v2, int scale) {
		if(scale < 0) {
			throw new IllegalArgumentException("The scale must be a positive integer or zero");
		}
		//由scale指定精度，精度后面的数字使用四舍五入
		return v1.multiply(v2).divide(BigDecimal.ONE, scale, RoundingMode.HALF_UP);
	}
	
	/**
	 * 除法运算，当发生除不尽的情况时，精确到小数点以后2位，以后的数字四舍五入
	 * @param v1 被除数
	 * @param v2 除数
	 * @return
	 */
	public static double div(double v1, double v2) {
		return div(v1, v2, 2);
	}

	/**
	 * 
	 * 除法运算，当发生除不尽的情况时，由scale参数指定精度，以后的数字四舍五入
	 * 
	 * @param v1 被除数
	 * @param v2 除数
	 * @param scale 精确到小数点以后几位
	 * @return
	 */
	public static double div(double v1, double v2, int scale) {
		if (scale < 0) {
			throw new IllegalArgumentException("The scale must be a positive integer or zero");
		}
		BigDecimal b1 = BigDecimal.valueOf(v1);
		BigDecimal b2 = BigDecimal.valueOf(v2);
		return b1.divide(b2, scale, BigDecimal.ROUND_HALF_UP).doubleValue();
	}
	
	/**
	 * 
	 * @description:  运算数1与运算数2相除，默认精度为6
	 * @param v1            运算数1
	 * @param v2            运算数2
	 * @return
	 *
	 */
	public static BigDecimal div(BigDecimal v1, BigDecimal v2) {
		return div(v1, v2, 6);
	}
	
	/**
	 * 
	 * @description: 运算数1与运算数2相除
	 * @param v1          运算数1
	 * @param v2          运算数2
	 * @param scale  精度
	 * @return       返回运算1与运算2相除的结果，结果精度为scale
	 *
	 */
	public static BigDecimal div(BigDecimal v1, BigDecimal v2, int scale) {
		if(scale < 0) {
			throw new IllegalArgumentException("The scale must be a positive integer or zero");
		}
		
		BigDecimal result = v1.divide(v2, scale, RoundingMode.HALF_UP);
		return result;
	}

	/**
	 * 判断 a 与 b 是否相等
	 * 
	 * @param a
	 * @param b
	 * @return a==b 返回true, a!=b 返回false
	 */
	public static boolean equal(double a, double b) {
		BigDecimal v1 = BigDecimal.valueOf(a);
		BigDecimal v2 = BigDecimal.valueOf(b);
		if (v1.compareTo(v2) == 0) {
			return true;
		}
		return false;
	}
	
	/**
	 * 
	 * @description: 判断a与b是否相等
	 * @param a      类型为BigDecimal
	 * @param b      类型为BigDecimal
	 * @return       a == b，则返回true，a != b, 则返回false
	 * @author:      huang.jin
	 * @date:        2017年9月20日 下午5:37:10
	 *
	 */
	public static boolean equal(BigDecimal a, BigDecimal b) {
		return a.compareTo(b) == 0;
	}

	/**
	 * 判断 a 是否小于 b
	 *
	 * @param a
	 * @param b
	 * @return a&lt;b 返回true, a&gt;=b 返回 false
	 */
	public static boolean lessThan(double a, double b) {
		BigDecimal v1 = BigDecimal.valueOf(a);
		BigDecimal v2 = BigDecimal.valueOf(b);
		if (v1.compareTo(v2) == -1) {
			return true;
		}
		return false;
	}

	/**
	 * 
	 * @description:    判断a值是否小于b值
	 * @param a         类型为BigDecimal
	 * @param b         类型为BigDecimal
	 * @return          
	 * @author:         huang.jin
	 * @date:           2017年9月27日 下午3:21:18
	 *
	 */
	public static boolean lessThan(BigDecimal a, BigDecimal b) {
		return a.compareTo(b) == -1;
	}
	
	/**
	 * 
	 * @description:    a值是否小于等于b
	 * @param a         类型为BigDecimal
	 * @param b         类型为BigDecimal
	 * @return          
	 * @author:         huang.jin
	 * @date:           2017年9月25日 下午4:29:14
	 *
	 */
	public static boolean lessThanOrEqualTo(BigDecimal a, BigDecimal b) {
		return a.compareTo(b) <= 0;
	}
	
	/**
	 * 
	 * @description:    判断a是否大于b
	 * @param a         类型为BigDecimal
	 * @param b         类型为BigDecimal
	 * @return          
	 * @author:         huang.jin
	 * @date:           2017年9月26日 上午11:43:02
	 *
	 */
	public static boolean greater(BigDecimal a, BigDecimal b) {
		return a.compareTo(b) == 1;
	}

	/**
	 * 判断 a 是否大于 b
	 *
	 * @param a
	 * @param b
	 * @return a&gt;b 返回true, a&lt;=b 返回 false
	 */
	public static boolean greater(double a, double b) {
		BigDecimal v1 = BigDecimal.valueOf(a);
		BigDecimal v2 = BigDecimal.valueOf(b);
		return v1.compareTo(v2) == 1;
	}

	/**
	 * 判断 a 是否大于等于 b
	 *
	 * @param a
	 * @param b
	 * @return a&gt;=b 返回true, a&lt;b 返回false
	 */
	public static boolean greaterThanOrEqualTo(BigDecimal a, BigDecimal b) {
		return a.compareTo(b) >= 0;
	}

	/**
	 * 判断 a 是否大于等于 b
	 * 
	 * @param a
	 * @param b
	 * @return a&gt;=b 返回true, a&lt;b 返回false
	 */
	public static boolean greaterThanOrEqualTo(double a, double b) {
		BigDecimal v1 = BigDecimal.valueOf(a);
		BigDecimal v2 = BigDecimal.valueOf(b);
		return v1.compareTo(v2) >= 0;
	}

	/**
	 * 把金额转成中文的金额描述，比如： 123324.49 --> 壹拾贰万叁仟叁佰贰拾肆元肆角玖分
	 * @param amount
	 * @return
	 */
	public static String amount2rmb(String amount) {
		if (amount == null || amount.trim().length() == 0) {
			throw new IllegalArgumentException("输入金额不能为空");
		} else if (!amount.matches("(-)?[\\d]{1,12}(.[\\d]{1,2})?")) { //整数位最大支持到千亿级，小数位最多2位
			throw new IllegalArgumentException("输入金额格式有误");
		}

		//0元的情况
		if (isZero(amount)) {
			return RMB_NUMS[0] + UNITS[0];
		}

		//去掉分隔符
		amount = amount.replace(",", "");

		//判断是否存在负号"-"
		boolean negative = false;
		if(amount.startsWith("-")){
			negative = true;
			amount = amount.replaceAll("-", "");
		}

		//分离整数部分和小数部分
		String integerStr;//整数部分数字
		String decimalStr;//小数部分数字
		if (amount.indexOf(".") == 0) {
			integerStr = "";
			decimalStr = amount.substring(1);
		} else if(amount.indexOf(".") > 0) {
			integerStr = amount.substring(0, amount.indexOf("."));
			decimalStr = amount.substring(amount.indexOf(".") + 1);
		} else {
			integerStr = amount;
			decimalStr = "";
		}

		String result = "";
		if (! isZero(integerStr)) {
			result += integer2rmb(integerStr) + UNITS[0]; // 整数部分
		}

		if (decimalStr.length() == 0 || isZero(decimalStr)) {
			result += UNITS[3]; // 添加[整]
		} else {
			result += decimal2rmb(decimalStr); // 小数部分
		}

		return negative ? "负" + result : result;
	}
	/**
	 * 将金额整数部分转换为中文大写
	 * @param integer	整数位的数字
	 * @return
	 */
	private static String integer2rmb(String integer) {
		StringBuilder buffer = new StringBuilder();
		// 从个位数开始转换
		int i, j;
		for (i = integer.length() - 1, j = 0; i >= 0; i--, j++) {
			char n = integer.charAt(i);
			if (n == '0') {
				// 当n是0且n的右边一位不是0时，插入[零]
				if (i < integer.length() - 1 && integer.charAt(i + 1) != '0') {
					buffer.append(RMB_NUMS[0]);
				}
				// 插入[万]或者[亿]
				if (j % 4 == 0) {
					if (i > 0 && integer.charAt(i - 1) != '0'
							|| i > 1 && integer.charAt(i - 2) != '0'
							|| i > 2 && integer.charAt(i - 3) != '0') {
						buffer.append(U2[j / 4]);
					}
				}
			} else {
				if (j % 4 == 0) {
					buffer.append(U2[j / 4]);     // 插入[万]或者[亿]
				}
				buffer.append(U1[j % 4]);         // 插入[拾]、[佰]或[仟]
				buffer.append(RMB_NUMS[n - '0']); // 插入数字
			}
		}
		return buffer.reverse().toString();
	}
	/**
	 * 将金额小数部分转换为中文大写
	 * @param decimal 小数位的数字
	 * @return
	 */
	private static String decimal2rmb(String decimal) {
		char jiao = decimal.charAt(0); // 角
		String result = RMB_NUMS[jiao - '0'] + UNITS[1];
		if (decimal.length() > 1) {
			char fen  = decimal.charAt(1); // 分
			result += fen > '0' ? RMB_NUMS[fen - '0'] + UNITS[2] : "";
		}
		return result;
	}
	public static void main(String[] args) {
	}
}
