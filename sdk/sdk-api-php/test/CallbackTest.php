<?php
/**
 * 测试入口类 -> 接收平台的异步通知回调
 *
 * PHP VERSION = PHP 7.4.15
 */
require "../Autoload.php";
require "Keys.php";

use xpay\ObjectUtil;
use xpay\Callback;
use xpay\SignUtil;

$signArr = getallheaders();
$signature = isset($signArr[\xpay\RequestUtil::SIGNATURE_HEADER]) ? $signArr[\xpay\RequestUtil::SIGNATURE_HEADER] : '';
if(! $signature){
    sendResp("02", "没有签名参数");
    exit;
}

$jsonData = file_get_contents("php://input");
if(! $jsonData){
    sendResp("02", "没有接收到响应数据");
    exit;
}

$callback = new Callback();
ObjectUtil::fillObjectWithJson($callback, $jsonData);

$isOk = SignUtil::verify($jsonData, $signature, $callback->getSignType(), PLAT_PUB_KEY);
if(!$isOk){
    sendResp("02", "验签失败");
    exit;
}

//处理业务逻辑
//print_r($callback);

sendResp("01", "success");


/**
 * @param string $code      响应码，01=成功 02=重试
 * @param string $message   响应信息
 */
function sendResp(string $code, string $message){
    header('HTTP/1.1 200 OK');
    header('Content-type: application/json; charset=UTF-8');
    echo '{code:"' . $code . '", message:"' . $message . '"}';
}



