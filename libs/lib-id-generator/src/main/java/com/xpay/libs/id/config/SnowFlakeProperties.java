package com.xpay.libs.id.config;

/**
 * 雪花算法的相关配置
 */
public class SnowFlakeProperties {
    private boolean enabled = false;
    private String clusterName = "default";//集群名称
    private InstanceIdType instanceIdType = InstanceIdType.HOST;
    private int instanceNum = 1;//实例编号，如果在同一台机器部署多个实例，可通过此编号来区分不同的实例，也可通过JVM启动参数 -DinstanceNum=xxx 来指定
    private Integer minWorkerId = 0;//雪花算法的最小workerId，也可通过JVM启动参数 -DminWorkerId=xxx 来指定
    private Integer maxWorkerId = 1023;//雪花算法的最大workerId，也可通过JVM启动参数 -DmaxWorkerId=xxx 来指定
    private Zookeeper zkReport = new Zookeeper();
    private RedisProperties redisReport = new RedisProperties();

    public boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    public InstanceIdType getInstanceIdType() {
        return instanceIdType;
    }

    public void setInstanceIdType(InstanceIdType instanceIdType) {
        this.instanceIdType = instanceIdType;
    }

    public int getInstanceNum() {
        return instanceNum;
    }

    public void setInstanceNum(int instanceNum) {
        this.instanceNum = instanceNum;
    }

    public Integer getMinWorkerId() {
        return minWorkerId;
    }

    public void setMinWorkerId(Integer minWorkerId) {
        this.minWorkerId = minWorkerId;
    }

    public Integer getMaxWorkerId() {
        return maxWorkerId;
    }

    public void setMaxWorkerId(Integer maxWorkerId) {
        this.maxWorkerId = maxWorkerId;
    }

    public Zookeeper getZkReport() {
        return zkReport;
    }

    public void setZkReport(Zookeeper zkReport) {
        this.zkReport = zkReport;
    }

    public RedisProperties getRedisReport() {
        return redisReport;
    }

    public void setRedisReport(RedisProperties redisReport) {
        this.redisReport = redisReport;
    }

    /**
     * 用什么作为实例id：host、ip
     */
    public enum InstanceIdType {
        HOST, IP
    }

    public static class Zookeeper {
        private String connectionString;
        private String username;
        private String password;
        private int connectionTimeout = 25000;//连接超时时间(单位：毫秒)
        private int sessionTimeout = 30000;//session超时时间(单位：毫秒)
        private int maxElapsedTime = 2000;//最大重试时间,即超过此时间后不再重连(单位：毫秒)
        private int sleepMsBetweenRetries = 400;//每次重试的间隔时间(单位：毫秒)

        public String getConnectionString() {
            return connectionString;
        }

        public void setConnectionString(String connectionString) {
            this.connectionString = connectionString;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public int getConnectionTimeout() {
            return connectionTimeout;
        }

        public void setConnectionTimeout(int connectionTimeout) {
            this.connectionTimeout = connectionTimeout;
        }

        public int getSessionTimeout() {
            return sessionTimeout;
        }

        public void setSessionTimeout(int sessionTimeout) {
            this.sessionTimeout = sessionTimeout;
        }

        public int getMaxElapsedTime() {
            return maxElapsedTime;
        }

        public void setMaxElapsedTime(int maxElapsedTime) {
            this.maxElapsedTime = maxElapsedTime;
        }

        public int getSleepMsBetweenRetries() {
            return sleepMsBetweenRetries;
        }

        public void setSleepMsBetweenRetries(int sleepMsBetweenRetries) {
            this.sleepMsBetweenRetries = sleepMsBetweenRetries;
        }

        /**
         * 判断是否有服务端配置
         * @return
         */
        public boolean isServerConfig(){
            return (getConnectionString() != null && getConnectionString().length() > 0) ;
        }
    }
}
