package com.xpay.common.utils;

import okhttp3.*;

import javax.net.ssl.*;
import java.io.IOException;
import java.net.Proxy;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class HttpUtil {
    private static final MediaType JSON_TYPE = MediaType.parse("application/json;charset=utf-8");
    private static final int CONNECT_TIMEOUT = 10;
    private static final int WRITE_TIMEOUT = 20;
    private static final int READ_TIMEOUT = 20;
    private static OkHttpClient httpClient = buildHttpClient(null);

    public static Response getSync(String url, Map<String, String> header, Map<String, String> param) throws Exception {
        if(param != null){
            int i=0, len = param.size();
            for(Map.Entry<String, String> entry : param.entrySet()){
                if(i==0){
                    url += "?";
                }
                url += entry.getKey() + "=" + entry.getValue();
                if(i < len-1){
                    url += "&";
                }
                i++;
            }
        }

        Request.Builder builder = new Request.Builder();
        addHeader(builder, header);
        Request request = builder.url(getUrl(url)).get().build();
        okhttp3.Response response = httpClient.newCall(request).execute();
        return convertResponseAndClose(response);
    }

    public static Response postFormSync(String url, Map<String, String> param) throws Exception {
        return postFormSync(url, null, param);
    }

    /**
     * 提交Form表单
     *
     * @param url
     * @param param
     * @return
     */
    public static Response postFormSync(String url, Map<String, String> header, Map<String, String> param) throws Exception {
        FormBody.Builder bodBuilder = new FormBody.Builder();
        if(param != null && !param.isEmpty()){
            for(Map.Entry<String, String> entry : param.entrySet()){
                bodBuilder.add(entry.getKey(), entry.getValue());
            }
        }

        RequestBody body = bodBuilder.build();

        Request.Builder reqBuilder = new Request.Builder();
        addHeader(reqBuilder, header);

        final Request request = reqBuilder.url(getUrl(url)).post(body).build();
        okhttp3.Response response = httpClient.newCall(request).execute();
        return convertResponseAndClose(response);
    }

    /**
     * post同步请求，提交Json数据
     *
     * @param url
     * @param json
     * @return
     */
    public static Response postJsonSync(String url, String json) throws Exception {
        final RequestBody requestBody = RequestBody.create(JSON_TYPE, json);
        final Request request = new Request.Builder()
                .url(getUrl(url))
                .post(requestBody)
                .build();

        okhttp3.Response response = httpClient.newCall(request).execute();
        return convertResponseAndClose(response);
    }

    /**
     * 用完之后记得调用 okhttp3.Response.close() 方法进行关闭
     * @param url
     * @param header
     * @param json
     * @return
     * @throws Exception
     */
    public static Response postJsonSync(String url, Map<String, String> header, String json) throws Exception {
        final RequestBody requestBody = RequestBody.create(JSON_TYPE, json);
        Request.Builder builder = new Request.Builder();
        if(header != null && !header.isEmpty()){
            for(Map.Entry<String, String> entry : header.entrySet()){
                builder.header(entry.getKey(), entry.getValue());
            }
        }
        final Request request = builder.url(getUrl(url))
                .post(requestBody)
                .build();
        okhttp3.Response response = httpClient.newCall(request).execute();
        return convertResponseAndClose(response);
    }

    /**
     * post异步请求，提交Json数据
     */
    public static void postJsonAsync(String url, String json, Callback callback) {
        postJsonAsync(url, null, json, callback);
    }

    public static void postJsonAsync(String url, Map<String, String> header, String json, Callback callback) {
        final RequestBody requestBody = RequestBody.create(JSON_TYPE, json);
        Request.Builder builder = new Request.Builder();
        if(header != null && !header.isEmpty()){
            for(Map.Entry<String, String> entry : header.entrySet()){
                builder.header(entry.getKey(), entry.getValue());
            }
        }
        final Request request = builder.url(getUrl(url))
                .post(requestBody)
                .build();

        if(callback == null){
            callback = new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                }
                @Override
                public void onResponse(Call call, okhttp3.Response response) throws IOException {
                }
            };
        }

        httpClient.newCall(request).enqueue(callback);
    }

    public static Response postFileSync(String url, Map<String, String> header, Map<String, String> param,
                                    String fileName, byte[] fileData) throws Exception {
        Request.Builder reqBuilder = new Request.Builder();
        if(header != null && header.size() > 0){
            for(Map.Entry<String, String> entry : header.entrySet()){
                reqBuilder.header(entry.getKey(), entry.getValue());
            }
        }

        MultipartBody.Builder bodyBuilder = new MultipartBody.Builder().setType(MultipartBody.FORM);
        if(param != null && param.size() > 0){
            for(Map.Entry<String, String> entry : param.entrySet()){
                bodyBuilder.addFormDataPart(entry.getKey(), entry.getValue());
            }
        }
        bodyBuilder.addFormDataPart("file", fileName, RequestBody.create(MediaType.parse("multipart/form-data"), fileData));

        Request request = reqBuilder.url(getUrl(url))
                .post(bodyBuilder.build())
                .build();

        okhttp3.Response response = httpClient.newCall(request).execute();
        return convertResponseAndClose(response);
    }

    private static String getUrl(String url) {
        if (url.startsWith("http://") || url.startsWith("https://")) {
            return url;
        } else {
            return "http://" + url;
        }
    }

    private static void addHeader(Request.Builder builder, Map<String, String> header){
        if(header == null || header.isEmpty()){
            return;
        }
        for(Map.Entry<String, String> entry : header.entrySet()){
            builder.header(entry.getKey(), entry.getValue());
        }
        if(header == null || (header != null && !header.containsKey("Connection"))){ //默认使用短连接
            builder.header("Connection", "close");
        }
    }

    public static OkHttpClient buildHttpClient(Map<String, Object> config) {
        try {
            final X509TrustManager trustManager = new X509TrustManager() {
                public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) {
                }

                public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                    if (chain == null) {
                        throw new IllegalArgumentException("checkServerTrusted: X509Certificate array is null");
                    }
                    if (!(chain.length > 0)) {
                        throw new IllegalArgumentException("checkServerTrusted: X509Certificate is empty");
                    }
                    try {
                        TrustManagerFactory tmf = TrustManagerFactory.getInstance("X509");
                        tmf.init((KeyStore) null);
                        for (TrustManager trustManager : tmf.getTrustManagers()) {
                            ((X509TrustManager) trustManager).checkServerTrusted(chain, authType);
                        }
                    } catch (Exception e) {
                        throw new CertificateException(e);
                    }
                }

                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                    return new java.security.cert.X509Certificate[]{};
                }
            };

            SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, new TrustManager[]{trustManager}, new SecureRandom());
            SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

            OkHttpClient.Builder builder = new OkHttpClient.Builder();
            builder.sslSocketFactory(sslSocketFactory, trustManager);
            builder.proxy(Proxy.NO_PROXY); //不使用代理，避免被第三方使用代理抓包
            builder.hostnameVerifier(new HostnameVerifier() {
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            });

            OkHttpClient okHttpClient = builder
                    .connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
                    .writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS)
                    .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
                    .retryOnConnectionFailure(false)//不允许重试
                    .build();

            return okHttpClient;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static Response convertResponseAndClose(okhttp3.Response response) throws IOException {
        try{
            Map<String, List<String>> headers = response.headers() != null ? response.headers().toMultimap() : new HashMap<>();
            byte[] body = response.body() != null ? response.body().bytes() : new byte[]{};
            return new Response(response.code(), response.message(), headers, body);
        } finally {
            response.close();
        }
    }


    public static class Response {
        private Integer httpStatus;
        private String httpError;
        private Map<String, List<String>> headers;
        private byte[] body;

        public Response (Integer httpStatus, String httpError, Map<String, List<String>> headers, byte[] body){
            this.httpStatus = httpStatus;
            this.httpError = httpError;
            this.headers = headers;
            this.body = body;
        }

        public Integer getHttpStatus() {
            return httpStatus;
        }

        public void setHttpStatus(Integer httpStatus) {
            this.httpStatus = httpStatus;
        }

        public String getHttpError() {
            return httpError;
        }

        public void setHttpError(String httpError) {
            this.httpError = httpError;
        }

        public Map<String, List<String>> getHeaders() {
            return headers;
        }

        public void setHeaders(Map<String, List<String>> headers) {
            this.headers = headers;
        }

        public byte[] getBody() {
            return body;
        }

        public void setBody(byte[] body) {
            this.body = body;
        }

        public String getFirstHeader(String name){
            if(headers != null && headers.size() > 0){
                List<String> valueList = headers.get(name);
                return valueList != null && valueList.size() > 0 ? valueList.get(0) : null;
            }
            return null;
        }

        public String getBodyString(){
            if(body == null || body.length == 0){
                return null;
            }else{
                return new String(body, StandardCharsets.UTF_8);
            }
        }

        public boolean isSuccessful() {
            return httpStatus >= 200 && httpStatus < 300;
        }
    }
}
