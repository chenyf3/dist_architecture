package com.xpay.gateway.api.filters.global;

import com.xpay.common.api.params.APIParam;
import com.xpay.gateway.api.params.FileUploadParam;
import com.xpay.gateway.api.service.ValidService;
import com.xpay.gateway.api.exceptions.GatewayException;
import com.xpay.gateway.api.helper.RequestHelper;
import com.xpay.gateway.api.config.conts.FilterOrder;
import com.xpay.common.statics.enums.common.ApiRespCodeEnum;
import com.xpay.common.utils.JsonUtil;
import com.xpay.common.utils.MD5Util;
import com.xpay.gateway.api.params.RequestParam;
import com.xpay.gateway.api.utils.TraceUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.web.server.ServerWebExchange;
import com.xpay.gateway.api.config.conts.GatewayErrorCode;
import reactor.core.publisher.Mono;

/**
 * @description 请求体鉴权校验，包括：签名校验、用户状态 等
 * @author chenyf
 * @date 2019-02-23
 */
public class RequestAuthFilter extends AbstractGlobalFilter {
    private final static String HASH_SEPARATOR = ",";
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private final RequestHelper requestHelper;
    private final ValidService validService;

    public RequestAuthFilter(RequestHelper requestHelper, ValidService validService){
        this.requestHelper = requestHelper;
        this.validService = validService;
    }

    /**
     * 设置当前过滤器的执行顺序：本过滤器在全局过滤器中的顺序建议为第3个，因为，如果鉴权不通过，就没有必要进行后续的过滤器处理了
     * @return
     */
    @Override
    public int getOrder() {
        return FilterOrder.REQUEST_AUTH_FILTER;
    }

    @Override
    protected Mono<Void> textBodyFilter(ServerWebExchange exchange, GatewayFilterChain chain) {
        boolean isVerifyOk = false;//默认为false，勿改此默认值
        RequestParam requestParam = getRequestParam(exchange);
        APIParam apiParam = new APIParam(requestParam.getSignType(), requestParam.getVersion());
        Throwable cause = null;
        String errMsg = null;

        //1.签名校验
        try {
            isVerifyOk = requestHelper.signVerify(requestParam, apiParam);
            if(!isVerifyOk){
                errMsg = "验签失败";
            }
        } catch (Throwable e) {
            cause = e;
            errMsg = "验签异常";
            logger.error("{} RequestParam = {}", errMsg, requestParam.toString(), e);
        }

        //2.用户状态校验
        if (isVerifyOk) {
            try {
                isVerifyOk = requestHelper.mchVerify(requestParam.getMchNo(), apiParam);
                if(!isVerifyOk){
                    errMsg = "商户状态不可用";
                }
            } catch (Throwable e) {
                cause = e;
                errMsg = "商户状态不可用";
                logger.error("{} RequestParam = {}", errMsg, requestParam.toString(), e);
            }
        }

        //3.如果校验失败，则进行日志打印、邮件通知等处理
        if (! isVerifyOk) {
            try {
                String ip = getRequestRealIp(exchange);
                Route route = exchange.getAttribute(ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR);
                validService.afterSignValidFail(route.getId(), ip, requestParam, cause);
            } catch(Throwable e) {
                logger.error("验签失败，验签失败后处理器有异常 RequestParam = {}", requestParam.toString(), e);
            }
        }

        if (isVerifyOk) {
            TraceUtil.timeTraceLog(exchange, this.getClass().getSimpleName());
            return chain.filter(exchange);
        } else {
            //抛出异常，由全局异常处理器来处理响应信息
            throw GatewayException.fail(ApiRespCodeEnum.SIGN_FAIL.getValue(), errMsg, GatewayErrorCode.SIGN_VALID_ERROR);
        }
    }

    @Override
    protected Mono<Void> fileBodyFilter(ServerWebExchange exchange, GatewayFilterChain chain) {
        boolean isVerifyOk = false;//默认为false，勿改此默认值
        String ip = getRequestRealIp(exchange);
        FileUploadParam uploadParam = getFileUploadParam(exchange);
        APIParam apiParam = new APIParam(uploadParam.getSignType(), uploadParam.getVersion());
        Throwable cause = null;
        String errMsg = null;

        //1.签名校验
        try {
            isVerifyOk = requestHelper.signVerify(uploadParam, apiParam);
            if(!isVerifyOk){
                errMsg = "验签失败";
            }
        } catch (Throwable e) {
            cause = e;
            errMsg = "验签异常";
            logger.error("{} FileUploadParam = {}", errMsg, uploadParam.toString(), e);
        }

        //2.文件hash值校验
        if (isVerifyOk) {
            String hash = uploadParam.getHash();
            String[] hashArr = hash.split(HASH_SEPARATOR);
            int fileSize = uploadParam.getFiles() == null ? 0 : uploadParam.getFiles().size();
            if (hashArr.length != fileSize) {
                isVerifyOk = false;
                errMsg = "hash个数("+ hashArr.length +")与文件个数("+ fileSize +")不匹配";
                logger.error(errMsg);
            } else {
                for (int i=0; i<fileSize; i++) {
                    try {
                        FileUploadParam.FileInfo file = uploadParam.getFiles().get(i);
                        String fileMd5 = MD5Util.getMD5Hex(file.getData());
                        isVerifyOk = fileMd5.equalsIgnoreCase(hashArr[i]);//hash值校验，不区分大小写
                        if(!isVerifyOk){
                            errMsg = "第" + (i+1) + "个文件的md5值和hash参数中的值不匹配";
                            break;
                        }
                    } catch (Throwable e) {
                        cause = e;
                        errMsg = "文件md5值校验异常";
                        logger.error("{} FileUploadParam = {}", errMsg, uploadParam.toString(), e);
                    }
                }
            }
        }

        //2.商户状态校验
        if (isVerifyOk) {
            try {
                isVerifyOk = requestHelper.mchVerify(uploadParam.getMchNo(), apiParam);
                if(!isVerifyOk){
                    errMsg = "商户状态不可用";
                }
            } catch (Throwable e) {
                cause = e;
                errMsg = "商户状态校验异常";
                logger.error("{} FileUploadParam = {}", errMsg, uploadParam.toString(), e);
            }
        }

        //3.如果校验失败，则进行日志打印、邮件通知等处理
        if (! isVerifyOk) {
            try {
                Route route = exchange.getAttribute(ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR);
                validService.afterSignValidFail(route.getId(), ip, uploadParam, cause);
            } catch(Throwable e) {
                logger.error("鉴权失败后的处理有异常 FileUploadParam = {}", uploadParam.toString(), e);
            }
        }

        if (isVerifyOk) {
            return chain.filter(exchange);
        } else {
            //抛出异常，由全局异常处理器来处理响应信息
            throw GatewayException.fail(ApiRespCodeEnum.SIGN_FAIL.getValue(), errMsg, GatewayErrorCode.SIGN_VALID_ERROR);
        }
    }
}
