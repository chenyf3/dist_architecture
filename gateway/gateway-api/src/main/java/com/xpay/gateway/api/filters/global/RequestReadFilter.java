package com.xpay.gateway.api.filters.global;

import com.xpay.common.api.constants.HttpHeaderKey;
import com.xpay.gateway.api.config.conts.FilterOrder;
import com.xpay.gateway.api.config.conts.GatewayErrorCode;
import com.xpay.gateway.api.config.properties.GatewayProperties;
import com.xpay.gateway.api.exceptions.GatewayException;
import com.xpay.gateway.api.params.FileUploadParam;
import com.xpay.common.statics.enums.common.ApiRespCodeEnum;
import com.xpay.common.utils.JsonUtil;
import com.xpay.common.utils.StringUtil;
import com.xpay.gateway.api.params.RequestParam;
import com.xpay.gateway.api.utils.ByteUtil;
import com.xpay.gateway.api.utils.TraceUtil;
import org.apache.commons.codec.net.URLCodec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.ResolvableType;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.codec.HttpMessageReader;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.http.codec.multipart.FormFieldPart;
import org.springframework.http.codec.multipart.Part;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.util.CollectionUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @description 读请求体并转换成RequestParam对象，之后再放到内存中存起来，这个过滤器必须是第1个执行，不然，
 * 后续的过滤器无法获得请求参数，同时此过滤器要配合 RequestModifyFilter 一起使用，不然后端服务无法获取到请求参数，
 * 因为请求数据在本过滤器中已被取出。
 * @author chenyf
 * @date 2019-02-23
 */
public class RequestReadFilter extends AbstractGlobalFilter {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private static final ResolvableType MULTIPART_DATA_TYPE = ResolvableType.forClassWithGenerics(MultiValueMap.class, String.class, Part.class);
    private static final Mono<MultiValueMap<String, Part>> EMPTY_MULTIPART_DATA = Mono.just(CollectionUtils.unmodifiableMultiValueMap(new LinkedMultiValueMap<String, Part>(0))).cache();
    private List<HttpMessageReader<?>> messageReaders;
    private GatewayProperties properties;

    public RequestReadFilter (ServerCodecConfigurer codecConfigurer, GatewayProperties properties) {
        this.messageReaders = codecConfigurer.getReaders();
        this.properties = properties;
    }

    /**
     * 设置当前过滤器的执行顺序：本过滤器在全局过滤器中的顺序必须为第1个，不然，后续的过滤器无法获得请求参数
     * @return
     */
    @Override
    public int getOrder() {
        return FilterOrder.REQUEST_READ_FILTER;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        if(! HttpMethod.POST.matches(request.getMethodValue())){
            throw GatewayException.fail(ApiRespCodeEnum.PARAM_FAIL.getValue(), "Only POST Method Are Support!", GatewayErrorCode.PARAM_CHECK_ERROR);
        }

        MediaType mediaType = exchange.getRequest().getHeaders().getContentType();
        if(MediaType.APPLICATION_JSON.equals(mediaType) || MediaType.APPLICATION_JSON_UTF8.equals(mediaType)){
            return readJsonBody(exchange, chain);
        }else if(MediaType.APPLICATION_FORM_URLENCODED.equals(mediaType)){
            return readUrlEncodeForm(exchange, chain);
        }else if(MediaType.MULTIPART_FORM_DATA.includes(mediaType)){ //multipart/form-data类型的表单，会有类似 “ ;boundary=7b25cf8f-afa3-48ec-8157-d5717f218a50” 这样的东西，这个是每个字段的分隔符，所以此处不能用equals()
            return readMultiPartForm(exchange, chain);
        }else{
            throw GatewayException.fail(ApiRespCodeEnum.PARAM_FAIL.getValue(), "ContentType : " + mediaType + " Are Not Support!", GatewayErrorCode.PARAM_CHECK_ERROR);
        }
    }

    @Override
    protected Mono<Void> textBodyFilter(ServerWebExchange exchange, GatewayFilterChain chain) {
        return null;
    }

    @Override
    protected Mono<Void> fileBodyFilter(ServerWebExchange exchange, GatewayFilterChain chain) {
        return null;
    }

    private Mono<Void> readJsonBody(ServerWebExchange exchange, GatewayFilterChain chain){
        final String signature = exchange.getRequest().getHeaders().getFirst(HttpHeaderKey.SIGNATURE_HEADER);

        return DataBufferUtils.join(exchange.getRequest().getBody())
                .flatMap((DataBuffer dataBuffer) -> {
                    byte[] oriBytes = new byte[dataBuffer.readableByteCount()];
                    dataBuffer.read(oriBytes);
                    DataBufferUtils.release(dataBuffer);//释放资源

                    RequestParam requestParam = JsonUtil.toBean(oriBytes, RequestParam.class);
                    if(requestParam != null){
                        requestParam.setTempField(signature, oriBytes);
                    }

                    TraceUtil.bodyLog(exchange, requestParam.toString());
                    super.cacheRequestParam(exchange, requestParam);
                    TraceUtil.timeTraceLog(exchange, this.getClass().getSimpleName());
                    return chain.filter(exchange);
                });
    }

