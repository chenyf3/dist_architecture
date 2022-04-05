<?php
namespace xpay;

/**
 * http工具类
 * Class HttpUtil
 * @package utils
 */
class HttpUtil {
    private static $_CLIENT_VERSION = 'PHP_' . PHP_MAJOR_VERSION;
    private static $_CAINFO = __DIR__ . "/ca/cacert.pem";//证书存放目录

    /**
     * 以post方式发起http请求，请求参数为json格式
     * @param string $url
     * @param array $header
     * @param string $jsonData
     * @param int $timeOut  超时时间(秒)
     * @return HttpResp
     */
    public static function postJsonSync(string $url, array $header, string $jsonData, int $timeOut) : HttpResp
    {
        $contentType = 'application/json;charset=UTF-8';
        return static::postSync($url, $contentType, $header, $jsonData, $timeOut);
    }

    /**
     * 以post方式发起http请求，请求参数为文件字节流
     * @param string $url
     * @param array $header
     * @param array $formParam
     * @param string $fileFormName
     * @param FileInfo[] $fileInfos
     * @return HttpResp
     */
    public static function postFileSync(string $url, array $header, array $formParam, string $fileFormName, array $fileInfos) : HttpResp
    {
        $delimiter = '--------' . RandomUtil::uniqueId();//字段分隔符
        $contentType = "multipart/form-data; boundary={$delimiter}";

        $postData = static::buildMultipartData($delimiter, $formParam, $fileFormName, $fileInfos);

        return static::postSync($url, $contentType, $header, $postData, 30);
    }

    /**
     * 发起http请求(同步等待响应)
     * @param string $url
     * @param string $contentType
     * @param array $header
     * @param $postData
     * @param int $timeOut  超时时间(秒)
     * @return HttpResp
     */
    public static function postSync(string $url, string $contentType, array $header, $postData, int $timeOut): HttpResp
    {
        $headers = [];
        $headers[] = 'Content-Type: ' . $contentType;
        $headers[] = 'Accept: application/json;charset=UTF-8';//设置预期响应类型
        $headers[] = 'CLIENT-VERSION: ' . static::$_CLIENT_VERSION;//客户端版本
        $headers[] = 'Expect:';//禁用"Expect"头域
        foreach($header as $key => $value){
            $headers[] = $key . ": " . $value;
        }

        $opts = [];
        $opts[CURLOPT_URL] = $url;//设置请求的url
        $opts[CURLOPT_POST] = 1;//设置post方式提交
        $opts[CURLOPT_RETURNTRANSFER] = true;//设置获取的信息以文件流的形式返回，而不是直接输出。
        $opts[CURLOPT_CONNECTTIMEOUT] = 5;//设置连接超时时间(秒)
        if ($timeOut && $timeOut > 0) {
            $opts[CURLOPT_TIMEOUT] = $timeOut;//设置超时时间(秒)
        } else {
            $opts[CURLOPT_TIMEOUT] = 20;//设置超时时间(秒)
        }
        $opts[CURLOPT_HTTPHEADER] = $headers;//设置请求头
        $opts[CURLOPT_POSTFIELDS] = $postData;//设置需要提交的数据
        $opts[CURLOPT_HTTP_VERSION] = CURL_HTTP_VERSION_2_0;
        $opts[CURLOPT_HEADER] = true;//设置要返回响应头
        if(strpos($url, "https") === 0){//https请求
            $opts[CURLOPT_SSL_VERIFYHOST] = 2;// 从证书中检查SSL加密算法是否存在
            if(static::$_CAINFO){
                $opts[CURLOPT_CAINFO] = static::$_CAINFO; //设置证书路径
                $opts[CURLOPT_SSL_VERIFYPEER] = true; //需要执行证书检查
            }else{
                $opts[CURLOPT_SSL_VERIFYPEER] = false; //跳过证书检查（不建议）
            }
        }

        $curl = curl_init();
        curl_setopt_array($curl, $opts);
        //执行请求并得到响应数据
        $respData = curl_exec($curl);

        //获取响应数据
        $code = curl_getinfo($curl, CURLINFO_HTTP_CODE);
        $headerSize = curl_getinfo($curl, CURLINFO_HEADER_SIZE);
        $headerStr = substr($respData, 0, $headerSize);
        $body = substr($respData, $headerSize);
        curl_close($curl);

        //设置返回值
        $headerArr = static::convertHeaderToArray($headerStr);
        $httpResp = new HttpResp();
        $httpResp->setCode($code);
        $httpResp->setHeaders($headerArr);
        $httpResp->setBody($body);
        return $httpResp;
    }

    /**
     * @param string $delimiter
     * @param array $formParam
     * @param string $fileFormName
     * @param FileInfo[] $fileInfos
     * @return string
     */
    private static function buildMultipartData(string $delimiter, array $formParam, string $fileFormName, array $fileInfos): string
    {
        $data = '';
        $eol = "\r\n";//换行符

        foreach ($formParam as $name => $content) {
            $data .= '--' . $delimiter . $eol
                . 'Content-Disposition: form-data; name="' . $name . "\"" . $eol.$eol
                . $content . $eol;
        }

        // 拼接文件流
        foreach ($fileInfos as $file) {
            $data .= '--' . $delimiter . $eol
                . 'Content-Disposition: form-data; name="' . $fileFormName . '"; filename="' . $file->getFilename() . '"' . $eol
                . 'Content-Type: ' . $file->getContentType() . $eol.$eol;
            $data .= $file->getData() . $eol;
        }

        $data .= '--' . $delimiter . '--' . $eol;
        return $data;
    }

    private static function convertHeaderToArray($respHeader) : array
    {
        $headers = [];
        $headArr = explode("\r\n", $respHeader);
        foreach ($headArr as $headerLine){
            $split = explode(':', $headerLine, 2);//响应头是以:符号来分隔key和value的
            if(sizeof($split) == 2){
                $headers[$split[0]] = $split[1];
            }
        }
        return $headers;
    }
}