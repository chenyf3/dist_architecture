package com.xpay.sdk.api.utils;

import com.xpay.sdk.api.entity.*;
import com.xpay.sdk.api.exceptions.SDKException;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * 请求处理工具类
 */
public class RequestUtil {
    public static final String SIGNATURE_HEADER = "signature";
    public static final String FILE_FORM_NAME = "file";
    public static final String HASH_SEPARATOR = ",";

    /**
     * 发起交易请求
     *   说明：
     *      1、此方法不会对商户私钥做任何处理，理论上不会造成私钥泄漏，如果商户觉得把私钥传递到此方法不安全，可以在调用本方法前
     *          自行调用SignUtil的相关方法为请求参数生成签名，自行调用RSAUtil的相关方法对sec_key进行加解密。
     *      2、发起请求时，当前方法会自动为请求参数生成签名、得到系统响应之后也会自动对响应数据进行验签，签名或验签失败会抛出 SDKException 异常
     *      3、发送请求时，如果Request中的sec_key和SecretKey.secKeyEncryptKey都不为空，会自动给sec_key加密
     *      4、得到响应时，如果Response中的sec_key和SecretKey.secKeyDecryptKey都不为空，会自动给sec_key解密
     *
     * @param url
     * @param request
     * @param secretKey
     * @return
     * @throws SDKException
     */
    public static Response doJsonRequest(String url, Request request, SecretKey secretKey) throws SDKException {
        //1.构建http(s)请求参数
        Map<String, String> reqHeader = new HashMap<>();
        StringBuffer reqBody = new StringBuffer();
        fillRequestParam(request, secretKey, reqHeader, reqBody, null);

        //2.发起http(s)请求
        HttpUtil.Response response;
        try{
            response = HttpUtil.postJsonSync(url, reqHeader, reqBody.toString(), null);
        }catch(Exception e){
            throw new SDKException("发送http请求时发生异常: " + e.getMessage(), e);
        }

        //3.解析响应数据并对响应数据进行验签
        return decodeHttpResponse(response, secretKey);
    }

    /**
     * 发起form表单请求
     * @param url
     * @param fromData
     * @param secretKey
     * @return
     */
    public static Response doFormRequest(String url, Map<String, String> fromData, SecretKey secretKey){
        //1.转化为Request对象
        Request request = new Request();
        request.setMethod(fromData.get("method"));
        request.setVersion(fromData.get("version"));
        request.setData(fromData.get("data"));
        request.setRandStr(fromData.get("randStr"));
        request.setSignType(fromData.get("signType"));
        request.setMchNo(fromData.get("mchNo"));
        request.setSecKey(fromData.get("secKey"));
        request.setTimestamp(fromData.get("timestamp"));

        //2.构建http(s)请求参数
        Map<String, String> reqHeader = new HashMap<>();
        Map<String, String> formBody = new TreeMap<>();
        fillRequestParam(request, secretKey, reqHeader, null, formBody);

        //3.发起http(s)请求
        HttpUtil.Response response;
        try{
            response = HttpUtil.postFormSync(url, reqHeader, formBody);
        }catch(Exception e){
            throw new SDKException("发送http请求时发生异常: " + e.getMessage(), e);
        }

        //4.解析响应数据并对响应数据进行验签
        return decodeHttpResponse(response, secretKey);
    }

