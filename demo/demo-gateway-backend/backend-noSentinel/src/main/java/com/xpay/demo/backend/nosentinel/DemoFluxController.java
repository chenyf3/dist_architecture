//package com.xpay.demo.backend.nosentinel;
//
//import com.xpay.common.api.dto.RequestDto;
//import com.xpay.common.api.dto.ResponseDto;
//import com.xpay.common.statics.enums.common.OrderStatusEnum;
//import com.xpay.common.utils.AESUtil;
//import com.xpay.common.utils.JsonUtil;
//import com.xpay.common.utils.RandomUtil;
//import com.xpay.common.utils.StringUtil;
//import com.xpay.demo.backend.nosentinel.vo.BatchRespVo;
//import com.xpay.demo.backend.nosentinel.vo.BatchVo;
//import com.xpay.demo.backend.nosentinel.vo.SingleRespVo;
//import com.xpay.demo.backend.nosentinel.vo.SingleVo;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.web.bind.annotation.RequestBody;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//import reactor.core.publisher.Mono;
//
//@RestController
//@RequestMapping("/demo")
//public class DemoFluxController {
//    Logger logger = LoggerFactory.getLogger(this.getClass());
//
////    @RequestMapping(value = "single", method = RequestMethod.GET)//测试全局异常处理器
//    @RequestMapping(value = "single")
//    public Mono<ResponseDto<SingleRespVo>> single(@RequestBody RequestDto<SingleVo> request){
//        logger.info("RequestDto = {}", JsonUtil.toJson(request));
//
//        if(true){
////            throw BizException.bizFailResp("", "测试controller中抛出的Biz异常");
//            throw new RuntimeException("测试controller中抛出系统异常");
//        }
//
//        ResponseDto<SingleRespVo> resp = singleProcess(request);
//        return Mono.just(resp);
//    }
//
//    @RequestMapping(value = "singleForm")
//    public Mono<ResponseDto<SingleRespVo>> singleForm(@RequestBody RequestDto<String> request){
//        logger.info("RequestDto = {}", JsonUtil.toJson(request));
//        ResponseDto<SingleRespVo> resp = singleProcess(request);
//        return Mono.just(resp);
//    }
//
//    private ResponseDto<SingleRespVo> singleProcess(RequestDto request){
//        String secKey = null, iv = null, msg = null;
//        SingleVo singleVo;
//        if(request.getData() instanceof String){
//            msg = "接收到FORM请求";
//            singleVo = JsonUtil.toBean((String) request.getData(), SingleVo.class);
//        }else{
//            msg = "接收到JSON请求";
//            singleVo = (SingleVo) request.getData();
//        }
//
//        SingleRespVo respVo = new SingleRespVo();
//        respVo.setOrderStatus(OrderStatusEnum.SUCCESS.getValue());
//        respVo.setCount(singleVo.getCount());
//        respVo.setProductAmount(singleVo.getProductAmount());
//        if(StringUtil.isNotEmpty(request.getSecKey())){
//            String[] secKeyArr = request.splitSecKey();
//            secKey = secKeyArr[0];
//            iv = secKeyArr[1];
//            respVo.setProductName(msg + ", 响应名称：" + AESUtil.decryptCBC(singleVo.getProductName(), secKey, iv));//解密
//        }else{
//            respVo.setProductName(msg + ", 响应名称：" + singleVo.getProductName());
//        }
//
//        secKey = RandomUtil.get16LenStr();
//        iv = RandomUtil.get16LenStr();
//        respVo.setProductName(AESUtil.encryptCBC(respVo.getProductName(), secKey, iv));//加密
//
//        ResponseDto<SingleRespVo> resp = ResponseDto.success(request.getMchNo(), request.getSignType(), respVo);
//        resp.setSecKey(secKey + ":" + iv);
//        return resp;
//    }
//}
