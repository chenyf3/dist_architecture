package com.xpay.service.message.biz.tts;

import com.xpay.common.statics.exception.BizException;
import com.xpay.common.utils.StringUtil;
import com.xpay.facade.message.dto.TTSDto;
import com.xpay.facade.message.enums.TTSPlatformEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * TTS是Text To Speech的缩写，即“从文本到语音”
 */
@Component
public class TTSBiz {
    @Autowired
    TTSTransfer aliCloudTTSTransfer;
    @Autowired
    TTSTransfer tencentTTSTransfer;

    public byte[] onlineAudioTransfer(TTSDto ttsDto) {
        if (StringUtil.isEmpty(ttsDto.getText())) {
            throw new BizException("转换的内容不能为空");
        } else if (ttsDto.getPlatform() == null){
            throw new BizException("转换平台不能为空");
        }

        TTSTransfer ttsTransfer = getTTSTransfer(ttsDto.getPlatform());
        return ttsTransfer.transferAudio(ttsDto);
    }

    private TTSTransfer getTTSTransfer(Integer platform){
        TTSPlatformEnum ttsPlatform = TTSPlatformEnum.getEnum(platform);
        if (ttsPlatform == null) {
            throw new BizException("未识别的转换平台，" + platform);
        } else if (ttsPlatform == TTSPlatformEnum.ALI_CLOUD) {
            return aliCloudTTSTransfer;
        } else if (ttsPlatform == TTSPlatformEnum.TENCENT) {
            return tencentTTSTransfer;
        } else {
            throw new BizException("未支持的转换平台，" + platform);
        }
    }
}
