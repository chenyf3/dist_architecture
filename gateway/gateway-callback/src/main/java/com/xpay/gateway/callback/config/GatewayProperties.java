package com.xpay.gateway.callback.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@ConfigurationProperties(prefix = "gateway")
public class GatewayProperties {
    public final static int NOT_LIMIT_RATE_VALUE = -1;
    /**
     * IP黑名单表达式(正则表达式)
     */
    private String ipBlackList = "";
    /**
     * 业务维护路径，如：/payment、/settle 等，如果设置为：ALL，则表示所有业务都进入维护
     */
    private String maintenancePath = "";
    /**
     * 需要取出并保存到RequestParam中的http请求头
     */
    private String fetchHeaders;
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

    public String getFetchHeaders() {
        return fetchHeaders;
    }

    public void setFetchHeaders(String fetchHeaders) {
        this.fetchHeaders = fetchHeaders;
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
        private Map<String, MethodConf> subPathConf = new HashMap<>();

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

        public Map<String, MethodConf> getSubPathConf() {
            return subPathConf;
        }

        public void setSubPathConf(Map<String, MethodConf> subPathConf) {
            this.subPathConf = subPathConf;
        }
    }

    public static class MethodConf {
        private Rate defaultRate = new Rate();
        private Map<String, Rate> subPathRate = new HashMap<>();

        public Rate getDefaultRate() {
            return defaultRate;
        }

        public void setDefaultRate(Rate defaultRate) {
            this.defaultRate = defaultRate;
        }

        public Map<String, Rate> getSubPathRate() {
            return subPathRate;
        }

        public void setSubPathRate(Map<String, Rate> subPathRate) {
            this.subPathRate = subPathRate;
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
