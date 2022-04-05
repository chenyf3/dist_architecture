package com.xpay.gateway.api.filters.global;

import com.xpay.common.api.constants.HttpHeaderKey;
import com.xpay.common.api.params.APIParam;
import com.xpay.gateway.api.helper.RequestHelper;
import com.xpay.gateway.api.params.FileUploadParam;
import com.xpay.common.utils.JsonUtil;
import com.xpay.common.utils.StringUtil;
import com.xpay.gateway.api.config.conts.FilterOrder;
import com.xpay.gateway.api.params.RequestParam;
import com.xpay.gateway.api.params.ResponseParam;
import com.xpay.gateway.api.utils.TraceUtil;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.factory.rewrite.CachedBodyOutputMessage;
import org.springframework.cloud.gateway.filter.factory.rewrite.MessageBodyDecoder;
import org.springframework.cloud.gateway.filter.factory.rewrite.MessageBodyEncoder;
import org.springframework.cloud.gateway.support.BodyInserterContext;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.codec.HttpMessageReader;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.web.reactive.function.BodyInserter;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.function.Function.identity;

/**
 * @description 响应体修改，包括：为响应体生成签名、给secKey字段加密 等
 * @author chenyf
 * @date 2019-02-23
 */
public class ResponseModifyFilter extends AbstractGlobalFilter {
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	private List<HttpMessageReader<?>> messageReaders;
	private Map<String, MessageBodyDecoder> messageBodyDecoders;
	private Map<String, MessageBodyEncoder> messageBodyEncoders;
	private RequestHelper requestHelper;

	public ResponseModifyFilter(ServerCodecConfigurer codecConfigurer,
								Set<MessageBodyEncoder> messageBodyEncoders,
								Set<MessageBodyDecoder> messageBodyDecoders,
								RequestHelper requestHelper){
		this.messageReaders = codecConfigurer.getReaders();
		this.messageBodyDecoders = messageBodyDecoders.stream().collect(Collectors.toMap(MessageBodyDecoder::encodingType, identity()));
		this.messageBodyEncoders = messageBodyEncoders.stream().collect(Collectors.toMap(MessageBodyEncoder::encodingType, identity()));
		this.requestHelper = requestHelper;
	}

	/**
	 * 设置当前过滤器的执行顺序：本过滤器在全局过滤器中的顺序为倒数第1个，在响应给用户之前给响应参数加上签名
	 * @return
	 */
	@Override
	public int getOrder() {
		return FilterOrder.RESPONSE_MODIFY_FILTER;
	}

