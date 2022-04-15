package com.xpay.common.service.dao;

import com.xpay.common.service.annotations.PK;
import com.xpay.common.service.exceptions.DBException;
import com.xpay.common.service.util.ClassUtil;
import com.xpay.common.statics.query.PageQuery;
import com.xpay.common.statics.result.PageResult;
import org.apache.ibatis.session.RowBounds;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.support.SqlSessionDaoSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 基于Mybatis的基础DAO，实现了常用的增删改查方法
 *
 * @author chenyf
 * @date 2018-02-02
 */
public class MyBatisDao<T, ID extends Serializable> extends SqlSessionDaoSupport {
    protected Logger logger = LoggerFactory.getLogger(this.getClass());
    /**
     * 需要排序时排序字段在Map中的key名
     */
    public final static String SORT_COLUMNS = "sortColumns";
    /**
     * mybatis的命名空间与sqlId之间的分隔符
     */
    private final static String NAMESPACE_SEPARATOR = ".";
    /**
     * 防止sql注入的正则：只允许：字母开头，中间允许字母、数字、下划线、空格、逗号，结尾允许 字母、数字
     */
    private final static Pattern SQL_INJECT_DEFEND_PATTERN = Pattern.compile("^[a-zA-Z][a-zA-Z0-9_\\s,]*[a-zA-Z0-9]+$");

    /**
     * 在mybatis的mapper文件中的命名空间（等于当前Dao的实体类的class全名）
     */
    private String mapperNamespace;
    /**
     * 主键的列名称，默认使用id作为这列的列名称
     */
    private String pkColumnName = "id";

    /**
     * ----------------  在mybatis的mapper文件中一些常用的、并且跟本类中的方法相对应的sqlId  ------------------
     */
    protected final static String INSERT_SQL = "insert";
    protected final static String INSERT_LIST_SQL = "batchInsert";
    protected final static String UPDATE_SQL = "update";
    protected final static String UPDATE_IF_NOT_NULL_SQL = "updateIfNotNull";
    protected final static String DELETE_BY_SQL = "deleteBy";
    protected final static String COUNT_BY_SQL = "countBy";
    protected final static String STATISTICS_BY_SQL = "statisticsBy";
    protected final static String LIST_BY_SQL = "listBy";
    protected final static String GET_BY_PK_SQL = "getById";
    protected final static String LIST_BY_PK_LIST_SQL = "listByIdList";
    protected final static String DELETE_BY_PK_SQL = "deleteById";
    protected final static String DELETE_BY_PK_LIST_SQL = "deleteByIdList";

    @Autowired
    @Override
    public void setSqlSessionTemplate(SqlSessionTemplate sqlSessionTemplate) {
        super.setSqlSessionTemplate(sqlSessionTemplate);
    }

    //初始化bean属性
    @Override
    protected void initDao() {
        Class cla = ClassUtil.getSuperClassGenericType(this.getClass(), 0);
        this.setMapperNamespace(cla.getName());

        //设置primary key的列名称
        Field[] fields = cla.getDeclaredFields();
        for (Field field : fields) {
            PK pk = field.getAnnotation(PK.class);
            if (pk != null) {
                pkColumnName = field.getName();
                break;
            }
        }
        Assert.hasLength(this.mapperNamespace, "Property 'mapperNamespace' are required");
        logger.debug("using '{}' as the primary key column name for {}", pkColumnName, cla.getName());
    }

    /**
     * 插入一条实体数据
     *
     * @param entity 实体数据
     * @throws RuntimeException 当插入成功的记录数不为1时，抛出此异常
     */
    public void insert(T entity) {
        int insertedRow = this.getSqlSession().insert(fillSqlId(INSERT_SQL), entity);
        if (insertedRow != 1) {
            throw new DBException(DBException.AFFECT_COUNT_NOT_UNIQUE, "数据插入的记录数不为1, insertedRow="+insertedRow);
        }
    }

