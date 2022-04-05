package com.xpay.common.statics.enums.common;

/**
 * Description: 请保证各自在redis中的数据，分别使用不同的key做为前缀，已使用的在此枚举中写明
 */
public enum RedisKeyPrefixEnum {
    /**
     * 运营后台session数据
     */
    WEB_PMS_SESSION_KEY,
}
