package com.xpay.libs.id.service;

import com.alibaba.druid.pool.DruidDataSource;
import com.xpay.libs.id.config.SegmentProperties;
import com.xpay.libs.id.common.IdGenException;
import com.xpay.libs.id.generator.IDGen;
import com.xpay.libs.id.generator.zero.ZeroIDGen;
import com.xpay.libs.id.generator.segment.SegmentIDGenImpl;

import com.xpay.libs.id.generator.segment.dao.IDAllocDao;
import com.xpay.libs.id.generator.segment.dao.impl.IDAllocDaoImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.List;

public class SegmentService {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private IDGen idGen;
    private DruidDataSource dataSource;
    private SegmentProperties segment;

    public SegmentService(SegmentProperties segment) {
        this.segment = segment;
        init();
    }

    public Long getId(String key) throws IdGenException {
        return idGen.get(key);
    }

    public List<Long> getId(String key, int count) throws IdGenException {
        return idGen.get(key, count);
    }

    public void destroy() {
        try{
            idGen.destroy();
        }catch(Exception e){
        }
        try{
            dataSource.close();
        }catch(Exception e){
        }
    }

    private void init() {
        boolean flag = segment != null && segment.getEnabled();
        if (flag) {
            // Config dataSource
            dataSource = new DruidDataSource();
            dataSource.setName(segment.getName());
            dataSource.setUrl(segment.getJdbcUrl());
            dataSource.setUsername(segment.getUsername());
            dataSource.setPassword(segment.getPassword());
            dataSource.setDriverClassName(segment.getDriverClassName());
            dataSource.setInitialSize(segment.getInitialSize());
            dataSource.setMaxActive(segment.getMaxActive());
            dataSource.setMinIdle(segment.getMinIdle());
            dataSource.setMaxWait(segment.getMaxWait());
            dataSource.setPoolPreparedStatements(segment.getPoolPreparedStatements());
            dataSource.setSharePreparedStatements(segment.getSharePreparedStatements());
            dataSource.setMaxPoolPreparedStatementPerConnectionSize(segment.getMaxPoolPreparedStatementPerConnectionSize());
            dataSource.setInitExceptionThrow(segment.getInitExceptionThrow());
            dataSource.setMaxOpenPreparedStatements(segment.getMaxOpenPreparedStatements());
            dataSource.setConnectionErrorRetryAttempts(segment.getConnectionErrorRetryAttempts());
            dataSource.setValidationQuery(segment.getValidationQuery());
            dataSource.setValidationQueryTimeout(segment.getValidationQueryTimeout());
            dataSource.setTestOnBorrow(segment.getTestOnBorrow());
            dataSource.setTestOnReturn(segment.getTestOnReturn());
            dataSource.setTestWhileIdle(segment.getTestWhileIdle());
            dataSource.setTimeBetweenEvictionRunsMillis(segment.getTimeBetweenEvictionRunsMillis());
            dataSource.setMinEvictableIdleTimeMillis(segment.getMinEvictableIdleTimeMillis());
            dataSource.setMaxEvictableIdleTimeMillis(segment.getMaxEvictableIdleTimeMillis());
            dataSource.setKeepAliveBetweenTimeMillis(segment.getKeepAliveBetweenTimeMillis());
            dataSource.setAsyncInit(segment.getAsyncInit());
            try {
                dataSource.init();
            } catch(SQLException e) {
                throw new RuntimeException("Segment Service dataSource Init Fail", e);
            }

            // Config Dao
            IDAllocDao dao = new IDAllocDaoImpl(dataSource);

            // Config ID Gen
            idGen = new SegmentIDGenImpl(segment.getSegmentStep(), segment.getLazily());
            ((SegmentIDGenImpl) idGen).setDao(dao);
            if (idGen.init()) {
                logger.info("Segment Service Init Successfully");
            } else {
                throw new RuntimeException("Segment Service Init Fail");
            }
        } else {
            idGen = new ZeroIDGen("SegmentService Use ZeroIDGen!");
        }
    }
}
