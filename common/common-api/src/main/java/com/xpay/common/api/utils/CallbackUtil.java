package com.xpay.common.api.utils;

import com.xpay.common.api.constants.HttpHeaderKey;
import com.xpay.common.api.params.CallbackParam;
import com.xpay.common.api.dto.CallbackResp;
import com.xpay.common.api.dto.SecretKey;
import com.xpay.common.statics.exception.BizException;
import com.xpay.common.utils.*;
import okhttp3.Call;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * 回调商户的工具类
 */
public class CallbackUtil {
    private static Logger logger = LoggerFactory.getLogger(CallbackUtil.class);

    /**
     * 同步请求回调商户
     * @param url       回调请求的地址
     * @param request   回调请求的参数
     * @param secretKey 回调请求时需要用到的相关密钥
     * @return
     */
    public static <T> CallbackResp requestSync(String url, CallbackParam<T> request, SecretKey secretKey) {
        //1.构建请求参数
        Map<String, String> reqHeader = new HashMap<>();
        StringBuffer reqBody = new StringBuffer();
        fillRequestParam(request, secretKey, reqHeader, reqBody);

        //2.发起http(s)请求，并根据响应结果设置响应数据
        CallbackResp callbackResp = new CallbackResp();
        callbackResp.setSignType(request.getSignType());
        HttpUtil.Response response = null;
        try {
            //2.1 发起http(s)请求并等待响应结果
            response = HttpUtil.postJsonSync(url, reqHeader, reqBody.toString());
            //2.2 填充响应体内容
            fillCallbackResp(response, callbackResp);
        } catch (SocketTimeoutException e){ //OKHttp这个工具在超时时会抛出 SocketTimeoutException，其他http工具未必还是抛这个异常
            callbackResp.setHttpStatus(CallbackResp.TIMEOUT_HTTP_STATUS);
            callbackResp.setHttpError(e.getMessage());
        } catch (Throwable ex) {
            logger.error("回调过程中发生异常 CallbackParam = {}", JsonUtil.toJson(request), ex);
            callbackResp.setHttpError("回调过程中发生异常, " + ex.getMessage());
        }

        //3.如果有需要，则执行验签
        boolean isNeedVerifyResp = CallbackParam.SIGN_RESP_VALUE.equals(request.getSignResp());
        if (callbackResp.isRequestOk() && isNeedVerifyResp) {
            signVerify(callbackResp, secretKey.getRespVerifyKey());
        }
        return callbackResp;
    }

    /**
     * 异步请求回调商户
     * @param url       回调请求的地址
     * @param request   回调请求的参数
     * @param secretKey 回调请求时需要用到的相关密钥
     * @param callback  回调请求得到响应之后的逻辑处理器，如不需要处理则传null
     */
    public static <T> void requestAsync(String url, CallbackParam<T> request, SecretKey secretKey, Callback callback) {
        //1.构建请求参数
        Map<String, String> reqHeader = new HashMap<>();
        StringBuffer reqBody = new StringBuffer();
        fillRequestParam(request, secretKey, reqHeader, reqBody);

        //2.设置请求完成之后的线程回调处理
        String signType = request.getSignType();
        boolean isNeedVerifyResp = CallbackParam.SIGN_RESP_VALUE.equals(request.getSignResp());
        okhttp3.Callback httpCallback = callback == null ? null : new okhttp3.Callback(){
            @Override
            public void onResponse(Call call, Response response) {
                CallbackResp callbackResp = new CallbackResp();
                callbackResp.setSignType(signType);
                try {
                    //2.1 填充响应对象的内容
                    HttpUtil.Response httpResponse = HttpUtil.convertResponseAndClose(response);
                    fillCallbackResp(httpResponse, callbackResp);
                    //2.2 如果有需要，则执行验签
                    if (callbackResp.isRequestOk() && isNeedVerifyResp) {
                        signVerify(callbackResp, secretKey.getRespVerifyKey());
                    }
                } catch(Exception e) {
                    callbackResp.setHttpError("对响应内容处理时出现异常，" + e.getMessage());
                }
                callback.onResponse(callbackResp);
            }

            @Override
            public void onFailure(Call call, IOException e) {
                CallbackResp callbackResp = new CallbackResp();
                callbackResp.setSignType(signType);
                if(e != null){
                    if(e instanceof SocketTimeoutException){//OKHttp这个工具在超时时会抛出 SocketTimeoutException，其他http工具未必还是抛这个异常
                        callbackResp.setHttpStatus(CallbackResp.TIMEOUT_HTTP_STATUS);
                    }
                    callbackResp.setHttpError(e.getMessage());
                }
                callback.onError(callbackResp, e);
            }
        };

        //3.异步发起http(s)请求
        HttpUtil.postJsonAsync(url, reqHeader, reqBody.toString(), httpCallback);
    }