    /**
     * 发起文件上传的请求
     * @param url
     * @param request
     * @param secretKey
     * @return
     */
    public static Response doFileRequest(String url, FileRequest request, SecretKey secretKey){
        //1.参数校验
        paramValid(request, secretKey);

        //2.如果secKey不为空，则对其进行rsa加密
        if(StringUtil.isNotEmpty(request.getSecKey())){
            String secKey = RSAUtil.encryptByPublicKey(request.getSecKey(), secretKey.getPlatPubKey());
            request.setSecKey(secKey);
        }

        //3、计算每个上传的文件的md5值
        String[] hashArr = new String[request.getFiles().size()];
        for(int i=0; i<request.getFiles().size(); i++){
            FileInfo file = request.getFiles().get(i);
            String md5 = MD5Util.getMD5Hex(file.getData());
            hashArr[i] = md5;
        }
        request.setHash(String.join(HASH_SEPARATOR, hashArr));

        //4.为文本字段生成签名串
        Map<String, String> textBody = request.getTextBodySort();
        String dataSignStr = getFormSignStr(textBody);
        byte[] dataBytes = dataSignStr.getBytes(StandardCharsets.UTF_8);
        String signature = SignUtil.sign(dataBytes, request.getSignType(), secretKey.getMchPriKey());
        Map<String, String> reqHeader = new HashMap<>();
        reqHeader.put(SIGNATURE_HEADER, signature);

        //5.发起http(s)请求
        HttpUtil.Response response;
        try{
            response = HttpUtil.postFileSync(url, reqHeader, textBody, FILE_FORM_NAME, request.getFiles());
        }catch(Exception e){
            throw new SDKException("发送http请求时发生异常: " + e.getMessage(), e);
        }

        //6.解析响应数据并对响应数据进行验签
        return decodeHttpResponse(response, secretKey);
    }

    /**
     * 文件下载
     * @param url
     * @param request
     * @param secretKey
     * @param readTimeOut   读请求超时(毫秒)
     * @return
     */
    public static HttpUtil.Response downloadFile(String url, Request request, SecretKey secretKey, Integer readTimeOut){
        //1.构建http(s)请求参数
        Map<String, String> reqHeader = new HashMap<>();
        StringBuffer reqBody = new StringBuffer();
        fillRequestParam(request, secretKey, reqHeader, reqBody, null);

        //2.发起http(s)请求
        HttpUtil.Response response;
        try {
            response = HttpUtil.postJsonSync(url, reqHeader, reqBody.toString(), readTimeOut);
        } catch(Exception e) {
            throw new SDKException("发送http请求时发生异常: " + e.getMessage(), e);
        }
        return response;
    }

    private static void fillRequestParam(Request request, SecretKey secretKey, Map<String, String> reqHeader,
                                         StringBuffer jsonBody, Map<String, String> formBody){
        //1.参数校验
        paramValid(request, secretKey);

        //对secKey执行rsa加密
        if(StringUtil.isNotEmpty(request.getSecKey())){
            String secKey = RSAUtil.encryptByPublicKey(request.getSecKey(), secretKey.getPlatPubKey());
            request.setSecKey(secKey);
        }

        //3.构建http(s)请求参数
        byte[] bodyBytes;
        if(jsonBody != null){
            String bodySignStr = JsonUtil.toJson(request);
            bodyBytes = bodySignStr.getBytes(StandardCharsets.UTF_8);
            jsonBody.append(bodySignStr);
        }else if(formBody != null){
            formBody.putAll(request.toSortMap());//按字典序排序(升序)
            String bodySignStr = getFormSignStr(formBody);
            bodyBytes = bodySignStr.getBytes(StandardCharsets.UTF_8);
        }else{
            throw new SDKException("jsonBody和formBody都为空");
        }

        //4.生成签名串
        try{
            String signature = SignUtil.sign(bodyBytes, request.getSignType(), secretKey.getMchPriKey());
            reqHeader.put(SIGNATURE_HEADER, signature);
        }catch(Exception e){
            throw new SDKException("签名时出现异常: " + e.getMessage(), e);
        }
    }

    private static void paramValid(Request request, SecretKey secretKey){
        if(StringUtil.isEmpty(request)){
            throw new SDKException("request不能为空");
        }

        if(StringUtil.isEmpty(request.getRandStr())){
            request.setRandStr(RandomUtil.get32LenStr());
        }
        if(request.getSecKey() == null){
            request.setSecKey("");
        }

        if(StringUtil.isEmpty(request.getMethod())){
            throw new SDKException("Request.method不能为空");
        }else if(StringUtil.isEmpty(request.getVersion())){
            throw new SDKException("Request.version不能为空");
        }else if(StringUtil.isEmpty(request.getData())){
            throw new SDKException("Request.data不能为空");
        }else if(StringUtil.isEmpty(request.getSignType())){
            throw new SDKException("Request.signType不能为空");
        }else if(StringUtil.isEmpty(request.getMchNo())){
            throw new SDKException("Request.mchNo不能为空");
        }else if(StringUtil.isEmpty(request.getTimestamp())){
            throw new SDKException("Request.timestamp不能为空");
        }

        if(StringUtil.isEmpty(secretKey)){
            throw new SDKException("secretKey不能为空");
        }else if(StringUtil.isEmpty(secretKey.getMchPriKey())){
            throw new SDKException("secretKey.mchPriKey不能为空");
        }else if(StringUtil.isEmpty(secretKey.getPlatPubKey())){
            throw new SDKException("secretKey.platPubKey不能为空");
        }
    }

