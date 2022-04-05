package com.xpay.demo.skywalking.task;

import org.apache.skywalking.apm.toolkit.trace.TraceCrossThread;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;
import java.util.function.Supplier;

@TraceCrossThread
public class CrossThreadTestSupplier implements Supplier<String> {

    private CountDownLatch latch;

    private static final Logger LOG = LoggerFactory.getLogger(CrossThreadTestTask.class);

    public CrossThreadTestSupplier(CountDownLatch latch) {
        this.latch = latch;
    }

    @Override
    public String get() {
        LOG.info("==> by CrossThreadTestSupplier");
        latch.countDown();
        return "complete";
    }

}
