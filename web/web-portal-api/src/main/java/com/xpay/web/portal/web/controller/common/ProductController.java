package com.xpay.web.portal.web.controller.common;

import com.xpay.common.statics.result.RestResult;
import com.xpay.web.api.common.annotations.CurrentUser;
import com.xpay.web.api.common.model.UserModel;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * @description 用户产品开通查询接口
 * @author: zhouf
 * @date: 2020/03/05
 */
@RestController
@RequestMapping("/product")
public class ProductController {

    /**
     * 查询商户的产品开通
     * @param userModel
     * @return
     */
    @GetMapping("/getProductPermit")
    public RestResult<Map<String, Object>> getProductPermit(@CurrentUser UserModel userModel) {
        Map<String, Object> result = new HashMap<>();
        String mchNo = userModel.getMchNo();
        //TODO
        return RestResult.success(result);
    }
}