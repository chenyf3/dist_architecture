package com.xpay.common.utils;

import org.joda.time.DateTime;
import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @Description:
 * @author: chenyf
 * @Date: 2018/1/5
 */
public class DateUtil {
    public static final String DATE_ONLY_REGEX = "^[1-9]\\d{3}-(0[1-9]|1[0-2])-(0[1-9]|[1-2][0-9]|3[0-1])$";
    public static final String DATE_TIME_REGEX = "^[1-9]\\d{3}-(0[1-9]|1[0-2])-(0[1-9]|[1-2][0-9]|3[0-1])(\\s+(20|21|22|23|[0-1]\\d):[0-5]\\d:[0-5]\\d)$";

    public static final DateTimeFormatter DATE_TIME_MILLS_FORMATTER = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss.SSS");
    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
    public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormat.forPattern("yyyy-MM-dd");
    public static final DateTimeFormatter COMPACT_DATE_FORMATTER = DateTimeFormat.forPattern("yyyyMMdd");
    public static final DateTimeFormatter COMPACT_DATETIME_FORMATTER = DateTimeFormat.forPattern("yyyyMMddHHmmss");
    public static final DateTimeFormatter SHORT_DATE_FORMATTER = DateTimeFormat.forPattern("yyMMdd");

    /**
     * 比较 source 和 target 大小，如果 source > target 则返回1，如果 source = target 则返回0，如果 source < target 则返回-1
     * @param source    对比日期
     * @param target    被对比日期
     * @param withUnit 对比的维度单元，可选值有：Calendar.YEAR/MONTH/DATE/HOUR/MINUTE/SECOND/MILLISECOND
     * @return
     */
    public static int compare(Date source, Date target, int withUnit) {
        Calendar dateCal = Calendar.getInstance();
        dateCal.setTime(source);

        Calendar otherDateCal = Calendar.getInstance();
        otherDateCal.setTime(target);
        switch (withUnit) {
            case Calendar.YEAR:
                dateCal.clear(Calendar.MONTH);
                otherDateCal.clear(Calendar.MONTH);
            case Calendar.MONTH:
                dateCal.set(Calendar.DATE, 1);
                otherDateCal.set(Calendar.DATE, 1);
            case Calendar.DATE:
                dateCal.set(Calendar.HOUR_OF_DAY, 0);
                otherDateCal.set(Calendar.HOUR_OF_DAY, 0);
            case Calendar.HOUR:
                dateCal.clear(Calendar.MINUTE);
                otherDateCal.clear(Calendar.MINUTE);
            case Calendar.MINUTE:
                dateCal.clear(Calendar.SECOND);
                otherDateCal.clear(Calendar.SECOND);
            case Calendar.SECOND:
                dateCal.clear(Calendar.MILLISECOND);
                otherDateCal.clear(Calendar.MILLISECOND);
            case Calendar.MILLISECOND:
                break;
            default:
                throw new IllegalArgumentException("withUnit 单位字段 " + withUnit + " 不合法！！");
        }
        return dateCal.compareTo(otherDateCal);
    }

    /**
     * 判断date时间是否在[start,end] 之间
     * @param date      对比日期
     * @param begin     开始日期
     * @param end       结束日期
     * @return
     */
    public static boolean isBetween(Date date, Date begin, Date end){
        return begin.getTime() <= date.getTime() &&  date.getTime() <= end.getTime();
    }

    /**
     * 判断date是否已经超过begin多少秒
     * @param date      对比日期
     * @param begin     被对比日期
     * @param seconds   超前的秒数
     * @return
     */
    public static boolean isOverhead(Date date, Date begin, int seconds){
        return date.getTime() - addSecond(begin, seconds).getTime() > 0;
    }