    private static void paramValid(FileRequest request, SecretKey secretKey){
        if(StringUtil.isEmpty(request)){
            throw new SDKException("request不能为空");
        }

        if(StringUtil.isEmpty(request.getRandStr())){
            request.setRandStr(RandomUtil.get32LenStr());
        }
        if(request.getSecKey() == null){
            request.setSecKey("");
        }

        if(StringUtil.isEmpty(request.getMethod())){
            throw new SDKException("Request.method不能为空");
        }else if(StringUtil.isEmpty(request.getVersion())){
            throw new SDKException("Request.version不能为空");
        }else if(StringUtil.isEmpty(request.getSignType())){
            throw new SDKException("Request.signType不能为空");
        }else if(StringUtil.isEmpty(request.getMchNo())){
            throw new SDKException("Request.mchNo不能为空");
        }else if(StringUtil.isEmpty(request.getTimestamp())){
            throw new SDKException("Request.timestamp不能为空");
        }

        if(StringUtil.isEmpty(secretKey)){
            throw new SDKException("secretKey不能为空");
        }else if(StringUtil.isEmpty(secretKey.getMchPriKey())){
            throw new SDKException("secretKey.mchPriKey不能为空");
        }else if(StringUtil.isEmpty(secretKey.getPlatPubKey())){
            throw new SDKException("secretKey.platPubKey不能为空");
        }
    }

    private static Response decodeHttpResponse(HttpUtil.Response httpResponse, SecretKey secretKey){
        if(StringUtil.isEmpty(httpResponse)){
            throw new SDKException("请求完成，但响应信息为空");
        }

        String signature = httpResponse.getFirstHeader(SIGNATURE_HEADER);
        if(StringUtil.isEmpty(signature)){
            throw new SDKException("响应签名为空，无法验签，ResponseBody: " + getRespBodyStr(httpResponse));
        }

        Response response;
        try{
            //1.对响应数据进行对象转换
            response = JsonUtil.toBean(httpResponse.getBody(), Response.class);
            if(response == null){
                throw new SDKException("响应数据为空，ResponseBody: " + getRespBodyStr(httpResponse));
            }

            //2.对响应数据进行验签
            boolean isSignOk = SignUtil.verify(httpResponse.getBody(), signature, response.getSignType(), secretKey.getPlatPubKey());
            if(! isSignOk){
                throw new SDKException("请求完成，但响应信息验签失败: " + " signature: " + signature + ", ResponseBody: " + getRespBodyStr(httpResponse));
            }
        }catch (Exception e){
            if(e instanceof SDKException){
                throw e;
            }else{
                throw new SDKException("响应数据转换时异常, signature: " + signature + "，ResponseBody: " + getRespBodyStr(httpResponse) , e);
            }
        }

        //3.对响应数据中的secKey进行rsa解密
        if(StringUtil.isNotEmpty(response.getSecKey())){
            String secKey = RSAUtil.decryptByPrivateKey(response.getSecKey(), secretKey.getMchPriKey());
            response.setSecKey(secKey);
        }
        return response;
    }

    private static String getFormSignStr(Map<String, String> formBody){
        StringBuffer sbf = new StringBuffer();
        for(Map.Entry<String, String> entry : formBody.entrySet()){
            sbf.append(entry.getKey())
                    .append("=")
                    .append(entry.getValue() == null ? "" : entry.getValue())
                    .append("&");
        }
        String str = sbf.toString();
        return str.substring(0, str.lastIndexOf("&"));
    }

    private static String getRespBodyStr(HttpUtil.Response httpResponse){
        return new String(httpResponse.getBody(), StandardCharsets.UTF_8);
    }
}
