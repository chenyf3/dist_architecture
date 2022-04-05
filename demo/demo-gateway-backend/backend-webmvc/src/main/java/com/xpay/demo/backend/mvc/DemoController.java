package com.xpay.demo.backend.mvc;

import com.xpay.common.api.dto.RequestDto;
import com.xpay.common.api.dto.ResponseDto;
import com.xpay.common.statics.enums.common.OrderStatusEnum;
import com.xpay.common.utils.AESUtil;
import com.xpay.common.utils.JsonUtil;
import com.xpay.common.utils.RandomUtil;
import com.xpay.common.utils.StringUtil;
import com.xpay.demo.backend.mvc.vo.BatchRespVo;
import com.xpay.demo.backend.mvc.vo.BatchVo;
import com.xpay.demo.backend.mvc.vo.SingleRespVo;
import com.xpay.demo.backend.mvc.vo.SingleVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/demo")
public class DemoController {
    Logger logger = LoggerFactory.getLogger(this.getClass());

//    @RequestMapping(value = "single", method = RequestMethod.GET)//测试全局异常处理器
    @RequestMapping(value = "single")
    public ResponseDto<SingleRespVo> single(@RequestBody RequestDto<SingleVo> request){
        logger.info("RequestDto = {}", JsonUtil.toJson(request));

        if(true){
//            throw BizException.bizFailResp("", "测试controller中抛出的Biz异常");
//            throw new RuntimeException("测试controller中抛出系统异常");
        }

        ResponseDto<SingleRespVo> resp = singleProcess(request);
        return resp;
    }

    @RequestMapping(value = "singleForm")
    public ResponseDto<SingleRespVo> singleForm(@RequestBody RequestDto<String> request){
        logger.info("RequestDto = {}", JsonUtil.toJson(request));
        ResponseDto<SingleRespVo> resp = singleProcess(request);
        return resp;
    }

    private ResponseDto<SingleRespVo> singleProcess(RequestDto request){
        String secKey = null, iv = null, msg = null;
        SingleVo singleVo;
        if(request.getData() instanceof String){
            msg = "接收到FORM请求";
            singleVo = JsonUtil.toBean((String) request.getData(), SingleVo.class);
        }else{
            msg = "接收到JSON请求";
            singleVo = (SingleVo) request.getData();
        }

        SingleRespVo respVo = new SingleRespVo();
        respVo.setOrderStatus(OrderStatusEnum.SUCCESS.getValue());
        respVo.setCount(singleVo.getCount());
        respVo.setProductAmount(singleVo.getProductAmount());
        if(StringUtil.isNotEmpty(request.getSecKey())){
            String[] secKeyArr = request.splitSecKey();
            secKey = secKeyArr[0];
            iv = secKeyArr[1];
            respVo.setProductName(msg + ", 响应名称：" + AESUtil.decryptCBC(singleVo.getProductName(), secKey, iv));//解密
        }else{
            respVo.setProductName(msg + ", 响应名称：" + singleVo.getProductName());
        }

        secKey = RandomUtil.get16LenStr();
        iv = RandomUtil.get16LenStr();
        respVo.setProductName(AESUtil.encryptCBC(respVo.getProductName(), secKey, iv));//加密

        ResponseDto<SingleRespVo> resp = ResponseDto.success(request.getMchNo(), request.getSignType(), respVo);
        resp.setSecKey(secKey + ":" + iv);
        return resp;
    }

    @RequestMapping("batch")
    public ResponseDto<BatchRespVo> batch(@RequestBody RequestDto<BatchVo> request){
        logger.info("detailList.size = {}, RequestDto = {} ", request.getData().getDetailList().size(), JsonUtil.toJson(request));

        int index = 0;
        List<SingleRespVo> singleRespVoList = new ArrayList<>();

        //下面这个测试需要开启ServletRequestFilter，即把 api.servlet-wrapper-filter.enabled 设置为true
//        logger.info("测试多次读取1 RequestBody ：{}", RequestUtil.readBodyStr(servletRequest));
//        logger.info("测试多次读取2 RequestBody ：{}", RequestUtil.readBodyStr(servletRequest));
//        logger.info("测试多次读取3 RequestBody ：{}", RequestUtil.readBodyStr(servletRequest));

        String[] secKeyArr = request.splitSecKey();
        String secKey = secKeyArr[0];
        String iv = secKeyArr[1];
        String newSecKey = RandomUtil.get16LenStr();
        String newIv = RandomUtil.get16LenStr();

        for(SingleVo singleVo : request.getData().getDetailList()){
            SingleRespVo singleRespVo = new SingleRespVo();
            String nameDec = AESUtil.decryptCBC(singleVo.getProductName(), secKey, iv);//解密

            if(index%3 == 0){
                singleRespVo.setOrderStatus(OrderStatusEnum.SUCCESS.getValue());
                nameDec = "受理成功名称：" + nameDec;
                singleRespVo.setProductName(AESUtil.encryptCBC(nameDec, newSecKey, newIv));//追加内容之后重新加密
            }else if(index%3 == 1){
                singleRespVo.setOrderStatus(OrderStatusEnum.PENDING.getValue());
                nameDec = "受理中名称：" + nameDec;
                singleRespVo.setProductName(AESUtil.encryptCBC(nameDec, newSecKey, newIv));//追加内容之后重新加密
            }else{
                singleRespVo.setOrderStatus(OrderStatusEnum.FAIL.getValue());
                nameDec = "受理失败名称：" + nameDec;
                singleRespVo.setProductName(AESUtil.encryptCBC(nameDec, newSecKey, newIv));//追加内容之后重新加密
            }
            singleRespVo.setCount(singleVo.getCount());
            singleRespVo.setProductAmount(singleVo.getProductAmount());
            singleRespVoList.add(singleRespVo);
            index++ ;
        }

        BatchRespVo batchRespVo = new BatchRespVo();
        batchRespVo.setOrderStatus(OrderStatusEnum.SUCCESS.getValue());
        batchRespVo.setTotalCount(String.valueOf(singleRespVoList.size()));
        batchRespVo.setSingleList(singleRespVoList);

        ResponseDto responseDto = ResponseDto.success(request.getMchNo(), request.getSignType(), batchRespVo);
        responseDto.joinSecKey(newSecKey, newIv);
        return responseDto;
    }
}
