package com.xpay.service.sequence.serviceImpl;

import com.xpay.facade.sequence.service.SequenceFacade;
import com.xpay.service.sequence.biz.SequenceBiz;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@DubboService
public class SequenceFacadeImpl implements SequenceFacade {
    @Autowired
    SequenceBiz sequenceBiz;

    /**
     * 获取redis循环递增id，当id值达到long型最大值时将重新从1开始递增
     * @param key
     * @return
     */
    @Override
    public Long nextRedisId(String key){
        return sequenceBiz.nextRedisId(key);
    }

    /**
     * 批量获取redis循环递增id，当id值达到long型最大值时将重新从1开始递增
     * @param key
     * @param count
     * @return
     */
    @Override
    public List<Long> nextRedisId(String key, int count){
        return sequenceBiz.nextRedisId(key, count);
    }

    @Override
    public String nextRedisId(String key, String prefix, boolean isWithDate) {
        return sequenceBiz.nextRedisId(key, prefix, isWithDate);
    }

    @Override
    public List<String> nextRedisId(String key, int count, String prefix, boolean isWithDate) {
        return sequenceBiz.nextRedisId(key, count, prefix, isWithDate);
    }

    /**
     * 获取redis循环递增id，当id值超过maxValue的值时将重新从1开始递增
     * @param key
     * @param maxValue
     * @return
     */
    @Override
    public Long nextRedisId(String key, long maxValue){
        return sequenceBiz.nextRedisId(key, maxValue);
    }

    @Override
    public String nextRedisId(String key, long maxValue, String prefix, boolean isWithDate) {
        return sequenceBiz.nextRedisId(key, maxValue, prefix, isWithDate);
    }

    @Override
    public Long nextSnowId() {
        return sequenceBiz.nextSnowId();
    }

    @Override
    public List<Long> nextSnowId(int count) {
        return sequenceBiz.nextSnowId(count);
    }

    @Override
    public String nextSnowId(String prefix, boolean isWithDate) {
        return sequenceBiz.nextSnowId(prefix, isWithDate);
    }

    @Override
    public List<String> nextSnowId(int count, String prefix, boolean isWithDate){
        return sequenceBiz.nextSnowId(count, prefix, isWithDate);
    }

    @Override
    public Long nextSegmentId(String bizKey){
        return sequenceBiz.nextSegmentId(bizKey);
    }

    /**
     * 使用数据库批量生成Id序列号
     * @see #nextSegmentId(String)
     * @param bizKey
     * @param count
     * @return
     */
    @Override
    public List<Long> nextSegmentId(String bizKey, int count){
        return sequenceBiz.nextSegmentId(bizKey, count);
    }

    public String nextSegmentId(String key, String prefix, boolean isWithDate){
        return sequenceBiz.nextSegmentId(key, prefix, isWithDate);
    }

    public List<String> nextSegmentId(String key, int count, String prefix, boolean isWithDate) {
        return sequenceBiz.nextSegmentId(key, count, prefix, isWithDate);
    }
}
