package com.xpay.libs.id;

import com.xpay.libs.id.common.Utils;
import com.xpay.libs.id.config.RedisProperties;
import com.xpay.libs.id.config.SnowFlakeProperties;
import com.xpay.libs.id.generator.IDGen;
import com.xpay.libs.id.generator.zero.ZeroIDGen;
import com.xpay.libs.id.service.SnowflakeService;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 测试环境：Windows10,8C16G,IntelliJ IDEA 2020.2.4 (Ultimate Edition)
 */
public class SnowflakeIDGenTest {
    SnowflakeService snowflakeService;

    @Ignore
    @Before
    public void before(){
    }

    /**
     * 测试使用zookeeper作为workerId生成器和数据上报服务器
     */
    @Ignore
    @Test
    public void testGetIdWithZkHolder() {
        initSnowflakeService("zookeeper", "");

        for (int i = 1; i < 1000; ++i) {
            try {
                Long r = snowflakeService.getId();
                System.out.println(r);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        sleep(5);//等待日志打印完毕
    }

    /**
     * 测试使用redis作为workerId生成器和数据上报服务器
     */
    @Ignore
    @Test
    public void testGetIdWithRedisHolder() {
        initSnowflakeService("redis", "cluster");

        for (int i = 1; i < 1000; ++i) {
            try {
                Long r = snowflakeService.getId();
                System.out.println(r);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        sleep(5);//等待日志打印完毕等
    }

    /**
     * 测试批量获取id
     */
    @Ignore
    @Test
    public void testBatchGetId() {
        initSnowflakeService("zookeeper", "sentinel");

        long start = System.currentTimeMillis();
        List<Long> idList = snowflakeService.getId(100000);
        long timeCost = System.currentTimeMillis() - start;
        System.out.println("timeCost = "+timeCost+"(ms) testBatchGetId: " + Utils.beanToJson(idList));
    }

    /**
     * 单线程性能测试
     * 测试结果：testGetId totalCount = 50000000 timeCost = 12581(ms) tps = 3974246.880216199 failCount = 0
     */
    @Ignore
    @Test
    public void testGetId(){
        initSnowflakeService("redis", "sentinel");

        int i = 0, max = 50000000, failCount = 0;
        long start = System.currentTimeMillis();
        while(++i <= max){
            try{
                Long id = snowflakeService.getId();
//                System.out.println("index="+i+",id="+id);//println语句比较耗时，对测试影响很大
            }catch(Exception e){
                failCount ++;
//                e.printStackTrace();
            }
        }

        long timeCost = System.currentTimeMillis() - start;
        double tps = max / (timeCost/1000d);
        System.out.println("testGetId totalCount = " + max + " timeCost = " + timeCost + "(ms) tps = " + tps + " failCount = " + failCount);
    }

    /**
     * 多线程性能测试
     * 测试结果：testGetIdMulti totalCount = 12000000 timeCost = 3071(ms) tps = 3907521.979811136 failCount = 0
     */
    @Ignore
    @Test
    public void testGetIdMulti(){
        initSnowflakeService("redis", "sentinel");

        long start = System.currentTimeMillis();
        int threadCount = 8, maxPerThread = 1500000;
        CountDownLatch countDown = new CountDownLatch(threadCount);
        AtomicInteger totalCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);
        while(threadCount-- > 0){
            new Thread(() -> {
                int i = 0;
                while(++i <= maxPerThread){
                    try{
                        Long id = snowflakeService.getId();
                        int count = totalCount.incrementAndGet();
//                        System.out.println("totalCount=" + count + ",id="+id);//println语句比较耗时，对测试影响很大
                    }catch(Exception e){
                        failCount.incrementAndGet();
                        totalCount.incrementAndGet();
//                        e.printStackTrace();
                    }
                }
                countDown.countDown();;
            }).start();
        }

        try{
            countDown.await();
        }catch(Exception e){
            e.printStackTrace();
        }

        long timeCost = System.currentTimeMillis() - start;
        double tps = totalCount.get() / (timeCost/1000d);
        System.out.println("testGetIdMulti totalCount = " + totalCount.get() + " timeCost = " + timeCost + "(ms) tps = " + tps + " failCount = " + failCount.get());
    }

    /**
     * 测试ZeroIDGen，即试一下纯内存计算并且无任何其他加锁情况下tps有多少
     * 测试结果：testGetZeroId totalCount = 50000000 timeCost = 45(ms) tps = 1.1111111111111112E9 failCount = 0
     */
    @Ignore
    @Test
    public void testGetZeroId(){
        IDGen idGen = new ZeroIDGen();

        int i = 0, max = 50000000, failCount = 0;
        long start = System.currentTimeMillis();
        while(++i <= max){
            try{
                Long id = idGen.get("key");
            }catch(Exception e){
                failCount ++;
            }
        }

        long timeCost = System.currentTimeMillis() - start;
        double tps = max / (timeCost/1000d);
        System.out.println("testGetZeroId totalCount = " + max + " timeCost = " + timeCost + "(ms) tps = " + tps + " failCount = " + failCount);
    }


    private void sleep(int second){
        try {
            Thread.sleep(second * 1000L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Ignore
    @After
    public void after() {
        if(snowflakeService != null){
            snowflakeService.destroy();
        }
    }

    /**
     * 初始化SnowflakeService
     * @param holder_type   holder类型：redis、zookeeper
     * @param cluster_type  redis集群类型：standalone、sentinel、cluster
     */
    private void initSnowflakeService(String holder_type, String cluster_type){
        String zkUser = "sequence_admin";
        String zkPassword = "RexelZkAdmin#982";
        String redisPassword = "redisPwd321";

        SnowFlakeProperties properties = new SnowFlakeProperties();
        properties.setEnabled(true);
        properties.setClusterName("mdwIdGenerator");
        properties.setInstanceIdType(SnowFlakeProperties.InstanceIdType.IP);
        properties.setInstanceNum(2030);

        if("redis".equals(holder_type)){
            RedisProperties redisProp = new RedisProperties();
            if("standalone".equals(cluster_type)){
                //单机模式
                redisProp.setHost("10.10.10.37");
                redisProp.setPort(6379);
                redisProp.setPassword(redisPassword);
            }else if("sentinel".equals(cluster_type)){
                //哨兵模式
                RedisProperties.Sentinel sentinel = new RedisProperties.Sentinel();
                sentinel.setMaster("master01");
                sentinel.setNodes(Arrays.asList(new String[]{"10.10.10.37:26379", "10.10.10.38:26379", "10.10.10.39:26379"}));
                sentinel.setPassword(redisPassword);
                redisProp.setSentinel(sentinel);
            }else if("cluster".equals(cluster_type)){
                //cluster模式
                RedisProperties.Cluster cluster = new RedisProperties.Cluster();
                cluster.setMaxRedirects(3);
                cluster.setNodes(Arrays.asList(new String[]{"10.10.10.37:6301", "10.10.10.38:6301", "10.10.10.39:6301"}));
                cluster.setPassword(redisPassword);
                redisProp.setCluster(cluster);
            }

            RedisProperties.Pool pool = new RedisProperties.Pool();
            pool.setMaxActive(30);
            pool.setMaxWait(3000);//毫秒
            redisProp.getJedis().setPool(pool);

            properties.setRedisReport(redisProp);
        }else if("zookeeper".equals(holder_type)){
            SnowFlakeProperties.Zookeeper zkCfg = new SnowFlakeProperties.Zookeeper();
            zkCfg.setConnectionString("10.10.10.39:2181");
            zkCfg.setConnectionTimeout(30000);
            zkCfg.setUsername(zkUser);
            zkCfg.setPassword(zkPassword);
            properties.setZkReport(zkCfg);
        }

        snowflakeService = new SnowflakeService(properties);
    }
}
