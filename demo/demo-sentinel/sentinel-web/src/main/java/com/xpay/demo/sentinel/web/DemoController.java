package com.xpay.demo.sentinel.web;

import com.xpay.common.utils.RandomUtil;
import com.xpay.demo.dubbo.api.HelloService;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

@RestController
@RequestMapping("/demo")
public class DemoController {
    @Autowired
    DemoService demoService;
    @Reference
    HelloService helloService;

    /**
     * 定义一个资源
     * @return
     */
    @RequestMapping("flow")
    public String flow() {
        int blockCount = 0;
        int totalCount = 50;
        for(int i=0; i<totalCount; i++){
            String result = demoService.flow();
            if(! "flow".equalsIgnoreCase(result)){
                blockCount ++;
            }
        }
        return "flow, totalCount = " + totalCount + ", blockCount = " + blockCount;
    }

    @RequestMapping("flowCluster")
    public String flowCluster() {
        int blockCount = 0;
        int totalCount = 50;
        for(int i=0; i<totalCount; i++){
            String result = demoService.flowCluster();
            if(! "flowCluster".equalsIgnoreCase(result)){
                blockCount ++;
            }
        }
        return "flowCluster, totalCount = " + totalCount + ", blockCount = " + blockCount;
    }

    @RequestMapping("flowClusterMany")
    public String flowClusterMany() throws Exception {
        int totalCount = 5000000;
        AtomicInteger blockCount = new AtomicInteger();
        AtomicInteger currCount = new AtomicInteger(0);
        AtomicInteger fallbackCount = new AtomicInteger();
        int threadCount = 8;

        long start = System.currentTimeMillis();
        CountDownLatch countDown = new CountDownLatch(threadCount);
        for(int i=0; i<threadCount; i++){
            new Thread(() -> {
                try {
                    while(currCount.incrementAndGet() <= totalCount){
                        String result = demoService.flowClusterMany();
                        if("flowClusterMany".equalsIgnoreCase(result)){

                        }else if("flowClusterManyFallback".equals(result)){
                            fallbackCount.incrementAndGet();
                        } else {
                            blockCount.incrementAndGet();
                        }
                    }
                } finally {
                    countDown.countDown();
                }
            }).start();
        }

        countDown.await();
        int costSec = (int)((System.currentTimeMillis() - start) / 1000);
        int tps = (int)(totalCount / costSec);

        return "flowClusterMany, costSec = " + costSec + ", totalCount = " + totalCount + ", tps = " + tps + ", blockCount = " + blockCount + ", fallbackCount = " + fallbackCount;
    }

    @RequestMapping("flowClusterManyFailover")
    public String flowClusterManyFailover() throws Exception {
        int totalCount = 5000000;
        AtomicInteger blockCount = new AtomicInteger();
        AtomicInteger currCount = new AtomicInteger(0);
        AtomicInteger fallbackCount = new AtomicInteger();
        int threadCount = 8;

        long start = System.currentTimeMillis();
        CountDownLatch countDown = new CountDownLatch(threadCount);
        for(int i=0; i<threadCount; i++){
            new Thread(() -> {
                try {
                    while(currCount.incrementAndGet() <= totalCount){
                        String result = demoService.flowClusterFailoverMany();
                        if("flowClusterFailoverMany".equalsIgnoreCase(result)){

                        }else if("flowClusterFailoverManyFallback".equals(result)){
                            fallbackCount.incrementAndGet();
                        } else {
                            blockCount.incrementAndGet();
                        }
                    }
                } finally {
                    countDown.countDown();
                }
            }).start();
        }

        countDown.await();
        int costSec = (int)((System.currentTimeMillis() - start) / 1000);
        int tps = (int)(totalCount / costSec);

        return "flowClusterManyFailover, costSec = " + costSec + ", totalCount = " + totalCount + ", tps = " + tps + ", blockCount = " + blockCount + ", fallbackCount = " + fallbackCount;
    }

    @RequestMapping("degrade")
    public String degrade() throws Exception {
        AtomicInteger totalCount = new AtomicInteger(), degradeCount = new AtomicInteger(), fallbackCount = new AtomicInteger();
        int threadCount = 8;
        long start = System.currentTimeMillis();
        CountDownLatch countDown = new CountDownLatch(threadCount);
        for(int i=0; i<threadCount; i++){
            new Thread(() -> {
                try {
                    int total = 50;
                    for(int j=0; j<total; j++){
                        String result = demoService.degrade("rand: " + RandomUtil.getInt(1, 10000));
                        if("degradeOccur".equals(result)){
                            degradeCount.incrementAndGet();
                        }else if("degradeFallback".equals(result)){
                            fallbackCount.incrementAndGet();
                        }
                        totalCount.incrementAndGet();
                    }
                } finally {
                    countDown.countDown();
                }
            }).start();
        }

        countDown.await();
        int costSec = (int)((System.currentTimeMillis() - start) / 1000);

        return "degrade, costSec = " + costSec + ", totalCount = " + totalCount + ", degradeCount = " + degradeCount + ", fallbackCount = " + fallbackCount;
    }

    /**
     * 用以测试dubbo流控，在dashboard上找到提供者端，然后增加该方法的流控规则，资源名为：com.xpay.demo.dubbo.api.HelloService:sayHello()
     * @return
     */
    @RequestMapping("dubbo")
    public String dubbo() {
        String result = "";
        Long start = System.currentTimeMillis();
        int curr = 0, total = 10000, success = 0, fail = 0;
        while (curr++ <= total){
            try{
                result = helloService.sayHello();
                success++;
            }catch (Exception e){
                fail++;
                e.printStackTrace();
            }
        }
        int costSec = (int)((System.currentTimeMillis() - start) / 1000);
        int tps = (int)(total / costSec);
        return "cost=" +costSec+ ", tps=" +tps+ ", total="+ total + ", success=" +success+ ", fail=" +fail+ ", "+result;
    }

    @RequestMapping("dubboNoLimit")
    public String dubboNoLimit() {
        String result = "";
        Long start = System.currentTimeMillis();
        int curr = 0, total = 10000, success = 0, fail = 0;
        while (curr++ <= total){
            try{
                result = helloService.helloWorld();
                success++;
            }catch (Exception e){
                fail++;
                e.printStackTrace();
            }
        }
        int costSec = (int)((System.currentTimeMillis() - start) / 1000);
        int tps = (int)(total / costSec);
        return "cost=" +costSec+ ", tps=" +tps+ ", total="+ total + ", success=" +success+ ", fail=" +fail+ ", "+result;
    }
}
