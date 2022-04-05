package com.xpay.service.message.biz.tts;

import com.alibaba.nls.client.AccessToken;
import com.alibaba.nls.client.protocol.NlsClient;
import com.alibaba.nls.client.protocol.OutputFormatEnum;
import com.alibaba.nls.client.protocol.tts.SpeechSynthesizer;
import com.alibaba.nls.client.protocol.tts.SpeechSynthesizerResponse;
import com.xpay.common.statics.exception.BizException;
import com.xpay.common.utils.DateUtil;
import com.xpay.common.utils.StringUtil;
import com.xpay.facade.message.dto.TTSDto;
import com.xpay.service.message.config.properties.AliCloudTTSProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;

@Component("aliCloudTTSTransfer")
public class AliCloudTTSTransfer implements TTSTransfer {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final AliCloudTTSProperties properties;
    private final Object lock = new Object();
    private AccessToken accessToken;
    private NlsClient client;

    public AliCloudTTSTransfer(AliCloudTTSProperties properties){
        this.properties = properties;
        if (!properties.getInitDelay()) {
            initialAndRefreshIfNeed();
        }
    }

    @Override
    public byte[] transferAudio(TTSDto ttsDto) {
        if(StringUtil.isEmpty(ttsDto.getText())) {
            throw new BizException("文本内容不能为空");
        }else if(ttsDto.getText().length() > 300){
            throw new BizException("文本长度不能超过300");
        }

        try {
            initialAndRefreshIfNeed();
        } catch (Exception e) {
            logger.error("初始化client或token刷新异常 trxNo={}", ttsDto.getTrxNo(), e);
            throw new BizException("客户端连接异常，请稍后重试");
        }

        Integer sampleRate = (ttsDto.getSampleRate() == null || (ttsDto.getSampleRate() != 8000 && ttsDto.getSampleRate() != 16000)) ? 16000 : ttsDto.getSampleRate();
        Integer pitchRate = (ttsDto.getPitchRate() == null || ttsDto.getPitchRate() < -500 || ttsDto.getPitchRate() > 500) ? 0 : ttsDto.getPitchRate();
        Integer speechRate = (ttsDto.getSpeechRate() == null || ttsDto.getSpeechRate() < -500 || ttsDto.getSpeechRate() > 500) ? 0 : ttsDto.getSpeechRate();
        Integer volume = (ttsDto.getVolume() == null || ttsDto.getVolume() <= 0 || ttsDto.getVolume() > 100) ? 50 : ttsDto.getVolume();
        String voice = StringUtil.isEmpty(ttsDto.getVoice()) ? "aiqi" : ttsDto.getVoice();
        SpeechSynthesizer synthesizer = null;
        SpeechSynthesizerListener listener;
        try {
            //创建实例，建立连接。
            listener = new SpeechSynthesizerListener(ttsDto.getTrxNo());
            synthesizer = new SpeechSynthesizer(client, listener);
            synthesizer.setAppKey(properties.getAppKey());
            //设置返回音频的编码格式
            synthesizer.setFormat(OutputFormatEnum.MP3);
            //设置返回音频的采样率
            synthesizer.setSampleRate(sampleRate);
            //发音人
            synthesizer.setVoice(voice);
            //语调，范围是-500~500，可选，默认是0。
            synthesizer.setPitchRate(pitchRate);
            //语速，范围是-500~500，默认是0。
            synthesizer.setSpeechRate(speechRate);
            //音量，取值范围：0～100。默认值：50
            synthesizer.setVolume(volume);
            //设置用于语音合成的文本
            synthesizer.setText(ttsDto.getText());
            // 是否开启字幕功能（返回相应文本的时间戳），默认不开启，需要注意并非所有发音人都支持该参数。
            synthesizer.addCustomedParam("enable_subtitle", false);
            //此方法将以上参数设置序列化为JSON格式发送给服务端，并等待服务端确认。
            long start = System.currentTimeMillis();
            synthesizer.start();
            logger.info("tts start latency " + (System.currentTimeMillis() - start) + " ms");
            start = System.currentTimeMillis();
            //等待语音合成结束
            synthesizer.waitForComplete(3000);
            logger.info("tts stop latency " + (System.currentTimeMillis() - start) + " ms");
        } catch (Exception e) {
            logger.error("阿里云语音转换异常 trxNo={} text={}", ttsDto.getTrxNo(), ttsDto.getText(), e);
            throw new BizException("语音转换异常");
        } finally {
            //关闭连接
            if (null != synthesizer) {
                synthesizer.close();
            }
        }

        return listener.getData();
    }

    @PreDestroy
    public void destroy(){
        if (client != null) {
            client.shutdown();
        }
    }

    private void initialAndRefreshIfNeed(){
        //初始化 accessToken、client
        if (client == null) {
            synchronized (lock) {
                if (client == null) {
                    try {
                        accessToken = new AccessToken(properties.getAccessKey(), properties.getSecretKey());
                        accessToken.apply();
                    } catch (Exception e) {
                        throw new RuntimeException("申请访问token时出现异常", e);
                    }

                    if (StringUtil.isEmpty(accessToken.getToken())) {
                        throw new IllegalArgumentException("访问token申请失败，请检查accessKey、secretKey、账户权限 等各项配置是否正确");
                    }

                    logger.info("访问token申请成功 token: {}, expireTime: {}", accessToken.getToken(), DateUtil.formatDateTime(accessToken.getExpireTime()));
                    client = new NlsClient(accessToken.getToken());
                }
            }
        }

        //刷新 accessToken
        if (this.isAccessTokenExpired()) {
            synchronized (lock) {
                if (this.isAccessTokenExpired()) {
                    try {
                        accessToken.apply();
                        client.setToken(accessToken.getToken());
                        logger.info("访问token刷新成功，token: {}, expireTime: {}", accessToken.getToken(), DateUtil.formatDateTime(accessToken.getExpireTime()));
                    } catch (Exception e) {
                        throw new RuntimeException("访问token刷新异常", e);
                    }
                }
            }
        }
    }

    private boolean isAccessTokenExpired() {
        long diff = System.currentTimeMillis() - accessToken.getExpireTime() * 1000;
        return diff >= 0 || diff * -1 <= 60000; //已经过期或者距离过期时间小于1分钟
    }

    private class SpeechSynthesizerListener extends com.alibaba.nls.client.protocol.tts.SpeechSynthesizerListener {
        private String trxNo;
        private ByteArrayOutputStream stream = new ByteArrayOutputStream();

        public SpeechSynthesizerListener(String trxNo) {
            this.trxNo = trxNo;
        }

        public byte[] getData(){
            return stream.toByteArray();
        }

        @Override
        public void onComplete(SpeechSynthesizerResponse response) {
        }

        @Override
        public void onMessage(ByteBuffer message) {
            byte[] bytesArray = new byte[message.remaining()];
            message.get(bytesArray, 0, bytesArray.length);
            try {
                stream.write(bytesArray);
            } catch (Exception e) {
                logger.error("等待语音合成结果时异常", e);
            }
        }

        @Override
        public void onFail(SpeechSynthesizerResponse response) {
            logger.error("语音合成失败 trxNo:{} status:{}", trxNo, response.getStatusText());
        }
    }
}



