<?php
/**
 * 测试入口类 -> 批量交易
 *
 * PHP VERSION = PHP 7.4.15
 */

require "../Autoload.php";
require "Keys.php";

use xpay\Request;
use xpay\SecretKey;
use xpay\RequestUtil;
use xpay\RandomUtil;
use xpay\ObjectUtil;
use xpay\AESUtil;
use xpay\SingleVo;
use xpay\BatchVo;
use xpay\BatchRespVo;

$maxCount = 1000;
$totalCount = 0;
$totalAmount = 0;
$secKey = RandomUtil::randomStr(16);
$iv = RandomUtil::randomStr(16);

$singleList = [];
for($i=1; $i<=$maxCount; $i++){
    $amount = 20.01;

    $single = new SingleVo();
    $single->setProductAmount($amount);
    $single->setProductName("都是交流交流发就发给对方感到我认为日u我认465dff34DWS34PO发的发生的34343，。？@！#%￥%~,;'=》》‘；【】@发生的开发商的方式飞机克里斯多夫快回家的思考方式对方老师的讲课费" . $i);
    $single->setCount(1);

    //加密
    $single->setProductName(AESUtil::encryptCBC($single->getProductName(), $secKey, $iv));

    $totalCount = $totalCount + $single->getCount();
    $totalAmount = $totalAmount + $amount;

    array_push($singleList, $single);
}

$batchVo = new BatchVo();
$batchVo->setDetailList($singleList);
$batchVo->setTotalCount($totalCount);
$batchVo->setTotalAmount($totalAmount);

$request = new Request();
$request->setMethod("demo.batch");
$request->setVersion("1.0");
$request->setMchNo(MCH_NO);
$request->setSignType("2");
$request->setRandStr(RandomUtil::randomStr(32));
$request->setData($batchVo);
$request->joinSecKey($secKey, $iv);//rsa有效

$secretKey = new SecretKey();
$secretKey->setMchPriKey(MCH_PRI_KEY);//商户私钥
$secretKey->setPlatPubKey(PLAT_PUB_KEY);//平台公钥

$url = HOST . "/backend-flux";
try{
    $response = RequestUtil::doJsonRequest($url, $request, $secretKey);

    echo "响应数据为 Response = ";
    print_r($response);

    if($response->isSuccess()){
        $batchRespVo = new BatchRespVo();
        ObjectUtil::fillObjectWithArray($batchRespVo, $response->getData());
        if('03' == $batchRespVo->getOrderStatus()){
            $secKeyArr = $response->splitSecKey();
            $secKey = $secKeyArr[0];
            $iv = $secKeyArr[1];

            $singleList = $batchRespVo->getSingleList();
            foreach($singleList as $key => &$value){
                $productName = $value['productName'];
                $productName = AESUtil::decryptCBC($productName, $secKey, $iv);
                $value['productName'] = $productName;
            }

            echo "解密后data的数据为 data = ";
            print_r($singleList);
        }
    }else{
        echo "错误信息为 resp_msg = " . $response->getRespMsg();
    }
}catch(Exception $e){
    $errMsg[] = $e->getMessage();
    $errMsg[] = $e->getFile() . '(' . $e->getLine() . ')';
    $errMsg[] = $e->getTraceAsString();
    print_r($errMsg);
}

