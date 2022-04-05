<?php
namespace xpay;

/**
 * http请求参数类
 * Class Request
 * @package entity
 */
class Request implements \JsonSerializable {
    private string $method = '';
    private string $version = "1.0";
    private $data;
    private string $randStr = '';
    private string $signType = "2";
    private string $mchNo = '';
    private string $secKey = '';
    private string $timestamp = '';

    /**
     * 进行json序列化时返回的内容
     * @return array|mixed
     */
    public function jsonSerialize()
    {
        $vars = get_object_vars($this);
        return $vars;
    }

    /**
     * @return string
     */
    public function getMethod(): string
    {
        return $this->method;
    }

    /**
     * @param string $method
     */
    public function setMethod(string $method): void
    {
        $this->method = $method;
    }

    /**
     * @return string
     */
    public function getVersion(): string
    {
        return $this->version;
    }

    /**
     * @param string $version
     */
    public function setVersion(string $version): void
    {
        $this->version = $version;
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

    /**
     * @return string
     */
    public function getTimestamp(): string
    {
        return $this->timestamp;
    }

    /**
     * @param string $timestamp
     */
    public function setTimestamp(string $timestamp): void
    {
        $this->timestamp = $timestamp;
    }

    public function joinSecKey(string $secKey, string $iv) : void
    {
        $this->secKey = $secKey . ":" . $iv;
    }
}