    private Mono<Void> readUrlEncodeForm(ServerWebExchange exchange, GatewayFilterChain chain) {
        String signatureEnc = exchange.getRequest().getHeaders().getFirst(HttpHeaderKey.SIGNATURE_HEADER);//先从请求头中获取签名串
        if(StringUtil.isEmpty(signatureEnc)){
            signatureEnc = exchange.getRequest().getQueryParams().getFirst(HttpHeaderKey.SIGNATURE_HEADER);//如果请求头中没有，则从url参数中获取签名串
            if(StringUtil.isNotEmpty(signatureEnc)){
                try{
                    signatureEnc = new URLCodec().decode(signatureEnc, StandardCharsets.UTF_8.name());
                }catch(Exception e){
                    throw GatewayException.fail(ApiRespCodeEnum.PARAM_FAIL.getValue(), "签名串url解码异常", GatewayErrorCode.PARAM_CHECK_ERROR);
                }
            }
        }

        final String signature = signatureEnc;
        return exchange.getFormData().flatMap(multiFormMap-> {
            Map<String, String> paramMap = new TreeMap<>(); //按字典序排序(升序)
            for(Map.Entry<String, String> entry : multiFormMap.toSingleValueMap().entrySet()){ //此处直接规定了一个key只能有一个值
                paramMap.put(entry.getKey(), entry.getValue());
            }

            byte[] signBody = getSignContent(paramMap);
            String bodyStr = JsonUtil.toJson(paramMap);
            RequestParam requestParam = JsonUtil.toBean(bodyStr, RequestParam.class);
            if(requestParam != null){
                requestParam.setTempField(signature, signBody);
            }

            TraceUtil.bodyLog(exchange, requestParam.toString());
            super.cacheRequestParam(exchange, requestParam);
            TraceUtil.timeTraceLog(exchange, this.getClass().getSimpleName());
            return chain.filter(exchange);
        });
    }

    private Mono<Void> readMultiPartForm(ServerWebExchange exchange, GatewayFilterChain chain){
        if(properties.getReadMultipartOriBody()){
            return readMultiPartTwice(exchange, chain);
        }else{
            return readMultiPartOnce(exchange, chain, exchange.getMultipartData(), null);
        }
    }

    private Mono<Void> readMultiPartTwice(ServerWebExchange exchange, GatewayFilterChain chain){
        return DataBufferUtils.join(exchange.getRequest().getBody()).flatMap(dataBuffer -> {
            //1. 取得源请求体数据
            byte[] oriBodyBytes = new byte[dataBuffer.readableByteCount()];
            dataBuffer.read(oriBodyBytes);
            DataBufferUtils.release(dataBuffer);//释放资源

            //2. 把请求体的数据封装到ServerHttpRequestDecorator里面
            ServerHttpRequestDecorator requestDecorator = new ServerHttpRequestDecorator(exchange.getRequest()) {
                @Override
                public Flux<DataBuffer> getBody() {
                    DataBuffer bodyBuffer = ByteUtil.toDataBuffer(oriBodyBytes);//把字节数组转换成DataBuffer
                    return Flux.just(bodyBuffer);
                }
            };

            //3.把请求数据转换成 MultiValueMap<String, Part> 类型的数据
            Mono<MultiValueMap<String, Part>> multipartData = repackageMultipartData(requestDecorator, messageReaders);

            //4.从MultiValueMap<String, Part>类型的数据中读出数据，并转换成 FileUploadParam
            return readMultiPartOnce(exchange, chain, multipartData, oriBodyBytes);
        });
    }

