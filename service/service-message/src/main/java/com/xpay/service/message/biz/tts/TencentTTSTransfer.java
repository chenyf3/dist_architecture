package com.xpay.service.message.biz.tts;

import com.tencent.SpeechClient;
import com.tencent.tts.model.SpeechSynthesisRequest;
import com.tencent.tts.model.SpeechSynthesisResponse;
import com.tencent.tts.service.SpeechSynthesizer;
import com.xpay.common.statics.exception.BizException;
import com.xpay.common.utils.JsonUtil;
import com.xpay.common.utils.StringUtil;
import com.xpay.facade.message.dto.TTSDto;
import com.xpay.service.message.config.properties.TencentTTSProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;

@Component("tencentTTSTransfer")
public class TencentTTSTransfer implements TTSTransfer {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private SpeechClient client;

    public TencentTTSTransfer(TencentTTSProperties properties){
        client = SpeechClient.newInstance(properties.getAppId(), properties.getSecretId(), properties.getSecretKey());
    }

    @Override
    public byte[] transferAudio(TTSDto ttsDto){
        if (StringUtil.isEmpty(ttsDto.getText())) {
            throw new BizException("文本内容不能为空");
        } else if(ttsDto.getText().length() > 150) {
            throw new BizException("文本长度不能超过150");
        }

        Integer sampleRate = (ttsDto.getSampleRate() == null || (ttsDto.getSampleRate() != 8000 && ttsDto.getSampleRate() != 16000)) ? 16000 : ttsDto.getSampleRate();
        Integer volume = ttsDto.getVolume() == null ? 5 : ttsDto.getVolume()/10;
        Integer voiceType = ttsDto.getVoice() == null ? 1008 : Integer.parseInt(ttsDto.getVoice());
        Float speed = ttsDto.getSpeechRate() == null ? 0f : Float.parseFloat(ttsDto.getSpeechRate().toString());
        speed = speed < -2 ? -2f : speed > 2 ? 2f : speed;
        SpeechSynthesisListener listener = new SpeechSynthesisListener(ttsDto.getTrxNo());
        try {
            //初始化SpeechSynthesizerRequest请求参数
            SpeechSynthesisRequest request = SpeechSynthesisRequest.initialize();
            request.setCodec("mp3");//mp3、pcm、opus
            request.setSampleRate(sampleRate);
            request.setVolume(volume);
            request.setSpeed(speed);
            request.setVoiceType(voiceType);
            //使用客户端client创建语音合成实例
            SpeechSynthesizer speechSynthesizer = client.newSpeechSynthesizer(request, listener);
            //执行语音合成
            speechSynthesizer.synthesis(ttsDto.getText());
        } catch (Exception e) {
            logger.error("腾讯云语音转换异常 trxNo={} text={}", ttsDto.getTrxNo(), ttsDto.getText(), e);
            throw new BizException("语音转换异常");
        }
        return listener.getData();
    }

    private class SpeechSynthesisListener extends com.tencent.tts.service.SpeechSynthesisListener {
        private String trxNo;
        private ByteArrayOutputStream stream = new ByteArrayOutputStream();

        public SpeechSynthesisListener(String trxNo){
            this.trxNo = trxNo;
        }

        public byte[] getData(){
            return stream.toByteArray();
        }

        @Override
        public void onComplete(SpeechSynthesisResponse response) {

        }

        @Override
        public void onMessage(byte[] data) {
            try {
                stream.write(data);
            } catch (Exception e) {
                logger.error("等待语音合成结果时异常", e);
            }
        }

        @Override
        public void onFail(SpeechSynthesisResponse response) {
            logger.error("语音合成失败 trxNo:{} response: {}", trxNo, JsonUtil.toJson(response));
        }
    }
}
