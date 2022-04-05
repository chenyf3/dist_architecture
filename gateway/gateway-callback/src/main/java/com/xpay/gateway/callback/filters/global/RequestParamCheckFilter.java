package com.xpay.gateway.callback.filters.global;

import com.xpay.common.utils.StringUtil;
import com.xpay.gateway.callback.config.GatewayProperties;
import com.xpay.gateway.callback.conts.FilterOrder;
import com.xpay.gateway.callback.enums.RespCodeEnum;
import com.xpay.gateway.callback.exceptions.GatewayException;
import com.xpay.gateway.callback.params.RequestParam;
import com.xpay.gateway.callback.util.TraceUtil;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Arrays;

/**
 * @description 请求参数校验，这个过滤器必须是在第2个，不然，后续的过滤器可能会会获取到错误的参数，或者因为某个参数为null而报空指针
 * @author chenyf
 * @date 2019-02-23
 */
public class RequestParamCheckFilter extends AbstractGlobalFilter {
    private GatewayProperties properties;

    public RequestParamCheckFilter(GatewayProperties properties){
        this.properties = properties;
    }

    /**
     * 设置当前过滤器的执行顺序：本过滤器在全局过滤器中的顺序必须为第2个，不然，后续的过滤器拿取参数时可能会出现空指针异常
     * @return
     */
    @Override
    public int getOrder() {
        return FilterOrder.REQUEST_PARAM_CHECK_FILTER;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        RequestParam requestParam = getRequestParam(exchange);
        String path = exchange.getRequest().getURI().getPath();
        String msg = requestPathValid(path);
        if(StringUtil.isEmpty(msg)){
            msg = paramValid(requestParam, path);
        }

        if(StringUtil.isEmpty(msg)){
            TraceUtil.timeTraceLog(exchange, this.getClass().getSimpleName());
            return chain.filter(exchange);
        }else{
            //抛出异常，由全局异常处理器来处理响应信息
            throw GatewayException.fail(requestParam.getCompany(), path, RespCodeEnum.PARAM_FAIL.getValue(), msg);
        }
    }

    public String requestPathValid(String requestPath){
        if(StringUtil.isEmpty(requestPath) || "/".equals(requestPath.trim())){
            return "请求路径不能为空";
        }else{
            return "";
        }
    }

    public String paramValid(RequestParam requestParam, String path){
        if(requestParam == null){
            return "请求体为空！";
        }else if(requestParam.getCompany() == null){
            return "无法确定回调方！";
        }else if(StringUtil.isEmpty(requestParam.getPath())){
            return "回调路径为空！";
        } else if (requestParam.getBody() == null || requestParam.getBody().length == 0) {
            return "回调数据为空！";
        }

        String[] pathArr = path.split(PATH_SEPARATOR);
        if(pathArr == null || pathArr.length < 3) {
            return "回调路径至少应包含一个子路径！";
        }

        String firstPath = getFirstPath(path);
        String allowMethods = properties.getPathConf().get(firstPath) == null ? "" : properties.getPathConf().get(firstPath).getAllowMethods();
        if(StringUtil.isEmpty(allowMethods)){
            return "当前路径不允许访问";
        }else{
            String subPath = excludeFirstPath(path);
            String[] methodArr = allowMethods.split(",");
            if(! Arrays.asList(methodArr).contains(subPath)){
                return "非法的回调路径";
            }
        }
        return "";
    }
}