    private Mono<Void> readMultiPartOnce(ServerWebExchange exchange, GatewayFilterChain chain,
                                                   Mono<MultiValueMap<String, Part>> multipartData,
                                                   byte[] oriBodyBytes){
        String signature = exchange.getRequest().getHeaders().getFirst(HttpHeaderKey.SIGNATURE_HEADER);
        return multipartData.flatMap(multiPartMap -> {
            Map<String, String> textParam = new TreeMap<>(); //保持字典序升序的排序，对于文本参数，一个字段只能允许一个值
            List<FileUploadParam.FileInfo> files = new ArrayList<>();//对于文件参数，一个字段可以有多个值，代表同时上传多个文件
            AtomicLong fileLength = new AtomicLong(0);//文件的字节总长度

            for (Map.Entry<String, List<Part>> entry : multiPartMap.entrySet()) {
                String name = entry.getKey();
                for(Part part : entry.getValue()){
                    MediaType mediaType = part.headers().getContentType();
                    if (mediaType == null || MediaType.TEXT_PLAIN.equals(mediaType)) {
                        textParam.put(name, ((FormFieldPart)part).value());
                    } else if(isAllowedFileMediaType(mediaType)) {
                        FileUploadParam.FileInfo fileInfo = new FileUploadParam.FileInfo();
                        fileInfo.setContentType(mediaType.toString());
                        fileInfo.setFilename(part.headers().getContentDisposition().getFilename());
                        //把文件内容读取到字节数组中
                        DataBufferUtils.join(part.content()).subscribe(dataBuffer -> {
                            byte[] fileBytes = new byte[dataBuffer.readableByteCount()];
                            dataBuffer.read(fileBytes);
                            DataBufferUtils.release(dataBuffer); //释放资源
                            fileInfo.setData(fileBytes);
                            fileInfo.setLength(fileBytes.length);
                            fileLength.addAndGet(fileBytes.length);
                        });
                        files.add(fileInfo);
                    } else {
                        return Mono.error(
                                GatewayException.fail(ApiRespCodeEnum.PARAM_FAIL.getValue(),
                                        "字段名: " + name + ", 未支持的ContentType类型: " + mediaType.toString(),
                                        GatewayErrorCode.PARAM_CHECK_ERROR)
                        );
                    }
                }
            }

            byte[] signBody = getSignContent(textParam);
            String textParamStr = JsonUtil.toJson(textParam);
            FileUploadParam uploadParam = JsonUtil.toBean(textParamStr, FileUploadParam.class);
            uploadParam.setData(null);//置为null，避免用户错传数据
            uploadParam.setSignature(signature);
            uploadParam.setSignBody(signBody);
            uploadParam.setOriBody(oriBodyBytes)
                    .setFileLength(fileLength.get())
                    .setFiles(files);

            TraceUtil.bodyLog(exchange, uploadParam.toString());
            super.cacheFileUploadParam(exchange, uploadParam);
            TraceUtil.timeTraceLog(exchange, this.getClass().getSimpleName());
            return chain.filter(exchange);
        });
    }

    /**
     * 取得待签名内容(form表单形式请求时)
     * @param formBody
     * @return
     */
    private byte[] getSignContent(Map<String, String> formBody) {
        StringBuilder sbf = new StringBuilder();
        for(Map.Entry<String, String> entry : formBody.entrySet()){
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

    private Mono<MultiValueMap<String, Part>> repackageMultipartData(ServerHttpRequest request,
                                                                     List<HttpMessageReader<?>> messageReaders) {
        return ((HttpMessageReader<MultiValueMap<String, Part>>) messageReaders
                .stream()
                .filter(reader -> reader.canRead(MULTIPART_DATA_TYPE, MediaType.MULTIPART_FORM_DATA))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No multipart HttpMessageReader.")))
                .readMono(MULTIPART_DATA_TYPE, request, Collections.emptyMap())
                .switchIfEmpty(EMPTY_MULTIPART_DATA)
                .cache();
    }

    private boolean isAllowedFileMediaType(MediaType mediaType) {
        return MediaType.APPLICATION_OCTET_STREAM.equals(mediaType) //通用的二进制文件
                || MediaType.APPLICATION_PDF.includes(mediaType) //.pdf文件
                || MediaType.APPLICATION_XML.includes(mediaType) //.xml文件
                || MediaType.TEXT_XML.includes(mediaType) //.xml文件
                || MediaType.IMAGE_PNG.includes(mediaType) //.png文件
                || MediaType.IMAGE_GIF.includes(mediaType) //.gif文件
                || MediaType.IMAGE_JPEG.includes(mediaType) //.jpg .jpeg 文件
                || com.xpay.common.api.enums.MediaType.TEXT_CSV.getValue().equals(mediaType.toString()) //.csv文件
                || com.xpay.common.api.enums.MediaType.APPLICATION_ZIP.getValue().equals(mediaType.toString()) //.zip文件
                || com.xpay.common.api.enums.MediaType.APPLICATION_RAR.getValue().equals(mediaType.toString()) //.rar文件
                || com.xpay.common.api.enums.MediaType.APPLICATION_EXCEL_O3.getValue().equals(mediaType.toString()) //.xls 文件
                || com.xpay.common.api.enums.MediaType.APPLICATION_EXCEL_07.getValue().equals(mediaType.toString()) //.xlsx 文件
                || com.xpay.common.api.enums.MediaType.APPLICATION_WORD_O3.getValue().equals(mediaType.toString()) //.doc 文件
                || com.xpay.common.api.enums.MediaType.APPLICATION_WORD_07.getValue().equals(mediaType.toString()) //.docx 文件
                ;
    }
}