    /**
     * 批量插入实体数据
     *
     * @param list 实体数据列表
     * @throws RuntimeException 当插入成功的记录数与list的size不一致时,抛出此异常
     */
    public void insert(List<T> list) {
        int insertedRow = this.getSqlSession().insert(fillSqlId(INSERT_LIST_SQL), list);
        if (insertedRow != list.size()) {
            throw new DBException(DBException.AFFECT_COUNT_NOT_EXPECT, "数据插入的记录数与预期不匹配，预期:" + list.size() + ",实际插入:" + insertedRow);
        }
    }

    /**
     * 根据自定义sql插入数据
     *
     * @param param .
     * @return int 实际插入数据的条数
     */
    public int insert(String sqlId, Object param) {
        return this.getSqlSession().insert(fillSqlId(sqlId), param);
    }

    /**
     * 根据多个条件删除记录
     *
     * @param paramMap .
     * @return 实际删除数据的条数
     */
    public int deleteBy(Map<String, Object> paramMap) {
        return this.deleteBy(DELETE_BY_SQL, paramMap);
    }

    /**
     * 根据自定义sql删除记录
     *
     * @param param .
     * @return 实际删除记录的条数
     */
    public int deleteBy(String sqlId, Object param) {
        return this.getSqlSession().delete(this.fillSqlId(sqlId), param);
    }

    /**
     * 更新数据
     *
     * @param entity 待更新的实体
     * @throws RuntimeException 当成功更新的数据条数不为1时，抛出此异常
     */
    public void update(T entity) {
        int updatedRow = this.getSqlSession().update(fillSqlId(UPDATE_SQL), entity);
        if (updatedRow != 1) {
            throw new DBException(DBException.AFFECT_COUNT_NOT_UNIQUE, "数据更新成功的记录数不为1, updatedRow="+updatedRow);
        }
    }

    /**
     * 批量更新对象，如果需要保持数据的一致性，需要确保调用这个方法的地方有支持事务
     *
     * @param entityList 待更新的实体列表
     */
    public void update(List<T> entityList) {
        int result = 0;
        int expectCount = entityList.size();
        for (T entity : entityList) {
            int updateCount = this.getSqlSession().update(fillSqlId(UPDATE_SQL), entity);
            result += updateCount;
        }
        if (result != entityList.size()) {
            throw new DBException(DBException.AFFECT_COUNT_NOT_EXPECT, "数据更新成功的记录数(" + result + ")与预期数(" + expectCount + ")不匹配");
        }
    }

    /**
     * 按值更新，字段值不为null的才更新
     *
     * @param entity 待更新的实体
     * @throws RuntimeException 当成功更新的数据条数不为1时，抛出此异常
     */
    public void updateIfNotNull(T entity) {
        int updatedRow = this.getSqlSession().update(fillSqlId(UPDATE_IF_NOT_NULL_SQL), entity);
        if (updatedRow != 1) {
            throw new DBException(DBException.AFFECT_COUNT_NOT_UNIQUE, "数据更新成功的记录数不为1, updatedRow="+updatedRow);
        }
    }

    /**
     * 按自定义sql的更新
     *
     * @param sqlId .
     * @param param .
     * @return 实际更新的记录数
     */
    public int update(String sqlId, Object param) {
        return this.getSqlSession().update(this.fillSqlId(sqlId), param);
    }

    /**
     * 取得只可能有一条记录的数据，如：根据 unique key 获取
     * 注意：请自行确保查询条件只会查到一条记录，否则会报错
     *
     * @param paramMap 查询条件
     * @return 查询到的实体数据
     */
    public T getOne(Map<String, Object> paramMap) {
        return this.getOne(LIST_BY_SQL, paramMap);
    }

    /**
     * 根据自定义语句，获取符合条件的单个对象
     *
     * @param sqlId .
     * @param param .
     * @return .
     */
    public <E> E getOne(String sqlId, Object param) {
        checkSortColumns(param);
        return this.getSqlSession().selectOne(fillSqlId(sqlId), param);
    }

