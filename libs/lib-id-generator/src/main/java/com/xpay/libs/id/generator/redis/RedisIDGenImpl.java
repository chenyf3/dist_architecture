package com.xpay.libs.id.generator.redis;

import com.xpay.libs.id.common.Utils;
import com.xpay.libs.id.config.RedisProperties;
import com.xpay.libs.id.generator.IDGen;
import com.xpay.libs.id.client.RedisClient;
import com.xpay.libs.id.common.IdGenException;
import com.xpay.libs.id.generator.common.Segment;
import com.xpay.libs.id.generator.common.SegmentBuffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * redis id 生成器
 * @author chenyf
 */
public class RedisIDGenImpl implements IDGen {
    public final static long REDIS_ID_MAX_VALUE = Long.MAX_VALUE - 10000000;
    private static final int MAX_STEP = 128000;//最大步长，如果初始步长是2000，则经过6次拓容之后就会达到最大步长
    private static final long SEGMENT_DURATION = 5 * 60 * 1000L;//一个Segment的维持时间，当一个Segment的维持时间低于这个时间时就会进行拓容，当一个Segment的维持时间高于这个时间时就会进行缩容
    private static final Logger logger = LoggerFactory.getLogger(RedisIDGenImpl.class);
    private final ExecutorService service = new ThreadPoolExecutor(5, Integer.MAX_VALUE, 60L, TimeUnit.SECONDS, new SynchronousQueue<Runnable>(), new UpdateThreadFactory());

    private final Map<String, SegmentBuffer> cache = new ConcurrentHashMap<>();
    private final Lock lock = new ReentrantLock();
    private final boolean segmentAble;
    private final int segmentStep;
    private final Set<String> segmentExcludes = new HashSet<>();
    private final String idHashKey;
    private final RedisClient redisClient;

    public RedisIDGenImpl(RedisProperties properties) {
        this.segmentAble = properties.getSegmentAble();
        this.segmentStep = properties.getSegmentStep();
        this.idHashKey = properties.getIdHashKey();
        if(Utils.isNotEmpty(properties.getSegmentExcludes())){
            String[] keys = properties.getSegmentExcludes().split(",");
            segmentExcludes.addAll(Arrays.asList(keys));
        }
        if(this.segmentStep < 100 || this.segmentStep > MAX_STEP){
            throw new IdGenException("segmentStep范围需在[100,"+MAX_STEP+"]之间");
        }
        this.redisClient = new RedisClient(properties);
    }

    public static class UpdateThreadFactory implements ThreadFactory {

        private static int threadInitNumber = 0;

        private static synchronized int nextThreadNum() {
            return threadInitNumber++;
        }

        @Override
        public Thread newThread(Runnable r) {
            return new Thread(r, "RedisId-Update-Thread-" + nextThreadNum());
        }
    }

    @Override
    public Long get(String key) {
        Long currId;
        if(segmentAble && !segmentExcludes.contains(key)){ //contains方法对性能影响还是有的，如无此类场景，可以移除此判断
            currId = getIdWithSegment(key, segmentStep, REDIS_ID_MAX_VALUE);
        }else{
            currId = getIdWithRedis(key, 1, REDIS_ID_MAX_VALUE);
        }
        return currId;
    }

    @Override
    public List<Long> get(String key, int count) {
        List<Long> idList = new ArrayList<>(count);
        if(segmentAble && !segmentExcludes.contains(key)){
            for(int i=0; i<count; i++){
                Long id = getIdWithSegment(key, segmentStep, REDIS_ID_MAX_VALUE);
                idList.add(id);
            }
        }else{
            Long currId = getIdWithRedis(key, count, REDIS_ID_MAX_VALUE);
            long idStart = currId - count;
            while(idStart++ < currId){
                idList.add(idStart);
            }
        }
        return idList;
    }

    /**
     * 获取id并指定id最大值，当id增长超过指定的最大值时，会重新从1开始递增
     * @param key
     * @param maxValue
     * @return
     */
    @Override
    public Long get(String key, long maxValue) {
        Long currId;
        if(segmentAble && !segmentExcludes.contains(key)){
            currId = getIdWithSegment(key, segmentStep, maxValue);
        }else{
            currId = getIdWithRedis(key, 1, maxValue);
        }
        return currId;
    }

    @Override
    public boolean init() {
        return true;
    }

    @Override
    public void destroy() {
        if(redisClient != null){
            redisClient.destroy();
        }
    }

    /**
     * 直接从redis中获取id号
     * @param key
     * @param count
     * @param maxId
     * @return
     */
    private Long getIdWithRedis(String key, int count, Long maxId){
        return redisClient.loopHIncrId(idHashKey, key, count, maxId);
    }

