package com.xpay.common.validator.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.regex.Pattern;

public class ValidUtil {

    public static boolean isEmpty(Object value) {
        return value == null || value.toString().trim().length() == 0;
    }

    public static boolean isNotEmpty(Object value) {
        return !isEmpty(value);
    }

    /**
     * 验证是否是有效的密码
     *
     * @param pwd         .
     * @param minLength   .
     * @param maxLength   .
     * @param letterMust  .
     * @param numberMust  .
     * @param specialMust .
     * @return .
     */
    public static boolean validPassword(String pwd, int minLength, int maxLength, boolean letterMust, boolean numberMust, boolean specialMust) {
        if (pwd == null || pwd.matches(".*\\s.*")) {
            //不能有空白字符
            return false;
        } else if (pwd.length() < minLength || pwd.length() > maxLength) {
            return false;
        } else if (letterMust && !pwd.matches(".*[a-zA-Z].*")) {
            //必须包含字母
            return false;
        } else if (numberMust && !pwd.matches(".*[0-9].*")) {
            //必须包含数字
            return false;
        } else if (numberMust && !pwd.matches(".*[^0-9a-zA-Z].*")) {
            return false;
        }
        return true;
    }

    public static boolean validPassword(String pwd) {
        Pattern pattern = Pattern.compile("(?=.*[\\d]+)(?=.*[a-zA-Z]+)(?=.*[^a-zA-Z0-9]+).{8,20}");
        return pattern.matcher(pwd).matches();
    }

    /**
     * 验证是否只包含数字和字母，
     * 当为null或者空字母串时，也返回false
     *
     * @param str .
     * @return .
     */
    public static boolean isLetterNumeric(String str) {
        if (str == null) {
            return false;
        }
        Pattern pattern = Pattern.compile("^[A-Za-z0-9]+$");
        return pattern.matcher(str).matches();
    }

    /**
     * 判断是否只包含字母
     * 当为null或者空字符串时，也返回false
     *
     * @param str .
     * @return .
     */
    public static boolean isLetterOnly(String str) {
        if (str == null) {
            return false;
        }
        Pattern pattern = Pattern.compile("^[A-Za-z]+$");
        return pattern.matcher(str).matches();
    }


    /**
     * 判断是否仅包含数字
     * 当为null或者空字符串时，也返回false
     *
     * @param str .
     * @return .
     */
    public static boolean isNumericOnly(String str) {
        if (str == null) {
            return false;
        } else {
            return str.matches("\\d+");
        }
    }

    /**
     * 金额校验
     * 小数点不能超过2位
     * 只为空字符串或Null时，返回false
     *
     * @param str .
     * @return .
     */
    public static boolean isAmount(String str) {
        if (str == null) {
            return false;
        }
        Pattern pattern = Pattern.compile("^\\d+(\\.\\d{1,2})?$");
        return pattern.matcher(str).matches();
    }


