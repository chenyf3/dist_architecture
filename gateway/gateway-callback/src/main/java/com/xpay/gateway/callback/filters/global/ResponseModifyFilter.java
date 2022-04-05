package com.xpay.gateway.callback.filters.global;

import com.xpay.common.utils.JsonUtil;
import com.xpay.gateway.callback.conts.FilterOrder;
import com.xpay.gateway.callback.enums.CompanyEnum;
import com.xpay.gateway.callback.helper.CompanyHelper;
import com.xpay.gateway.callback.params.RequestParam;
import com.xpay.gateway.callback.params.ResponseParam;
import com.xpay.gateway.callback.service.CompanyService;
import com.xpay.gateway.callback.util.TraceUtil;
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
import org.springframework.http.HttpStatus;
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
	private CompanyHelper companyHelper;

	public ResponseModifyFilter(ServerCodecConfigurer codecConfigurer,
								Set<MessageBodyEncoder> messageBodyEncoders,
								Set<MessageBodyDecoder> messageBodyDecoders,
								CompanyHelper companyHelper){
		this.messageReaders = codecConfigurer.getReaders();
		this.messageBodyDecoders = messageBodyDecoders.stream().collect(Collectors.toMap(MessageBodyDecoder::encodingType, identity()));
		this.messageBodyEncoders = messageBodyEncoders.stream().collect(Collectors.toMap(MessageBodyEncoder::encodingType, identity()));
		this.companyHelper = companyHelper;
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
			HttpStatus httpStatus = exchange.getResponse().getStatusCode();//后端服务的响应状态

			//重新封装一次响应体，目的是为了读取到响应体内容，然后对响应体进行转换
			ClientResponse clientResponse = prepareClientResponse(body, oriHeaders);
			Mono<byte[]> modifiedBody = clientResponse.bodyToMono(inClass).flatMap(originalBody -> {
				ResponseParam response = buildResponseParam(exchange, httpStatus);
				String contentType = response.getContentType();
				byte[] newBody = response.getBody();
				long bodyLength = response.getContentLength();

				setStatusCode(HttpStatus.valueOf(response.getHttpStatus()));//设置http响应状态

				oriHeaders.clear();
				oriHeaders.setContentType(MediaType.valueOf(contentType));
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

		private ResponseParam buildResponseParam(ServerWebExchange exchange, HttpStatus httpStatus) {
			ResponseParam response = new ResponseParam();
			try {
				RequestParam requestParam = getRequestParam(exchange);
				CompanyEnum company = requestParam.getCompany();
				CompanyService companyService = companyHelper.getCompanyService(company);
				boolean isSuccess = httpStatus.is2xxSuccessful();

				String contentType = companyService.getResponseContentType();
				Map<String, Object> respMap = companyService.buildResponse(isSuccess);
				byte[] respBodyByte = JsonUtil.toJson(respMap).getBytes(StandardCharsets.UTF_8);
				response.setHttpStatus(httpStatus.value());
				response.setContentType(contentType);
				response.setContentLength(respBodyByte.length);
				response.setBody(respBodyByte);
			} catch (Throwable e) {
				logger.error("生成响应信息时异常 response = {}", JsonUtil.toJson(response), e);
			}
			return response;
		}
	}
}
