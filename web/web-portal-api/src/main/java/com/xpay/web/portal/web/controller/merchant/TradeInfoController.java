package com.xpay.web.portal.web.controller.merchant;

import com.xpay.common.statics.result.RestResult;
import com.xpay.common.utils.DateUtil;
import com.xpay.common.utils.RandomUtil;
import com.xpay.web.api.common.annotations.CurrentUser;
import com.xpay.web.api.common.model.UserModel;
import com.xpay.web.portal.web.controller.BaseController;
import com.xpay.web.portal.web.vo.merchant.TradeDataReqVo;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

/**
 * 交易信息控制器
 */
@RestController
@RequestMapping("/tradeInfo")
public class TradeInfoController extends BaseController {
    /**
     * 交易数据统计
     * @return
     */
    @RequestMapping("/getTradeDataStatics")
    public RestResult<List<Map<String, Object>>> getTradeDataStatics(@RequestBody TradeDataReqVo reqVo, @CurrentUser UserModel userModel){
        String startDay = reqVo.getStartDay();
        String endDay = reqVo.getEndDay();
        String mchNo = userModel.getMchNo();


        //以下是模拟出来的交易数据
        Date nowDate = new Date();
        Date monthStartDay = DateUtil.getMonthStartDay(nowDate);
        Date startDate = null, endDate = null;
        if(DateUtil.compare(DateUtil.convertDate(startDay), monthStartDay, 5) >= 0){//当月
            startDate = DateUtil.getMonthStartDay(nowDate);
            endDate = nowDate;
        }else{//上月
            Date lastMonthDate = DateUtil.addMonth(nowDate, -1);
            startDate = DateUtil.getMonthStartDay(lastMonthDate);
            endDate = DateUtil.getMonthEndDay(lastMonthDate);
        }

        List<Map<String, Object>> list = new ArrayList<>();
        while (DateUtil.compare(startDate, endDate, 5) <= 0){
            Map<String, Object> map = new HashMap<>();
            map.put("amount", RandomUtil.getInt(80, 3000000));
            map.put("num", RandomUtil.getInt(2, 1000));
            map.put("tradeDate", DateUtil.formatDate(startDate));
            list.add(map);

            startDate = DateUtil.addDay(startDate, 1);
        }

        return RestResult.success(list);
    }
}
