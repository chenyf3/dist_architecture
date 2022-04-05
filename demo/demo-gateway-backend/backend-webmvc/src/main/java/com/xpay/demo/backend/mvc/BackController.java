package com.xpay.demo.backend.mvc;

import com.xpay.common.api.dto.CallbackDto;
import com.xpay.common.statics.constants.mqdest.TopicDest;
import com.xpay.common.statics.constants.mqdest.TopicGroup;
import com.xpay.common.statics.enums.product.ProductCodeEnum;
import com.xpay.common.statics.enums.product.ProductTypeEnum;
import com.xpay.common.utils.RandomUtil;
import com.xpay.starter.plugin.plugins.MQSender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/demo")
public class BackController {
    @Autowired
    MQSender mqSender;

    /**
     * 模拟发送一个回调通知
     * @param mchNo
     * @param callbackUrl
     * @return
     */
    @RequestMapping(value = "callback")
    public String callback(String mchNo, String callbackUrl){
        CallbackDto callbackDto = new CallbackDto();
        callbackDto.setCallbackUrl(callbackUrl);
        callbackDto.setSignType("2");
        callbackDto.setMchNo(mchNo);
        callbackDto.setTrxNo(RandomUtil.get16LenStr());
        callbackDto.setMchTrxNo(RandomUtil.get16LenStr());
        callbackDto.setTopic(TopicDest.MERCHANT_NOTIFY);
        callbackDto.setTags(TopicGroup.COMMON_GROUP);
        callbackDto.setProductType(ProductTypeEnum.PAY_RECEIVE.getValue());
        callbackDto.setProductCode(ProductCodeEnum.RECEIVE_WECHAT_H5.getValue());

        Map<String, String> resp = new HashMap<>();
        int rand = RandomUtil.getInt(2);
        if(rand % 2 == 0){
            resp.put("status", "01");
            resp.put("desc", "交易成功了");
        }else{
            resp.put("status", "02");
            resp.put("desc", "交易失败");
        }

        callbackDto.setData(resp);

        mqSender.sendOne(callbackDto);

        return "ok";
    }
}
