<?php
namespace xpay;

/**
 * 平台回调类
 * Class Callback
 * @package xpay
 */
class Callback {
    private string $mchNo = '';
    private $data;
    private string $randStr = '';
    private string $signType = '';
    private string $secKey = '';

    /**
     * @return string
     */
    public function getMchNo(): string
    {
        return $this->mchNo;
    }

    /**
     * @param string $mchNo
     */
    public function setMchNo(string $mchNo): void
    {
        $this->mchNo = $mchNo;
    }

    /**
     * @return mixed
     */
    public function getData()
    {
        return $this->data;
    }

    /**
     * @param mixed $data
     */
    public function setData($data): void
    {
        $this->data = $data;
    }

    /**
     * @return string
     */
    public function getRandStr(): string
    {
        return $this->randStr;
    }

    /**
     * @param string $randStr
     */
    public function setRandStr(string $randStr): void
    {
        $this->randStr = $randStr;
    }

    /**
     * @return string
     */
    public function getSignType(): string
    {
        return $this->signType;
    }

    /**
     * @param string $signType
     */
    public function setSignType(string $signType): void
    {
        $this->signType = $signType;
    }

    /**
     * @return string
     */
    public function getSecKey(): string
    {
        return $this->secKey;
    }

    /**
     * @param string $secKey
     */
    public function setSecKey(string $secKey): void
    {
        $this->secKey = $secKey;
    }
}