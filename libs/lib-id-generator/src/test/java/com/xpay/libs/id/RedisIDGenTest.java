package com.xpay.libs.id;

import com.xpay.libs.id.common.Utils;
import com.xpay.libs.id.config.RedisProperties;
import com.xpay.libs.id.service.RedisIdService;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 测试环境：Windows10,8C16G,IntelliJ IDEA 2020.2.4 (Ultimate Edition)
 */
public class RedisIDGenTest {
    private String segmentExcludeKey = "testGetIdSegmentExclude";
    RedisIdService redisIdService;

    @Ignore
    @Before
    public void before(){
    }

    /**
     * 单线程性能测试
     * 测试结果：testGetId totalCount = 50000000 timeCost = 6321(ms) tps = 7910140.800506249 failCount = 0
     */
    @Ignore
    @Test
    public void testGetId() {
        initRedisIdService("standalone");//standalone、sentinel、cluster

        String key = "testGetId";
        int i = 0, max = 50000000, failCount = 0;

        long start = System.currentTimeMillis();
        while(++i <= max){
            try{
                Long id = redisIdService.getId(key);
//                System.out.println("index="+i+",id="+id); //println语句比较耗时，对测试影响很大
            }catch(Exception e){
//                e.printStackTrace();
                failCount ++;
            }
        }

        long timeCost = System.currentTimeMillis() - start;
        double tps = max / (timeCost/1000d);
        System.out.println("testGetId totalCount = " + max + " timeCost = " + timeCost + "(ms) tps = " + tps + " failCount = " + failCount);
    }