    private static void fillRequestParam(CallbackParam request, SecretKey secretKey, Map<String, String> reqHeader, StringBuffer reqBody){
        //1.参数校验
        paramValid(request, secretKey);

        //2.对sec_key执行rsa加密
        if (StringUtil.isNotEmpty(request.getSecKey())) {
            String secKey = RSAUtil.encryptByPublicKey(request.getSecKey(), secretKey.getSecKeyEncryptKey());
            request.setSecKey(secKey);
        }

        //3.对请求参数生成签名串
        try {
            String reqJsonStr = JsonUtil.toJsonWithNull(request);
            byte[] reqJsonByte = reqJsonStr.getBytes(StandardCharsets.UTF_8);
            String signature = SignUtil.sign(reqJsonByte, request.getSignType(), secretKey.getReqSignKey());

            reqHeader.put(HttpHeaderKey.SIGNATURE_HEADER, signature);
            reqBody.append(reqJsonStr);
        } catch (Exception e) {
            throw new BizException("签名失败: " + e.getMessage(), e);
        }
    }

    private static <T> void paramValid(CallbackParam callback, SecretKey secretKey){
        if(callback == null){
            throw new BizException("callback不能为空");
        }

        if (StringUtil.isEmpty(callback.getRandStr())) {
            callback.setRandStr(RandomUtil.get32LenStr());
        }

        if(StringUtil.isEmpty(callback.getMchNo())){
            throw new BizException("CallbackDto.mchNo不能为空");
        }else if(StringUtil.isEmpty(callback.getRandStr())){
            throw new BizException("CallbackDto.randStr不能为空");
        }else if(callback.getData() == null){
            throw new BizException("CallbackDto.data不能为空");
        }else if(StringUtil.isEmpty(callback.getSignType())){
            throw new BizException("CallbackDto.signType不能为空");
        }

        if(secretKey == null){
            throw new BizException("secretKey不能为空");
        }else if(StringUtil.isEmpty(secretKey.getReqSignKey())){
            throw new BizException("SecretKey.reqSignKey不能为空");
        }else if(StringUtil.isNotEmpty(callback.getSecKey()) && StringUtil.isEmpty(secretKey.getSecKeyEncryptKey())){
            throw new BizException("CallbackDto.secKey有值时SecretKey.secKeyEncryptKey不能为空");
        }
    }

    private static void fillCallbackResp(HttpUtil.Response response, CallbackResp callbackResp) {
        callbackResp.setHttpStatus(response.getHttpStatus());
        callbackResp.setHttpError(response.getHttpError());
        callbackResp.setBody(response.getBody());

        String signature = response.getFirstHeader(HttpHeaderKey.SIGNATURE_HEADER);
        callbackResp.setSignature(signature);

        if (response.getBody() != null && response.getBody().length > 0) {
            try{
                HashMap respMsg = JsonUtil.toBean(response.getBody(), HashMap.class);
                callbackResp.setCode(respMsg==null ? null : (String) respMsg.get("code"));
            }catch(Exception e){
            }
        }
    }

    private static void signVerify(CallbackResp resp, String verifyKey){
        try{
            boolean isOk = SignUtil.verify(resp.getBody(), resp.getSignature(), resp.getSignType(), verifyKey);
            resp.setVerifyResult(isOk ? CallbackResp.VERIFY_PASS : CallbackResp.VERIFY_FAIL);
        }catch(Exception e){
            resp.setVerifyResult(CallbackResp.VERIFY_ERR);
        }
    }



    public interface Callback {

        void onResponse(CallbackResp resp);

        void onError(CallbackResp resp, Throwable e);
    }
}
