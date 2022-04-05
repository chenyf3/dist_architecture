<?php
/**
 * 测试入口类 -> 文件上传
 *
 * PHP VERSION = PHP 7.4.15
 */
require "../Autoload.php";
require "Keys.php";

use xpay\FileRequest;
use xpay\SecretKey;
use xpay\RequestUtil;
use xpay\RandomUtil;
use xpay\FileUtil;
use xpay\MediaType;


$filename = '';
$type = '';
$size = '';
$fileInfos = [];
if(! isset($_POST['formFlag'])){//说明不是从FileUpload.html进入到当前脚本中的
    echo "脚本方式运行，将直接读取本地文件上传 ------>>>> \n";

    $fileDir = "D:\\upload\\";
    $fileArray = [];
    $fileArray['Elasticsearch服务器开发（第2版）.pdf'] = MediaType::APPLICATION_PDF;
    $fileArray['ElasticSearch文档.docx'] = MediaType::APPLICATION_WORD_07;
    $fileArray['test_image_upload.jpg'] = MediaType::IMAGE_JPEG;
    $fileArray['阿里巴巴Java开发手册.zip'] = MediaType::APPLICATION_ZIP;
    $fileArray['03版excel表格.xls'] = MediaType::APPLICATION_EXCEL_O3;
    $fileArray['07版excel表格.xlsx'] = MediaType::APPLICATION_EXCEL_07;

    foreach ($fileArray as $filename => $mediaType) {
        $filePath = $fileDir . $filename;
        $file = new \xpay\FileInfo();
        $file->setFilename($filename);
        $file->setContentType($mediaType);
        $file->setData(FileUtil::readFileInBytes($filePath));
        echo $file->getFilename() . " 文件大小为：" . filesize($filePath) . "\n";
        $fileInfos[] = $file;
    }

}else{
    echo "FORM表单提交，将上传提交过来的文件 ------>>>> <br>";

    if(! isset($_FILES['file'])) {
        echo "没有获取到上传的文件!!" . " <br>";
        exit;
    }
    $error = $_FILES['file']['error'];
    if($error && $error > 0){
        echo "文件上传失败，错误码为：" . $error . " <br>";
        exit;
    }

    $filename = $_FILES['file']['name'];//源文件名
    $type = $_FILES['file']['type'];//文件的MIME类型
    $size = $_FILES['file']['size'];//文件大小，单位：字节
    $filePath = $_FILES['file']['tmp_name'];//文件被上传后在服务端储存的临时文件名

    $file1 = new \xpay\FileInfo();
    $file1->setFilename($filename);
    $file1->setContentType($type);
    $file1->setData(FileUtil::readFileInBytes($filePath));
    $fileInfos[] = $file1;

    echo "拿到了上传的文件 -------> filePath = $filePath <<<<------ <br>";
}

$extras = [];
$extras['key_01'] = 'value_01';
$extras['key_02'] = 'value_02';
$extras['key_03'] = 'value_03';

$request = new FileRequest();
$request->setMethod("demo.upload");
$request->setVersion("1.0");
$request->setMchNo(MCH_NO);
$request->setSignType("2");
$request->setRandStr(RandomUtil::randomStr(32));
$request->setExtras(json_encode($extras, JSON_UNESCAPED_UNICODE));
$request->setFileInfos($fileInfos);

$secretKey = new SecretKey();
$secretKey->setMchPriKey(MCH_PRI_KEY);//商户私钥
$secretKey->setPlatPubKey(PLAT_PUB_KEY);//平台公钥

$url = HOST . "/backend-flux";
//$url = HOST . "/backend-mvc";
try {
    $response = RequestUtil::doFileRequest($url, $request, $secretKey);

    echo "响应数据为 Response = ";
    print_r($response);
} catch (Exception $e) {
    $errMsg[] = $e->getMessage();
    $errMsg[] = $e->getFile() . '(' . $e->getLine() . ')';
    $errMsg[] = $e->getTraceAsString();
    print_r($errMsg);
}

