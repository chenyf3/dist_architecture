package com.xpay.utils

/**
 * jenkins任务辅助工具类
 */
public class JobUtil {

    /**
     * 根据应用名称获取该应用在jenkins中的任务名
     * @param appName
     * @return
     */
    public static String getJobName(String appName){
        return appName
    }

    /**
     * 分离出收件人，即收件人列表中的第一个
     * @param mailAddressComma
     * @return
     */
    public static String getEmailTo(String mailAddressComma){
        String[] emailArr = mailAddressComma.split(",")
        if(emailArr.length > 0){
            return emailArr[0]
        }else{
            return ''
        }
    }

    /**
     * 分离出抄送人，即收件人列表中的第二个之后的
     * @param mailAddressComma
     * @return
     */
    public static String getEmailCC(String mailAddressComma){
        String[] emailArr = mailAddressComma.split(",")
        if(emailArr.length <= 1){
            return ''
        }

        List ccList = []
        for(int i=1; i<emailArr.length; i++){
            if(emailArr[i] != null && emailArr[i].trim().length() > 0){
                ccList.add(emailArr[i])
            }
        }
        return ccList.size() >= 1 ? ccList.join(',') : ''
    }

    /**
     * 计算一个字符串在一个数组中的索引位置，若改字符串在数组中不存在则返回 -1
     * @param appArr
     * @param appName
     * @return
     */
    public static int getIndex(String[] appArr, String appName){
        if(appArr != null && appArr.length > 0 && appName != null && appName.trim().length() > 0){
            for(int i=0; i<appArr.length; i++){
                if(appName.equals(appArr[i])){
                    return i
                }
            }
        }
        return -1
    }
}
