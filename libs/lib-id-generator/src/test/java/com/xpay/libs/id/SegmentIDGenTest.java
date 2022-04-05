package com.xpay.libs.id;

import com.xpay.libs.id.common.Utils;
import com.xpay.libs.id.config.SegmentProperties;
import com.xpay.libs.id.service.SegmentService;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 测试环境：Windows10,8C16G,IntelliJ IDEA 2020.2.4 (Ultimate Edition)
 */
public class SegmentIDGenTest {
    SegmentService segmentService;

    @Ignore
    @Before
    public void before(){
        SegmentProperties properties = new SegmentProperties();
        properties.setEnabled(true);
        properties.setJdbcUrl("jdbc:mysql://10.10.10.39:3306/sequence?useUnicode=true&characterEncoding=UTF-8&useSSL=true&serverTimezone=Asia/Shanghai&useInformationSchema=true");
        properties.setUsername("app_dev");
        properties.setPassword("2021@Mysql");
        properties.setMaxActive(100);
        segmentService = new SegmentService(properties);
    }

    /**
     * 单线程性能测试
     * 测试结果：testGetId totalCount = 50000000 timeCost = 13119(ms) tps = 3811266.1025992837 failCount = 9
     */
    @Ignore
    @Test
    public void testGetId() {
        String key = "testGetId";
        int i = 0, max = 50000000, failCount = 0;

        long start = System.currentTimeMillis();
        while(++i <= max){
            try{
                Long id = segmentService.getId(key);
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
     * 测试结果：testGetIdMulti totalCount = 12000000 timeCost = 5745(ms) tps = 2088772.8459530026 failCount = 44
     */
    @Ignore
    @Test
    public void testGetIdMulti(){
        String key = "testGetIdMulti";

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
                        Long id = segmentService.getId(key);
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
     * 测试批量获取id
     */
    @Ignore
    @Test
    public void testBatchGetId() {
        long start = System.currentTimeMillis();
        List<Long> idList = segmentService.getId("testBatchGetId", 100000);
        long timeCost = System.currentTimeMillis() - start;
        System.out.println("timeCost = "+timeCost+"(ms) testBatchGetId: " + Utils.beanToJson(idList));
    }

    /**
     * 测试大量key时的tps性能（设置key和获取id在一起进行的）
     * 测试结果：
     *
     * 表中没任何key时：testMultiKeyGetIdOut keyCount = 10000 totalCount = 12000000 timeCost = 569247(ms) tps = 21080.48000252966 failCount = 0
     * 表中已有key时：testMultiKeyGetIdOut keyCount = 10000 totalCount = 12000000 timeCost = 393380(ms) tps = 30504.855356144188 failCount = 0
     */
    @Ignore
    @Test
    public void testMultiKeyGetId(){
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

                        Long id = segmentService.getId(keyTemp);
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
        System.out.println("testMultiKeyGetIdOut keyCount = " + realKeyCount + " totalCount = " + totalCount.get() + " timeCost = " + timeCost + "(ms) tps = " + tps + " failCount = " + failCount.get());
    }

    /**
     * 测试大量key时的tps性能（设置key和获取id是分开的，先设置key再获取id）
     * 测试结果：
     * setKey keyCount = 10000 timeCost = 561275(ms) tps = 17.81657832613247
     * testMultiKeyGetIdSplit keyCount = 10000 totalCount = 12000000 timeCost = 7249(ms) tps = 1655400.7449303353 failCount = 0
     */
    @Ignore
    @Test
    public void testMultiKeyGetIdSplit(){
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
                Long id = segmentService.getId(keyTemp);
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

                        Long id = segmentService.getId(keyTemp);
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
        if(segmentService != null){
            segmentService.destroy();
        }
    }
}
