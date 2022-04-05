<?php
namespace xpay;

/**
 * 响应参数类
 * Class Response
 * @package entity
 */
class Response {
    private string $respCode = '';
    private string $respMsg = '';
    private $data;
    private string $mchNo = '';
    private string $randStr = '';
    private string $signType = '';
    private string $secKey = '';

    /**
     * @return string
     */
    public function getRespCode(): string
    {
        return $this->respCode;
    }

    /**
     * @param string $respCode
     */
    public function setRespCode(string $respCode): void
    {
        $this->respCode = $respCode;
    }

    /**
     * @return string
     */
    public function getRespMsg(): string
    {
        return $this->respMsg;
    }

    /**
     * @param string $respMsg
     */
    public function setRespMsg(string $respMsg): void
    {
        $this->respMsg = $respMsg;
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
     * @param string|null $secKey
     */
    public function setSecKey(?string $secKey): void
    {
        $this->secKey = $secKey;
    }

    /**
     * 判断本次请求是否成功
     * @return bool
     */
    public function isSuccess(): bool
    {
        return "01" === $this->respCode;
    }

    /**
     * 从resp_msg中获取错误码
     * @return string
     */
    public function getRespMsgCode(): string
    {
        if($this->respMsg && ($index = strpos($this->respMsg, "[")) >= 0
            && strpos($this->respMsg, "]") == (strlen($this->respMsg)-1)){
            $code = substr($this->respMsg, $index+1);
            return substr($code, 0, strlen($code)-1);
        }else{
            return "";
        }
    }

    public function splitSecKey() : array
    {
        if($this->secKey){
            return explode(":", $this->secKey);
        }else{
            return [];
        }
    }
}