package com.xpay.demo.backend.flux;

import com.xpay.common.api.dto.RequestDto;
import com.xpay.common.api.dto.ResponseDto;
import com.xpay.common.utils.JsonUtil;
import com.xpay.demo.backend.flux.vo.SingleRespVo;
import com.xpay.demo.backend.flux.vo.SingleVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/demo")
public class FlowController {
    Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * 在sentinel-dashboard增加流控规则
     * 资源名：/demo/flowLimit
     * 针对来源：default
     * 阈值类型：qps
     * 集群阈值：2
     * 是否集群：是
     * 集群阈值模式：总体阈值
     * 失败退化：是
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "flowLimit")
    public Mono<ResponseDto<SingleRespVo>> flowLimit(@RequestBody RequestDto<SingleVo> request) {
        logger.info("RequestDto = {}", JsonUtil.toJson(request));

        SingleRespVo respVo = new SingleRespVo();
        respVo.setProductName("没有发生流控，正常返回");

        ResponseDto<SingleRespVo> resp = ResponseDto.success(request.getMchNo(), request.getSignType(), respVo);
        return Mono.just(resp);
    }
}
