package com.xpay.service.sequence.biz;

import com.xpay.common.statics.exception.BizException;
import com.xpay.common.utils.DateUtil;
import com.xpay.libs.id.common.IdGenException;
import com.xpay.libs.id.service.RedisIdService;
import com.xpay.libs.id.service.SegmentService;
import com.xpay.libs.id.service.SnowflakeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * id序号生成逻辑层
 * @author chenyf
 */
@Component
public class SequenceBiz {
    public final static String LONG_VALUE_FORMAT = "%1$018d";
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired(required = false)
    private SnowflakeService snowflakeService;
    @Autowired(required = false)
    private SegmentService segmentService;
    @Autowired(required = false)
    private RedisIdService redisIdService;

    public Long nextRedisId(String key){
        try {
            return redisIdService.getId(key);
        } catch (IdGenException e) {
            throw new BizException(e.getMessage(), e);
        } catch (Exception e) {
            logger.error("id获取失败时异常 key={}", key, e);
            throw new BizException("id获取失败，系统异常！");
        }
    }

    public List<Long> nextRedisId(String key, int count){
        try {
            return redisIdService.getId(key, count);
        } catch (IdGenException e) {
            throw new BizException(e.getMessage(), e);
        } catch (Exception e) {
            logger.error("id获取失败时异常 key={}", key, e);
            throw new BizException("id获取失败，系统异常！");
        }
    }

    public String nextRedisId(String key, String prefix, boolean isWitchDate) {
        Long id = nextRedisId(key);
        return formatId(prefix, isWitchDate, id, LONG_VALUE_FORMAT);
    }

    public List<String> nextRedisId(String key, int count, String prefix, boolean isWithDate) {
        List<String> idStrList = new ArrayList<>(count);
        List<Long> idList = nextRedisId(key, count);
        for(Long id : idList){
            String idStr = formatId(prefix, isWithDate, id, LONG_VALUE_FORMAT);
            idStrList.add(idStr);
        }
        return idStrList;
    }

    public Long nextRedisId(String key, long maxValue){
        try {
            return redisIdService.getId(key, maxValue);
        } catch (IdGenException e) {
            throw new BizException(e.getMessage(), e);
        } catch (Exception e) {
            logger.error("id获取失败时异常 key={}", key, e);
            throw new BizException("id获取失败，系统异常！");
        }
    }

    public String nextRedisId(String key, long maxValue, String prefix, boolean isWithDate){
        try {
            int len = String.valueOf(maxValue).length();
            String format = "%1$0" + len + "d";
            long seqValue = redisIdService.getId(key, maxValue);
            return formatId(prefix, isWithDate, seqValue, format);//这种指定最大值值的，直接写死要跟日期连用
        } catch (IdGenException e) {
            throw new BizException(e.getMessage(), e);
        } catch (Exception e) {
            logger.error("id获取失败时异常 key={}", key, e);
            throw new BizException("id获取失败，系统异常！");
        }
    }

    public Long nextSnowId() {
        try {
            return snowflakeService.getId();
        } catch (IdGenException e) {
            throw new BizException(e.getMessage(), e);
        } catch (Exception e) {
            logger.error("id获取失败时异常", e);
            throw new BizException("id获取失败，系统异常！");
        }
    }

    public List<Long> nextSnowId(int count) {
        try {
            return snowflakeService.getId(count);
        } catch (IdGenException e) {
            throw new BizException(e.getMessage(), e);
        } catch (Exception e) {
            logger.error("id获取失败时异常 count={}", count, e);
            throw new BizException("id获取失败，系统异常！");
        }
    }

    public String nextSnowId(String prefix, boolean isWithDate) {
        Long id = nextSnowId();
        return formatId(prefix, isWithDate, id);
    }

    public List<String> nextSnowId(int count, String prefix, boolean isWithDate) {
        List<String> idStrList = new ArrayList<>(count);
        List<Long> idList = nextSnowId(count);
        for(Long id : idList){
            String idStr = formatId(prefix, isWithDate, id);
            idStrList.add(idStr);
        }
        return idStrList;
    }

    public Long nextSegmentId(String key) {
        try {
            return segmentService.getId(key);
        } catch (IdGenException e) {
            throw new BizException(e.getMessage(), e);
        } catch (Exception e) {
            logger.error("id获取失败时异常 key={}", key, e);
            throw new BizException("id获取失败，系统异常！");
        }
    }

    /**
     * 使用数据库批量生成Id序列号
     *
     * @param key
     * @param count
     * @return
     * @see #nextSegmentId(String)
     */
    public List<Long> nextSegmentId(String key, int count) {
        try {
            return segmentService.getId(key, count);
        } catch (IdGenException e) {
            throw new BizException(e.getMessage(), e);
        } catch (Exception e) {
            logger.error("id获取失败时异常 key={}", key, e);
            throw new BizException("id获取失败，系统异常！");
        }
    }

    public String nextSegmentId(String key, String prefix, boolean isWithDate){
        long id = nextSegmentId(key);
        return formatId(prefix, isWithDate, id, LONG_VALUE_FORMAT);
    }

    public List<String> nextSegmentId(String key, int count, String prefix, boolean isWithDate) {
        List<String> idStrList = new ArrayList<>(count);
        List<Long> idList = nextSegmentId(key, count);
        for(Long id : idList){
            String idStr = formatId(prefix, isWithDate, id, LONG_VALUE_FORMAT);
            idStrList.add(idStr);
        }
        return idStrList;
    }

    private String formatId(String prefix, boolean isWithDate, Long id){
        return isWithDate ? prefix + DateUtil.formatShortDate(new Date()) + id : prefix + id;
    }
    private String formatId(String prefix, boolean isWithDate, Long id, String format){
        String seqStr = String.format(format, id);
        if(isWithDate){
            return prefix + DateUtil.formatShortDate(new Date()) + seqStr;
        }else{
            return prefix + seqStr;
        }
    }
}
