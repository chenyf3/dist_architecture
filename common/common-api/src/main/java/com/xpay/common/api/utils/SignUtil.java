package com.xpay.common.api.utils;

import com.xpay.common.statics.enums.common.SignTypeEnum;
import com.xpay.common.statics.exception.BizException;
import com.xpay.common.utils.CodeUtil;
import com.xpay.common.utils.MD5Util;
import com.xpay.common.utils.RSAUtil;

import java.nio.charset.StandardCharsets;

/**
 * 签名、验签的工具类
 *
 * @author chenyf
 * @date 2018-12-15
 */
public class SignUtil {
    /**
     * 验证签名
     *
     * @param data
     * @param signature
     * @param signType
     * @param privateKey
     * @return
     */
    public static boolean verify(byte[] data, String signature, String signType, String privateKey) {
        if (String.valueOf(SignTypeEnum.RSA.getValue()).equals(signType)) {
            return RSAUtil.verify(data, privateKey, signature);
        } else if (String.valueOf(SignTypeEnum.MD5.getValue()).equals(signType)) {
            String signStr = genMD5Sign(data, privateKey);
            return signStr.equals(signature);
        } else {
            throw new BizException("验签失败，未预期的签名类型：" + signType);
        }
    }

    /**
     * 生成签名
     *
     * @param data
     * @param privateKey
     * @return
     */
    public static String sign(byte[] data, String signType, String privateKey) {
        if (data == null) {
            return null;
        }

        String signResult;
        if (String.valueOf(SignTypeEnum.RSA.getValue()).equals(signType)) {
            signResult = RSAUtil.sign(data, privateKey);
        } else if (String.valueOf(SignTypeEnum.MD5.getValue()).equals(signType)) {
            signResult = genMD5Sign(data, privateKey);
        } else {
            //抛出签名失败的异常
            throw new BizException("签名失败，未预期的签名类型：" + signType);
        }
        return signResult;
    }

    private static String genMD5Sign(byte[] signData, String key) {
        String dataStr = new String(signData, StandardCharsets.UTF_8);
        return CodeUtil.base64Encode(MD5Util.getMD5(dataStr + key));
    }
}
