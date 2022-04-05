package com.xpay.libs.id.config;

/**
 * 数据库分段发号的相关配置
 */
public class SegmentProperties {
    private boolean enabled = false;
    private int segmentStep = 2000;//分段增长初始步长
    private boolean lazily = true;//是否懒加载，即只有使用到某个biz_key的时候才会把这它加载到内存中
    private String name;
    private String jdbcUrl;
    private String username;
    private String password;
    private String driverClassName;
    private int initialSize = 0;
    private int maxActive = 8;
    private int minIdle = 0;
    private long maxWait = -1L;
    private boolean poolPreparedStatements = false;
    private boolean sharePreparedStatements = false;
    private int maxPoolPreparedStatementPerConnectionSize = 10;
    private boolean initExceptionThrow = true;
    private int maxOpenPreparedStatements = -1;
    private int connectionErrorRetryAttempts = 1;
    private String validationQuery = null;
    private int validationQueryTimeout = -1;
    private boolean testOnBorrow = false;
    private boolean testOnReturn = false;
    private boolean testWhileIdle = true;
    private long timeBetweenEvictionRunsMillis = 60000L;
    private long minEvictableIdleTimeMillis = 1800000L;
    private long maxEvictableIdleTimeMillis = 25200000L;
    private long keepAliveBetweenTimeMillis = 120000L;
    private boolean asyncInit = false;

    public boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public int getSegmentStep() {
        return segmentStep;
    }

    public void setSegmentStep(int segmentStep) {
        this.segmentStep = segmentStep;
    }

    public boolean getLazily() {
        return lazily;
    }

    public void setLazily(boolean lazily) {
        this.lazily = lazily;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getJdbcUrl() {
        return jdbcUrl;
    }

    public void setJdbcUrl(String jdbcUrl) {
        this.jdbcUrl = jdbcUrl;
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

    public String getDriverClassName() {
        return driverClassName;
    }

    public void setDriverClassName(String driverClassName) {
        this.driverClassName = driverClassName;
    }

    public int getInitialSize() {
        return initialSize;
    }

    public void setInitialSize(int initialSize) {
        this.initialSize = initialSize;
    }

    public int getMaxActive() {
        return maxActive;
    }

    public void setMaxActive(int maxActive) {
        this.maxActive = maxActive;
    }

    public int getMinIdle() {
        return minIdle;
    }

    public void setMinIdle(int minIdle) {
        this.minIdle = minIdle;
    }

    public long getMaxWait() {
        return maxWait;
    }

    public void setMaxWait(long maxWait) {
        this.maxWait = maxWait;
    }

    public boolean getPoolPreparedStatements() {
        return poolPreparedStatements;
    }

    public void setPoolPreparedStatements(boolean poolPreparedStatements) {
        this.poolPreparedStatements = poolPreparedStatements;
    }

    public boolean getSharePreparedStatements() {
        return sharePreparedStatements;
    }

    public void setSharePreparedStatements(boolean sharePreparedStatements) {
        this.sharePreparedStatements = sharePreparedStatements;
    }

    public int getMaxPoolPreparedStatementPerConnectionSize() {
        return maxPoolPreparedStatementPerConnectionSize;
    }

    public void setMaxPoolPreparedStatementPerConnectionSize(int maxPoolPreparedStatementPerConnectionSize) {
        this.maxPoolPreparedStatementPerConnectionSize = maxPoolPreparedStatementPerConnectionSize;
    }

    public boolean getInitExceptionThrow() {
        return initExceptionThrow;
    }

    public void setInitExceptionThrow(boolean initExceptionThrow) {
        this.initExceptionThrow = initExceptionThrow;
    }

    public int getMaxOpenPreparedStatements() {
        return maxOpenPreparedStatements;
    }

    public void setMaxOpenPreparedStatements(int maxOpenPreparedStatements) {
        this.maxOpenPreparedStatements = maxOpenPreparedStatements;
    }

    public int getConnectionErrorRetryAttempts() {
        return connectionErrorRetryAttempts;
    }

    public void setConnectionErrorRetryAttempts(int connectionErrorRetryAttempts) {
        this.connectionErrorRetryAttempts = connectionErrorRetryAttempts;
    }

    public String getValidationQuery() {
        return validationQuery;
    }

    public void setValidationQuery(String validationQuery) {
        this.validationQuery = validationQuery;
    }

    public int getValidationQueryTimeout() {
        return validationQueryTimeout;
    }

    public void setValidationQueryTimeout(int validationQueryTimeout) {
        this.validationQueryTimeout = validationQueryTimeout;
    }

    public boolean getTestOnBorrow() {
        return testOnBorrow;
    }

    public void setTestOnBorrow(boolean testOnBorrow) {
        this.testOnBorrow = testOnBorrow;
    }

    public boolean getTestOnReturn() {
        return testOnReturn;
    }

    public void setTestOnReturn(boolean testOnReturn) {
        this.testOnReturn = testOnReturn;
    }

    public boolean getTestWhileIdle() {
        return testWhileIdle;
    }

    public void setTestWhileIdle(boolean testWhileIdle) {
        this.testWhileIdle = testWhileIdle;
    }

    public long getTimeBetweenEvictionRunsMillis() {
        return timeBetweenEvictionRunsMillis;
    }

    public void setTimeBetweenEvictionRunsMillis(long timeBetweenEvictionRunsMillis) {
        this.timeBetweenEvictionRunsMillis = timeBetweenEvictionRunsMillis;
    }

    public long getMinEvictableIdleTimeMillis() {
        return minEvictableIdleTimeMillis;
    }

    public void setMinEvictableIdleTimeMillis(long minEvictableIdleTimeMillis) {
        this.minEvictableIdleTimeMillis = minEvictableIdleTimeMillis;
    }

    public long getMaxEvictableIdleTimeMillis() {
        return maxEvictableIdleTimeMillis;
    }

    public void setMaxEvictableIdleTimeMillis(long maxEvictableIdleTimeMillis) {
        this.maxEvictableIdleTimeMillis = maxEvictableIdleTimeMillis;
    }

    public long getKeepAliveBetweenTimeMillis() {
        return keepAliveBetweenTimeMillis;
    }

    public void setKeepAliveBetweenTimeMillis(long keepAliveBetweenTimeMillis) {
        this.keepAliveBetweenTimeMillis = keepAliveBetweenTimeMillis;
    }

    public boolean getAsyncInit() {
        return asyncInit;
    }

    public void setAsyncInit(boolean asyncInit) {
        this.asyncInit = asyncInit;
    }
}
