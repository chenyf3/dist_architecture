package com.xpay.gateway.callback.service;

import com.xpay.gateway.callback.params.RequestParam;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.TreeMap;

public interface CompanyService {

    /**
     * 填充验签数据，比如：
     * 1. 取得回调方生成的签名串(从header或者body里面取，根据每个公司的规则来)，并回set到RequestParam上面
     * 2. 从 {@link RequestParam#getBody} 中生成待验签的数据，并回set到RequestParam上面
     * @param requestParam  回调请求数据填充到RequestParam对象上
     */
    public void fillSignInfo(RequestParam requestParam);

    /**
     * 取得待验签字符串
     * @param body  回调数据
     * @return
     */
    default byte[] getSignContent(TreeMap<String, String> body) {
        StringBuilder sbf = new StringBuilder();
        for(Map.Entry<String, String> entry : body.entrySet()){
            sbf.append(entry.getKey())
                    .append("=")
                    .append(entry.getValue() == null ? "" : entry.getValue())
                    .append("&");
        }
        String str = sbf.toString();
        if(str.length() > 0){
            str = str.substring(0, str.lastIndexOf("&"));
        }
        return str.getBytes(StandardCharsets.UTF_8);
    }

    /**
     * 判断是否验签通过
     * @param signature 回调方生成的签名串
     * @param content   待验签的数据
     * @return
     */
    public boolean isSignaturePass(String signature, byte[] content);

    /**
     * 修改请求体，比如字段解密等
     * @param requestParam  请求体内容
     */
    public void modifyRequestParam(RequestParam requestParam);

    /**
     * 取得响应数据的媒体类型
     * @return
     */
    default String getResponseContentType() {
        return "application/json";
    }

    /**
     * 根据响应码构建响应信息
     * @param code  响应码
     * @param msg   响应描述
     * @return
     */
    public Map<String, Object> buildResponse(String code, String msg);

    /**
     * 根据是否接收成功构建响应信息
     * @param isSuccess 是否接收成功
     * @return
     */
    public Map<String, Object> buildResponse(boolean isSuccess);
}