    /**
     * 获取所有记录并返回List
     *
     * @return .
     */
    public List<T> listAll() {
        return this.listAll(null);
    }

    /**
     * 获取所有记录并返回List，并指定排序字段
     *
     * @param sortColumns 排序条件
     * @return .
     */
    public List<T> listAll(String sortColumns) {
        Map<String, String> paramMap = null;
        if (isNotEmpty(sortColumns)) {
            checkSortColumns(sortColumns);
            paramMap = new HashMap<>(1);
            paramMap.put(SORT_COLUMNS, sortColumns);
        }
        return this.getSqlSession().selectList(fillSqlId(LIST_BY_SQL), paramMap);
    }

    /**
     * 取得符合条件的总记录数
     *
     * @param paramMap .
     * @return .
     */
    public long countBy(Map<String, Object> paramMap) {
        return this.countBy(COUNT_BY_SQL, paramMap);
    }

    /**
     * 根据自定义语句，取得符合条件的总记录数
     * @param sqlId     统计的sqlId
     * @param param     查询参数
     * @return
     */
    public long countBy(String sqlId, Object param) {
        Long counts = this.getSqlSession().selectOne(fillSqlId(sqlId), param);
        return counts == null ? 0 : counts;
    }

    /**
     * 统计
     * @param paramMap  查询参数
     * @param <S>       返回的统计实体
     * @return
     */
    public <S> S statisticsBy(Map<String, Object> paramMap) {
        return statisticsBy(STATISTICS_BY_SQL, paramMap);
    }

    /**
     * 统计
     * @param sqlId     执行统计的sqlId
     * @param paramMap  查询参数
     * @param <S>       返回的统计实体
     * @return
     */
    public <S> S statisticsBy(String sqlId, Map<String, Object> paramMap) {
        return this.getSqlSession().selectOne(fillSqlId(sqlId), paramMap);
    }

    /**
     * 多条件and查询并返回List(不分页、不排序)
     *
     * @param paramMap .
     * @return .
     */
    public List<T> listBy(Map<String, Object> paramMap) {
        return this.listBy(paramMap, null);
    }

    /**
     * 多条件and查询并返回List(不分页、排序)
     *
     * @param paramMap
     * @param sortColumns
     * @return
     */
    public List<T> listBy(Map<String, Object> paramMap, String sortColumns) {
        return this.listBy(LIST_BY_SQL, paramMap, sortColumns);
    }

    /**
     * 根据自定义语句，取得符合条件的记录并返回List(不分页)
     *
     * @param sqlId
     * @param paramMap
     * @return
     */
    public <E> List<E> listBy(String sqlId, Map<String, Object> paramMap) {
        return this.listBy(sqlId, paramMap, null);
    }

    public <E> List<E> listBy(Map<String, Object> paramMap, Integer offset, Integer limit) {

    }

    public <E> List<E> listBy(Map<String, Object> paramMap, String sortColumns, Integer offset, Integer limit) {

    }

    /**
     * 根据自定义语句，取得符合条件的记录并返回List(分页、不排序)
     * @param sqlId         mapper文件中的sqlId
     * @param paramMap      查询参数
     * @param offset        分页时的起始偏移量
     * @param limit         当前页查询条数
     * @return
     */
    public <E> List<E> listBy(String sqlId, Map<String, Object> paramMap, Integer offset, Integer limit) {
        return listBy(sqlId, paramMap, null, offset, limit);
    }

    /**
     * 根据自定义语句，取得符合条件的记录并返回List(分页、可排序)
     * @param sqlId         mapper文件中的sqlId
     * @param paramMap      查询参数
     * @param sortColumns   排序字段
     * @param offset        分页时的起始偏移量
     * @param limit         当前页查询条数
     * @param <E>
     * @return
     */
    public <E> List<E> listBy(String sqlId, Map<String, Object> paramMap, String sortColumns, Integer offset, Integer limit) {
        if (isNotEmpty(sortColumns)) {
            this.checkSortColumns(sortColumns);
            if (paramMap == null) {
                paramMap = new HashMap(1);
            }
            paramMap.put(SORT_COLUMNS, sortColumns);
        } else {
            this.checkSortColumns(paramMap);
        }
        return this.getSqlSession().selectList(fillSqlId(sqlId), paramMap, new RowBounds(offset, limit));
    }

