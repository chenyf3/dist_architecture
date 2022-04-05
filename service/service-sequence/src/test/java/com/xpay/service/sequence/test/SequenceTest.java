package com.xpay.service.sequence.test;

import com.xpay.common.utils.JsonUtil;
import com.xpay.facade.sequence.service.SequenceFacade;
import org.apache.dubbo.config.annotation.Reference;
import org.junit.Ignore;
import org.junit.Test;

import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 测试环境：Windows10,8C16G,IntelliJ IDEA 2020.2.4 (Ultimate Edition)
 * 比较简单粗糙的测试结果如下：
 *
 testSnowSeq totalCount = 9000000 timeCost = 2234(ms) tps = 4028648.1647269474 failCount = 0
 testSnowMulti totalCount = 8000000 timeCost = 2010(ms) tps = 3980099.5024875626 failCount = 0

 testRedisSeq totalCount = 9000000 timeCost = 2574(ms) tps = 3496503.4965034965 failCount = 3
 testRedisMulti totalCount = 8000000 timeCost = 2511(ms) tps = 3185981.6806053366 failCount = 1
 testRedisSegmentExclude totalCount = 3000 timeCost = 22606(ms) tps = 132.70813058480047 failCount = 0

 testSegmentSeq totalCount = 9000000 timeCost = 4818(ms) tps = 1867995.0186799504 failCount = 134
 testSegmentMulti totalCount = 8000000 timeCost = 5001(ms) tps = 1599680.0639872025 failCount = 74

 结论：雪花算法达到400W/s左右的tps，redis分段发号达到300W/s的tps，数据库分段发号达到150W+/s的tps，其中，雪花算法已经快接近此算法的理论极限值了，
      而redis和数据库都使用分段发号情况下，redis比数据库快了一倍，主要是更新segment时数据库的耗时比redis要久导致的，并且，由于更新时间较长，
      会导致id获取失败的概率升高，而不使用分段发号时的redis，只能达到 130+/s 的tps，此方式仅适合并发小但对id连续性要求比较高的的场景

 */
public class SequenceTest extends BaseTestCase {
    @Reference
    SequenceFacade sequenceFacade;

    @Ignore
    @Test
    public void testSnowSeq(){
        int i = 0, max = 9000000, failCount = 0;

        long start = System.currentTimeMillis();
        while(++i <= max){
            try{
                Long id = sequenceFacade.nextSnowId();
//                System.out.println("index="+i+",id="+id);//println语句比较耗时，对测试影响很大
            }catch(Exception e){
                failCount ++;
//                e.printStackTrace();
            }
        }

        long timeCost = System.currentTimeMillis() - start;
        double tps = max / (timeCost/1000d);
        System.out.println("testSnowSeq totalCount = " + max + " timeCost = " + timeCost + "(ms) tps = " + tps + " failCount = " + failCount);
    }

    @Ignore
    @Test
    public void testSnowMulti(){
        long start = System.currentTimeMillis();
        int threadCount = 8, maxPerThread = 1000000;
        CountDownLatch countDown = new CountDownLatch(threadCount);
        AtomicInteger totalCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);
        while(threadCount-- > 0){
            new Thread(() -> {
                int i = 0;
                while(++i <= maxPerThread){
                    try{
                        Long id = sequenceFacade.nextSnowId();
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
        System.out.println("testSnowMulti totalCount = " + totalCount.get() + " timeCost = " + timeCost + "(ms) tps = " + tps + " failCount = " + failCount.get());
    }

    @Ignore
    @Test
    public void testRedisSeq(){
        String key = "testRedisSeq";
        int i = 0, max = 9000000, failCount = 0;

        long start = System.currentTimeMillis();
        while(++i <= max){
            try{
                Long id = sequenceFacade.nextRedisId(key);
//                System.out.println("index="+i+",id="+id); //println语句比较耗时，对测试影响很大
            }catch(Exception e){
//                e.printStackTrace();
                failCount ++;
            }
        }

        long timeCost = System.currentTimeMillis() - start;
        double tps = max / (timeCost/1000d);
        System.out.println("testRedisSeq totalCount = " + max + " timeCost = " + timeCost + "(ms) tps = " + tps + " failCount = " + failCount);
    }

    @Ignore
    @Test
    public void testRedisSeqMulti(){
        String key = "testRedisMulti";
        int threadCount = 8; long maxPerThread = 1000000;
        CountDownLatch countDown = new CountDownLatch(threadCount);
        AtomicInteger totalCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);
        long start = System.currentTimeMillis();
        while(threadCount-- > 0){
            new Thread(() -> {
                int i = 0;
                while(++i <= maxPerThread){
                    try{
                        Long id = sequenceFacade.nextRedisId(key);
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
        System.out.println("testRedisMulti totalCount = " + totalCount.get() + " timeCost = " + timeCost + "(ms) tps = " + tps + " failCount = " + failCount.get());
    }

    @Ignore
    @Test
    public void testRedisSegmentExclude(){
        String key = "testRedisSegmentExclude";
        int i = 0, max = 3000, failCount = 0;

        long start = System.currentTimeMillis();
        while(++i <= max){
            try{
                Long id = sequenceFacade.nextRedisId(key);
//                System.out.println("index="+i+",id="+id);//println语句比较耗时，对测试影响很大
            }catch(Exception e){
                failCount ++;
//                e.printStackTrace();
            }
        }

        long timeCost = System.currentTimeMillis() - start;
        double tps = max / (timeCost/1000d);
        System.out.println("testRedisSegmentExclude totalCount = " + max + " timeCost = " + timeCost + "(ms) tps = " + tps + " failCount = " + failCount);
    }

    @Ignore
    @Test
    public void testSegmentSeq(){
        String key = "TEST_DB_SEQ";
        int i = 0, max = 9000000, failCount = 0;

        long start = System.currentTimeMillis();
        while(++i <= max){
            try{
                Long id = sequenceFacade.nextSegmentId(key);
//                System.out.println("index="+i+",id="+id);//println语句比较耗时，对测试影响很大
            }catch(Exception e){
                failCount ++;
//                e.printStackTrace();
            }
        }

        long timeCost = System.currentTimeMillis() - start;
        double tps = max / (timeCost/1000d);
        System.out.println("testSegmentSeq totalCount = " + max + " timeCost = " + timeCost + "(ms) tps = " + tps + " failCount = " + failCount);
    }

    @Ignore
    @Test
    public void testSegmentMulti(){
        String key = "TEST_DB_SEQ_MULTI";

        long start = System.currentTimeMillis();
        int threadCount = 8, maxPerThread = 1000000;
        CountDownLatch countDown = new CountDownLatch(threadCount);
        AtomicInteger totalCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);
        while(threadCount-- > 0){
            new Thread(() -> {
                int i = 0;
                while(++i <= maxPerThread){
                    try{
                        Long id = sequenceFacade.nextSegmentId(key);
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
        System.out.println("testSegmentMulti totalCount = " + totalCount.get() + " timeCost = " + timeCost + "(ms) tps = " + tps + " failCount = " + failCount.get());
    }

    @Ignore
    @Test
    public void testBatchSegmentSeq(){
        String key = "TEST_DB_SEQ";
        List<Long> idList = sequenceFacade.nextSegmentId(key, 5000);
        System.out.println("idList = " + JsonUtil.toJson(idList));
    }
}