    /**
     * 在date日期上往前或往后推移years年
     * @param date      日期
     * @param years     往前或往后推移的年数，负数时表示往前推移
     * @return
     */
    public static Date addYear(Date date, int years) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.YEAR, years);
        return calendar.getTime();
    }

    /**
     * 在date日期上往前或往后推移months个月
     * @param date      日期
     * @param months    往前或往后推移的月数，负数时表示往前推移
     * @return
     */
    public static Date addMonth(Date date, int months) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.MONTH, months);
        return calendar.getTime();
    }

    /**
     * 在date日期上往前或往后推移days天
     * @param date  日期
     * @param days  往前或往后推移的天数，负数时表示往前推移
     * @return
     */
    public static Date addDay(Date date, int days) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DATE, days);
        return calendar.getTime();
    }

    /**
     * 在date日期上往前或往后推移minutes分钟
     * @param date      日期
     * @param minutes   往前或往后推移的分钟数，负数时表示往前推移
     * @return
     */
    public static Date addMinute(Date date, int minutes) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.MINUTE, minutes);
        return calendar.getTime();
    }

    /**
     * 在date日期上往前或往后推移seconds秒数
     * @param date      日期
     * @param seconds   往前或往后推移的秒数，负数时表示往前推移
     * @return
     */
    public static Date addSecond(Date date, int seconds) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.SECOND, seconds);
        return calendar.getTime();
    }

    /**
     * 获取秒数
     * @return
     */
    public static int getSecond(Date date) {
        return (int) (date.getTime() / 1000);
    }

    /**
     * 判断是否是日期格式，如：2017-05-31
     * @param str
     * @return
     */
    public static boolean isDateFormat(String str) {
        Matcher mat = Pattern.compile(DATE_ONLY_REGEX).matcher(str);
        return mat.matches();
    }

    /**
     * 判断是否是日期+时间格式，2017-05-31 15:24:31
     * @return
     */
    public static boolean isDateTimeFormat(String str) {
        if (str == null) {
            return false;
        }
        Matcher mat = Pattern.compile(DATE_TIME_REGEX).matcher(str);
        return mat.matches();
    }

    /**
     * 转换为东八区时区(Asia/Shanghai)
     * @param zonedDateTime
     * @return
     */
    public static ZonedDateTime shanghaiZone(ZonedDateTime zonedDateTime) {
        return zonedDateTime != null ? zonedDateTime.withZoneSameInstant(ZoneId.of("Asia/Shanghai")) : null;
    }

    /**
     * 把Date格式转成 yyyy-MM-dd HH:mm:ss 格式的字符串
     * @param date
     * @return
     */
    public static String formatDateTime(Date date) {
        LocalDateTime dateTime = parseJodaDateTime(date);
        return dateTime.toString(DATE_TIME_FORMATTER);
    }

    /**
     * 把秒数转换为 yyyy-MM-dd HH:mm:ss 格式的字符串
     * @param seconds
     * @return
     */
    public static String formatDateTime(long seconds) {
        return formatDateTime(new Date(seconds * 1000));
    }

    /**
     * 把Date格式转成 yyyy-MM-dd HH:mm:ss.SSS 格式的字符串
     * @param date
     * @return
     */
    public static String formatDateTimeMills(Date date) {
        LocalDateTime dateTime = parseJodaDateTime(date);
        return dateTime.toString(DATE_TIME_MILLS_FORMATTER);
    }

    /**
     * 把Date格式转换成 yyyy-MM-dd 格式的字符串
     * @param date
     * @return
     */
    public static String formatDate(Date date) {
        LocalDateTime dateTime = parseJodaDateTime(date);
        return dateTime.toString(DATE_FORMATTER);
    }

    /**
     * 把输入的字符串转换成 yyyy-MM-dd 的字符串格式
     * @param dateStr 可能为 yyyy-MM-dd HH:mm:ss 或 yyyy-MM-dd 格式
     * @return
     */
    public static String formatDate(String dateStr) {
        LocalDateTime dateTime = parseJodaDateTime(dateStr);
        return dateTime.toString(DATE_FORMATTER);
    }

    /**
     * 把Date格式转换成 yyMMdd 格式的字符串
     * @param date
     * @return
     */
    public static String formatShortDate(Date date) {
        LocalDateTime dateTime = parseJodaDateTime(date);
        return dateTime.toString(SHORT_DATE_FORMATTER);
    }

    /**
     * 把Date格式转换成 yyyyMMdd 格式的字符串
     * @param date
     * @return
     */
    public static String formatCompactDate(Date date) {
        LocalDateTime dateTime = parseJodaDateTime(date);
        return dateTime.toString(COMPACT_DATE_FORMATTER);
    }

    /**
     * 把Date格式转换成 yyyyMMddHHmmss 格式的字符串
     * @param date
     * @return
     */
    public static String formatCompactDateTime(Date date) {
        LocalDateTime dateTime = parseJodaDateTime(date);
        return dateTime.toString(COMPACT_DATETIME_FORMATTER);
    }

    /**
     * 把Date转换成只带年月日的Date格式(即 yyyy-MM-dd 00:00:00)
     * @param date
     * @return
     */
    public static Date convertDate(Date date) {
        LocalDateTime dateTime = parseJodaDateTime(date);
        String dateStr = dateTime.toString(DATE_FORMATTER);
        return DATE_FORMATTER.parseDateTime(dateStr).toDate();
    }

    /**
     * 字符串转换成只带年月日的Date格式(即 yyyy-MM-dd 00:00:00)
     * @param dateStr   格式 yyyy-MM-dd
     * @return
     */
    public static Date convertDate(String dateStr) {
        LocalDateTime dateTime = parseJodaDateTime(dateStr);
        String date = dateTime.toString(DATE_FORMATTER);
        return DATE_FORMATTER.parseDateTime(date).toDate();
    }

    /**
     * 把日期格式字符串转换为Date格式
     * @param dateTimeStr   yyyy-MM-dd HH:mm:ss
     * @return
     */
    public static Date convertDateTime(String dateTimeStr){
        DateTime dateTime = DATE_TIME_FORMATTER.parseDateTime(dateTimeStr);
        return dateTime.toDate();
    }

    /**
     * 取得没有毫秒时间戳的日期
     * @return
     */
    public static Date getDateWithoutMills(Date date) {
        LocalDateTime dateTime = parseJodaDateTime(date);
        return dateTime.withMillisOfSecond(0).toDate();
    }

    /**
     * 得到date参数当天的起始时间点(即 yyyy-MM-dd 00:00:00)
     * @return
     */
    public static Date getDayStart(Date date) {
        LocalDateTime dateTime = parseJodaDateTime(date);
        return dateTime.withTime(0, 0, 0, 0).toDate();
    }

    /**
     * 得到date参数当天的起始时间点(返回秒)
     * @return
     */
    public static int getDayStartSecond(Date date) {
        String timestamp = String.valueOf(getDayStart(date).getTime() / 1000);
        return Integer.valueOf(timestamp);
    }

    /**
     * 得到date参数当天的结束时间点(即 yyyy-MM-dd 23:59:59)
     * @param date
     * @return
     */
    public static Date getDayEnd(Date date) {
        LocalDateTime dateTime = parseJodaDateTime(date);
        return dateTime.withTime(23, 59, 59, 999).toDate();
    }

    /**
     * 获取date参数所在月份的开始日期
     * @param date
     * @return
     */
    public static Date getMonthStartDay(Date date){
        LocalDateTime startDay = parseJodaDateTime(date).dayOfMonth().withMinimumValue();
        return startDay.toDate();
    }

    /**
     * 获取date参数所在月份的结束日期
     * @param date
     * @return
     */
    public static Date getMonthEndDay(Date date){
        LocalDateTime endDay = parseJodaDateTime(date).dayOfMonth().withMaximumValue();
        return endDay.toDate();
    }

    /**
     * 把秒时间戳转换成毫秒时间戳(即 秒 * 1000)
     * @param seconds
     * @return
     */
    public static long secondToMillSecond(int seconds){
        return seconds * 1000L; //此处把秒乘以1000转成毫秒时一定要是Long型的1000，否则会使时间变小
    }

    public static LocalDateTime parseJodaDateTime(Date date) {
        return new LocalDateTime(date);
    }

    public static LocalDateTime parseJodaDateTime(Long dateTime) {
        return new LocalDateTime(dateTime);
    }

    public static LocalDateTime parseJodaDateTime(String dateStr) {
        return new LocalDateTime(dateStr);
    }

    public static void main(String[] args) {
        Date date = DateUtil.getDayStart(new Date());
        Date date2 = DateUtil.getDayEnd(new Date());
        System.out.println(DateUtil.formatDateTime(date));
        System.out.println(DateUtil.formatDateTime(date2));
        System.out.println(getDayStartSecond(new Date()));
        System.out.println(formatDateTime(new Date()));
    }
}
