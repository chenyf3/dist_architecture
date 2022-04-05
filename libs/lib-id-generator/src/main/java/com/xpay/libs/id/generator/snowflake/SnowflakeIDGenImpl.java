package com.xpay.libs.id.generator.snowflake;

import com.xpay.libs.id.config.SnowFlakeProperties;
import com.xpay.libs.id.generator.IDGen;
import com.xpay.libs.id.common.IdGenException;
import com.xpay.libs.id.generator.snowflake.holder.SnowflakeRedisHolder;
import com.xpay.libs.id.generator.snowflake.holder.SnowflakeZookeeperHolder;

import java.util.Random;

public class SnowflakeIDGenImpl implements IDGen {
    private final static String JVM_MIN_WORKER_ID = "minWorkerId";//当前集群的最小workerId
    private final static String JVM_MAX_WORKER_ID = "maxWorkerId";//当前集群的最大workerId

    private final long twepoch;//开始时间戳
    private final long workerIdBits = 10L;//workerId占用多少位
    private final long sequenceBits = 12L;//序列号占用多少位
    private final long maxWorkerId = ~(-1L << workerIdBits); //最大能够分配的workerId = 1023
    private final long workerIdLeftShift = sequenceBits;//workerId应该左移多少位：10位
    private final long timestampLeftShift = sequenceBits + workerIdBits;//时间戳应该左移多少位：22位
    private final long sequenceMask = ~(-1L << sequenceBits);//序列号掩码，十进制数是：4095，二进制数是：111111111111
    private long workerId = -1;
    private long sequence = 0L;
    private long lastTimestamp = -1L;
    private static final Random RANDOM = new Random();
    private Integer currMinWorkerId;//当前集群的最小workerId
    private Integer currMaxWorkerId;//当前集群的最大workerId

    private SnowflakeRedisHolder redisHolder;
    private SnowflakeZookeeperHolder zkHolder;

    /**
     * 使用Zookeeper获取雪花算法的workerId，并定时上报节点时间
     * @param instanceId    snowflake实例id，需保证全局唯一
     * @param twepoch       起始时间戳
     * @param clusterName   集群名称
     * @param properties    雪花算法配置
     */
    public SnowflakeIDGenImpl(String instanceId, long twepoch, String clusterName, SnowFlakeProperties properties) {
        this.twepoch = twepoch;
        initParamAndCheck(properties);
        if(properties.getZkReport() != null && properties.getZkReport().isServerConfig()){
            zkHolder = new SnowflakeZookeeperHolder(instanceId, clusterName, properties.getZkReport(), currMinWorkerId, currMaxWorkerId);
        }else if(properties.getRedisReport() != null && properties.getRedisReport().isServerConfig()){
            redisHolder = new SnowflakeRedisHolder(instanceId, clusterName, properties.getRedisReport(), currMinWorkerId, currMaxWorkerId);
        }else{
            throw new IdGenException("请指定zookeeper或者redis作为雪花算法的数据上报中心");
        }
    }

    /**
     * 初始化
     * @return
     */
    public boolean init() {
        boolean initSuccess = false;
        Integer workerId = null;
        String holderName = null;
        if(zkHolder != null){
            initSuccess = zkHolder.init();
            workerId = zkHolder.getWorkerId();
            holderName = "ZookeeperHolder";
        }else if(redisHolder != null){
            initSuccess = redisHolder.init();
            workerId = redisHolder.getWorkerId();
            holderName = "RedisHolder";
        }

        if (initSuccess) {
            this.workerId = workerId;
        } else {
            throw new IdGenException("Snowflake IDGen With " + holderName + " Initial Fail !");
        }
        return true;
    }

    @Override
    public synchronized Long get(String key) {
        long timestamp = timeGen();
        if (timestamp < lastTimestamp) {
            long offset = lastTimestamp - timestamp;
            if (offset <= 5) {//如果发生很短时间的时钟回拨，则休眠一小段时间，等待回拨的时间过去
                try {
                    wait(offset << 1);
                    timestamp = timeGen();
                    if (timestamp < lastTimestamp) {
                        throw new IdGenException("The Server's Clock Has Back Forward Too Much, Cannot Wait, Please Try Again!");
                    }
                } catch (InterruptedException e) {
                    throw new IdGenException("The Server's Clock Was Interrupted, Please Try Again!");
                }
            } else {
                throw new IdGenException("The Server's Clock Has Back Forward Too Much, Please Try Again!");
            }
        }
        if (lastTimestamp == timestamp) {
            sequence = (sequence + 1) & sequenceMask;
            if (sequence == 0) { //等于0时，表示当前毫秒内的序列号已经用完，则等到下一毫秒，出现0的时刻为：1000000000000 & 111111111111 = 0000000000000
                sequence = RANDOM.nextInt(100);
                timestamp = tilNextMillis(lastTimestamp);
            }
        } else {
            //如果是新的毫秒开始，在一定范围内取随机值，避免毫秒切换时总是从0开始递增
            sequence = RANDOM.nextInt(100);
        }
        lastTimestamp = timestamp;
        //高42位是时间戳(首位是1时，将发生溢出，此算法不再适用)，中间10位是workerId，低12位是序列号，总共64位构成Long型数据
        long id = ((timestamp - twepoch) << timestampLeftShift) | (workerId << workerIdLeftShift) | sequence;
        return id;
    }

    @Override
    public Long get(String key, long maxValue) throws IdGenException {
        throw new IdGenException("Not Support!");
    }

    protected long tilNextMillis(long lastTimestamp) {
        long timestamp = timeGen();
        while (timestamp <= lastTimestamp) {
            timestamp = timeGen();
        }
        return timestamp;
    }

    protected long timeGen() {
        return System.currentTimeMillis();
    }

    public long getWorkerId() {
        return workerId;
    }

    public void destroy(){
        if(redisHolder != null){
            redisHolder.destroy();
        }
        if(zkHolder != null){
            zkHolder.destroy();
        }
    }

    private void initParamAndCheck(SnowFlakeProperties properties){
        if(timeGen() < twepoch){
            throw new IdGenException("Snowflake not support twepoch gt currentTime");
        }

        Integer minId = properties.getMinWorkerId();
        String minIdStr = System.getProperty(JVM_MIN_WORKER_ID, null);
        if(minIdStr != null && minIdStr.trim().length() > 0){
            minId = Integer.valueOf(minIdStr);
        }
        currMinWorkerId = minId < 0 ? 0 : minId;

        Integer maxId = (int) maxWorkerId;
        String maxIdStr = System.getProperty(JVM_MAX_WORKER_ID, null);
        if(maxIdStr != null && maxIdStr.trim().length() > 0){
            maxId = Integer.valueOf(maxIdStr);
        }
        currMaxWorkerId = maxId;

        if(currMinWorkerId > currMaxWorkerId){
            throw new IdGenException("当前集群的最小workerId: "+currMinWorkerId+" 须小于或等于当前集群的最大workerId：" + currMaxWorkerId);
        }
    }
}
