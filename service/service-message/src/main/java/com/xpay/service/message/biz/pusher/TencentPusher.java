package com.xpay.service.message.biz.pusher;

import com.tencent.xinge.XingeApp;
import com.tencent.xinge.bean.*;
import com.tencent.xinge.bean.ios.Alert;
import com.tencent.xinge.bean.ios.Aps;
import com.tencent.xinge.push.app.PushAppRequest;
import com.xpay.common.statics.exception.BizException;
import com.xpay.facade.message.dto.PushDto;
import com.xpay.facade.message.enums.AppNameEnum;
import com.xpay.facade.message.enums.OSTypeEnum;
import com.xpay.facade.message.enums.PushTargetEnum;
import com.xpay.service.message.config.properties.TencentPusherProperties;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class TencentPusher implements MobilePusher {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final Map<String, XingeApp> xingeAppMap = new ConcurrentHashMap<>();

    public TencentPusher(TencentPusherProperties properties){
        properties.getAppMap().forEach((key,value) -> {
            XingeApp xingeApp = new XingeApp.Builder()
                    .appId(value.getAccessId())
                    .secretKey(value.getSecretKey())
                    .domainUrl(properties.getUrl())
                    .build();
            xingeAppMap.put(key, xingeApp);
        });
    }

    @Override
    public boolean pushMessage(PushDto pushDto) {
        AppNameEnum appName = AppNameEnum.getEnum(pushDto.getAppName());
        if (!xingeAppMap.containsKey(appName.name())) {
            throw new BizException("当前应用未配置相关信息，应用名：" + appName);
        }

        XingeApp xingeApp = xingeAppMap.get(appName.name());
        AudienceType audienceType = getAudienceType(pushDto.getTarget());
        PushAppRequest pushAppRequest = new PushAppRequest();
        pushAppRequest.setAudience_type(audienceType);
        pushAppRequest.setMessage_type(MessageType.notify);

        OSTypeEnum osType = OSTypeEnum.getEnum(pushDto.getOsType());
        Message message = new Message();
        message.setTitle(pushDto.getTitle());
        message.setContent(pushDto.getJsonBody());
        if (osType == OSTypeEnum.ANDROID) {
            MessageAndroid messageAndroid = new MessageAndroid();
            message.setAndroid(messageAndroid);
        } else if (osType == OSTypeEnum.IOS) {
            Aps aps = new Aps();
            aps.setAlert(new Alert());
            MessageIOS messageIOS = new MessageIOS();
            messageIOS.setAps(aps);
            message.setIos(messageIOS);
        } else {
            throw new BizException("暂不支持此操作系统的终端设备推送，osType: " + osType);
        }
        pushAppRequest.setMessage(message);

        ArrayList targetValueList = new ArrayList(pushDto.getTargetValues());
        if (AudienceType.token_list == audienceType) {
            pushAppRequest.setToken_list(targetValueList);
        } else if (AudienceType.account_list == audienceType) {
            pushAppRequest.setAccount_list(targetValueList);
        } else if (AudienceType.tag == audienceType) {
            TagListObject tagListObject = new TagListObject();
            tagListObject.setTags(targetValueList);
            tagListObject.setOp(OpType.OR);
            pushAppRequest.setTag_list(tagListObject);
        } else if (AudienceType.all == audienceType) {
            // do nothing
        }

        JSONObject jsonObject = xingeApp.pushApp(pushAppRequest);
        int retCode = jsonObject.getInt("ret_code");
        if (retCode == 0) {
            return true;
        } else {
            logger.error("移动推送失败，trxNo: {} Response: {}", pushDto.getTrxNo(), jsonObject.toString());
            return false;
        }
    }

    private AudienceType getAudienceType(Integer target){
        PushTargetEnum pushTargetEnum = PushTargetEnum.getEnum(target);
        switch (pushTargetEnum) {
            case DEVICE:
            case ALIAS:
                return AudienceType.token_list;
            case ACCOUNT:
                return AudienceType.account_list;
            case TAG:
                return AudienceType.tag;
            case ALL:
                return AudienceType.all;
            default:
                throw new BizException("未支持的推送目标类型");
        }
    }
}