    /**
     * 根据自定义语句，取得符合条件的记录并返回List(不分页、可排序)
     *
     * @param paramMap
     * @param sortColumns
     * @return
     */
    public <E> List<E> listBy(String sqlId, Map<String, Object> paramMap, String sortColumns) {
        if (isNotEmpty(sortColumns)) {
            this.checkSortColumns(sortColumns);
            if (paramMap == null) {
                paramMap = new HashMap(1);
            }
            paramMap.put(SORT_COLUMNS, sortColumns);
        } else {
            this.checkSortColumns(paramMap);
        }

        return this.getSqlSession().selectList(fillSqlId(sqlId), paramMap);
    }

    /**
     * 多条件and查询并返回List(可分页、可排序)
     *
     * @param paramMap
     * @param pageQuery
     * @return
     */
    public PageResult<List<T>> listPage(Map<String, Object> paramMap, PageQuery pageQuery) {
        return this.listPage(LIST_BY_SQL, COUNT_BY_SQL, paramMap, pageQuery);
    }

    /**
     * 多条件and查询并返回List(可分页、可排序)
     *
     * @param sqlId     查询的sqlId
     * @param paramMap
     * @param pageQuery
     * @return
     */
    public <E> PageResult<List<E>> listPage(String sqlId, Map<String, Object> paramMap, PageQuery pageQuery) {
        return this.listPage(sqlId, COUNT_BY_SQL, paramMap, pageQuery);
    }

    /**
     * 根据自定义语句，取得符合条件的记录并返回List(可分页、可排序)
     *
     * @param sqlId      查询的sqlId
     * @param countSqlId 计算总记录数的sqlId，如果PageQuery中的isNeedTotalRecord=false，则此值会被忽略
     * @param paramMap
     * @param pageQuery
     * @return
     */
    public <E> PageResult<List<E>> listPage(String sqlId, String countSqlId, Map<String, Object> paramMap, PageQuery pageQuery) {
        Long totalRecord = 0L;
        List<E> dataList;
        if (pageQuery.isNeedTotalRecord()) {
            totalRecord = this.countBy(countSqlId, paramMap);
            if (totalRecord <= 0) {
                //如果总记录数为0，就直接返回了
                dataList = new ArrayList<>();
                return PageResult.newInstance(dataList, pageQuery, totalRecord);
            }
        }

        if (isNotEmpty(pageQuery.getSortColumns())) {
            checkSortColumns(pageQuery.getSortColumns());
            if (paramMap == null) {
                paramMap = new HashMap(1);
            }
            paramMap.put(SORT_COLUMNS, pageQuery.getSortColumns());
        } else {
            checkSortColumns(paramMap);
        }

        dataList = this.getSqlSession().selectList(fillSqlId(sqlId), paramMap,
                new RowBounds(getOffset(pageQuery), pageQuery.getPageSize()));
        if (!pageQuery.isNeedTotalRecord()) {
            totalRecord = (long) dataList.size();
        }
        return PageResult.newInstance(dataList, pageQuery, totalRecord);
    }

    /**
     * 多条件查询并返回MAP（不分页）
     *
     * @param paramMap
     * @param property
     * @param <K>
     * @return
     */
    public <K> Map<K, T> mapBy(Map<String, Object> paramMap, String property) {
        return this.mapBy(LIST_BY_SQL, paramMap, property);
    }

