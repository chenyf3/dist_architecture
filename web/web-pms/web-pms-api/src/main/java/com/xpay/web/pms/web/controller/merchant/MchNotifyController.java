package com.xpay.web.pms.web.controller.merchant;

import com.xpay.common.statics.annotations.Permission;
import com.xpay.common.statics.exception.BizException;
import com.xpay.common.statics.query.PageQuery;
import com.xpay.common.statics.result.PageResult;
import com.xpay.common.statics.result.RestResult;
import com.xpay.common.utils.BeanUtil;
import com.xpay.common.utils.JsonUtil;
import com.xpay.common.utils.StringUtil;
import com.xpay.facade.mchnotify.dto.NotifyLogDto;
import com.xpay.facade.mchnotify.dto.NotifyRecordDto;
import com.xpay.facade.mchnotify.service.NotifyRecordFacade;
import com.xpay.web.api.common.annotations.CurrentUser;
import com.xpay.web.api.common.model.UserModel;
import com.xpay.web.pms.web.controller.BaseController;
import com.xpay.web.pms.web.vo.merchant.MchNotifyQueryVo;
import jakarta.validation.Valid;
import org.apache.dubbo.config.annotation.DubboReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("mchNotify")
public class MchNotifyController extends BaseController {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    @DubboReference
    NotifyRecordFacade notifyRecordFacade;

    /**
     * 分页查询通知记录
     * @param queryVo
     * @return
     */
    @Permission("merchant:notify:list")
    @RequestMapping("listMchNotifyPage")
    public RestResult<PageResult<List<NotifyRecordDto>>> listMchNotifyPage(@RequestBody @Valid MchNotifyQueryVo queryVo){
        PageQuery pageQuery = PageQuery.newInstance(queryVo.getCurrentPage(), queryVo.getPageSize());
        Map<String, Object> map = BeanUtil.toMapNotNull(queryVo);
        PageResult<List<NotifyRecordDto>> pageResult = notifyRecordFacade.listPage(map, pageQuery);
        return RestResult.success(pageResult);
    }

    /**
     * 查询通知记录
     * @param recordId
     * @return
     */
    @Permission("merchant:notify:list")
    @RequestMapping("getNotifyRecord")
    public RestResult<NotifyRecordDto> getNotifyRecord(@RequestParam Long recordId){
        NotifyRecordDto record = notifyRecordFacade.getNotifyRecord(recordId);
        return RestResult.success(record);
    }

    /**
     * 通知日志
     * @param recordId
     * @return
     */
    @Permission("merchant:notify:list")
    @RequestMapping("notifyLog")
    public RestResult<List<NotifyLogDto>> notifyLog(@RequestParam Long recordId){
        NotifyRecordDto record = notifyRecordFacade.getNotifyRecord(recordId);
        List<NotifyLogDto> notifyLogs = JsonUtil.toList(record.getNotifyLogs(), NotifyLogDto.class);
        return RestResult.success(notifyLogs);
    }

    /**
     * 补发通知
     * @param recordId
     * @param userModel
     * @return
     */
    @Permission("merchant:notify:manage")
    @RequestMapping("notifyAgain")
    public RestResult<String> notifyAgain(@RequestParam Long recordId, @CurrentUser UserModel userModel){
        boolean isSuccess = notifyRecordFacade.notifyAgain(recordId, userModel.getLoginName());
        return isSuccess ? RestResult.success("补发通知成功") :  RestResult.error("补发通知失败");
    }

    /**
     * 批量补发
     * @param recordIdStr
     * @param userModel
     * @return
     */
    @Permission("merchant:notify:manage")
    @RequestMapping("notifyAgainBatch")
    public RestResult<String> notifyAgainBatch(@RequestParam String recordIdStr, @CurrentUser UserModel userModel){
        int successCount = 0, failCount = 0;
        String[] recordIdArr = recordIdStr.split(",");
        for(int i=0; i<recordIdArr.length; i++){
            if(StringUtil.isEmpty(recordIdArr[i])){
                continue;
            }

            try{
                Long recordId = Long.valueOf(recordIdArr[i]);
                boolean isOk = notifyRecordFacade.notifyAgain(recordId, userModel.getLoginName());
                if(isOk){
                    successCount ++;
                }else{
                    failCount ++;
                }
            }catch(BizException e){
                failCount ++;
                logger.error("mchNotifyRecordId={} 商户通知补发失败, errMsg={}", recordIdArr[i], e.getMsg());
            }catch(Exception e){
                failCount ++;
                logger.error("mchNotifyRecordId={} 商户通知补发失败, errMsg={}", recordIdArr[i], e.getMessage());
            }
        }

        return RestResult.success("批量补发完毕，成功(" + successCount + ")条，失败(" + failCount + ")条");
    }
}
