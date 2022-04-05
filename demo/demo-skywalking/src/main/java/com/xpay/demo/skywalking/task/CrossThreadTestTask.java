package com.xpay.demo.skywalking.task;

import org.apache.skywalking.apm.toolkit.trace.TraceCrossThread;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;

@TraceCrossThread // 告诉agent需要追踪task的run方法
public class CrossThreadTestTask implements Runnable{

    private CountDownLatch latch ;

    private static final Logger LOG = LoggerFactory.getLogger(CrossThreadTestTask.class);

    public CrossThreadTestTask(CountDownLatch latch) {
        this.latch = latch;
    }

    @Override
    public void run() {
        LOG.info("==> by CrossThreadTestTask");
        latch.countDown();
    }

}
