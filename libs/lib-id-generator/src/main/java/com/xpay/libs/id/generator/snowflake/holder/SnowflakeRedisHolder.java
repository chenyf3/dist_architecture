package com.xpay.libs.id.generator.snowflake.holder;

import com.xpay.libs.id.common.IdGenException;
import com.xpay.libs.id.common.Utils;
import com.xpay.libs.id.config.RedisProperties;
import com.xpay.libs.id.client.RedisClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

/**
 * 使用Redis来保存生成雪花算法的机器节点数据，相比起zookeeper集群来，redis集群的可靠性没那么高，
 * 但只要在新增节点或节点重启时没有出现master宕机和主从同步不一致的问题就可以（redis需要开启持久化）
 * @author chenyf
 */
public class SnowflakeRedisHolder {
    private static final Logger logger = LoggerFactory.getLogger(SnowflakeRedisHolder.class);
    private final RedisClient redisClient;//redis客户端
    private final String endpointDataKey;//在redis服务端保存当前节点数据的key
    private final String workerIdKey;//在redis服务端保存workerId的key
    private final String instanceId;//实例id，要保证全局唯一，可以使用当前节点的 ip、instanceNum 来构成，比如：10.10.10.01:1
    private final int currMinWorkerId;//当前集群的最小workerId
    private final int currMaxWorkerId;//当前集群的最大workerId
    private int workerId = -1;//当前实例的workerId

    /**
     * 构造器
     * @param instanceId
     * @param redisProperties
     * @param clusterName
     */
    public SnowflakeRedisHolder(String instanceId, String clusterName, RedisProperties redisProperties,
                                 Integer currMinWorkerId, Integer currMaxWorkerId) {
        this.currMinWorkerId = currMinWorkerId;
        this.currMaxWorkerId = currMaxWorkerId;
        this.instanceId = instanceId;
        String prefix = "snowflake.{" + clusterName; //为了使 redis cluster 模式下也能让两个key处于同一个节点
        this.endpointDataKey = prefix + "}.endpointData";
        this.workerIdKey = prefix + "}.workerId";
        this.redisClient = new RedisClient(redisProperties);
    }

    /**
     * 初始化
     * @return
     */
    public synchronized boolean init() {
        try {
            //1.初始化当前集群
            initCurrClusterIfNeed(workerIdKey);

            //2.检查当前节点是否已存在，如果已存在，则解析出数据并校验，如果校验通过则设置到当前实例对象上
            boolean isExists = loadEndpointData(endpointDataKey, instanceId);
            if (isExists) {
                logger.info("旧实例重启成功 currMinWorkerId={} currMaxWorkerId={} currWorkerId={} instanceId={}",
                        this.currMinWorkerId, this.currMaxWorkerId, this.workerId, this.instanceId);
                return true;
            }

            //3.如果当前节点的数据还不存在，则为当前节点生成一个新的workerId
            initEndpointData(endpointDataKey, instanceId, workerIdKey);
            logger.info("新实例启动成功 currMinWorkerId={} currMaxWorkerId={} currWorkerId={} instanceId={}",
                    this.currMinWorkerId, this.currMaxWorkerId, this.workerId, this.instanceId);
            return true;
        } catch(Exception e) {
            logger.error("Start node ERROR", e);
            return false;
        }
    }

    /**
     * 初始化当前集群
     * @param workerIdKey
     */
    private void initCurrClusterIfNeed(String workerIdKey) {
        if(currMinWorkerId < 0){
            return;
        }

        Long currMaxId = 0L;
        String currMaxIdStr = redisClient.get(workerIdKey);
        if(currMaxIdStr != null){
            currMaxId = Long.valueOf(currMaxIdStr);
        }

        boolean isInitialDone = false;
        while(currMaxId < currMinWorkerId-1){
            isInitialDone = true;
            currMaxId = redisClient.incr(workerIdKey);
        }

        if (isInitialDone) {
            logger.info("当前集群初始化成功 currMinWorkerId={} currMaxWorkerId={} currMaxId={}", currMinWorkerId, currMaxWorkerId, currMaxId);
        } else {
            logger.info("当前集群已无需初始化 currMinWorkerId={} currMaxWorkerId={} currMaxId={}", currMinWorkerId, currMaxWorkerId, currMaxId);
        }
    }

