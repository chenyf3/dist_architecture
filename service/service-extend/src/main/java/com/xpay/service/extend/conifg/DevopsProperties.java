package com.xpay.service.extend.conifg;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * devops配置属性
 */
@Configuration
@EnableConfigurationProperties(DevopsProperties.class)
@ConfigurationProperties(prefix = "devops")
public class DevopsProperties {
    private Jenkins jenkins = new Jenkins();
    private Publish publish = new Publish();
    private NetConfig netConfig = new NetConfig();

    public Jenkins getJenkins() {
        return jenkins;
    }

    public void setJenkins(Jenkins jenkins) {
        this.jenkins = jenkins;
    }

    public Publish getPublish() {
        return publish;
    }

    public void setPublish(Publish publish) {
        this.publish = publish;
    }

    public NetConfig getNetConfig() {
        return netConfig;
    }

    public void setNetConfig(NetConfig netConfig) {
        this.netConfig = netConfig;
    }

    public static class Jenkins {
        /**
         * jenkins地址，例如：http://127.0.0.1:8080
         */
        private String url;
        /**
         * jenkins访问授权，用户名:token，例如：jenkins_client:11aae2e8aa50734883fba555oldk094287
         */
        private String credential;
        /**
         * 执行项目构建的jenkins任务名
         */
        private String buildJobName;
        /**
         * 执行上线发布的jenkins任务名
         */
        private String publishJobName;
        /**
         * 触发jenkins任务时的静默期
         */
        private Integer jobQuietPeriod = 5;

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getCredential() {
            return credential;
        }

        public void setCredential(String credential) {
            this.credential = credential;
        }

        public String getBuildJobName() {
            return buildJobName;
        }

        public void setBuildJobName(String buildJobName) {
            this.buildJobName = buildJobName;
        }

        public String getPublishJobName() {
            return publishJobName;
        }

        public void setPublishJobName(String publishJobName) {
            this.publishJobName = publishJobName;
        }

        public Integer getJobQuietPeriod() {
            return jobQuietPeriod;
        }

        public void setJobQuietPeriod(Integer jobQuietPeriod) {
            this.jobQuietPeriod = jobQuietPeriod;
        }
    }

    public static class Publish {
        /**
         * 上线发布记录的扫描间隔(秒)
         */
        private Integer scanInterval = 30;
        /**
         * 上线发布结果通知人(邮件地址)，多个收件人用英文输入发的逗号(,)分割
         */
        private String emailReceiver;
        /**
         * 上线发布记录的超时时间，如果发生超时，此条发布记录将不再被处理
         */
        private Integer timeoutSec = 60 * 60;

        public Integer getScanInterval() {
            return scanInterval;
        }

        public void setScanInterval(Integer scanInterval) {
            this.scanInterval = scanInterval;
        }

        public String getEmailReceiver() {
            return emailReceiver;
        }

        public void setEmailReceiver(String emailReceiver) {
            this.emailReceiver = emailReceiver;
        }

        public Integer getTimeoutSec() {
            return timeoutSec;
        }

        public void setTimeoutSec(Integer timeoutSec) {
            this.timeoutSec = timeoutSec;
        }
    }

    /**
     * 网络配置
     */
    public static class NetConfig {
        /**
         * 是否支持网络绑定配置，一般来说开发、测试环境可能不需要，可以设置为false，那其余配置字段就自动无效了
         */
        public boolean bindable = true;
        /**
         * 网络绑定类型：
         *  dns：表示使用dns和机房入网ip进行绑定
         *  dcdn：表示使用dcdn和机房入网ip进行绑定
         *  cdn：标识使用cdn和机房入网ip进行绑定
         *  waf：表示使用waf和机房入网ip进行绑定
         */
        private BindType bindType;
        /**
         * 访问阿里云等的accessKey
         */
        private String accessKey;
        /**
         * 访问阿里云等的secretKey
         */
        private String secretKey;
        /**
         * 域名，多个域名用英文的逗号分割，例如：www.example.com,api.example.com,mall.example.com
         */
        private String domains;
        /**
         * 网路流量切换用时(秒)
         */
        private Integer netSwitchSec = 30;
        /**
         * 阿里云的waf实例id，bindType=waf 时使用，多个实例时用英文逗号分割
         */
        private String wafInstances;
        /**
         * 机房配置，如：机房编号、机房名称、机房识别正则表达式、机房入口ip 等
         */
        private List<Idc> idcList = new ArrayList<>();

        public String getAccessKey() {
            return accessKey;
        }

        public void setAccessKey(String accessKey) {
            this.accessKey = accessKey;
        }

        public String getSecretKey() {
            return secretKey;
        }

        public void setSecretKey(String secretKey) {
            this.secretKey = secretKey;
        }

        public boolean getBindable() {
            return bindable;
        }

        public void setBindable(boolean bindable) {
            this.bindable = bindable;
        }

        public BindType getBindType() {
            return bindType;
        }

        public void setBindType(BindType bindType) {
            this.bindType = bindType;
        }

        public String getDomains() {
            return domains;
        }

        public void setDomains(String domains) {
            this.domains = domains;
        }

        public Integer getNetSwitchSec() {
            return netSwitchSec;
        }

        public void setFlowSwitchSec(Integer netSwitchSec) {
            this.netSwitchSec = netSwitchSec;
        }

        public String getWafInstances() {
            return wafInstances;
        }

        public void setWafInstances(String wafInstances) {
            this.wafInstances = wafInstances;
        }

        public List<Idc> getIdcList() {
            return idcList;
        }

        public void setIdcList(List<Idc> idcList) {
            this.idcList = idcList;
        }
    }

    public static class Idc {
        /**
         * 机房编码
         */
        private String code;
        /**
         * 机房名称
         */
        private String name;
        /**
         * 机房描述
         */
        private String desc;
        /**
         * 用来判断一个地址(域名或IP)是否属于当前机房的正则表达式，比如机房域名为：idc12sz101.xpay.cc
         * 则正则表达式可以为：^(idc)([1-9]|([1-9]\d)|(1\d\d)|(2([0-4]\d|5[0-5])))(sz)([1-9]|([1-9]\d)|(1\d\d)|(2([0-4]\d|5[0-4])))(.xpay.cc)$
         */
        private String regex;
        /**
         * 机房的公网入口IP，多个IP地址用英文逗号分割
         */
        private String incomeIps;

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDesc() {
            return desc;
        }

        public void setDesc(String desc) {
            this.desc = desc;
        }

        public String getRegex() {
            return regex;
        }

        public void setRegex(String regex) {
            this.regex = regex;
        }

        public String getIncomeIps() {
            return incomeIps;
        }

        public void setIncomeIps(String incomeIps) {
            this.incomeIps = incomeIps;
        }
    }

    public enum BindType {
        WAF,
        DCDN, //全球全站加速
        CDN, //CDN加速
        DNS
    }
}
