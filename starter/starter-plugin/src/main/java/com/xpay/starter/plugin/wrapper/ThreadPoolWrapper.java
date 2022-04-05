package com.xpay.starter.plugin.wrapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.util.concurrent.ListenableFuture;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 把线程池内部的情况进行日志打印
 * @author chenyf
 */
public class ThreadPoolWrapper extends ThreadPoolTaskExecutor {
    private static final Logger logger = LoggerFactory.getLogger(ThreadPoolWrapper.class);

    private void showThreadPoolInfo(){
        ThreadPoolExecutor threadPoolExecutor = getThreadPoolExecutor();
        if(null == threadPoolExecutor){
            return;
        }

        logger.info("prefixName={} taskCount={} completedCount={} activeCount={} queueSize={}", this.getThreadNamePrefix(),
                threadPoolExecutor.getTaskCount(), threadPoolExecutor.getCompletedTaskCount(), threadPoolExecutor.getActiveCount(), threadPoolExecutor.getQueue().size());
    }

    @Override
    public void execute(Runnable task) {
        showThreadPoolInfo();
        super.execute(task);
    }

    @Override
    public void execute(Runnable task, long startTimeout) {
        showThreadPoolInfo();
        super.execute(task, startTimeout);
    }

    @Override
    public Future<?> submit(Runnable task) {
        showThreadPoolInfo();
        return super.submit(task);
    }

    @Override
    public <T> Future<T> submit(Callable<T> task) {
        showThreadPoolInfo();
        return super.submit(task);
    }

    @Override
    public ListenableFuture<?> submitListenable(Runnable task) {
        showThreadPoolInfo();
        return super.submitListenable(task);
    }

    @Override
    public <T> ListenableFuture<T> submitListenable(Callable<T> task) {
        showThreadPoolInfo();
        return super.submitListenable(task);
    }
}
