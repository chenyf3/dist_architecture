package com.xpay.sdk.api.utils;

import com.xpay.sdk.api.enums.SignType;
import com.xpay.sdk.api.exceptions.SDKException;

import java.nio.charset.StandardCharsets;

/**
 * 签名、验签的工具类
 * @author chenyf
 * @date 2018-12-15
 */
public class SignUtil {

    /**
     * 生成签名
     * @param data
     * @param key
     * @return
     */
    public static String sign(byte[] data, String singType, String key){
        if (SignType.RSA.getValue().equals(singType)) {
            return RSAUtil.sign(data, key);
        } else if (SignType.MD5.getValue().equals(singType)) {
            return genMD5Sign(data, key);
        } else {
            //抛出签名失败的异常
            throw new SDKException("签名失败，未预期的签名类型：" + singType);
        }
    }

    /**
     * 同步响应时验证签名
     * @param data
     * @param key
     * @return
     */
    public static boolean verify(byte[] data, String signature, String signType, String key){
        if(SignType.RSA.getValue().equals(signType)){
            return RSAUtil.verify(data, key, signature);
        }else if(SignType.MD5.getValue().equals(signType)){
            String signStr = genMD5Sign(data, key);
            return signStr.equals(signature);
        }else{
            return false;
        }
    }

    private static String genMD5Sign(byte[] signData, String key) {
        String dataStr = new String(signData, StandardCharsets.UTF_8);
        return CodeUtil.base64Encode(MD5Util.getMD5(dataStr + key));
    }
}
