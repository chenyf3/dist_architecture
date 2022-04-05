package com.xpay.common.utils;

import java.util.Random;
import java.util.UUID;

/**
 * @Description:
 * @author: chenyf
 * @Date: 2018/2/7
 */
public class RandomUtil {
    public final static String[] PWD_LOWER_CASES = { "a", "b", "c", "d", "e", "f", "g", "h", "j", "k", "m", "n", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z"};
    public final static String[] PWD_UPPER_CASES = { "A", "B", "C", "D", "E", "F", "G", "H", "J", "K", "M", "N","P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z"};
    public final static String[] PWD_NUMS_LIST = {"2", "3", "4", "5", "6", "7", "8", "9"};//密码能包含的数字
    public final static String[] PWD_SYMBOLS_ARRAY = {"!", "@", "_", "#", "$", "%", "&", "?"};//密码能包含的特殊字符

    /**
     * 产生16长度的随机字符串
     * @return
     */
    public static String get16LenStr(){
        if(getInt(2) == 0){
            return MD5Util.getMD5Hex(UUID.randomUUID().toString()).substring(16);
        }else{
            return MD5Util.getMD5Hex(UUID.randomUUID().toString()).substring(1, 17);
        }
    }

    /**
     * 产生32长度的随机字符串
     * @return
     */
    public static String get32LenStr(){
        return MD5Util.getMD5Hex(UUID.randomUUID().toString());
    }

    /**
     * 产生 min ~ max 之间的随机数
     * @param min
     * @param max
     * @return
     */
    public static int getInt(int min, int max){
        Random rand = new Random();
        return rand.nextInt((max - min) + 1) + min;
    }

    /**
     * 产生 0 ~ max 之间的随机数
     * @param max
     * @return
     */
    public static int getInt(int max){
        Random random = new Random();
        return random.nextInt(max);
    }

    /**
     * 产生 count 长度的随机整型字符串
     * @param count
     * @return
     */
    public static String getDigitStr(int count){
        StringBuilder sb = new StringBuilder();
        for(int i=0; i < count; i++){
            int value = getInt(9);
            sb.append(value);
        }
        return sb.toString();
    }

    /**
     * 生成随机密码：按照密码长度做生成策略，分别为小写字母占密码长度的1/2，大写字母和数字各占1/4，符号占剩余无法除尽的（最多两个符号）
     * @param pwd_len
     * @return
     */
    public static String genRandomPwd(int pwd_len) {
        if (pwd_len < 6 || pwd_len > 20) {
            throw new RuntimeException("密码长度需介于6~20之间");
        }

        int lower = pwd_len / 2;
        int upper = (pwd_len - lower) / 2;
        int num = (pwd_len - lower) / 2;
        int symbol = pwd_len - lower - upper - num;

        Random random = new Random();
        if(symbol <= 1){ //特殊字符保证至少有一个
            int browIdx = random.nextInt(symbol==1 ? 1 : 2);

            //随机选一种借出
            symbol = 1;
            if(0 == browIdx || 6 == pwd_len){
                lower--;
            }else if(1 == browIdx){
                upper--;
            }else{
                num--;
            }
        }

        StringBuffer pwd = new StringBuffer();
        int position;
        while ((lower + upper + num + symbol) > 0) {
            if (lower > 0) {
                position = random.nextInt(pwd.length() + 1);
                pwd.insert(position, PWD_LOWER_CASES[random.nextInt(PWD_LOWER_CASES.length)]);
                lower--;
            }

            if (upper > 0) {
                position = random.nextInt(pwd.length() + 1);
                pwd.insert(position, PWD_UPPER_CASES[random.nextInt(PWD_UPPER_CASES.length)]);
                upper--;
            }

            if (num > 0) {
                position = random.nextInt(pwd.length() + 1);
                pwd.insert(position, PWD_NUMS_LIST[random.nextInt(PWD_NUMS_LIST.length)]);
                num--;
            }

            if (symbol > 0) {
                position = random.nextInt(pwd.length() + 1);
                pwd.insert(position, PWD_SYMBOLS_ARRAY[random.nextInt(PWD_SYMBOLS_ARRAY.length)]);
                symbol--;
            }
        }
        return pwd.toString();
    }

    public static void main(String[] args){
    }
}
