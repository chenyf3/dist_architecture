package com.xpay.gateway.api.filters.factory;

import com.xpay.common.api.params.APIParam;
import com.xpay.gateway.api.service.ValidService;
import com.xpay.gateway.api.utils.IPUtil;
import com.xpay.gateway.api.exceptions.GatewayException;
import com.xpay.gateway.api.helper.RequestHelper;
import com.xpay.common.statics.enums.common.ApiRespCodeEnum;
import com.xpay.common.utils.StringUtil;
import com.xpay.gateway.api.config.conts.GatewayErrorCode;
import com.xpay.gateway.api.config.conts.ReqCacheKey;
import com.xpay.gateway.api.params.RequestParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.http.server.reactive.ServerHttpRequest;

import java.util.Arrays;
import java.util.List;

/**
 * 已弃用，请改用 {@link com.xpay.gateway.api.filters.global.RequestIpFilter}
 *
 * IP校验过滤器(本过滤器不是全局过滤器，如需使用，需要在配置文件中配置)，本过滤器设计的初衷是为了符合部分业务需要验证来源IP，部分业务不需要校验
 * 来源IP的的情况，但是细想一下，此场景应该是不存在，理论上应该所有请求都需要校验来源IP才能保障到安全性
 * @author chenyf
 */
@Deprecated
public class IPValidGatewayFilterFactory extends AbstractGatewayFilterFactory<IPValidGatewayFilterFactory.Config> {
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	public static final String IP_VALID_KEY = "ipValidKey";

	@Autowired
	private RequestHelper requestHelper;
	@Autowired
	private ValidService validService;

	public IPValidGatewayFilterFactory() {
		super(Config.class);
	}

	@Override
	public List<String> shortcutFieldOrder() {
		return Arrays.asList(IP_VALID_KEY);
	}

	public GatewayFilter apply(Config config) {
		return (exchange, chain) -> {
			ServerHttpRequest request = exchange.getRequest();
			String ip = IPUtil.getXRealIpAddress(request);
			RequestParam requestParam = (RequestParam) exchange.getAttributes().get(ReqCacheKey.CACHE_REQUEST_BODY_OBJECT_KEY);

			boolean isVerifyOk = false;
			String mchNo = requestParam.getMchNo();
			String msg = "";
			String expectIp = "";
			String ipValidKey = config.getIpValidKey();
			if(StringUtil.isEmpty(ipValidKey)){
				isVerifyOk = false;
				msg = "Key为空，无法校验请求来源";
			}else{
				try{
					isVerifyOk = requestHelper.ipVerify(mchNo, ip, ipValidKey, new APIParam(requestParam.getSignType(), requestParam.getVersion()));
					if(! isVerifyOk){
						msg = "非法来源";
					}
				}catch (Throwable e){
					msg = "请求来源校验异常";
					isVerifyOk = false;
					logger.error("IP校验失败异常 RequestParam = {}", requestParam.toString(), e);
				}
			}

			if(! isVerifyOk){
				try{
					Route route = exchange.getAttribute(ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR);
					validService.afterIpValidFail(route.getId(), ip, expectIp, requestParam);
				}catch(Throwable e){
					logger.error("IP校验失败，IP校验失败处理器有异常 RequestParam = {}", requestParam.toString(), e);
				}
			}

			if(isVerifyOk){
				return chain.filter(exchange);
			}else{
				throw GatewayException.fail(ApiRespCodeEnum.SYS_FORBID.getValue(), msg, GatewayErrorCode.IP_VALID_ERROR);
			}
		};
	}

	public static class Config {
		private String ipValidKey;

		public String getIpValidKey() {
			return ipValidKey;
		}

		public void setIpValidKey(String ipValidKey) {
			this.ipValidKey = ipValidKey;
		}
	}
}
