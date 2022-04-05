package com.xpay.web.api.common.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "web.api")
public class WebApiProperties {
    public final static int DEFAULT_SERVER_EXPIRE_SEC = 30 * 60;
    public final static String FIXED_WHITE_LIST_PATH = "/user/forgetLoginPwdCode,/user/retrieveLoginPwd," +
            "/user/imgVerifyCode,/user/smsVerifyCode,/user/login,/error" +
            ",";

    @Value("${spring.profiles.active:\"\"}")
    private String profile;
    @Value("${spring.application.name:\"\"}")
    private String appName;
    private String platformName = "system";//平台名称，用以发送邮件等时使用
    private String allowOrigins = "*";
    private String allowMethods = "*";
    private String allowHeaders = "content-type,content-length,X-TOKEN,*";//不同浏览器有兼容性问题，所以需要显式声明一些头
    private String maxAge = "1800";
    private boolean allowCredentials = true;
    private Integer tokenExpiredSec = null;//token本身的有效期，为null表示不过期，依靠tokenServerExpiredSec的时间来决定
    private Integer tokenServerExpiredSec = DEFAULT_SERVER_EXPIRE_SEC;//token在服务端的有效期(秒)，默认30分钟
    private String tokenSecretKey;
    private String rsaPublicKey;
    private String rsaPrivateKey;
    private String downloadPath = "/download/";
    private String whiteListPrefix = "/public/,/static/";
    private String whiteListSuffix = "doc,docx,xls,xlsx,png,jpeg,bmp,ico,html,js";
    private String fileSysUrl;
    /**
     * 平台地址
     */
    private String platformUrl;
    /**
     * 服务电话
     */
    private String servicePhone;

    public String getProfile() {
        return profile;
    }

    public void setProfile(String profile) {
        this.profile = profile;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getPlatformName() {
        return platformName;
    }

    public void setPlatformName(String platformName) {
        this.platformName = platformName;
    }

    public String getAllowOrigins() {
        return allowOrigins;
    }

    public void setAllowOrigins(String allowOrigins) {
        this.allowOrigins = allowOrigins;
    }

    public String getAllowMethods() {
        return allowMethods;
    }

    public void setAllowMethods(String allowMethods) {
        this.allowMethods = allowMethods;
    }

    public String getAllowHeaders() {
        return allowHeaders;
    }

    public void setAllowHeaders(String allowHeaders) {
        this.allowHeaders = allowHeaders;
    }

    public String getMaxAge() {
        return maxAge;
    }

    public void setMaxAge(String maxAge) {
        this.maxAge = maxAge;
    }

    public boolean getAllowCredentials() {
        return allowCredentials;
    }

    public void setAllowCredentials(boolean allowCredentials) {
        this.allowCredentials = allowCredentials;
    }

    public Integer getTokenExpiredSec() {
        return tokenExpiredSec;
    }

    public void setTokenExpiredSec(Integer tokenExpiredSec) {
        this.tokenExpiredSec = tokenExpiredSec;
    }

    public Integer getTokenServerExpiredSec() {
        return tokenServerExpiredSec;
    }

    public void setTokenServerExpiredSec(Integer tokenServerExpiredSec) {
        this.tokenServerExpiredSec = tokenServerExpiredSec;
    }

    public String getTokenSecretKey() {
        return tokenSecretKey;
    }

    public void setTokenSecretKey(String tokenSecretKey) {
        this.tokenSecretKey = tokenSecretKey;
    }

    public String getRsaPublicKey() {
        return rsaPublicKey;
    }

    public void setRsaPublicKey(String rsaPublicKey) {
        this.rsaPublicKey = rsaPublicKey;
    }

    public String getRsaPrivateKey() {
        return rsaPrivateKey;
    }

    public void setRsaPrivateKey(String rsaPrivateKey) {
        this.rsaPrivateKey = rsaPrivateKey;
    }

    public String getWhiteListPrefix() {
        if(whiteListPrefix == null){
            return FIXED_WHITE_LIST_PATH;
        }else{
            return FIXED_WHITE_LIST_PATH + whiteListPrefix;
        }
    }

    public void setWhiteListPrefix(String whiteListPrefix) {
        this.whiteListPrefix = whiteListPrefix;
    }

    public String getWhiteListSuffix() {
        return whiteListSuffix;
    }

    public void setWhiteListSuffix(String whiteListSuffix) {
        this.whiteListSuffix = whiteListSuffix;
    }

    public String getDownloadPath() {
        return downloadPath;
    }

    public void setDownloadPath(String downloadPath) {
        this.downloadPath = downloadPath;
    }

    public String getFixedWhiteListPath(){
        return FIXED_WHITE_LIST_PATH;
    }

    public String getFileSysUrl() {
        return fileSysUrl;
    }

    public void setFileSysUrl(String fileSysUrl) {
        this.fileSysUrl = fileSysUrl;
    }

    public String getPlatformUrl() {
        return platformUrl;
    }

    public void setPlatformUrl(String platformUrl) {
        this.platformUrl = platformUrl;
    }

    public String getServicePhone() {
        return servicePhone;
    }

    public void setServicePhone(String servicePhone) {
        this.servicePhone = servicePhone;
    }
}
