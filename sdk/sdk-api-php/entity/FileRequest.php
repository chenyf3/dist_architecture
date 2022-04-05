<?php
namespace xpay;

class FileRequest implements \JsonSerializable {
    private string $method  = '';
    private string $version  = '';
    private string $randStr  = '';
    private string $signType  = '';
    private string $mchNo  = '';
    private string $secKey = '';
    private string $timestamp = ''; //时间戳，精确倒毫秒
    private string $extras = ''; //额外的参数(如有需要，可以使用)
    private string $hash = ''; //文件的hash值，如：md5值，多个文件时用英文的逗号分割

    /**
     * 文件数组列表
     * @var FileInfo[]
     */
    private array $fileInfos = [];

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

    /**
     * @return string
     */
    public function getExtras(): string
    {
        return $this->extras;
    }

    /**
     * @param string $extras
     */
    public function setExtras(string $extras): void
    {
        $this->extras = $extras;
    }

    /**
     * @return string
     */
    public function getHash(): string
    {
        return $this->hash;
    }

    /**
     * @param string $hash
     */
    public function setHash(string $hash): void
    {
        $this->hash = $hash;
    }

    /**
     * @return FileInfo[]
     */
    public function getFileInfos(): array
    {
        return $this->fileInfos;
    }

    /**
     * @param FileInfo[]
     */
    public function setFileInfos(array $fileInfos): void
    {
        $this->fileInfos = $fileInfos;
    }

    /**
     * @return array
     */
    public function getTextBodySorted(): array
    {
        //通过反射取得所有属性和属性的值
        $reflect = new \ReflectionClass(self::class);
        $props = $reflect->getProperties(\ReflectionProperty::IS_PUBLIC | \ReflectionProperty::IS_PRIVATE | \ReflectionProperty::IS_PROTECTED);
        $bodyArr = [];
        foreach ($props as $prop) {
            $prop->setAccessible(true);
            $key = $prop->getName();
            $value = $prop->getValue($this);

            if($key === 'fileInfos'){
                continue;
            }

            $bodyArr[$key] = $value;
        }

        //按key的字典序升序排序，并保留key值
        ksort($bodyArr);
        return $bodyArr;
    }
}
