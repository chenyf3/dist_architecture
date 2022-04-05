package com.xpay.libs.id.config;

import java.util.ArrayList;
import java.util.List;

/**
 * redis配置属性
 */
public class RedisProperties {
    private boolean enabled = false;//是否开启Redis生成Id序列号的功能
    private int database = 0;
    private String host;
    private String password;
    private int port = 6379;
    private boolean ssl;
    private int connTimeout = 5000;//连接超时时间，单位：毫秒
    private int readTimeout = 2000;//从server读取数据超时时间，单位：毫秒
    private String clientName;
    private Sentinel sentinel = new Sentinel();
    private Cluster cluster = new Cluster();
    private final Jedis jedis = new Jedis();
    //下面是自增id的相关配置项
    private boolean segmentAble = true;//是否使用分段发号
    private int segmentStep = 2000;//分段增长初始步长
    private String segmentExcludes = "";//不使用分段发号的key，多个key时用英文逗号分割
    private String idHashKey = "distributeIds";

    public boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public int getDatabase() {
        return this.database;
    }

    public void setDatabase(int database) {
        this.database = database;
    }

    public String getHost() {
        return this.host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getPassword() {
        return this.password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getPort() {
        return this.port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public boolean getSsl() {
        return this.ssl;
    }

    public void setSsl(boolean ssl) {
        this.ssl = ssl;
    }

    public int getConnTimeout() {
        return connTimeout;
    }

    public void setConnTimeout(int connTimeout) {
        this.connTimeout = connTimeout;
    }

    public int getReadTimeout() {
        return readTimeout;
    }

    public void setReadTimeout(int readTimeout) {
        this.readTimeout = readTimeout;
    }

    public String getClientName() {
        return this.clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public Sentinel getSentinel() {
        return this.sentinel;
    }

    public void setSentinel(Sentinel sentinel) {
        this.sentinel = sentinel;
    }

    public Cluster getCluster() {
        return this.cluster;
    }

    public void setCluster(Cluster cluster) {
        this.cluster = cluster;
    }

    public Jedis getJedis() {
        return this.jedis;
    }

    public boolean getSegmentAble() {
        return segmentAble;
    }

    public void setSegmentAble(boolean segmentAble) {
        this.segmentAble = segmentAble;
    }

    public int getSegmentStep() {
        return segmentStep;
    }

    public void setSegmentStep(int segmentStep) {
        this.segmentStep = segmentStep;
    }

    public String getSegmentExcludes() {
        return segmentExcludes;
    }

    public void setSegmentExcludes(String segmentExcludes) {
        this.segmentExcludes = segmentExcludes;
    }

    public String getIdHashKey() {
        return idHashKey;
    }

    public void setIdHashKey(String idHashKey) {
        this.idHashKey = idHashKey;
    }

    /**
     * 判断是否有服务端配置
     * @return
     */
    public boolean isServerConfig(){
        return !getCluster().getNodes().isEmpty() || !getSentinel().getNodes().isEmpty() || (host != null && host.length() > 0) ;
    }

    /**
     * Cluster properties.
     */
    public static class Cluster {
        private List<String> nodes = new ArrayList<>();
        private String password;//连接密码
        private Integer maxRedirects = 3;

        public List<String> getNodes() {
            return this.nodes;
        }

        public void setNodes(List<String> nodes) {
            this.nodes = nodes;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public Integer getMaxRedirects() {
            return this.maxRedirects;
        }

        public void setMaxRedirects(Integer maxRedirects) {
            this.maxRedirects = maxRedirects;
        }
    }

    /**
     * Redis sentinel properties.
     */
    public static class Sentinel {
        private String master;
        private List<String> nodes = new ArrayList<>();
        private String password;//哨兵的连接密码

        public String getMaster() {
            return this.master;
        }

        public void setMaster(String master) {
            this.master = master;
        }

        public List<String> getNodes() {
            return this.nodes;
        }

        public void setNodes(List<String> nodes) {
            this.nodes = nodes;
        }

        public String getPassword() {
            return this.password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }

    /**
     * Jedis client properties.
     */
    public static class Jedis {
        private Pool pool = new Pool();

        public Pool getPool() {
            return this.pool;
        }

        public void setPool(Pool pool) {
            this.pool = pool;
        }
    }

    /**
     * Pool properties.
     */
    public static class Pool {
        private int maxIdle = 8;
        private int minIdle = 1;
        private int maxActive = 8;
        private Integer maxWait = -1;//等待获取连接的超时时间（毫秒）
        private Integer timeBetweenEvictionRuns;//单位：毫秒

        public int getMaxIdle() {
            return this.maxIdle;
        }

        public void setMaxIdle(int maxIdle) {
            this.maxIdle = maxIdle;
        }

        public int getMinIdle() {
            return this.minIdle;
        }

        public void setMinIdle(int minIdle) {
            this.minIdle = minIdle;
        }

        public int getMaxActive() {
            return this.maxActive;
        }

        public void setMaxActive(int maxActive) {
            this.maxActive = maxActive;
        }

        public Integer getMaxWait() {
            return this.maxWait;
        }

        public void setMaxWait(Integer maxWait) {
            this.maxWait = maxWait;
        }

        public Integer getTimeBetweenEvictionRuns() {
            return this.timeBetweenEvictionRuns;
        }

        public void setTimeBetweenEvictionRuns(Integer timeBetweenEvictionRuns) {
            this.timeBetweenEvictionRuns = timeBetweenEvictionRuns;
        }
    }
}
