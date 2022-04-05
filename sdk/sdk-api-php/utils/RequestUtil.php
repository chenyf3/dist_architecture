<?php
namespace xpay;

use Exception;

/**
 * 发起交易请求的工具类
 * Class RequestUtil
 * @package utils
 */
class RequestUtil{
    const SIGNATURE_HEADER = "signature";
    const FILE_FORM_NAME = "files";
    const HASH_SEPARATOR = ",";

    /**
     * 发起JSON格式的请求
     *   说明：
     *      1、此方法不会对商户私钥做任何处理，理论上不会造成私钥泄漏，如果商户觉得把私钥传递到此方法不安全，可以在调用本方法前
     *          自行调用SignUtil的相关方法为请求参数生成签名，自行调用RSAUtil的相关方法对sec_key进行加解密。
     *      2、发起请求时，当前方法会自动为请求参数生成签名、得到系统响应之后也会自动对响应数据进行验签，签名或验签失败会抛出 SDKException异常
     *      3、发送请求时，如果Request中的secKey不为空，会自动给secKey加密
     *      4、得到响应时，如果Response中的secKey不为空，会自动给secKey解密
     *
     * @param string $url
     * @param Request $request
     * @param SecretKey $secretKey
     * @return Response
     * @throws SDKException
     */
    public static function doJsonRequest(string $url, Request $request, SecretKey $secretKey): Response
    {
        //1.参数校验
        static::jsonRequestValid($request, $secretKey);

        //2.如果secKey有值，则对其进行rsa加密
        if($request->getSecKey()){
            $request->setSecKey(RSAUtil::encrypt($request->getSecKey(), $secretKey->getPlatPubKey()));
        }

        //3.为请求体生成签名
        $header = [];
        $reqBody = json_encode($request, JSON_UNESCAPED_UNICODE);//JSON_UNESCAPED_UNICODE避免中文转码
        $signStr = SignUtil::sign($reqBody, $request->getSignType(), $secretKey->getMchPriKey());
        $header[static::SIGNATURE_HEADER] = $signStr;

        //4.发起http(s)请求
        $httpResp = HttpUtil::postJsonSync($url, $header, $reqBody, 20);
        if(! $httpResp || !$httpResp->getBody()){
            throw new SDKException(SDKException::BIZ_ERROR, "请求完成，但响应体为空");
        }

        //5.对响应数据验签 & 响应结果对象转换
        return static::decodeHttpResponse($httpResp, $secretKey);
    }

    /**
     * 文件上传
     * @param string $url
     * @param FileRequest $request
     * @param SecretKey $secretKey
     * @return Response
     * @throws SDKException
     */
    public static function doFileRequest(string $url, FileRequest $request, SecretKey $secretKey): Response
    {
        //1.参数校验
        static::fileRequestValid($request, $secretKey);

        //2.如果secKey不为空，则对其进行rsa加密
        if($request->getSecKey()){
            $request->setSecKey(RSAUtil::encrypt($request->getSecKey(), $secretKey->getPlatPubKey()));
        }

        //3.计算每个上传文件的md5值
        $hashArr = [];
        foreach ($request->getFileInfos() as $fileInfo) {
            $fileMd5 = MD5Util::getMd5Str($fileInfo->getData());
            $hashArr[] = $fileMd5;
            if(!$fileInfo->getContentType()){
                $fileInfo->setContentType("application/octet-stream");
            }
        }
        $request->setHash(implode(static::HASH_SEPARATOR, $hashArr));

        //4.为文本字段生成签名串
        $bodyArr = $request->getTextBodySorted();
        $bodySignStr = static::getFormSignStr($bodyArr);
        $signature = SignUtil::sign($bodySignStr, $request->getSignType(), $secretKey->getMchPriKey());
        $header[static::SIGNATURE_HEADER] = $signature;

        //5.发起http(s)请求
        $httpResp = HttpUtil::postFileSync($url, $header, $bodyArr, static::FILE_FORM_NAME, $request->getFileInfos());
        if(! $httpResp || !$httpResp->getBody()){
            throw new SDKException(SDKException::BIZ_ERROR, "请求完成，但响应体为空");
        }

        //6.对响应数据验签 & 响应结果对象转换
        return static::decodeHttpResponse($httpResp, $secretKey);
    }

    /**
     * 文件下载
     * @param string $url
     * @param Request $request
     * @param SecretKey $secretKey
     * @param int $timeOut
     * @return HttpResp
     * @throws SDKException
     */
    public static function downloadFile(string $url, Request $request, SecretKey $secretKey, int $timeOut): HttpResp
    {
        //1.参数校验
        static::jsonRequestValid($request, $secretKey);

        //2.如果secKey有值，则对其进行rsa加密
        if($request->getSecKey()){
            $request->setSecKey(RSAUtil::encrypt($request->getSecKey(), $secretKey->getPlatPubKey()));
        }

        //3.为请求体生成签名
        $header = [];
        $reqBody = json_encode($request, JSON_UNESCAPED_UNICODE);//JSON_UNESCAPED_UNICODE避免中文转码
        $signStr = SignUtil::sign($reqBody, $request->getSignType(), $secretKey->getMchPriKey());
        $header[static::SIGNATURE_HEADER] = $signStr;

        //4.发起http(s)请求
        $httpResp = HttpUtil::postJsonSync($url, $header, $reqBody, $timeOut);
        if(! $httpResp || !$httpResp->getBody()){
            throw new SDKException(SDKException::BIZ_ERROR, "请求完成，但响应体为空");
        }

        return $httpResp;
    }

