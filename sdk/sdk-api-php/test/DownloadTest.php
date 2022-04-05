<?php
/**
 * 测试入口类 -> 下载文件
 *
 * PHP VERSION = PHP 7.4.15
 */
require "../Autoload.php";
require "Keys.php";

use xpay\Request;
use xpay\SecretKey;
use xpay\RequestUtil;
use xpay\RandomUtil;
use xpay\FileUtil;

$fileDir = "D:\\download\\";
$fileName = 'Elasticsearch服务器开发（第2版）.pdf';

$request = new Request();
$request->setMethod("demo.download");
$request->setVersion("1.0");
$request->setMchNo(MCH_NO);
$request->setSignType("2");
$request->setRandStr(RandomUtil::randomStr(32));
$request->setData($fileName);

$secretKey = new SecretKey();
$secretKey->setMchPriKey(MCH_PRI_KEY);//商户私钥
$secretKey->setPlatPubKey(PLAT_PUB_KEY);//平台公钥

$url = HOST . "/backend-flux";
try {
    $response = RequestUtil::downloadFile($url, $request, $secretKey, 60);

    $respInfo['code'] = $response->getCode();
    $respInfo['header'] = $response->getHeaders();
    echo "响应数据为 Response = ";
    print_r($respInfo);

    if ($response->isSuccessful()) {
        FileUtil::writeFile($fileDir, $fileName, $response->getBody());
    }else{
        echo "错误信息为 resp_msg = " . json_encode($response);
    }
} catch (Exception $e) {
    print_r($e);
}