    /**
     * 多线程性能测试
     * 测试结果：testGetIdMulti totalCount = 12000000 timeCost = 5021(ms) tps = 2389962.1589324838 failCount = 10
     */
    @Ignore
    @Test
    public void testGetIdMulti(){
        initRedisIdService("sentinel");//standalone、sentinel、cluster

        String key = "testGetIdMulti";
        int threadCount = 8; long maxPerThread = 1500000;
        CountDownLatch countDown = new CountDownLatch(threadCount);
        AtomicInteger totalCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);
        long start = System.currentTimeMillis();
        while(threadCount-- > 0){
            new Thread(() -> {
                int i = 0;
                while(++i <= maxPerThread){
                    try{
                        Long id = redisIdService.getId(key);
                        int count = totalCount.incrementAndGet();
//                        System.out.println("totalCount=" + count + ",id="+id); //println语句比较耗时，对测试影响很大
                    }catch(Exception e){
                        failCount.incrementAndGet();
                        totalCount.incrementAndGet();
//                        e.printStackTrace();
                    }
                }
                countDown.countDown();
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
     * 测试获取id时指定最大id，并测试看能不能做到id循环
     */
    @Ignore
    @Test
    public void testGetIdWithMaxId() {
        initRedisIdService("sentinel");//standalone、sentinel、cluster

        String key = "testGetIdWithMaxId";
        int i = 0;
        long maxValue = 128000 + 1;
        long totalCount = maxValue + 100;
        int failCount = 0;

        long start = System.currentTimeMillis();
        while(++i <= totalCount){
            try{
                Long id = redisIdService.getId(key, maxValue);
                System.out.println("index="+i+",id="+id);
            }catch(Exception e){
//                e.printStackTrace();
                failCount ++;
            }
        }

        long timeCost = System.currentTimeMillis() - start;
        System.out.println("testGetIdWithMaxId totalCount = " + totalCount + " timeCost = " + timeCost + "(ms) failCount = " + failCount);
    }

    /**
     * 测试批量获取id
     */
    @Ignore
    @Test
    public void testBatchGetId() {
        initRedisIdService("sentinel");//standalone、sentinel、cluster

        long start = System.currentTimeMillis();
        List<Long> idList = redisIdService.getId("testBatchGetId", 100000);
        long timeCost = System.currentTimeMillis() - start;
        System.out.println("timeCost = "+timeCost+"(ms) testBatchGetId: " + Utils.beanToJson(idList));
    }

    /**
     * 测试redis不使用分段发号的性能
     * testGetIdSegmentExclude totalCount = 16000 timeCost = 15754(ms) tps = 1015.615081883966 failCount = 0
     */
    @Ignore
    @Test
    public void testGetIdSegmentExclude(){
        initRedisIdService("sentinel");//standalone、sentinel、cluster

        String key = segmentExcludeKey;
        int threadCount = 8; long maxPerThread = 2000;
        CountDownLatch countDown = new CountDownLatch(threadCount);
        AtomicInteger totalCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);
        long start = System.currentTimeMillis();
        while(threadCount-- > 0){
            new Thread(() -> {
                int i = 0;
                while(++i <= maxPerThread){
                    try{
                        Long id = redisIdService.getId(key);
                        int count = totalCount.incrementAndGet();
//                        System.out.println("totalCount=" + count + ",id="+id); //println语句比较耗时，对测试影响很大
                    }catch(Exception e){
                        failCount.incrementAndGet();
                        totalCount.incrementAndGet();
//                        e.printStackTrace();
                    }
                }
                countDown.countDown();
            }).start();
        }

        try{
            countDown.await();
        }catch(Exception e){
            e.printStackTrace();
        }

        long timeCost = System.currentTimeMillis() - start;
        double tps = totalCount.get() / (timeCost/1000d);
        System.out.println("testGetIdSegmentExclude totalCount = " + totalCount.get() + " timeCost = " + timeCost + "(ms) tps = " + tps + " failCount = " + failCount.get());
    }

    /**
     * 测试稳定性，查看日志输出文件：d:/log/id-generator-stable-test.log，日志路径和文件名请查看 resources/log4j2.xml
     */
    @Ignore
    @Test
    public void testGetIdStable() {
        initRedisIdService("sentinel");//standalone、sentinel、cluster

        Logger logger = LoggerFactory.getLogger("com.xpay.middleware.id.testGetIdStable");

        String key = "testGetIdStable";
        java.text.DecimalFormat df = new java.text.DecimalFormat("#.00");
        int i = 0, max = Integer.MAX_VALUE, failCount = 0;
        long totalCount = 0, currCount = 0;

        long start = System.currentTimeMillis();
        while(++i <= max){
            try{
                Long id = redisIdService.getId(key);
            }catch(Exception e){
//                e.printStackTrace();
                failCount ++;
            }

            totalCount ++;
            currCount ++;
            long timeCost = System.currentTimeMillis() - start;
            if(timeCost - 5000 > 0){ //每5秒输出一次
                String tps = df.format(currCount / (timeCost/1000d));
                logger.info("testGetIdStableOut totalCount = " + totalCount + " currCount = " + currCount + " timeCost = " + timeCost + "(ms) tps = " + tps + " failCount = " + failCount);

                failCount = 0;
                currCount = 0;
                start = System.currentTimeMillis();
            }
        }
    }

    /**
     * 测试大量key时的tps性能（设置key和获取id在一起进行的）
     * 测试结果：testMultiKeyGetId keyCount = 10000 totalCount = 12000000 timeCost = 97794(ms) tps = 122706.91453463404 failCount = 0
     */
    @Ignore
    @Test
    public void testMultiKeyGetId(){
        initRedisIdService("sentinel");//standalone、sentinel、cluster

        int keyCount = 10000;
        Map<String, String> realKeyMap = new ConcurrentHashMap<>();
        String format = "%1$0" + String.valueOf(keyCount).length() + "d";
        String key = "testMultiKeyGetId";
        int threadCount = 8; long maxPerThread = 1500000;
        CountDownLatch countDown = new CountDownLatch(threadCount);
        AtomicInteger totalCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);
        Random random = new Random();
        long start = System.currentTimeMillis();
        while (threadCount-- > 0) {
            new Thread(() -> {
                int i = 0;
                while (++i <= maxPerThread) {
                    try {
                        int index = random.nextInt(keyCount);
                        String keyTemp = key + "_" + String.format(format, index);
                        realKeyMap.putIfAbsent(keyTemp, "");

                        Long id = redisIdService.getId(keyTemp);
                        int count = totalCount.incrementAndGet();
//                        System.out.println("totalCount=" + count + ",id="+id); //println语句比较耗时，对测试影响很大
                    } catch(Exception e) {
                        failCount.incrementAndGet();
                        totalCount.incrementAndGet();
//                        e.printStackTrace();
                    }
                }
                countDown.countDown();
            }).start();
        }

        try{
            countDown.await();
        }catch(Exception e){
            e.printStackTrace();
        }

        int realKeyCount = realKeyMap.size();
        long timeCost = System.currentTimeMillis() - start;
        double tps = totalCount.get() / (timeCost/1000d);
        System.out.println("testMultiKeyGetId keyCount = " + realKeyCount + " totalCount = " + totalCount.get() + " timeCost = " + timeCost + "(ms) tps = " + tps + " failCount = " + failCount.get());
    }

    /**
     * 测试大量key时的tps性能（设置key和获取id是分开的，先设置key再获取id）
     * 测试结果：
     *
     * testMultiKeyGetIdSplit keyCount = 10000 totalCount = 12000000 timeCost = 4726(ms) tps = 2539145.1544646635 failCount = 0
     */
    @Ignore
    @Test
    public void testMultiKeyGetIdSplit(){
        initRedisIdService("cluster");//standalone、sentinel、cluster

        //1、先设置大量的key
        int keyCount = 10000;
        List<String> keyList = new ArrayList<>();
        String format = "%1$0" + String.valueOf(keyCount).length() + "d";
        String key = "testMultiKeyGetId";
        long start = System.currentTimeMillis();
        for (int i=1; i<=keyCount; i++) {
            try {
                String keyTemp = key + "_" + String.format(format, i);
                keyList.add(keyTemp);
                Long id = redisIdService.getId(keyTemp);
            } catch(Exception e) {
            }
        }
        long timeCost = System.currentTimeMillis() - start;
        double tps = keyCount / (timeCost/1000d);
        System.out.println("setKey keyCount = " + keyCount + " timeCost = " + timeCost + "(ms) tps = " + tps);


        Map<String, String> realKeyMap = new ConcurrentHashMap<>();
        int threadCount = 8; long maxPerThread = 1500000;
        CountDownLatch countDown = new CountDownLatch(threadCount);
        AtomicInteger totalCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);
        Random random = new Random();
        start = System.currentTimeMillis();
        while (threadCount-- > 0) {
            new Thread(() -> {
                int i = 0;
                while (++i <= maxPerThread) {
                    try {
                        int index = random.nextInt(keyCount);
                        String keyTemp = keyList.get(index);
                        realKeyMap.putIfAbsent(keyTemp, "");

                        Long id = redisIdService.getId(keyTemp);
                        int count = totalCount.incrementAndGet();
//                        System.out.println("totalCount=" + count + ",id="+id); //println语句比较耗时，对测试影响很大
                    } catch(Exception e) {
                        failCount.incrementAndGet();
                        totalCount.incrementAndGet();
//                        e.printStackTrace();
                    }
                }
                countDown.countDown();
            }).start();
        }

        try{
            countDown.await();
        }catch(Exception e){
            e.printStackTrace();
        }

        int realKeyCount = realKeyMap.size();
        timeCost = System.currentTimeMillis() - start;
        tps = totalCount.get() / (timeCost/1000d);
        System.out.println("testMultiKeyGetIdSplit keyCount = " + realKeyCount + " totalCount = " + totalCount.get() + " timeCost = " + timeCost + "(ms) tps = " + tps + " failCount = " + failCount.get());
    }

