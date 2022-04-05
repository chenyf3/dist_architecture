package com.xpay.facade.message.dto;

import com.xpay.facade.message.enums.TTSPlatformEnum;

import java.io.Serializable;

public class TTSDto implements Serializable {
    /**
     * 在线语音转换平台，详见：{@link TTSPlatformEnum}
     */
    private Integer platform = TTSPlatformEnum.TENCENT.getValue();
    /**
     * 要转换的文本内容
     */
    private String text;
    /**
     * 发音人
     */
    private String voice;
    /**
     * 音量，[0, 100]
     */
    private Integer volume;
    /**
     * 采样频率，可选值有：8000、16000
     */
    private Integer sampleRate;
    /**
     * 语调
     * 阿里云：[-500, 500]
     * 腾讯云：不支持
     */
    private Integer pitchRate;
    /**
     * 语速
     * 阿里云：[-500, 0, 500]，对应的语速倍速区间为 [0.5, 1.0, 2.0]
     * 腾讯云：[-2，2]，比如：-2=0.6倍，-1=0.8倍，0=1.0倍，1=1.2倍，2=1.5倍
     */
    private Integer speechRate;
    /**
     * 业务订单号，用以日志记录，可选项
     */
    private String trxNo;

    public Integer getPlatform() {
        return platform;
    }

    public void setPlatform(Integer platform) {
        this.platform = platform;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getVoice() {
        return voice;
    }

    public void setVoice(String voice) {
        this.voice = voice;
    }

    public Integer getVolume() {
        return volume;
    }

    public void setVolume(Integer volume) {
        this.volume = volume;
    }

    public Integer getSampleRate() {
        return sampleRate;
    }

    public void setSampleRate(Integer sampleRate) {
        this.sampleRate = sampleRate;
    }

    public Integer getPitchRate() {
        return pitchRate;
    }

    public void setPitchRate(Integer pitchRate) {
        this.pitchRate = pitchRate;
    }

    public Integer getSpeechRate() {
        return speechRate;
    }

    public void setSpeechRate(Integer speechRate) {
        this.speechRate = speechRate;
    }

    public String getTrxNo() {
        return trxNo;
    }

    public void setTrxNo(String trxNo) {
        this.trxNo = trxNo;
    }
}
