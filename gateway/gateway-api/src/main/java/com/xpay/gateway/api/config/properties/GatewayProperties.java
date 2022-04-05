package com.xpay.gateway.api.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@ConfigurationProperties(prefix = "gateway")
public class GatewayProperties {
    public final static int NOT_LIMIT_RATE_VALUE = -1;
    /**
     * 上传文件时是否读取源请求体，看业务是否需要对源请求体做一些校验等处理，开启此功能会有一定的性能损耗
     */
    private boolean readMultipartOriBody = false;
    /**
     * IP黑名单表达式(正则表达式)
     */
    private String ipBlackList = "";
    /**
     * 业务维护路径，如：/payment、/settle 等，如果设置为：ALL，则表示所有业务都进入维护
     */
    private String maintenancePath = "";
    /**
     * 请求的有效时间
     */
    private long requestExpire = 10 * 60 * 1000;//即10分钟内的请求有效
    /**
     * 针对每一个Path的配置
     */
    private Map<String, PathConf> pathConf = new HashMap<>();
    /**
     * 本地缓存的配置
     */
    private Cache cache = new Cache();

    public boolean getReadMultipartOriBody() {
        return readMultipartOriBody;
    }

    public void setReadMultipartOriBody(boolean readMultipartOriBody) {
        this.readMultipartOriBody = readMultipartOriBody;
    }

    public String getIpBlackList() {
        return ipBlackList;
    }

    public void setIpBlackList(String ipBlackList) {
        this.ipBlackList = ipBlackList;
    }

    public String getMaintenancePath() {
        return maintenancePath;
    }

    public void setMaintenancePath(String maintenancePath) {
        this.maintenancePath = maintenancePath;
    }

    public long getRequestExpire() {
        return requestExpire;
    }

    public void setRequestExpire(long requestExpire) {
        this.requestExpire = requestExpire;
    }

    public Map<String, PathConf> getPathConf() {
        return pathConf;
    }

    public void setPathConf(Map<String, PathConf> pathConf) {
        this.pathConf = pathConf;
    }

    public Cache getCache() {
        return cache;
    }

    public void setCache(Cache cache) {
        this.cache = cache;
    }



    public static class PathConf {
        private String allowMethods;
        private Rate defaultRate = new Rate();
        private Map<String, MethodConf> methodConf = new HashMap<>();

        public String getAllowMethods() {
            return allowMethods;
        }

        public void setAllowMethods(String allowMethods) {
            this.allowMethods = allowMethods;
        }

        public Rate getDefaultRate() {
            return defaultRate;
        }

        public void setDefaultRate(Rate defaultRate) {
            this.defaultRate = defaultRate;
        }

        public Map<String, MethodConf> getMethodConf() {
            return methodConf;
        }

        public void setMethodConf(Map<String, MethodConf> methodConf) {
            this.methodConf = methodConf;
        }
    }

    public static class MethodConf {
        private Rate defaultRate = new Rate();
        private Map<String, Rate> mchRate = new HashMap<>();

        public Rate getDefaultRate() {
            return defaultRate;
        }

        public void setDefaultRate(Rate defaultRate) {
            this.defaultRate = defaultRate;
        }

        public Map<String, Rate> getMchRate() {
            return mchRate;
        }

        public void setMchRate(Map<String, Rate> mchRate) {
            this.mchRate = mchRate;
        }
    }

    public static class Rate {
        private int replenishRate = NOT_LIMIT_RATE_VALUE;
        private int burstCapacity = NOT_LIMIT_RATE_VALUE;

        public int getReplenishRate() {
            return replenishRate;
        }

        public void setReplenishRate(int replenishRate) {
            this.replenishRate = replenishRate;
        }

        public int getBurstCapacity() {
            return burstCapacity;
        }

        public void setBurstCapacity(int burstCapacity) {
            this.burstCapacity = burstCapacity;
        }
    }

    /**
     * 本地缓存的配置项
     */
    public static class Cache {
        /**
         * 过期时间
         */
        private Duration expireAfterWrite = Duration.ofSeconds(60);

        private Duration expireAfterAccess;

        private Duration refreshAfterWrite;

        /**
         * 最大长度
         */
        private Integer maximumSize = 10000;
        /**
         * 初始容量
         */
        private Integer initialCapacity = 50;
        /**
         * 并发级别
         */
        private Integer concurrencyLevel = 10;

        public Duration getExpireAfterWrite() {
            return expireAfterWrite;
        }

        public void setExpireAfterWrite(Duration expireAfterWrite) {
            this.expireAfterWrite = expireAfterWrite;
        }

        public Duration getExpireAfterAccess() {
            return expireAfterAccess;
        }

        public void setExpireAfterAccess(Duration expireAfterAccess) {
            this.expireAfterAccess = expireAfterAccess;
        }

        public Duration getRefreshAfterWrite() {
            return refreshAfterWrite;
        }

        public void setRefreshAfterWrite(Duration refreshAfterWrite) {
            this.refreshAfterWrite = refreshAfterWrite;
        }

        public Integer getMaximumSize() {
            return maximumSize;
        }

        public void setMaximumSize(Integer maximumSize) {
            this.maximumSize = maximumSize;
        }

        public Integer getInitialCapacity() {
            return initialCapacity;
        }

        public void setInitialCapacity(Integer initialCapacity) {
            this.initialCapacity = initialCapacity;
        }

        public Integer getConcurrencyLevel() {
            return concurrencyLevel;
        }

        public void setConcurrencyLevel(Integer concurrencyLevel) {
            this.concurrencyLevel = concurrencyLevel;
        }
    }
}
