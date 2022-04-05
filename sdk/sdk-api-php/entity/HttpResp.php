<?php
namespace xpay;

/**
 * http响应类
 * Class HttpResp
 * @package xpay
 */
class HttpResp {
    private int $code;
    private array $headers;
    private string $body;

    /**
     * @return int
     */
    public function getCode(): int
    {
        return $this->code;
    }

    /**
     * @param int $code
     */
    public function setCode(int $code): void
    {
        $this->code = $code;
    }

    /**
     * @return array
     */
    public function getHeaders(): array
    {
        return $this->headers;
    }

    /**
     * @param array $headers
     */
    public function setHeaders(array $headers): void
    {
        $this->headers = $headers;
    }

    /**
     * @return string
     */
    public function getBody()
    {
        return $this->body;
    }

    /**
     * @param string|null $body
     */
    public function setBody(?string $body): void
    {
        $this->body = $body;
    }

    /**
     * 判断http请求是否成功
     * @return bool
     */
    public function isSuccessful() : bool
    {
        return $this->code >= 200 && $this->code < 300;
    }
}