    /**
     * 判断是否为整数
     * 若为null或空字符串，也返回false
     *
     * @param str .
     * @return .
     */
    public static boolean isInteger(String str) {
        try {
            Integer.parseInt(str);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }


    /**
     * 判断是否为Double
     * 若为null或者空字符串，也返回false
     *
     * @param str .
     * @return .
     */
    public static boolean isDouble(String str) {
        try {
            Double.parseDouble(str);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }


    /**
     * 判断输入的字符串是否符合Email样式.
     *
     * @param str .
     * @return .
     */
    public static boolean isEmail(String str) {
        if (str == null) {
            return false;
        }
        Pattern pattern = Pattern.compile("^\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*$");
        return pattern.matcher(str).matches();
    }

    /**
     * 判断输入的字符串是否为纯汉字
     * 若为null或者空字符串，也返回false
     *
     * @param str .
     * @return
     */
    public static boolean isChinese(String str) {
        if (str == null) {
            return false;
        }
        Pattern pattern = Pattern.compile("[\u0391-\uFFE5]+$");
        return pattern.matcher(str).matches();
    }


    /**
     * 是否为日期格式：yyyy-MM-dd
     *
     * @param dateStr .
     * @return .
     */
    public static boolean isDate(String dateStr) {
        if (dateStr == null) {
            return false;
        }
        try {
            new SimpleDateFormat("yyyy-MM-dd").parse(dateStr);
            return false;
        } catch (ParseException e) {
            return false;
        }
    }

    /**
     * 是否为时间格式：hh:mm:ss
     *
     * @param timeStr .
     * @return .
     */
    public static boolean isTime(String timeStr) {
        if (timeStr == null) {
            return false;
        }
        try {
            new SimpleDateFormat("HH:mm:ss").parse(timeStr);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 是否为日期时间格式：yyyy-MM-dd HH:mm:ss
     *
     * @param dateTimeStr .
     * @return .
     */
    public static boolean isDateTime(String dateTimeStr) {
        if (dateTimeStr == null) {
            return false;
        }
        try {
            new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(dateTimeStr);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 判断是不是合法手机号码
     *
     * @param mobile .
     * @return .
     */
    public static boolean isMobile(String mobile) {
        if (mobile == null) {
            return false;
        }
        Pattern pattern = Pattern.compile("^1[0-9]{10}$");
        return pattern.matcher(mobile).matches();
    }

    /**
     * 是否为座机 (010-66571346)
     *
     * @param telephone .
     * @return .
     */
    public static boolean isTelephone(String telephone) {
        if (telephone == null) {
            return false;
        }
        Pattern pattern = Pattern.compile("^0[0-9]{2,3}[-|－][0-9]{7,8}([-|－][0-9]{1,4})?$");
        return pattern.matcher(telephone).matches();
    }


    /**
     * 是否为合法IP地址
     *
     * @param ip .
     * @return .
     */
    public static boolean isIP(String ip) {
        if (ip == null) {
            return false;
        }
        Pattern pattern = Pattern
                .compile("^(\\d{1,2}|1\\d\\d|2[0-4]\\d|25[0-5])\\.(\\d{1,2}|1\\d\\d|2[0-4]\\d|25[0-5])\\.(\\d{1,2}|1\\d\\d|2[0-4]\\d|25[0-5])\\.(\\d{1,2}|1\\d\\d|2[0-4]\\d|25[0-5])$");
        return pattern.matcher(ip).matches();
    }

    /**
     * 是否为合法MAC地址，验证十六进制格式
     *
     * @param mac .
     * @return .
     */
    public static boolean isMac(String mac) {
        if (mac == null) {
            return false;
        }
        Pattern pattern = Pattern.compile("^([0-9a-fA-F]{2})(([\\s:-][0-9a-fA-F]{2}){5})$");
        return pattern.matcher(mac).matches();
    }

    /**
     * 是否为合法的QQ号码.<br/>
     * 1、QQ号的开头不能是0为开头 .<br/>
     * 2、QQ号必须由数字构成，不包含其他字符.<br/>
     * 3、QQ号长度为5到15位，多的算违规.
     *
     * @param qq .
     * @return true/false .
     */
    public static boolean isQQ(String qq) {
        if (qq == null) {
            return false;
        }
        Pattern pattern = Pattern.compile("^[1-9][0-9]{4,14}$");
        return pattern.matcher(qq).matches();
    }

    /**
     * 是否为合法的银行卡号
     *
     * @param bankCard 银行卡号
     * @return .
     */
    public static boolean isBankCard(String bankCard) {
        if (bankCard == null) {
            return false;
        }
        if (isNotEmpty(bankCard)) {
            String nonCheckCodeCardId = bankCard.substring(0, bankCard.length() - 1);
            if (nonCheckCodeCardId.matches("\\d+")) {
                char[] chs = nonCheckCodeCardId.toCharArray();
                int luhmSum = 0;
                for (int i = chs.length - 1, j = 0; i >= 0; i--, j++) {
                    int k = chs[i] - '0';
                    if (j % 2 == 0) {
                        k *= 2;
                        k = k / 10 + k % 10;
                    }
                    luhmSum += k;
                }
                char b = (luhmSum % 10 == 0) ? '0' : (char) ((10 - luhmSum % 10) + '0');
                return bankCard.charAt(bankCard.length() - 1) == b;
            }
        }
        return false;
    }

    /**
     * 数字串长度符合要求
     *
     * @param str       .
     * @param minLength 最小长度
     * @param maxLength 最大长度
     * @return .
     */
    public static boolean isStrLengthValid(String str, int minLength, int maxLength) {
        if (str == null) {
            return false;
        }
        return str.length() >= minLength && str.length() <= maxLength;
    }


}
