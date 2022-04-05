package com.xpay.libs.id.generator.segment.dao.impl;

import com.xpay.libs.id.generator.segment.dao.IDAllocDao;
import com.xpay.libs.id.generator.segment.dao.IDAllocMapper;
import com.xpay.libs.id.generator.segment.model.IDAlloc;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.TransactionFactory;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IDAllocDaoImpl implements IDAllocDao {
    private String mapperNamespace;
    private SqlSessionFactory sqlSessionFactory;

    public IDAllocDaoImpl(DataSource dataSource) {
        TransactionFactory transactionFactory = new JdbcTransactionFactory();
        Environment environment = new Environment("development", transactionFactory, dataSource);
        Configuration configuration = new Configuration(environment);
        configuration.addMapper(IDAllocMapper.class);
        sqlSessionFactory = new SqlSessionFactoryBuilder().build(configuration);
        mapperNamespace = IDAllocMapper.class.getName();//设置Mapper的命名空间
    }

    @Override
    public int addIDAlloc(IDAlloc idAlloc) {
        SqlSession sqlSession = sqlSessionFactory.openSession(false);
        try {
            int count = sqlSession.insert(fillSqlId("addIDAlloc"), idAlloc);
            sqlSession.commit();
            return count;
        } finally {
            sqlSession.close();
        }
    }

    @Override
    public List<IDAlloc> listAllIDAlloc() {
        SqlSession sqlSession = sqlSessionFactory.openSession(false);
        try {
            return sqlSession.selectList(fillSqlId("listAllIDAlloc"));
        } finally {
            sqlSession.close();
        }
    }

    @Override
    public List<String> listAllKeys() {
        SqlSession sqlSession = sqlSessionFactory.openSession(false);
        try {
            return sqlSession.selectList(fillSqlId("listAllKeys"));
        } finally {
            sqlSession.close();
        }
    }

    @Override
    public IDAlloc getIDAllocByKey(String key) {
        SqlSession sqlSession = sqlSessionFactory.openSession(false);
        try {
            return getIDAllocByKey(sqlSession, key);
        } finally {
            sqlSession.close();
        }
    }

    @Override
    public IDAlloc increaseMaxIdAndGetIDAlloc(String key) {
        SqlSession sqlSession = sqlSessionFactory.openSession(false);
        try {
            sqlSession.update(fillSqlId("increaseMaxId"), key);
            IDAlloc result = getIDAllocByKey(sqlSession, key);
            sqlSession.commit();
            return result;
        } finally {
            sqlSession.close();
        }
    }

    @Override
    public IDAlloc increaseMaxIdAndGetIDAlloc(String key, int step) {
        SqlSession sqlSession = sqlSessionFactory.openSession(false);
        try {
            Map<String, Object> param = new HashMap<>();
            param.put("key", key);
            param.put("step", step);
            sqlSession.update(fillSqlId("increaseMaxIdByDynamicStep"), param);
            IDAlloc result = getIDAllocByKey(sqlSession, key);
            sqlSession.commit();
            return result;
        } finally {
            sqlSession.close();
        }
    }

    private IDAlloc getIDAllocByKey(SqlSession sqlSession, String key){
        return sqlSession.selectOne(fillSqlId("getIDAllocByKey"), key);
    }

    private String fillSqlId(String sqlId){
        return mapperNamespace + "." + sqlId;
    }
}
