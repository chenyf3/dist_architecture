package com.xpay.web.pms.web.controller.publish;

import com.xpay.common.statics.result.RestResult;
import com.xpay.common.utils.StringUtil;
import com.xpay.facade.extend.enums.BuildResultEnum;
import com.xpay.facade.extend.service.DevopsFacade;
import com.xpay.starter.plugin.client.RedisClient;
import com.xpay.web.pms.config.AppConstant;
import com.xpay.web.pms.web.controller.BaseController;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("public")
public class DevOpsController extends BaseController {
    @Autowired
    RedisClient redisClient;

    @DubboReference
    DevopsFacade devopsFacade;

    /**
     * 项目发布之后的回调处理
     * @param token
     * @param buildSeq
     * @param result
     * @return
     */
    @GetMapping("publishCallback")
    public RestResult<String> publishCallback(@RequestParam String token, @RequestParam String buildSeq,
                                              @RequestParam String result){
        if(StringUtil.isEmpty(token)){
            return RestResult.error("令牌不能为空");
        }else if(StringUtil.isEmpty(buildSeq)){
            return RestResult.error("构建序列号不能为空");
        }else if(StringUtil.isEmpty(result)){
            return RestResult.error("构建结果不能为空");
        }

        String cacheKey = AppConstant.PROJECT_PUBLISH_TOKEN_KEY + ":" + token;
        String cacheVal = redisClient.get(cacheKey);
        if(StringUtil.isEmpty(cacheVal)){
            return RestResult.error("令牌已过期");
        }else{
            redisClient.del(cacheKey);
        }

        Long id = Long.valueOf(buildSeq);
        BuildResultEnum resultEnum = BuildResultEnum.getEnum(result);
        devopsFacade.publishResultCallback(id, resultEnum);
        return RestResult.success("success");
    }

    /**
     * 机房代码同步之后的回调处理
     * @param token
     * @param result
     * @return
     */
    @GetMapping("syncCallback")
    public RestResult<String> syncCallback(@RequestParam String token, @RequestParam String result) {
        if(StringUtil.isEmpty(token)){
            return RestResult.error("令牌不能为空");
        }else if(StringUtil.isEmpty(result)){
            return RestResult.error("构建结果不能为空");
        }

        String cacheKey = AppConstant.PROJECT_PUBLISH_TOKEN_KEY + ":" + token;
        String cacheVal = redisClient.get(cacheKey);
        if(StringUtil.isEmpty(cacheVal)){
            return RestResult.error("令牌已过期");
        }else{
            redisClient.del(cacheKey);
        }

        BuildResultEnum resultEnum = BuildResultEnum.getEnum(result);
        //TODO 根据自己的业务需求做一些处理，比如：等待一段时间之后就多个机房放量

        return RestResult.success("success");
    }
}
