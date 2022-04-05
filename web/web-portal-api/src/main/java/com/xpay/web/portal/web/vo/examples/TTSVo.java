package com.xpay.web.portal.web.vo.examples;

import jakarta.validation.constraints.NotNull;

public class TTSVo {
    /**
     * 转换平台 1=阿里云 2=腾讯云
     */
    private Integer platform;

    @NotNull(message="播放内容不能为空")
    private String text;
    /**
     * 音量，[0, 100]
     */
    private Integer volume;
    /**
     * 语调
     * 阿里云：[-500, 500]
     */
    private Integer pitchRate;
    /**
     * 语速
     * 阿里云：[-500, 0, 500]，对应的语速倍速区间为 [0.5, 1.0, 2.0]
     */
    private Integer speechRate;

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

    public Integer getVolume() {
        return volume;
    }

    public void setVolume(Integer volume) {
        this.volume = volume;
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
}