    /**
     * 自定义语句查询并返回MAP（不分页）
     * key:value = 某个值为字符串的字段:实体对象 的键值对，其中key是property参数指定的字段名的值
     *
     * @param paramMap
     * @return
     */
    public <K, E> Map<K, E> mapBy(String sqlId, Map<String, Object> paramMap, String property) {
        checkSortColumns(paramMap);
        return this.getSqlSession().selectMap(fillSqlId(sqlId), paramMap, property);
    }

    /**
     * 多条件查询并返回MAP（分页）
     *
     * @param paramMap
     * @param property
     * @param <K>
     * @return
     */
    public <K> PageResult<Map<K, T>> mapPage(Map<String, Object> paramMap, String property, PageQuery pageQuery) {
        return this.mapPage(LIST_BY_SQL, COUNT_BY_SQL, paramMap, property, pageQuery);
    }

    /**
     * 自定义语句查询并返回MAP（分页）
     * 注意：会在数据库进行排序，但返回到程序中的Map是无序的
     *
     * @param sqlId      查询的sqlId
     * @param countSqlId 计算总记录数的sqlId，如果PageQuery中的isNeedTotalRecord=false，则此值会被忽略
     * @param paramMap
     * @param property
     * @param pageQuery
     * @param <K>
     * @param <E>
     * @return Map key:value = 某个值为字符串的字段:实体对象 的键值对，其中key是property参数指定的字段名的值
     */
    public <K, E> PageResult<Map<K, E>> mapPage(String sqlId, String countSqlId, Map<String, Object> paramMap, String property, PageQuery pageQuery) {
        Long totalRecord = 0L;
        Map<K, E> dataMap;
        if (pageQuery.isNeedTotalRecord()) {
            totalRecord = this.countBy(countSqlId, paramMap);
            if (totalRecord <= 0) {
                //如果总记录数为0，就直接返回了
                dataMap = new HashMap<>();
                return PageResult.newInstance(dataMap, pageQuery, totalRecord);
            }
        }

        if (isNotEmpty(pageQuery.getSortColumns())) {
            checkSortColumns(pageQuery.getSortColumns());
            if (paramMap == null) {
                paramMap = new HashMap(1);
            }
            paramMap.put(SORT_COLUMNS, pageQuery.getSortColumns());
        } else {
            checkSortColumns(paramMap);
        }

        dataMap = this.getSqlSession().selectMap(fillSqlId(sqlId), paramMap, property,
                new RowBounds(getOffset(pageQuery), pageQuery.getPageSize()));
        if (!pageQuery.isNeedTotalRecord()) {
            totalRecord = (long) dataMap.size();
        }
        return PageResult.newInstance(dataMap, pageQuery, totalRecord);
    }


    /**------------------------------------------------- 根据PK主键作相关操作的便捷方法 START ---------------------------------------------------*/
    /**
     * 根据主键删除记录
     * 当单一主键时传主键对象即可,当为组合组件时传MAP
     *
     * @param id 记录的主键id
     */
    public void deleteById(ID id) {
        int deletedRow = this.getSqlSession().delete(fillSqlId(DELETE_BY_PK_SQL), id);
        if (deletedRow != 1) {
            throw new DBException(DBException.AFFECT_COUNT_NOT_UNIQUE, "数据删除成功的记录数不为1, deletedRow="+deletedRow);
        }
    }

    /**
     * 根据多个主键删除记录
     *
     * @param idList 记录的主键id列表
     */
    public void deleteByIdList(List<ID> idList) {
        int deleteRow = this.getSqlSession().delete(fillSqlId(DELETE_BY_PK_LIST_SQL), idList);
        if (deleteRow != idList.size()) {
            throw new DBException(DBException.AFFECT_COUNT_NOT_EXPECT, "数据删除成功的记录数与预期删除数不一致,预期删除数:" + idList.size() + ",实际删除数:" + deleteRow);
        }
    }

    /**
     * 根据主键获取记录
     *
     * @param id
     * @return
     */
    public T getById(ID id) {
        return this.getSqlSession().selectOne(fillSqlId(GET_BY_PK_SQL), id);
    }

