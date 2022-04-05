<?php
namespace xpay;

/**
 * AES加解密工具类
 * Class AESUtil
 * @package utils
 */
class AESUtil {
    const AES_MODE = "AES-128-CBC";

    /**
     * AES加密，模式为：AES/CBC/PKCS5Padding
     * @param string $data
     * @param string $secKey
     * @param string $iv
     * @return string
     * @throws SDKException
     */
    public static function encryptCBC(string $data, string $secKey, string $iv): string
    {
        $encrypted = openssl_encrypt($data, self::AES_MODE, $secKey, OPENSSL_RAW_DATA, $iv);
        if($encrypted === false){
            throw new SDKException(SDKException::BIZ_ERROR, "aes加密失败");
        }
        return base64_encode($encrypted);
    }

    /**
     * AES解密，模式为：AES/CBC/PKCS5Padding
     * @param string $data
     * @param string $secKey
     * @param string $iv
     * @return string
     * @throws SDKException
     */
    public static function decryptCBC(string $data, string $secKey, string $iv): string
    {
        $decrypted = openssl_decrypt(base64_decode($data), self::AES_MODE, $secKey, OPENSSL_RAW_DATA, $iv);
        if($decrypted === false){
            throw new SDKException(SDKException::BIZ_ERROR, "aes解密失败");
        }
        return $decrypted;
    }
}