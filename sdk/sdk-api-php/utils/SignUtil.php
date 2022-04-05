<?php
namespace xpay;

use ReflectionClass;
use ReflectionProperty;

/**
 * 签名、验签工具类
 * Class SignUtil
 * @package utils
 */
class SignUtil {
    const MS5_BOUND_SYMBOL = "&key=";

    /**
     * 签名
     * @param string $signData 需要签名的数据
     * @param string $signType 签名类型
     * @param string $priKey 用以签名的私钥
     * @return string
     * @throws \exceptions\SDKException
     * @throws SDKException
     */
    public static function sign(string $signData, string $signType, string $priKey): string
    {
        if("2" === $signType){
            return RSAUtil::sign($signData, $priKey);
        }else{
            throw new SDKException(SDKException::BIZ_ERROR, "未支持的签名类型：" . $signType);
        }
    }

    /**
     * 验签
     * @param string $signData 需要验签的数据
     * @param string $signParam 需要被校验签名的源数据
     * @param string $signType 签名类型
     * @param string $pubKey 用以验签的公钥
     * @return bool
     * @throws \exceptions\SDKException
     * @throws SDKException
     */
    public static function verify(string $signData, string $signParam, string $signType, string $pubKey): bool
    {
        if("2" === $signType){
            return RSAUtil::verify($signData, $signParam, $pubKey);
        }else{
            throw new SDKException(SDKException::BIZ_ERROR, "未支持的签名类型：" . $signType);
        }
    }
}