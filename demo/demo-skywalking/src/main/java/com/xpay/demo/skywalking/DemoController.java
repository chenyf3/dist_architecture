package com.xpay.demo.skywalking;

import com.xpay.demo.skywalking.task.CrossThreadTestSupplier;
import com.xpay.demo.skywalking.task.CrossThreadTestTask;
import com.xpay.demo.skywalking.vo.QryInfo;
import com.xpay.demo.skywalking.vo.UserInfo;
import org.apache.skywalking.apm.toolkit.trace.ActiveSpan;
import org.apache.skywalking.apm.toolkit.trace.CallableWrapper;
import org.apache.skywalking.apm.toolkit.trace.RunnableWrapper;
import org.apache.skywalking.apm.toolkit.trace.SupplierWrapper;
import org.apache.skywalking.apm.toolkit.trace.Tag;
import org.apache.skywalking.apm.toolkit.trace.Tags;
import org.apache.skywalking.apm.toolkit.trace.Trace;
import org.apache.skywalking.apm.toolkit.trace.TraceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@RestController
@RequestMapping("/demo")
public class DemoController {

    private static final Logger LOG = LoggerFactory.getLogger(DemoController.class);

    @Autowired
    UserBiz userBiz;

    @RequestMapping("/testTrace")
    @Trace(operationName = "testTrace")// 指定端点名称（endpoint），不指定则默认接口全路径名
    @Tags({@Tag(key = "tag1", value = "arg[0]"),// 打标签：表示key为params，value为第一个参数，实际上打印标签时是调toString方法
            @Tag(key = "tag2", value = "returnedObj"),// 表示key为result，value为方法返回值
            @Tag(key = "tag3", value = "returnedObj.name")}// 也可以只打印某个字段
    )
    public UserInfo testTrace(@RequestBody QryInfo qryInfo) {
        // 不推荐使用ActiveSpan的api打标签、日志，对代码侵入性过强
        testAddSpanLog();

        // 测试打印tracer信息
        testPrintTraceInfo();

        // 测试几种跨线程跟踪方式（推荐使用注解方式：testCrossThreadWay1、testCrossThreadWay4）
        testCrossThreadWay1();
        testCrossThreadWay2();
        testCrossThreadWay3();
        testCrossThreadWay4();
        testCrossThreadWay5();
        return userBiz.getUserInfoById(qryInfo.getUid());
    }

    private void testAddSpanLog() {
        ActiveSpan.tag("name", "zhangsan");
        ActiveSpan.error("这是一条error级别日志！");
        ActiveSpan.info("这是一条info级别日志");
        ActiveSpan.debug("这是一条debug级别日志");
    }

    private void testPrintTraceInfo() {
        LOG.info("==> traceId:{}", TraceContext.traceId());
        LOG.info("==> spanId:{}", TraceContext.spanId());
        LOG.info("==> segmentId:{}", TraceContext.segmentId());
        TraceContext.putCorrelation("who", "zhangsan");
        LOG.info("==> who:{}", TraceContext.getCorrelation("who").get());
    }

    private void testCrossThreadWay1() {
        final CountDownLatch latch = new CountDownLatch(1);
        new Thread(new CrossThreadTestTask(latch)).start();
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void testCrossThreadWay2() {
        final CountDownLatch latch = new CountDownLatch(1);
        ExecutorService executorService = Executors.newFixedThreadPool(1);
        executorService.submit(CallableWrapper.of(new Callable<String>() {
            @Override public String call() throws Exception {
                LOG.info("==> by CallableWrapper");
                latch.countDown();
                return null;
            }
        }));
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void testCrossThreadWay3() {
        final CountDownLatch latch = new CountDownLatch(1);
        ExecutorService executorService = Executors.newFixedThreadPool(1);
        executorService.execute(RunnableWrapper.of(new Runnable() {
            @Override public void run() {
                LOG.info("==> by RunnableWrapper");
                latch.countDown();
            }
        }));
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void testCrossThreadWay4() {
        final CountDownLatch latch = new CountDownLatch(1);
        CompletableFuture.supplyAsync(new CrossThreadTestSupplier(latch));
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void testCrossThreadWay5() {
        final CountDownLatch latch = new CountDownLatch(1);
        CompletableFuture.supplyAsync(SupplierWrapper.of(()->{
            LOG.info("==> by SupplierWrapper");
            latch.countDown();
            return null;
        }));
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