    /**
     * 采用分段发号的方式从redis获取id
     * @param key
     * @param maxId
     * @return
     */
    private Long getIdWithSegment(String key, int step, long maxId) {
        if (step > maxId) {
            throw new IdGenException("step cannot bigger than maxId");
        }

        SegmentBuffer buffer = cache.getOrDefault(key, null);
        if (buffer == null) {
            addSegmentBuffer(key, step, maxId);
            buffer = cache.get(key);
        }

        //出现这种情况是属于调用的客户端调整maxId的情况，此时并不会立即生效，如果客户端有多个实例，而这多个实例又不是同步修改了新的maxId，
        // 那么有可能会出现新maxId和旧maxId交替的情况，直到所有的客户端实例都更换成新maxId后，当前SegmentBuffer中的maxValue才能稳定下来
        // 这个过程的时间长短取决于客户端多个实例之间maxId统一的快慢，如果希望更快一点，可以在客户端实例的maxId统一之后重启当前id生成器服务端
        if (buffer.getMaxValue() != maxId) {
            buffer.setMaxValue(maxId);
        }

        Long id = getIdFromSegmentBuffer(buffer);
        return id;
    }

    /**
     * 初始化SegmentBuffer
     * @param key
     * @param step
     * @param maxValue
     */
    private void addSegmentBuffer(String key, int step, long maxValue){
        lock.lock();
        try {
            if(cache.containsKey(key)) { //double check
                return;
            }

            SegmentBuffer buffer = new SegmentBuffer();
            buffer.setKey(key);
            buffer.setMaxValue(maxValue);
            buffer.setMinStep(step);
            buffer.setStep(step);
            updateSegmentFromRedis(key, buffer.getCurrent());
            buffer.setInitOk(true);
            cache.put(key, buffer);//初始化完成之后再put进去，避免在初始化过程中时被其他线程读到
        } finally {
            lock.unlock();
        }
    }

    private Long getIdFromSegmentBuffer(final SegmentBuffer buffer) {
        while (true) {
            buffer.rLock().lock();
            try {
                final Segment segment = buffer.getCurrent();
                //当前Segment的剩余id小于90%的时候，就去加载下一个Segment的内容，这样一来，当当前Segment使用完的时候就可以直接切换到下一个Segment而不需等待
                if (!buffer.isNextReady() && (segment.getIdle() < 0.9 * segment.getStep())
                        && buffer.getThreadRunning().compareAndSet(false, true)) {
                    service.execute(() -> {
                        Segment next = buffer.getSegments()[buffer.nextPos()];
                        boolean updateOk = false;
                        try {
                            long start = System.currentTimeMillis();
                            updateSegmentFromRedis(buffer.getKey(), next);
                            updateOk = true;
                            long cost = System.currentTimeMillis() - start;
                            logger.info("cost={} success update segment from redis key={} {}", cost, buffer.getKey(), next);
                        } catch (Exception e) {
                            logger.error("key={} updateSegmentFromRedis exception", buffer.getKey(), e);
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

            //程序能走到这一步，说明当前Segment的id已经用完了，需要等待一段时间，等到next Segment更新完成
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
                    buffer.switchPos();//切换到下一个Segment
                    buffer.setNextReady(false);
                } else { //redis更新太慢或者需要更新的key太多，导致相互之间竞争线程池资源
                    throw new IdGenException("Too Busy! Both two segments are not ready!");
                }
            } finally {
                buffer.wLock().unlock();
            }
        }
    }

    private void updateSegmentFromRedis(String key, Segment segment) {
        SegmentBuffer buffer = segment.getBuffer();
        int nextStep = buffer.getStep();
        long maxValue = buffer.getMaxValue();
        long timestamp = buffer.getUpdateTimestamp();

        if (!buffer.isInitOk()) {

        } else if(buffer.getUpdateTimestamp() == 0) {
            timestamp = System.currentTimeMillis();
        } else if(segment.getBuffer().getMaxValue() <= MAX_STEP) {
            //id最大值小于最大步长的将不进行step拓容，避免拓容后就超过了其id的最大值
            timestamp = System.currentTimeMillis();
        } else {
            timestamp = System.currentTimeMillis();
            long duration = timestamp - buffer.getUpdateTimestamp();
            if (duration < SEGMENT_DURATION) {
                if (nextStep * 2 <= MAX_STEP) { //维持时间小于Segment的停留时间，则拓容
                    nextStep = nextStep * 2;
                }
            } else if (duration >= SEGMENT_DURATION * 2) { //维持时间超过2倍的Segment停留时间，则缩容
                nextStep = nextStep / 2 >= buffer.getMinStep() ? nextStep / 2 : nextStep;
            }
        }
        //由于step的自动拓容，所以，如果maxValue跟MAX_STEP的值比较接近，当step增长到MAX_STEP之后，再次执行 redisClient.loopIncrId() 时
        // 很可能就会被判定大于maxValue了，从而进入id循环，重新从1开始递增，这时可能会造成比较多的id被浪费掉了，所以，建议maxValue的值至少是
        // MAX_STEP值的2倍
        Long currMaxId = redisClient.loopHIncrId(idHashKey, key, nextStep, maxValue);
        long value = currMaxId - nextStep;
        segment.getValue().set(value == 0 ? 1 : value); //第一个id要从1开始
        segment.setMax(currMaxId);
        segment.setStep(nextStep);

        buffer.setStep(nextStep);
        buffer.setUpdateTimestamp(timestamp);
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
                    logger.warn("Thread {} Interrupted", Thread.currentThread().getName());
                    break;
                }
            }
        }
        logger.warn("waitAndSleep for {}(ms)", (System.currentTimeMillis() - start));
    }
}