    /**
     * 简单参数校验
     * @param Request $request
     * @param SecretKey $secretKey
     * @throws SDKException
     */
    private static function jsonRequestValid(Request $request, SecretKey $secretKey){
        if(! $request){
            throw new SDKException(SDKException::PARAM_ERROR, "request不能为空");
        }else if(! $request->getMethod()){
            throw new SDKException(SDKException::PARAM_ERROR, "Request.method不能为空");
        }else if(! $request->getVersion()){
            throw new SDKException(SDKException::PARAM_ERROR, "Request.version不能为空");
        }else if(! $request->getData()){
            throw new SDKException(SDKException::PARAM_ERROR, "Request.data不能为空");
        }else if(! $request->getSignType()){
            throw new SDKException(SDKException::PARAM_ERROR, "Request.signType不能为空");
        }else if(! $request->getMchNo()){
            throw new SDKException(SDKException::PARAM_ERROR, "Request.mchNo不能为空");
        }

        if(! $secretKey){
            throw new SDKException(SDKException::PARAM_ERROR, "secretKey不能为空");
        }else if(! $secretKey->getMchPriKey()){
            throw new SDKException(SDKException::PARAM_ERROR, "SecretKey.mchPriKey不能为空");
        }else if(! $secretKey->getPlatPubKey()){
            throw new SDKException(SDKException::PARAM_ERROR, "SecretKey.platPubKey不能为空");
        }

        $request->setTimestamp(RandomUtil::millSecond());
    }

    /**
     * 简单参数校验
     * @param FileRequest $request
     * @param SecretKey $secretKey
     * @throws SDKException
     */
    private static function fileRequestValid(FileRequest $request, SecretKey $secretKey){
        if(! $request){
            throw new SDKException(SDKException::PARAM_ERROR, "request不能为空");
        }else if(! $request->getMethod()){
            throw new SDKException(SDKException::PARAM_ERROR, "Request.method不能为空");
        }else if(! $request->getVersion()){
            throw new SDKException(SDKException::PARAM_ERROR, "Request.version不能为空");
        }else if(! $request->getSignType()){
            throw new SDKException(SDKException::PARAM_ERROR, "Request.signType不能为空");
        }else if(! $request->getMchNo()){
            throw new SDKException(SDKException::PARAM_ERROR, "Request.mchNo不能为空");
        }else if(! $request->getFileInfos()){
            throw new SDKException(SDKException::PARAM_ERROR, "Request.fileInfos不能为空");
        }

        if(! $secretKey){
            throw new SDKException(SDKException::PARAM_ERROR, "secretKey不能为空");
        }else if(! $secretKey->getMchPriKey()){
            throw new SDKException(SDKException::PARAM_ERROR, "SecretKey.mchPriKey不能为空");
        }else if(! $secretKey->getPlatPubKey()){
            throw new SDKException(SDKException::PARAM_ERROR, "SecretKey.platPubKey不能为空");
        }

        $request->setTimestamp(RandomUtil::millSecond());
    }

    private static function getFormSignStr(array $bodyArray){
        $dataStr = '';
        foreach($bodyArray as $key => $value) {
            $dataStr .= "$key=$value&";
        }
        return $dataStr ? substr($dataStr, 0, -1) : '';
    }

    /**
     * 响应结果转换&验签
     * @param HttpResp $httpResp
     * @param SecretKey $secretKey
     * @return Response
     * @throws SDKException
     */
    private static function decodeHttpResponse(HttpResp $httpResp, SecretKey $secretKey): Response
    {
        $respBody = $httpResp->getBody();
        $respHeaders = $httpResp->getHeaders();

        $signature = isset($respHeaders[static::SIGNATURE_HEADER]) ? $respHeaders[static::SIGNATURE_HEADER] : '';
        if(!$signature){
            throw new SDKException(SDKException::BIZ_ERROR, "响应签名为空，无法验签，respBody: " . $respBody);
        }

        $isOk = false;
        try{
            $response = new Response();
            try{
                ObjectUtil::fillObjectWithJson($response, $respBody);
            }catch (Exception $e){
                throw new SDKException(SDKException::BIZ_ERROR, "请求完成，但响应信息转换失败: " . $e->getMessage() . "，respBody = " . $respBody);
            }

            $isOk = SignUtil::verify($respBody, $signature, $response->getSignType(), $secretKey->getPlatPubKey());
        }catch(SDKException $e){
            throw $e;
        }catch (Exception $e){
            throw new SDKException(SDKException::BIZ_ERROR, "请求完成，但响应信息验签异常: " . $e->getMessage() . "，respBody = " . $respBody);
        }
        if($isOk !== true){
            throw new SDKException(SDKException::BIZ_ERROR, "响应信息验签不通过！ respBody = " . $httpResp->getBody());
        }

        //7.对secKey进行解密
        if($response->getSecKey()){
            $secKey = RSAUtil::decrypt($response->getSecKey(), $secretKey->getMchPriKey());
            $response->setSecKey($secKey);
        }
        return $response;
    }
}