    /**
     * 加载节点数据
     * @param endpointDataKey
     * @param instanceId
     * @return
     */
    private boolean loadEndpointData(String endpointDataKey, String instanceId) {
        String endpointDataStr = redisClient.hget(endpointDataKey, instanceId);
        if (Utils.isEmpty(endpointDataStr)) {
            return false;
        }

        Endpoint endpoint = Utils.jsonToBean(endpointDataStr, Endpoint.class);
        if(endpoint.getTimestamp() > System.currentTimeMillis()) {
            throw new RuntimeException("redis服务端的时间大于当前节点的时间，请排查当前节点是否发生了时钟回拨！");
        }
        this.workerId = endpoint.getWorkerId();
        this.checkWorkerId();
        //定期上报节点的数据到 redis server
        scheduledUploadData(endpointDataKey, instanceId, this.workerId);
        return true;
    }

    /**
     * 初始化当前节点的数据，最重要的还是是workerId的分配，关于workerId的分配需要注意一下：
     * 1、如果只有一个机房，一般来说是所有机器共用一套redis集群，那么用这个方法获取出来的workerId是不会重复(redis主从同步不一致时除外)
     * 2、如果有多个机房，并且几个机房共用一套redis集群，那么用这个方法获取出来的workerId是不会重复(redis主从同步不一致时除外)
     * 3、如果有多个机房，并且每个机房有各自的redis集群，此时用这个方法获取的workerId就会造成重复了，此时需要对各个机房的实例进行分片规划，
     *   为各个机房设置不同的实例区间，比如：机房A：[1,20], 机房B：[21,40], 机房C：[41,60]，这样一来，只要每个机房的机器数量不超过20，
     *   就不会出现ID重复的情况
     * @param endpointDataKey
     * @param instanceId
     * @param workerIdKey
     * @return
     */
    private Endpoint initEndpointData(String endpointDataKey, String instanceId, String workerIdKey) {
        //1.生成一个workerId并分配给当前节点
        Long workerId = redisClient.incr(workerIdKey);
        this.workerId = workerId.intValue();
        this.checkWorkerId();
        //2.保存当前节点的相关数据到远程服务端(workerId等)，如果保存失败，则可能会因此浪费掉一个workerId
        Endpoint endpoint = updateEndpointData(endpointDataKey, instanceId, workerId.intValue());
        //3.定期上报节点的数据到 redis server
        scheduledUploadData(endpointDataKey, instanceId, this.workerId);
        return endpoint;
    }

    /**
     * 定时上报当前节点数据
     * @param endpointDataKey
     * @param instanceId
     * @param workerId
     */
    private void scheduledUploadData(final String endpointDataKey, final String instanceId, final int workerId) {
        Executors.newSingleThreadScheduledExecutor(new ThreadFactory() {
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r, "schedule-upload-time");
                thread.setDaemon(true);
                return thread;
            }
        }).scheduleWithFixedDelay(new Runnable() {
            public void run() {
                try{
                    updateEndpointData(endpointDataKey, instanceId, workerId);
                }catch(Exception e){
                    logger.error("上报节点数据时出现异常 instanceId={}", instanceId, e);
                }
            }
        }, 0L, 3L, TimeUnit.SECONDS);//每3s上报数据
    }

    /**
     * 更新节点数据到 redis server
     * @param endpointsKey
     * @param instanceId
     * @param workerId
     * @return
     */
    private Endpoint updateEndpointData(String endpointsKey, String instanceId, int workerId) {
        Endpoint endpoint = new Endpoint(workerId, System.currentTimeMillis());
        String json = Utils.beanToJson(endpoint);
        redisClient.hset(endpointsKey, instanceId, json);
        return endpoint;
    }

    private void checkWorkerId(){
        if(! (this.workerId >= this.currMinWorkerId && this.workerId <= currMaxWorkerId)){
            throw new IdGenException("redis分配的workerId: " + this.workerId + "必须在[" + currMinWorkerId + "," + currMaxWorkerId + "]之间");
        }
    }

    /**
     * 获取workerId
     * @return
     */
    public int getWorkerId(){
        return this.workerId;
    }

    /**
     * 销毁当前实例
     */
    public void destroy(){
        redisClient.destroy();
    }

    /**
     * 上报的数据结构
     */
    static class Endpoint {
        private Integer workerId;
        private long timestamp;

        public Endpoint(){}

        public Endpoint(int workerId, long timestamp) {
            this.workerId = workerId;
            this.timestamp = timestamp;
        }

        public int getWorkerId() {
            return workerId;
        }

        public void setWorkerId(Integer workerId) {
            this.workerId = workerId;
        }

        public long getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(long timestamp) {
            this.timestamp = timestamp;
        }
    }
}