    /**
     * 根据多个主键获取记录
     *
     * @param idList List<Long>
     * @return
     */
    public List<T> listByIdList(List<ID> idList) {
        return this.getSqlSession().selectList(fillSqlId(LIST_BY_PK_LIST_SQL), idList);
    }

    /**
     * 多条件and查询并返回以id为key的MAP（不分页）
     * key:value = 主键:实体对象 的键值对，其中key默认是字段名为id的值
     *
     * @param paramMap
     * @return
     */
    public Map<ID, T> mapById(Map<String, Object> paramMap) {
        return this.mapBy(LIST_BY_SQL, paramMap, pkColumnName);
    }

    /**
     * 多条件查询并返回以id为key的MAP（分页）
     * <p>
     * key:value = 主键:实体对象 的键值对，其中key默认是字段名为id的值
     *
     * @param paramMap
     * @return
     */
    public PageResult<Map<ID, T>> mapByIdPage(Map<String, Object> paramMap, PageQuery pageQuery) {
        return this.mapPage(LIST_BY_SQL, COUNT_BY_SQL, paramMap, pkColumnName, pageQuery);
    }
    /**------------------------------------------------- 根据PK主键作相关操作的便捷方法 END ---------------------------------------------------*/


    /**
     * 先判断当前sqlId是否已经包含了命名空间，如果有，直接返回，如果没有，则为其补充在Mapper中完整的查询ID，即为这个sqlId加上了命名空间
     *
     * @param sqlId
     * @return
     */
    protected final String fillSqlId(String sqlId) {
        if (isNotEmpty(sqlId)) {
            //已经指定了命名空间，则直接返回
            if (sqlId.contains(mapperNamespace)) {
                return sqlId;
            }
        }
        return mapperNamespace + NAMESPACE_SEPARATOR + sqlId;
    }

    /**
     * 防止sortColumns参数里面的sql注入
     * @param param
     * @return
     */
    protected final void checkSortColumns(Object param) {
        if(param == null) {
            return;
        }

        String sortColumns = null;
        if (param instanceof String) {
            sortColumns = (String) param;
        } else if (param instanceof Map) {
            Map map = (Map) param;
            if (map.containsKey(SORT_COLUMNS)) {
                Object obj = map.get(SORT_COLUMNS);
                if (obj == null) {
                    throw new IllegalArgumentException("sortColumns cannot be null");
                } else if (obj instanceof String) {
                    sortColumns = (String) obj;
                } else {
                    throw new IllegalArgumentException("sortColumns illegal data type");
                }
            }
        }

        if (sortColumns == null) {
            return;
        } else if (isEmpty(sortColumns)) { //不允许空字符串
            throw new IllegalArgumentException("sortColumns cannot be empty string");
        }

        Matcher matcher = SQL_INJECT_DEFEND_PATTERN.matcher(sortColumns.trim());
        if (!matcher.matches()) {
            throw new IllegalArgumentException("sortColumns contains illegal character");
        }
    }

    /**
     * 取得Mapper中的命名空间
     *
     * @return
     */
    protected void setMapperNamespace(String mapperNamespace) {
        this.mapperNamespace = mapperNamespace;
    }

    /**
     * 计算分页查询时的起始位置
     *
     * @param pageQuery
     * @return
     */
    private int getOffset(PageQuery pageQuery) {
        int calPageCurrent = pageQuery.getCurrentPage() - 1;
        calPageCurrent = (calPageCurrent < 0) ? 0 : calPageCurrent;
        return calPageCurrent * pageQuery.getPageSize();
    }

    /**
     * 判断字符串是否不为空
     * @param str
     * @return
     */
    private boolean isNotEmpty(String str) {
        return !isEmpty(str);
    }

    /**
     * 判断字符串是否为空
     * @param str
     * @return
     */
    private boolean isEmpty(String str) {
        return str == null || str.trim().length() == 0;
    }
}