	@Override
	public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
		//把输出响应内容的类替换成自定义的，等到有响应内容返回的时候，就会进入到我们自定义的类中的方法，然后就可以在自定义的类里面修改响应体内容了
		//这也解释了为什么本过滤器在 NettyRoutingFilter 转发到后端服务之前就执行了，但依然可以修改到响应体的内容的原因
		ModifiedServerHttpResponse response = new ModifiedServerHttpResponse(exchange, this.getClass().getSimpleName());
		return chain.filter(exchange.mutate().response(response).build());
	}

	@Override
	protected Mono<Void> textBodyFilter(ServerWebExchange exchange, GatewayFilterChain chain) {
		return null;
	}

	@Override
	protected Mono<Void> fileBodyFilter(ServerWebExchange exchange, GatewayFilterChain chain) {
		return null;
	}

	/**
	 * 响应体输出的逻辑
	 */
	protected class ModifiedServerHttpResponse extends ServerHttpResponseDecorator {
		private final Class<byte[]> inClass = byte[].class;
		private final Class<byte[]> outClass = byte[].class;
		private final ServerWebExchange exchange;
		private final String filterName;

		public ModifiedServerHttpResponse(ServerWebExchange exchange, String filterName) {
			super(exchange.getResponse());
			this.exchange = exchange;
			this.filterName = filterName;
		}

		@Override
		public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {
			HttpHeaders oriHeaders = exchange.getResponse().getHeaders();
			final boolean isRespOriBody = Boolean.parseBoolean(oriHeaders.getFirst(HttpHeaderKey.RESP_ORIGINAL_BODY_KEY));
			final boolean isBackendError = !exchange.getResponse().getStatusCode().is2xxSuccessful();//后端服务是否发生了错误

			//重新封装一次响应体，目的是为了读取到响应体内容，然后对响应体进行转换
			ClientResponse clientResponse = prepareClientResponse(body, oriHeaders);
			Mono<byte[]> modifiedBody = clientResponse.bodyToMono(inClass).flatMap(originalBody -> {
				if (isBackendError) {
					originalBody = null; //后端服务异常，直接置null，避免把后端的异常堆栈信息暴露到客户端
				} else if(isRespOriBody){ //把后端服务的内容原样返回
					oriHeaders.remove(HttpHeaderKey.RESP_ORIGINAL_BODY_KEY);
					return Mono.just(originalBody);
				}

				ResponseInfo responseInfo = buildResponseInfo(exchange, originalBody);
				String signature = responseInfo.getSignature();
				byte[] newBody = responseInfo.getBody();
				long bodyLength = newBody.length;

				oriHeaders.clear();
				oriHeaders.set(HttpHeaderKey.SIGNATURE_HEADER, signature);
				oriHeaders.setContentType(MediaType.APPLICATION_JSON_UTF8);
				oriHeaders.setContentLength(bodyLength);
				return Mono.just(newBody);
			});

			BodyInserter bodyInserter = BodyInserters.fromPublisher(modifiedBody, outClass);
			HttpHeaders tempHeaders = new HttpHeaders();
			tempHeaders.putAll(oriHeaders);
			CachedBodyOutputMessage outputMessage = new CachedBodyOutputMessage(exchange, tempHeaders);
			return bodyInserter.insert(outputMessage, new BodyInserterContext()).then(Mono.defer(() -> {
						Mono<DataBuffer> messageBody = writeBody(getDelegate(), outputMessage, outClass);
						HttpHeaders headers = this.getDelegate().getHeaders();
						if (!headers.containsKey(HttpHeaders.TRANSFER_ENCODING)
								&& !headers.containsKey(HttpHeaders.CONTENT_LENGTH)) {
							messageBody = messageBody.doOnNext(data -> headers.setContentLength(data.readableByteCount()));
						}

						TraceUtil.timeTraceLog(exchange, filterName); //记录日志
						return this.getDelegate().writeWith(messageBody);
					})
			);
		}

		@Override
		public Mono<Void> writeAndFlushWith(Publisher<? extends Publisher<? extends DataBuffer>> body) {
			return writeWith(Flux.from(body).flatMapSequential(p -> p));
		}

		private ClientResponse prepareClientResponse(Publisher<? extends DataBuffer> body, HttpHeaders httpHeaders) {
			ClientResponse.Builder builder;
			builder = ClientResponse.create(exchange.getResponse().getStatusCode(), messageReaders);
			return builder.headers(headers -> headers.putAll(httpHeaders))
					.body(Flux.from(body))
					.build();
		}

		/**
		 * 对响应体进行编码
		 * @param httpResponse
		 * @param message
		 * @param outClass
		 * @return
		 */
		private Mono<DataBuffer> writeBody(ServerHttpResponse httpResponse,
										   CachedBodyOutputMessage message, Class<?> outClass) {
			Mono<DataBuffer> response = DataBufferUtils.join(message.getBody());
			if (this.outClass.isAssignableFrom(outClass)) {
				return response;
			}

			List<String> encodingHeaders = httpResponse.getHeaders().getOrEmpty(HttpHeaders.CONTENT_ENCODING);
			for (String encoding : encodingHeaders) {
				MessageBodyEncoder encoder = messageBodyEncoders.get(encoding);
				if (encoder != null) {
					DataBufferFactory dataBufferFactory = httpResponse.bufferFactory();
					response = response.publishOn(Schedulers.parallel()).map(buffer -> {
						byte[] encodedResponse = encoder.encode(buffer);
						DataBufferUtils.release(buffer);
						return encodedResponse;
					}).map(dataBufferFactory::wrap);
					break;
				}
			}

			return response;
		}

		private ResponseInfo buildResponseInfo(ServerWebExchange exchange, byte[] originalBody) {
			String mchNo = "", signType = "", version = "";
			if(isTextRequestBody(exchange)){
				RequestParam requestParam = getRequestParam(exchange);
				mchNo = requestParam.getMchNo();
				signType = requestParam.getSignType();
				version = requestParam.getVersion();
			}else if(isFileRequestBody(exchange)){
				FileUploadParam uploadParam = getFileUploadParam(exchange);
				mchNo = uploadParam.getMchNo();
				signType = uploadParam.getSignType();
				version = uploadParam.getVersion();
			}

			//初始化ResponseParam
			ResponseParam responseParam = null;
			try {
				if (originalBody != null && originalBody.length > 0) {
					responseParam = JsonUtil.toBean(originalBody, ResponseParam.class);
				}
			} catch(Exception e) {
				logger.error("响应信息转换异常 originalResponseBody: {}", new String(originalBody, StandardCharsets.UTF_8), e);
			}
			if(responseParam == null){
				responseParam = new ResponseParam();
			}
			if(StringUtil.isEmpty(responseParam.getMchNo())){
				responseParam.setMchNo(mchNo);
			}
			if(StringUtil.isEmpty(responseParam.getSignType())){
				responseParam.setSignType(signType);
			}
			responseParam.unknownIfEmpty();

			ResponseInfo responseInfo = new ResponseInfo();
			try {
				String secKey = responseParam.getSecKey();
				APIParam apiParam = new APIParam(signType, version);
				//如果secKey不为空，则进行加密
				secKey = requestHelper.secKeyEncrypt(secKey, mchNo, apiParam);
				responseParam.setSecKey(secKey);
				//生成签名
				byte[] respBodyByte = responseParam.toResponseBody().getBytes(StandardCharsets.UTF_8);
				String signature = requestHelper.genSignature(respBodyByte, responseParam.getMchNo(), apiParam);
				responseInfo.setBody(respBodyByte);
				responseInfo.setSignature(signature);
			} catch (Throwable e) {
				logger.error("给响应信息添加签名时异常 ResponseParam = {}", responseParam.toString(), e);
			}
			return responseInfo;
		}
	}

	private static class ResponseInfo {
		private byte[] body;
		private String signature;

		public byte[] getBody() {
			return body;
		}

		public void setBody(byte[] body) {
			this.body = body;
		}

		public String getSignature() {
			return signature;
		}

		public void setSignature(String signature) {
			this.signature = signature;
		}
	}
}