    @Ignore
    @After
    public void after() {
        if(redisIdService != null){
            redisIdService.destroy();
        }
    }

    private void initRedisIdService(String cluster_type){
        String redisPassword = "redisPwd321";
        RedisProperties properties = new RedisProperties();
        if("standalone".equals(cluster_type)){
            //单机模式
            properties.setHost("10.10.10.39");
            properties.setPort(6379);
            properties.setPassword(redisPassword);
        }else if("sentinel".equals(cluster_type)){
            //哨兵模式
            RedisProperties.Sentinel sentinel = new RedisProperties.Sentinel();
            sentinel.setMaster("master01");
            sentinel.setNodes(Arrays.asList(new String[]{"10.10.10.37:26379", "10.10.10.38:26379", "10.10.10.39:26379"}));
            sentinel.setPassword(redisPassword);
            properties.setSentinel(sentinel);
        }else if("cluster".equals(cluster_type)){
            //cluster模式
            RedisProperties.Cluster cluster = new RedisProperties.Cluster();
            cluster.setMaxRedirects(3);
            cluster.setNodes(Arrays.asList(new String[]{"10.10.10.37:6301", "10.10.10.38:6301", "10.10.10.39:6301"}));
            cluster.setPassword(redisPassword);
            properties.setCluster(cluster);
        }

        properties.setEnabled(true);
        properties.setSegmentExcludes(segmentExcludeKey);

        RedisProperties.Pool pool = new RedisProperties.Pool();
        pool.setMaxActive(100);
        pool.setMaxWait(3000);//毫秒
        properties.getJedis().setPool(pool);

        redisIdService = new RedisIdService(properties);
    }
}
