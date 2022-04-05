<?php
/**
 * 自动加载，把需要使用的相关类自动加载到php环境中
 */

spl_autoload_register('autoload');

function autoload(){
    require "entity/Callback.php";
    require "entity/FileInfo.php";
    require "entity/FileRequest.php";
    require "entity/HttpResp.php";
    require "entity/MediaType.php";
    require "entity/Request.php";
    require "entity/Response.php";
    require "entity/SecretKey.php";
    require "exceptions/SDKException.php";
    require "utils/AESUtil.php";
    require "utils/FileUtil.php";
    require "utils/HttpUtil.php";
    require "utils/MD5Util.php";
    require "utils/ObjectUtil.php";
    require "utils/RandomUtil.php";
    require "utils/RequestUtil.php";
    require "utils/RSAUtil.php";
    require "utils/SignUtil.php";

    require "test/BatchVo.php";
    require "test/SingleVo.php";
    require "test/BatchRespVo.php";
    require "test/SingleRespVo.php";
}