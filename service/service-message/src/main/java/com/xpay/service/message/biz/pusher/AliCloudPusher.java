package com.xpay.service.message.biz.pusher;

import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.profile.DefaultProfile;
import com.aliyuncs.profile.IClientProfile;
import com.aliyuncs.push.model.v20160801.PushMessageToAndroidRequest;
import com.aliyuncs.push.model.v20160801.PushMessageToAndroidResponse;
import com.aliyuncs.push.model.v20160801.PushMessageToiOSRequest;
import com.aliyuncs.push.model.v20160801.PushMessageToiOSResponse;
import com.xpay.common.statics.exception.BizException;
import com.xpay.facade.message.dto.PushDto;
import com.xpay.facade.message.enums.AppNameEnum;
import com.xpay.facade.message.enums.OSTypeEnum;
import com.xpay.facade.message.enums.PushTargetEnum;
import com.xpay.service.message.config.properties.AliCloudPusherProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @desc 阿里云移动推送
 */
@Component
public class AliCloudPusher implements MobilePusher {
    private Logger log = LoggerFactory.getLogger(this.getClass());
    /**
     * 最大推送数量
     */
    private final static int MAX_TARGET_NUM = 1000;
    private DefaultAcsClient pushClient;
    @Autowired
    private AliCloudPusherProperties properties;

    @Override
    public boolean pushMessage(PushDto pushDto){
        OSTypeEnum osType = OSTypeEnum.getEnum(pushDto.getOsType());
        if(OSTypeEnum.ANDROID == osType){
            return pushMessageToAndroid(pushDto);
        }else if(OSTypeEnum.IOS == osType){
            return pushMessageToIOS(pushDto);
        }else{
            throw new BizException("暂不支持此设备类型:" + osType);
        }
    }

    private boolean pushMessageToAndroid(PushDto pushDto) {
        AppNameEnum appName = AppNameEnum.getEnum(pushDto.getAppName());
        Long appKey = getAppKey(appName);
        String target = getPushTargetName(pushDto.getTarget());

        String trxNo = pushDto.getTrxNo();
        PushMessageToAndroidRequest request = new PushMessageToAndroidRequest();
        //设置appKey
        request.setAppKey(appKey);
        // 根据别名推送，内容为sn号
        request.setTarget(target);
        // targetValue 最多支持1000个，以","分隔
        request.setTargetValue(formatTargetValue(pushDto.getTargetValues()));
        // title
        request.setTitle(pushDto.getTitle());
        //设置推送数据
        request.setBody(pushDto.getJsonBody());
        try {
            // 目前需要推送的客户端有：PDA及收银机
            PushMessageToAndroidResponse response = getPushClient().getAcsResponse(request);
            log.info("阿里云移动推送完毕, trxNo: {} requestId: {}, messageId: {}", trxNo, response.getRequestId(), response.getMessageId());
        } catch (Exception e) {
            log.error("阿里云移动推送失败 trxNo: {}", trxNo, e);
            throw new BizException(BizException.UNEXPECT_ERROR, "阿里云移动推送请求异常:" + trxNo);
        }
        return true;
    }

    private boolean pushMessageToIOS(PushDto pushDto) {
        AppNameEnum appName = AppNameEnum.getEnum(pushDto.getAppName());
        Long appKey = getAppKey(appName);
        String target = getPushTargetName(pushDto.getTarget());

        String trxNo = pushDto.getTrxNo();
        PushMessageToiOSRequest request = new PushMessageToiOSRequest();
        //设置appKey
        request.setAppKey(appKey);
        // 根据别名推送，内容为sn号
        request.setTarget(target);
        // targetValue 最多支持1000个，以","分隔
        request.setTargetValue(formatTargetValue(pushDto.getTargetValues()));
        // title
        request.setTitle(pushDto.getTitle());
        //设置推送数据
        request.setBody(pushDto.getJsonBody());
        try {
            // 目前需要推送的客户端有：PDA及收银机
            PushMessageToiOSResponse response = getPushClient().getAcsResponse(request);
            log.info("阿里云移动推送完毕, trxNo: {} requestId: {}, messageId: {}", trxNo, response.getRequestId(), response.getMessageId());
        } catch (Exception e) {
            log.error("阿里云移动推送失败 trxNo: {}", trxNo, e);
            throw new BizException(BizException.UNEXPECT_ERROR, "阿里云移动推送请求异常:" + trxNo);
        }
        return true;
    }

    private String getPushTargetName(Integer pushTarget){
        PushTargetEnum pushTargetEnum = PushTargetEnum.getEnum(pushTarget);
        return pushTargetEnum.name();
    }

    private Long getAppKey(AppNameEnum appName){
        AliCloudPusherProperties.App app = properties.getAppMap().get(appName.name());
        if (app == null || app.getAppKey() == null) {
            throw new BizException("未配置当前应用的相关信息，应用名：" + appName.name());
        }
        return app.getAppKey();
    }

    private String formatTargetValue(List<String> targetValues) {
        if (targetValues == null || targetValues.size() == 0) {
            throw new BizException(BizException.PARAM_INVALID, "阿里云移动推送：targetValues不能为空");
        }
        if (targetValues.size() > MAX_TARGET_NUM) {
            throw new BizException(BizException.PARAM_INVALID, "阿里云移动推送：targetValues参数过多，最多支持1000个");
        }
        return String.join(",", targetValues);
    }

    private DefaultAcsClient getPushClient(){
        if(pushClient != null) {
            return pushClient;
        }

        synchronized (this.getClass()){
            if(pushClient == null){
                IClientProfile profile = DefaultProfile.getProfile(properties.getRegionId(), properties.getAccessKey(), properties.getSecretKey());
                pushClient = new DefaultAcsClient(profile);
            }
        }
        return pushClient;
    }
}
