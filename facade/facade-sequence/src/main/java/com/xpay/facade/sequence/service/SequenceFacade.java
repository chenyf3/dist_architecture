package com.xpay.facade.sequence.service;

import com.xpay.common.statics.exception.BizException;

import java.util.List;

public interface SequenceFacade {
    /**
     * 获取redis循环递增id（单调递增），最大值为long型最大值，默认采用分段发号，应用重启时会有id不连续的情况，如果不采用分段发号性能在1k/s上下，但不会有id不连续的情况
     * @param key
     * @return
     */
    public Long nextRedisId(String key) throws BizException;

    /**
     * 批量获取redis循环递增id（单调递增），最大值为long型最大值
     * @param key
     * @param count
     * @return
     */
    public List<Long> nextRedisId(String key, int count) throws BizException;

    /**
     * 获取带格式化的redis循环递增id（单调递增），最大值为long型最大值，返回格式化后的id，样例：
     * 带日期：BL210706000000000000000028，无日期：BL000000000000000028
     *
     * @param key
     * @param prefix
     * @param isWithDate
     * @return
     * @throws BizException
     */
    public String nextRedisId(String key, String prefix, boolean isWithDate) throws BizException;

    /**
     * 批量获取带格式化的redis循环递增id（单调递增）带日期：BL210706000000000000000028，无日期：BL000000000000000028
     * @param key
     * @param count
     * @param prefix
     * @param isWithDate
     * @return
     * @throws BizException
     */
    public List<String> nextRedisId(String key, int count, String prefix, boolean isWithDate) throws BizException;

    /**
     * 获取redis循环递增id（单调递增），当id值超过maxValue的值时将重新从1开始递增，样例：BL0000000028、BL000000038、BL00000058 等
     * @param key
     * @param maxValue
     * @return
     */
    public Long nextRedisId(String key, long maxValue) throws BizException;

    /**
     * 批量获取带格式化的redis循环递增id（单调递增），当id值超过maxValue的值时将重新从1开始递增，同时，把前缀拼接序列号之后返回
     * @param key
     * @param maxValue
     * @param prefix
     * @return
     */
    public String nextRedisId(String key, long maxValue, String prefix, boolean isWithDate) throws BizException;

    /**
     * 使用雪花算法生成id序列号(雪花算法id的字符串长度最大为18)，样例：94556422511005796
     * 特点：
     *     1、效率高
     *     2、趋势递增，无法绝对单调递增
     *     3、如果workerId设置不当，会引起Id序列号重复
     * @return
     */
    public Long nextSnowId() throws BizException;

    /**
     * 使用雪花算法批量生成id序列号(雪花算法id的字符串长度最大为18)，样例：94556422511005796
     * @see #nextSnowId()
     * @param count     生成id的个数
     * @return
     */
    public List<Long> nextSnowId(int count) throws BizException;

    /**
     * 使用雪花算法生成id序列号，并可以拼接前缀和日期，适用于业务流水号等(雪花算法id的字符串长度最大为18)，样例：，
     * 带日期：BL2021071694556422511005796，无日期：BL94556422511005796
     *
     * @see #nextSnowId()
     * @param prefix
     * @param isWithDate
     * @return
     */
    public String nextSnowId(String prefix, boolean isWithDate) throws BizException;

    /**
     * 使用雪花算法批量生成id序列号，并可以拼接前缀和日期，适用于业务流水号(雪花算法id的字符串长度最大为18)
     * @see #nextSnowId(String, boolean)
     * @param count
     * @param prefix
     * @param isWithDate
     * @return
     */
    public List<String> nextSnowId(int count, String prefix, boolean isWithDate) throws BizException;

    /**
     * 使用数据库分段发号生成id序列号，适合需要id单调递增的业务
     * 特点：
     *      1、其效率取决于数据库更新一条记录的效率，一般情况下没有雪花算法和redis的高
     *      2、绝对单调递增，最大值是long类型的最大长度
     *      3、如果数据库是单机部署，会有单点故障问题，若应用宕机或重启，不会存在序列号重复，安全性比redis要高
     *      4、如果要解决单点故障，需要通过PXC等高可用、强一致性集群部署才可解决此问题，成本较高
     *      5、如果应用重启，会存在一部分序列号被浪费掉，业务上看来就是序列号不连续
     *
     * @param key    业务标识(不要使用中文)
     * @return
     */
    public Long nextSegmentId(String key) throws BizException;

    /**
     * 使用数据库分段发号批量生成id序列号，适合需要id单调递增的业务
     * @see #nextSegmentId(String)
     * @param key       业务标识(不要使用中文)
     * @param count     生成id的个数
     * @return
     */
    public List<Long> nextSegmentId(String key, int count) throws BizException;

    /**
     * 使用数据库分段发号生成id序列号，适合需要id单调递增的业务，返回样例：BL000000000000000012
     * @see #nextSegmentId(String)
     * @param key
     * @param prefix
     * @return
     * @throws BizException
     */
    public String nextSegmentId(String key, String prefix, boolean isWithDate) throws BizException;

    /**
     * 使用数据库分段发号批量生成id序列号，适合需要id单调递增的业务，返回样例：BL20210608000000000000000012、BL000000000000000012
     * @see #nextSegmentId(String)
     * @param prefix
     * @param key
     * @return
     * @throws BizException
     */
    public List<String> nextSegmentId(String key, int count, String prefix, boolean isWithDate) throws BizException;
}
