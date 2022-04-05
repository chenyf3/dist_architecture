package com.xpay.gateway.api.filters.global;

import com.xpay.common.api.constants.HttpHeaderKey;
import com.xpay.common.api.params.APIParam;
import com.xpay.gateway.api.params.FileUploadParam;
import com.xpay.gateway.api.utils.ByteUtil;
import com.xpay.gateway.api.config.conts.GatewayErrorCode;
import com.xpay.gateway.api.exceptions.GatewayException;
import com.xpay.gateway.api.helper.RequestHelper;
import com.xpay.common.statics.enums.common.ApiRespCodeEnum;
import com.xpay.common.utils.JsonUtil;
import com.xpay.common.utils.StringUtil;
import com.xpay.gateway.api.config.conts.FilterOrder;
import com.xpay.gateway.api.params.RequestParam;
import com.xpay.gateway.api.utils.TraceUtil;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.factory.rewrite.CachedBodyOutputMessage;
import org.springframework.cloud.gateway.support.BodyInserterContext;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.reactive.function.BodyInserter;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

/**
 * @description 修改请求体，包括：secKey的解密 等
 * @author chenyf
 * @date 2019-02-23
 */
public class RequestModifyFilter extends AbstractGlobalFilter {
    private RequestHelper requestHelper;

    public RequestModifyFilter(RequestHelper requestHelper){
        this.requestHelper = requestHelper;
    }

    /**
     * 设置当前过滤器的执行顺序：本过滤器在全局过滤器中的顺序建议为第4个
     * @return
     */
    @Override
    public int getOrder() {
        return FilterOrder.REQUEST_MODIFY_FILTER;
    }

    @Override
    protected Mono<Void> textBodyFilter(ServerWebExchange exchange, GatewayFilterChain chain) {
        RequestParam requestParam = getRequestParam(exchange);

        //1.重新封装请求体、计算请求体的字节大小
        byte[] bodyByte = modifyBody(exchange, requestParam);
        String contentType = MediaType.APPLICATION_JSON_UTF8_VALUE;
        return writeRequestBody(exchange, chain, bodyByte, contentType, requestParam.getMchNo(), requestParam.getSignType());
    }

    @Override
    protected Mono<Void> fileBodyFilter(ServerWebExchange exchange, GatewayFilterChain chain) {
        FileUploadParam uploadParam = getFileUploadParam(exchange);

        //1.重新封装请求体、计算请求体的字节大小
        String boundary = "--------" + MimeTypeUtils.generateMultipartBoundaryString();
        String contentType = "multipart/form-data; boundary=" + boundary;
        byte[] bodyByte = modifyBody(uploadParam, boundary);

        //2.更新缓存中的body
        uploadParam.clearTempField();//清空临时字段
        cacheFileUploadParam(exchange, uploadParam);

        return writeRequestBody(exchange, chain, bodyByte, contentType, uploadParam.getMchNo(), uploadParam.getSignType());
    }

    private byte[] modifyBody(ServerWebExchange exchange, RequestParam requestParam) {
        //1.对sec_key进行解密
        secKeyDecrypt(requestParam);

        //2.更新缓存中的body
        requestParam.clearTempField();
        cacheRequestParam(exchange, requestParam);

        //3.把请求体转换成字节数组
        String bodyStr = JsonUtil.toJson(requestParam);
        return bodyStr.getBytes(StandardCharsets.UTF_8);
    }

    private byte[] modifyBody(FileUploadParam uploadParam, String boundary) {
        ByteArrayOutputStream bodyStream = new ByteArrayOutputStream();

        //1.对sec_key进行解密
        secKeyDecrypt(uploadParam);

        //2.把参数转换成multipart/form-data表单提交的形式
          //2.1 对文本参数进行转换
        String eol = "\r\n";//换行符
        String separator = "--" + boundary + eol;//字段分割符
        StringBuilder content = new StringBuilder();
        uploadParam.getTextParamSorted().forEach((key,val) -> {
            content.append(separator)
                    .append("Content-Disposition: form-data; ").append("name=\"").append(key).append("\"").append(eol)
                    .append("Content-Type: ").append(MediaType.TEXT_PLAIN_VALUE).append(eol)
                    .append(eol)
                    .append(val == null ? "" : val).append(eol);
        });
        byte[] textBytes = ByteUtil.getBytes(content.toString());
        bodyStream.write(textBytes, 0, textBytes.length);

          //2.2 对文件参数进行转换
        byte[] eolByte = ByteUtil.getBytes(eol);
        uploadParam.getFiles().forEach(file -> {
            StringBuilder fileContent = new StringBuilder();
            fileContent.append(separator)
                    .append("Content-Disposition: form-data; ").append("name=\"").append("files").append("\"; ")
                    .append("filename=\"").append(file.getFilename()).append("\"").append(eol)
                    .append("Content-Type: ").append(file.getContentType()).append(eol)
                    .append(eol);

            byte[] fileBytes = ByteUtil.getBytes(fileContent.toString());
            bodyStream.write(fileBytes, 0, fileBytes.length);
            bodyStream.write(file.getData(), 0, file.getData().length);
            bodyStream.write(eolByte, 0, eolByte.length);
        });

           //2.3 表单结束符
        StringBuilder suffix = new StringBuilder();
        suffix.append("--").append(boundary).append("--").append(eol);
        byte[] suffixByte = ByteUtil.getBytes(suffix.toString());
        bodyStream.write(suffixByte, 0, suffixByte.length);
        return bodyStream.toByteArray();
    }


