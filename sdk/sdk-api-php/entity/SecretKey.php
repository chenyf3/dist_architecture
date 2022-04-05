<?php
namespace xpay;

/**
 * 密钥类
 * Class SecretKey
 * @package entity
 */
class SecretKey {

    /**
     * 商户私钥，作用有二：
     *  1、为请求报文生成签名摘要
     *  2、对响应报文的sec_key进行解密(如果有)
     */
    private string $mchPriKey = '';

    /**
     * 平台公钥，作用有二
     *  1、对响应报文进行验签
     *  2、对请求报文的sec_key进行加密(如果有)
     */
    private string $platPubKey = '';

    /**
     * @return string
     */
    public function getMchPriKey(): string
    {
        return $this->mchPriKey;
    }

    /**
     * @param string $mchPriKey
     */
    public function setMchPriKey(string $mchPriKey): void
    {
        $this->mchPriKey = $mchPriKey;
    }

    /**
     * @return string
     */
    public function getPlatPubKey(): string
    {
        return $this->platPubKey;
    }

    /**
     * @param string $platPubKey
     */
    public function setPlatPubKey(string $platPubKey): void
    {
        $this->platPubKey = $platPubKey;
    }
}