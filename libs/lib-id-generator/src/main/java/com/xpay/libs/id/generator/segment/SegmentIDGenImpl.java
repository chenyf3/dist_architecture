package com.xpay.libs.id.generator.segment;

import com.xpay.libs.id.generator.IDGen;
import com.xpay.libs.id.generator.common.Segment;
import com.xpay.libs.id.generator.common.SegmentBuffer;
import com.xpay.libs.id.common.IdGenException;
import com.xpay.libs.id.generator.segment.dao.IDAllocDao;
import com.xpay.libs.id.generator.segment.model.IDAlloc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class SegmentIDGenImpl implements IDGen {
    private static final Logger logger = LoggerFactory.getLogger(SegmentIDGenImpl.class);
    private static final int MAX_STEP = 128000;//最大步长，如果初始步长是2000，则经过6次拓容之后就会达到最大步长
    private static final long SEGMENT_DURATION = 5 * 60 * 1000L;//一个Segment的维持时间，当一个Segment的维持时间低于这个时间时就会进行拓容，当一个Segment的维持时间高于这个时间时就会进行缩容
    private final ExecutorService service = new ThreadPoolExecutor(5, Integer.MAX_VALUE, 60L, TimeUnit.SECONDS, new SynchronousQueue<Runnable>(), new UpdateThreadFactory());
    private final int segmentStep;
    private final Map<String, SegmentBuffer> cache = new ConcurrentHashMap<>();
    private final Lock lock = new ReentrantLock();
    private final boolean lazily;//是否懒加载，如果设置为true，则只有使用到某个biz_key的时候才会把这它加载到内存中
    private IDAllocDao dao;

    public SegmentIDGenImpl(int segmentStep, boolean lazily){
        this.segmentStep = segmentStep;
        this.lazily = lazily;
    }

    public static class UpdateThreadFactory implements ThreadFactory {
        private static int threadInitNumber = 0;

        private static synchronized int nextThreadNum() {
            return threadInitNumber++;
        }

        @Override
        public Thread newThread(Runnable r) {
            return new Thread(r, "SegmentId-Update-Thread-" + nextThreadNum());
        }
    }

    @Override
    public boolean init() {
        updateCacheFromDbAtEveryMinute();
        return true;
    }

    @Override
    public Long get(final String key) {
        //如果当前这个key还不存在于cache中，则新增
        SegmentBuffer buffer = cache.getOrDefault(key, null);
        if (buffer == null) {
            addSegmentBuffer(key, true);
            buffer = cache.get(key);
        }

        return getIdFromSegmentBuffer(buffer);
    }

    @Override
    public Long get(String key, long maxValue) throws IdGenException {
        throw new IdGenException("Not Support!");
    }

    public List<IDAlloc> listAllIDAlloc() {
        return dao.listAllIDAlloc();
    }

    public Map<String, SegmentBuffer> getCache() {
        return cache;
    }

    public IDAllocDao getDao() {
        return dao;
    }

    public void setDao(IDAllocDao dao) {
        this.dao = dao;
    }

    private void updateCacheFromDbAtEveryMinute() {
        ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor(new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread t = new Thread(r);
                t.setName("db-and-cache-sync-thread");
                t.setDaemon(true);
                return t;
            }
        });
        service.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                updateCacheFromDb();
            }
        }, 0, 60, TimeUnit.SECONDS);
    }

    private void updateCacheFromDb() {
        int allKeyCount = 0;
        long start = System.currentTimeMillis();
        try {
            Set<String> needCacheKeySet = new HashSet<>();
            Set<String> mayRemoveKeySet = new HashSet<>();
            List<String> dbKeyList = dao.listAllKeys();
            Set<String> cacheKeySet = cache.keySet();

            if (dbKeyList == null || dbKeyList.isEmpty()) {
                mayRemoveKeySet.addAll(cacheKeySet);
            } else {
                allKeyCount = dbKeyList.size();
                //找出在db中存在，但cache中不存在的key
                if(!lazily){
                    for(String key : dbKeyList){
                        if(! cacheKeySet.contains(key)){
                            needCacheKeySet.add(key);
                        }
                    }
                }

                //找出在cache中存在，但db中不存在的key
                for(String tag : cacheKeySet){
                    if(! dbKeyList.contains(tag)){
                        mayRemoveKeySet.add(tag);
                    }
                }
            }

            int idx = 0;
            for (String key : needCacheKeySet) {
                idx ++;
                addSegmentBuffer(key, false);
                logger.info("Add Segment to MemCache {}/{} key: {}", idx, needCacheKeySet.size(), key);
            }

            idx = 0;
            for (String key : mayRemoveKeySet) { //db中已不存在的key要从cache中删除
                idx ++;
                boolean isRemove = removeSegmentBuffer(key);
                if (isRemove) {
                    logger.info("Remove Segment from MemCache {}/{} key: {}", idx, mayRemoveKeySet.size(), key);
                }
            }
        } catch (Exception e) {
            logger.error("update Segment MemCache from db exception", e);
        } finally {
            logger.info("db and cache sync finish! cost: {}(ms) allKeyCount: {} lazily: {}", System.currentTimeMillis()-start, allKeyCount, lazily);
        }
    }

    private void addSegmentBuffer(String key, boolean ensureExist){
        lock.lock();
        try {
            if(cache.containsKey(key)) { //double check
                return;
            }

            if(ensureExist){
                IDAlloc idAlloc = dao.getIDAllocByKey(key);
                if (idAlloc == null) {
                    idAlloc = new IDAlloc();
                    idAlloc.setKey(key);
                    idAlloc.setMinStep(segmentStep);
                    idAlloc.setMaxId(0);

                    boolean isSuccess = false;
                    try {
                        isSuccess = dao.addIDAlloc(idAlloc) > 0;
                    } catch(Exception e) {
                        String msg = e.getMessage();
                        if(msg != null && msg.contains("Duplicate") && msg.contains("key 'biz_key'")){ //记录重复
                            isSuccess = true;
                        }else{
                            logger.error("key={} 新增记录异常", key, e);
                        }
                    }
                    if(isSuccess){
                        logger.info("新增Segment成功 key={}", key);
                    }else{
                        throw new IdGenException("新增记录失败key：" + key);
                    }
                }
            }

            SegmentBuffer buffer = new SegmentBuffer();
            buffer.setKey(key);
            updateSegmentFromDb(key, buffer.getCurrent());
            buffer.setInitOk(true);
            cache.put(key, buffer);//初始化完成之后再put进去，避免在初始化过程中时被其他线程读到
        } finally {
            lock.unlock();
        }
    }

    private boolean removeSegmentBuffer(String key) {
        boolean isRemove = false;
        lock.lock();
        try {
            //删除之前先判断下db中是否确实不存在，避免刚刚写进去的记录在计算时没有被加载到而导致误删
            IDAlloc leaf = dao.getIDAllocByKey(key);
            if (leaf == null) {
                cache.remove(key);
                isRemove = true;
            }
        } finally {
            lock.unlock();
        }
        return isRemove;
    }

    private Long getIdFromSegmentBuffer(final SegmentBuffer buffer) {
        while (true) {
            buffer.rLock().lock();
            try {
                final Segment segment = buffer.getCurrent();
                if (!buffer.isNextReady() && (segment.getIdle() < 0.9 * segment.getStep())
                        && buffer.getThreadRunning().compareAndSet(false, true)) {
                    service.execute(() -> {
                        Segment next = buffer.getSegments()[buffer.nextPos()];
                        boolean updateOk = false;
                        try {
                            long start = System.currentTimeMillis();
                            updateSegmentFromDb(buffer.getKey(), next);
                            updateOk = true;
                            long cost = System.currentTimeMillis() - start;
                            logger.info("cost={} success update segment from db key={} {}", cost, buffer.getKey(), next);
                        } catch (Exception e) {
                            logger.warn("key={} updateSegmentFromDb exception", buffer.getKey(), e);
                        } finally {
                            if (updateOk) {
                                buffer.wLock().lock();
                                buffer.setNextReady(true);
                                buffer.getThreadRunning().set(false);
                                buffer.wLock().unlock();
                            } else {
                                buffer.getThreadRunning().set(false);
                            }
                        }
                    });
                }

                long value = segment.getValue().getAndIncrement();
                if (value < segment.getMax()) {
                    return value;
                }
            } finally {
                buffer.rLock().unlock();
            }

            waitAndSleep(buffer);
            buffer.wLock().lock();
            try {
                final Segment segment = buffer.getCurrent();
                long value = segment.getValue().getAndIncrement();
                if (value < segment.getMax()) {
                    return value;
                }

                //当前Segment中的id已用完，需要切换到下一个Segment
                if (buffer.isNextReady()) {
                    buffer.switchPos();
                    buffer.setNextReady(false);
                } else { //数据库更新太慢或者需要更新的key太多，导致相互之间竞争线程池资源
                    throw new IdGenException("Too Busy! Both two segments are not ready!");
                }
            } finally {
                buffer.wLock().unlock();
            }
        }
    }

    private void updateSegmentFromDb(String key, Segment segment) {
        SegmentBuffer buffer = segment.getBuffer();
        IDAlloc leafAlloc;
        if (!buffer.isInitOk()) {
            leafAlloc = dao.increaseMaxIdAndGetIDAlloc(key);
            buffer.setStep(leafAlloc.getMinStep());//初始step
            buffer.setMinStep(leafAlloc.getMinStep());
        } else if (buffer.getUpdateTimestamp() == 0) {
            leafAlloc = dao.increaseMaxIdAndGetIDAlloc(key);
            buffer.setUpdateTimestamp(System.currentTimeMillis());
            buffer.setStep(leafAlloc.getMinStep());//初始step
            buffer.setMinStep(leafAlloc.getMinStep());
        } else {
            long duration = System.currentTimeMillis() - buffer.getUpdateTimestamp();
            int nextStep = buffer.getStep();
            if (duration < SEGMENT_DURATION) {
                if (nextStep * 2 <= MAX_STEP) { //维持时间小于Segment的停留时间，则拓容
                    nextStep = nextStep * 2;
                }
            } else if (duration >= SEGMENT_DURATION * 2) { //维持时间超过2倍的Segment停留时间，则缩容
                nextStep = nextStep / 2 >= buffer.getMinStep() ? nextStep / 2 : nextStep;
            }
            leafAlloc = dao.increaseMaxIdAndGetIDAlloc(key, nextStep);
            buffer.setUpdateTimestamp(System.currentTimeMillis());
            buffer.setStep(nextStep);
            buffer.setMinStep(leafAlloc.getMinStep());//leafAlloc的step为DB中的step
        }
        // must set value before set max
        long value = leafAlloc.getMaxId() - buffer.getStep();
        segment.getValue().set(value == 0 ? 1 : value);//第一个id要从1开始
        segment.setMax(leafAlloc.getMaxId());
        segment.setStep(buffer.getStep());
    }

    private void waitAndSleep(SegmentBuffer buffer) {
        long start = System.currentTimeMillis();
        int roll = 0;
        while (buffer.getThreadRunning().get()) {
            roll += 1;
            if(roll > 10000) {
                try {
                    TimeUnit.MILLISECONDS.sleep(10);
                    break;
                } catch (InterruptedException e) {
                    logger.warn("Thread {} Interrupted",Thread.currentThread().getName());
                    break;
                }
            }
        }
        logger.warn("waitAndSleep for {}(ms)", (System.currentTimeMillis() - start));
    }

    public void destroy(){

    }
}