    private boolean secKeyDecrypt(RequestParam requestParam){
        if(StringUtil.isEmpty(requestParam.getSecKey())){
            return false;
        }

        try{
            //如果secKey不为空，则对secKey进行解密
            String secKey = requestParam.getSecKey();
            String mchNo = requestParam.getMchNo();
            APIParam apiParam = new APIParam(requestParam.getSignType(), requestParam.getVersion());
            secKey = requestHelper.secKeyDecrypt(secKey, mchNo, apiParam);
            requestParam.setSecKey(secKey);
            return true;
        }catch(Throwable ex){
            throw GatewayException.fail(ApiRespCodeEnum.PARAM_FAIL.getValue(), "secKey解密失败", GatewayErrorCode.PARAM_CHECK_ERROR);
        }
    }

    private boolean secKeyDecrypt(FileUploadParam uploadParam){
        if(StringUtil.isEmpty(uploadParam.getSecKey())){
            return false;
        }

        try{
            //如果secKey不为空，则对secKey进行解密
            String secKey = uploadParam.getSecKey();
            String mchNo = uploadParam.getMchNo();
            APIParam apiParam = new APIParam(uploadParam.getSignType(), uploadParam.getVersion());
            secKey = requestHelper.secKeyDecrypt(secKey, mchNo, apiParam);
            uploadParam.setSecKey(secKey);
            return true;
        }catch(Throwable ex){
            throw GatewayException.fail(ApiRespCodeEnum.PARAM_FAIL.getValue(), "secKey解密失败", GatewayErrorCode.PARAM_CHECK_ERROR);
        }
    }

    private Mono<Void> writeRequestBody(ServerWebExchange exchange, GatewayFilterChain chain, byte[] body,
                                        String contentType, String mchNo, String signType){
        //3.设置请求头
        String requestIp = getRequestRealIp(exchange);//获取来源IP
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaderKey.REQUEST_ORI_IP_KEY, requestIp);//将来源IP放到请求头里
        headers.set(HttpHeaderKey.REQUEST_MCH_NO_KEY, mchNo);
        headers.set(HttpHeaderKey.REQUEST_SIGN_TYPE_KEY, signType);
        headers.setContentType(MediaType.valueOf(contentType));
        headers.setContentLength(body.length);

        //4.更新转发到后端服务的body
        Mono modifiedBody = Mono.just(body);
        BodyInserter bodyInserter = BodyInserters.fromPublisher(modifiedBody, byte[].class);
        CachedBodyOutputMessage outputMessage = new CachedBodyOutputMessage(exchange, headers);
        return bodyInserter.insert(outputMessage, new BodyInserterContext())
                .then(Mono.defer(() -> {
                    ServerHttpRequestDecorator decorator = decorateServer(exchange, headers, outputMessage);
                    TraceUtil.timeTraceLog(exchange, this.getClass().getSimpleName());
                    return chain.filter(exchange.mutate().request(decorator).build());
                }));
    }

    private ServerHttpRequestDecorator decorateServer(ServerWebExchange exchange, HttpHeaders headers,
                                        CachedBodyOutputMessage outputMessage) {
        return new ServerHttpRequestDecorator(exchange.getRequest()) {
            @Override
            public HttpHeaders getHeaders() {
                long contentLength = headers.getContentLength();
                HttpHeaders httpHeaders = new HttpHeaders();
                httpHeaders.putAll(headers);
                if (contentLength > 0) {
                    httpHeaders.setContentLength(contentLength);
                } else {
                    httpHeaders.set(HttpHeaders.TRANSFER_ENCODING, "chunked");//标识请求体大小不确定，分块传输，接收端将以最后一个大小为0的块为结束
                }
                return httpHeaders;
            }

            @Override
            public Flux<DataBuffer> getBody() {
                return outputMessage.getBody();
            }
        };
    }
}
