package com.xpay.common.statics.query;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * 使用elasticsearch查询的请求参数，当前类当做客户端的查询参数，而{@link com.xpay.starter.plugin.client.ESClient}则作为服务端，
 * 进行参数解析、执行查询、返回结果等处理
 *
 * 使用注意事项：
 *      1、此类尽可能贴近sql语法和含义，但并没有完全支持所有sql功能，已支持的功能可直接查看当前类提供的方法
 *      2、进行模糊查询时，在ES中是进行全文检索，这跟数据库中like查询会有一些不同
 *      3、聚合统计只支持count、sum、min、max、avg，也支持在聚合统计时进行分组 group by，聚合查询不支持order by
 *      4、非聚合查询(即常规查询)支持order by，但不支持 group by
 *      5、如果ES中存储的字段名和查询参数名格式不一致(如ES中是下划线，查询参数是驼峰)，可使用{@link #buildCamelCase()}或者{@link #buildSnakeCase()}
 *          这两个方法，那么在查询和返回时会自动进行转换，但会损耗一部分性能
 *      6、如果查询参数中，在ES中并不存在此字段，则在ESClient中拼接查询参数时会自动过滤掉
 *      7、如果是普通的分页查询，建议不要超过10000条，不然会报错，这是ES为避免深分页问题而特意设置的，如果需要遍历数据，需要使用scroll滚动查询
 *      8、使用scroll滚动查询时，首次查询时scrollId设置空值即可，查询返回之后，再把scrollId设置到查询参数即可
 *
 */
public class EsQuery implements Serializable {
    private static final long serialVersionUID = 1L;
    private QueryParam queryParam = new QueryParam();

    public EsQuery(){}

    /**
     * 无模式，原样查询，原样返回
     * @see #buildCamelCase()
     * @see #buildSnakeCase()
     * @return
     */
    public static EsQuery buildNoneCase(){
        return new EsQuery();
    }

    /**
     * 驼峰模式，表明客户端的输入和输出都为驼峰，但是ES中存储的字段名为下划线，查询ES时，会把参数名转成下划线，查询结果从ES返回时，会把结果转成驼峰
     * @return
     */
    public static EsQuery buildCamelCase(){
        EsQuery esQuery = new EsQuery();
        esQuery.getQueryParam().setWordCase(QueryParam.CAMEL_CASE);
        return esQuery;
    }

    /**
     * 下划线模式，表明客户端的输入和输出都为下划线，但是ES中存储的字段名为驼峰，查询ES时，会把参数名转成驼峰，查询结果从ES返回时，会把结果转成下划线
     * @return
     */
    public static EsQuery buildSnakeCase(){
        EsQuery esQuery = new EsQuery();
        esQuery.getQueryParam().setWordCase(QueryParam.SNAKE_CASE);
        return esQuery;
    }

    /**
     * 需要返回的查询字段，默认是所有字段
     * @param fields
     * @return
     */
    public EsQuery select(String... fields){
        if(fields != null && fields.length > 0){
            this.queryParam.setSelectFields(fields);
        }
        return this;
    }

    /**
     * 查询数据源，即ES中的index名
     * @param index
     * @return
     */
    public EsQuery from(String index){
        this.queryParam.setIndex(index);
        return this;
    }

    /**
     * 等于(精确匹配)
     * @param field
     * @param value
     * @return
     */
    public EsQuery eq(String field, Object value){
        this.queryParam.eqMap.put(field, value);
        return this;
    }

    /**
     * 等于(精确匹配)
     * @param eqMap
     * @return
     */
    public EsQuery eq(Map<String, Object> eqMap){
        if(eqMap == null || eqMap.isEmpty()){
            return this;
        }

        for(Map.Entry<String, Object> entry : eqMap.entrySet()){
            this.eq(entry.getKey(), entry.getValue());
        }
        return this;
    }

    /**
     * 不等于(精确匹配)
     * @param field
     * @param value
     * @return
     */
    public EsQuery neq(String field, Object value){
        this.queryParam.neqMap.put(field, value);
        return this;
    }

    /**
     * 大于
     * @param field
     * @param value
     * @return
     */
    public EsQuery gt(String field, Object value){
        this.queryParam.gtMap.put(field, value);
        return this;
    }

    /**
     * 大于等于
     * @param field
     * @param value
     * @return
     */
    public EsQuery gte(String field, Object value){
        this.queryParam.gteMap.put(field, value);
        return this;
    }

    /**
     * 小于
     * @param field
     * @param value
     * @return
     */
    public EsQuery lt(String field, Object value){
        this.queryParam.ltMap.put(field, value);
        return this;
    }

    /**
     * 小于等于
     * @param field
     * @param value
     * @return
     */
    public EsQuery lte(String field, Object value){
        this.queryParam.lteMap.put(field, value);
        return this;
    }

    /**
     * 区间查询，等同于sql的between
     * @param field
     * @param start
     * @param end
     * @return
     */
    public EsQuery between(String field, Object start, Object end){
        gte(field, start);
        lte(field, end);
        return this;
    }

    /**
     * 等同于sql的 in(...) 查询
     * @param field
     * @param values
     * @return
     */
    public EsQuery in(String field, Object[] values){
        this.queryParam.inMap.put(field, values);
        return this;
    }

    /**
     * 等同于sql的 not in(...) 查询
     * @param field
     * @param values
     * @return
     */
    public EsQuery notIn(String field, Object[] values){
        this.queryParam.notInMap.put(field, values);
        return this;
    }

    /**
     * 全文检索，类似于sql的like查询
     * @param field
     * @param value
     * @return
     */
    public EsQuery fullText(String field, Object value){
        this.queryParam.likeMap.put(field, value);
        return this;
    }

    /**
     * 字段值为null，等同于：select * from table where field is null
     * @param field
     * @return
     */
    public EsQuery isNull(String field) {
        this.queryParam.nullMap.put(field, "1");
        return this;
    }

    /**
     * 字段值不为null，等同于：select * from table where field is not null
     * @param field
     * @return
     */
    public EsQuery isNotNull(String field) {
        this.queryParam.notNullMap.put(field, "1");
        return this;
    }

    /**
     * 计算总数（聚合统计时用）
     * @param field
     * @return
     */
    public EsQuery count(String field){
        this.queryParam.aggregation(field).count();
        return this;
    }

    /**
     * 计算总和（聚合统计时用）
     * @param field
     * @return
     */
    public EsQuery sum(String field){
        this.queryParam.aggregation(field).sum();
        return this;
    }

    /**
     * 计算最大值（聚合统计时用）
     * @param field
     * @return
     */
    public EsQuery max(String field){
        this.queryParam.aggregation(field).max();
        return this;
    }

    /**
     * 计算最小值（聚合统计时用）
     * @param field
     * @return
     */
    public EsQuery min(String field){
        this.queryParam.aggregation(field).min();
        return this;
    }

    /**
     * 计算平均值（聚合统计时用）
     * @param field
     * @return
     */
    public EsQuery avg(String field){
        this.queryParam.aggregation(field).avg();
        return this;
    }

    /**
     * 分组，仅在统计时有用，仅支持单个字段（聚合统计时用）
     * @param field
     * @return
     */
    public EsQuery groupBy(String field){
        this.queryParam.setGroupBy(field);
        return this;
    }

    /**
     * 排序，仅在常规查询有用，聚合查询不起作用
     * @param sortColumns 排序的字段，如果有多个字段，使用英文的逗号分割
     * @return
     */
    public EsQuery orderBy(String sortColumns){
        this.queryParam.setOrderBy(sortColumns);
        return this;
    }

    /**
     * 分页查询（会有深分页问题）
     * @param pageCurrent
     * @param pageSize
     * @return
     */
    public EsQuery page(Integer pageCurrent, Integer pageSize){
        this.queryParam.setPageCurrent(pageCurrent);
        this.queryParam.setPageSize(pageSize);
        return this;
    }

    /**
     * 不分页查询，也需要指定条数
     * @param pageSize
     * @return
     */
    public EsQuery size(Integer pageSize){
        this.queryParam.setPageSize(pageSize);
        return this;
    }

    /**
     * 设置使用scroll滚动查询
     * @param expireSec     超时时间
     * @param pageSize      每页数量
     * @param scrollId      滚动id，首次查询时传null
     * @return
     */
    public EsQuery scroll(long expireSec, int pageSize, String scrollId){
        this.queryParam.setScrollMode(true);
        this.queryParam.setScrollId(scrollId);
        this.queryParam.setScrollExpireSec(expireSec);
        this.queryParam.setPageSize(pageSize);
        return this;
    }

    /**
     * 查询超时时间
     * @param millSec
     * @return
     */
    public EsQuery timeout(long millSec){
        this.queryParam.setTimeout(millSec);
        return this;
    }

    /**
     * 查询结果返回的类，如果这个类在服务端也存在，则查询结果会直接返回这个类的实体对象，如果这个类在服务端不存在，则会抛出异常，如果不设置，默认返回HashMap
     * @param clz
     * @return
     */
    public EsQuery resultClass(Class clz){
        this.queryParam.setReturnClassName(clz.getName());
        return this;
    }

    public void doQueryParamCase(ParamHelper paramHelper){
        this.queryParam.doQueryParamCase(paramHelper);
    }

    public QueryParam getQueryParam() {
        return this.queryParam;
    }

    public void setQueryParam(QueryParam queryParam) {
        this.queryParam = queryParam;
    }


    /**--------------------------------------------- 获取查询参数的方法 START ------------------------------------------**/
    /**
     * 理论上，为了保障查询参数的正确性，下面的这些方法返回时，都应该重新new一个对象返回的，但考虑到性能问题，所以没有这样做，
     * 仅仅是在开发人员中进行约定，大家都约定不对返回值做任何修改、新增操作
     */
    public boolean isWordCase(){
        return isCamelCase() || isSnakeCase();
    }
    public boolean isCamelCase(){
        return this.queryParam.getWordCase() == QueryParam.CAMEL_CASE;
    }
    public boolean isSnakeCase(){
        return this.queryParam.getWordCase() == QueryParam.SNAKE_CASE;
    }
    public String getIndex() {
        return this.queryParam.getIndex();
    }
    public String getGroupBy() {
        return this.queryParam.getGroupBy();
    }
    public String getOrderBy() {
        return this.queryParam.getOrderBy();
    }
    public long getTimeout() {
        return this.queryParam.getTimeout();
    }
    public String getReturnClassName() {
        return this.queryParam.getReturnClassName();
    }
    public boolean getScrollMode() {
        return this.queryParam.getScrollMode();
    }
    public String getScrollId() {
        return this.queryParam.getScrollId();
    }
    public long getScrollExpireSec() {
        return this.queryParam.getScrollExpireSec();
    }
    public int getPageCurrent() {
        return this.queryParam.getPageCurrent();
    }
    public int getPageSize() {
        return this.queryParam.getPageSize();
    }
    public String[] getSelectFields() {
        return this.queryParam.getSelectFields();
    }
    public Map<String, Object> getEqMap() {
        return this.queryParam.getEqMap();
    }
    public Map<String, Object> getNeqMap() {
        return this.queryParam.getNeqMap();
    }
    public Map<String, Object> getGtMap() {
        return this.queryParam.getGtMap();
    }
    public Map<String, Object> getGteMap() {
        return this.queryParam.getGteMap();
    }
    public Map<String, Object> getLtMap() {
        return this.queryParam.getLtMap();
    }
    public Map<String, Object> getLteMap() {
        return this.queryParam.getLteMap();
    }
    public Map<String, Object> getLikeMap() {
        return this.queryParam.getLikeMap();
    }
    public Map<String, Object[]> getInMap() {
        return this.queryParam.getInMap();
    }
    public Map<String, Object[]> getNotInMap() {
        return this.queryParam.getNotInMap();
    }
    public Map<String, Object> getNullMap() {
        return this.queryParam.getNullMap();
    }
    public Map<String, Object> getNotNullMap() {
        return this.queryParam.getNotNullMap();
    }
    public Map<String, Aggregation> getAggMap() {
        return this.queryParam.getAggMap();
    }
    /**--------------------------------------------- 获取查询参数的方法 END --------------------------------------------**/


    /**
     * 查询参数，设立内部类的目的是为了查询参数不会被随意更改
     */
    private class QueryParam implements Serializable {
        private static final long serialVersionUID = 1L;
        public static final int CAMEL_CASE = 1;//驼峰模式
        public static final int SNAKE_CASE = -1;//下划线模式
        private static final int INI_PARAM_MAP_CAP = 8;//参数的初始容量

        private String index; //一张表就是一个index
        private String returnClassName; //返回结果的类名

        /**
         * 参数的拼写模式，1=驼峰模式 -1=下划线模式，当为驼峰模式时，客户端的输入和输出都为驼峰，但是ES中存储的字段名为下划线，
         * 查询ES时，会把参数名转成下划线，查询结果从ES返回时，会把结果转成驼峰，当为下划线模式时，客户端的输入和输出都为下划线，
         * 但是ES中存储的字段名为驼峰，查询ES时，会把参数名转成驼峰，查询结果从ES返回时，会把结果转成下划线
         */
        private int wordCase = 0;
        private boolean scrollMode; //是否是滚动查询
        private String scrollId; //滚动查询时的scrollId
        private long scrollExpireSec = 30;//scroll模式下scrollId默认超时时间(秒)
        private int pageCurrent = 1;//默认的当前页
        private int pageSize = 20;//默认的每页查询数
        private long timeout = 30000;//查询超时时间(毫秒)

        private String[] selectFields;
        private String groupBy; //仅在统计时有用,且仅支持单个字段分组
        private String orderBy; //排序字段
        private Map<String, Object> eqMap = new HashMap(INI_PARAM_MAP_CAP);
        private Map<String, Object> neqMap = new HashMap(INI_PARAM_MAP_CAP);
        private Map<String, Object> gtMap = new HashMap(INI_PARAM_MAP_CAP);
        private Map<String, Object> gteMap = new HashMap(INI_PARAM_MAP_CAP);
        private Map<String, Object> ltMap = new HashMap(INI_PARAM_MAP_CAP);
        private Map<String, Object> lteMap = new HashMap(INI_PARAM_MAP_CAP);
        private Map<String, Object> likeMap = new HashMap(INI_PARAM_MAP_CAP);
        private Map<String, Object[]> inMap = new HashMap(INI_PARAM_MAP_CAP);
        private Map<String, Object[]> notInMap = new HashMap(INI_PARAM_MAP_CAP);
        private Map<String, Object> nullMap = new HashMap(INI_PARAM_MAP_CAP);
        private Map<String, Object> notNullMap = new HashMap(INI_PARAM_MAP_CAP);
        private Map<String, Aggregation> aggMap = new HashMap(INI_PARAM_MAP_CAP);

        public String getIndex() {
            return index;
        }

        public void setIndex(String index) {
            this.index = index;
        }

        public String getGroupBy() {
            return groupBy;
        }

        public void setGroupBy(String groupBy) {
            this.groupBy = groupBy;
        }

        public String getOrderBy() {
            return orderBy;
        }

        public void setOrderBy(String orderBy) {
            this.orderBy = orderBy;
        }

        public String getReturnClassName() {
            return returnClassName;
        }

        public void setReturnClassName(String returnClassName) {
            this.returnClassName = returnClassName;
        }

        public int getWordCase() {
            return wordCase;
        }

        public void setWordCase(int wordCase) {
            this.wordCase = wordCase;
        }

        public boolean getScrollMode() {
            return scrollMode;
        }

        public void setScrollMode(boolean scrollMode) {
            this.scrollMode = scrollMode;
        }

        public String getScrollId() {
            return scrollId;
        }

        public void setScrollId(String scrollId) {
            this.scrollId = scrollId;
        }

        public long getScrollExpireSec() {
            return scrollExpireSec;
        }

        public void setScrollExpireSec(long scrollExpireSec) {
            this.scrollExpireSec = scrollExpireSec;
        }

        public int getPageCurrent() {
            return pageCurrent;
        }

        public void setPageCurrent(int pageCurrent) {
            this.pageCurrent = pageCurrent;
        }

        public int getPageSize() {
            return pageSize;
        }

        public void setPageSize(int pageSize) {
            this.pageSize = pageSize;
        }

        public long getTimeout() {
            return timeout;
        }

        public void setTimeout(long timeout) {
            this.timeout = timeout;
        }

        public String[] getSelectFields() {
            return selectFields;
        }

        public void setSelectFields(String[] selectFields) {
            this.selectFields = selectFields;
        }

        public Map<String, Object> getEqMap() {
            return eqMap;
        }

        public void setEqMap(Map<String, Object> eqMap) {
            this.eqMap = eqMap;
        }

        public Map<String, Object> getNeqMap() {
            return neqMap;
        }

        public void setNeqMap(Map<String, Object> neqMap) {
            this.neqMap = neqMap;
        }

        public Map<String, Object> getGtMap() {
            return gtMap;
        }

        public void setGtMap(Map<String, Object> gtMap) {
            this.gtMap = gtMap;
        }

        public Map<String, Object> getGteMap() {
            return gteMap;
        }

        public void setGteMap(Map<String, Object> gteMap) {
            this.gteMap = gteMap;
        }

        public Map<String, Object> getLtMap() {
            return ltMap;
        }

        public void setLtMap(Map<String, Object> ltMap) {
            this.ltMap = ltMap;
        }

        public Map<String, Object> getLteMap() {
            return lteMap;
        }

        public void setLteMap(Map<String, Object> lteMap) {
            this.lteMap = lteMap;
        }

        public Map<String, Object> getLikeMap() {
            return likeMap;
        }

        public void setLikeMap(Map<String, Object> likeMap) {
            this.likeMap = likeMap;
        }

        public Map<String, Object[]> getInMap() {
            return inMap;
        }

        public void setInMap(Map<String, Object[]> inMap) {
            this.inMap = inMap;
        }

        public Map<String, Object[]> getNotInMap() {
            return notInMap;
        }

        public void setNotInMap(Map<String, Object[]> notInMap) {
            this.notInMap = notInMap;
        }

        public Map<String, Object> getNullMap() {
            return nullMap;
        }

        public void setNullMap(Map<String, Object> nullMap) {
            this.nullMap = nullMap;
        }

        public Map<String, Object> getNotNullMap() {
            return notNullMap;
        }

        public void setNotNullMap(Map<String, Object> notNullMap) {
            this.notNullMap = notNullMap;
        }

        public Map<String, Aggregation> getAggMap() {
            return aggMap;
        }

        public void setAggMap(Map<String, Aggregation> aggMap) {
            this.aggMap = aggMap;
        }

        private Aggregation aggregation(String field) {
            Aggregation aggregation = this.aggMap.get(field);
            if(aggregation != null) {
                return aggregation;
            }
            synchronized (QueryParam.this) {
                if (!this.aggMap.containsKey(field)) {
                    this.aggMap.put(field, new Aggregation(field));
                }
            }
            return this.aggMap.get(field);
        }

        public void doQueryParamCase(ParamHelper paramHelper) {
            if (this.wordCase != CAMEL_CASE && this.wordCase != SNAKE_CASE && this.scrollId != null) {
                return;
            }

            boolean isToSnakeCase = this.wordCase == CAMEL_CASE;
            if (this.selectFields != null && this.selectFields.length > 0) {
                for (int i = 0; i < this.selectFields.length; i++) {
                    this.selectFields[i] = isToSnakeCase ? paramHelper.toSnakeCase(this.selectFields[i]) : paramHelper.toCamelCase(this.selectFields[i]);
                }
            }
            if (paramHelper.isNotEmpty(this.groupBy)) {
                this.groupBy = isToSnakeCase ? paramHelper.toSnakeCase(this.groupBy) : paramHelper.toCamelCase(this.groupBy);
            }
            if (paramHelper.isNotEmpty(this.orderBy)) {
                StringBuffer sbf = new StringBuffer();
                String[] sortColumnArray = this.orderBy.split(",");
                for (int i = 0; i < sortColumnArray.length; i++) {
                    String[] sortColumn = sortColumnArray[i].split(" ");
                    if (sortColumn.length > 1) {
                        sbf.append(isToSnakeCase ? paramHelper.toSnakeCase(sortColumn[0]) : paramHelper.toCamelCase(sortColumn[0]))
                                .append(" ").append(sortColumn[sortColumn.length - 1]);
                    } else {
                        sbf.append(isToSnakeCase ? paramHelper.toSnakeCase(sortColumn[0]) : paramHelper.toCamelCase(sortColumn[0]));
                    }
                }
            }
            if (this.getEqMap() != null && !this.getEqMap().isEmpty()) {
                Map<String, Object> map = new HashMap<>(this.getEqMap().size());
                for(Map.Entry<String, Object> entry : this.getEqMap().entrySet()){
                    String key = isToSnakeCase ? paramHelper.toSnakeCase(entry.getKey()) : paramHelper.toCamelCase(entry.getKey());
                    map.put(key, entry.getValue());
                }
                this.setEqMap(map);
            }
            if (this.getNeqMap() != null && !this.getNeqMap().isEmpty()) {
                Map<String, Object> map = new HashMap<>(this.getNeqMap().size());
                for(Map.Entry<String, Object> entry : this.getNeqMap().entrySet()){
                    String key = isToSnakeCase ? paramHelper.toSnakeCase(entry.getKey()) : paramHelper.toCamelCase(entry.getKey());
                    map.put(key, entry.getValue());
                }
                this.setNeqMap(map);
            }
            if (this.getGtMap() != null && !this.getGtMap().isEmpty()) {
                Map<String, Object> map = new HashMap<>(this.getGtMap().size());
                for(Map.Entry<String, Object> entry : this.getGtMap().entrySet()){
                    String key = isToSnakeCase ? paramHelper.toSnakeCase(entry.getKey()) : paramHelper.toCamelCase(entry.getKey());
                    map.put(key, entry.getValue());
                }
                this.setGtMap(map);
            }
            if (this.getGteMap() != null && !this.getGteMap().isEmpty()) {
                Map<String, Object> map = new HashMap<>(this.getGteMap().size());
                for(Map.Entry<String, Object> entry : this.getGteMap().entrySet()){
                    String key = isToSnakeCase ? paramHelper.toSnakeCase(entry.getKey()) : paramHelper.toCamelCase(entry.getKey());
                    map.put(key, entry.getValue());
                }
                this.setGteMap(map);
            }
            if (this.getLtMap() != null && !this.getLtMap().isEmpty()) {
                Map<String, Object> map = new HashMap<>(this.getLtMap().size());
                for(Map.Entry<String, Object> entry : this.getLtMap().entrySet()){
                    String key = isToSnakeCase ? paramHelper.toSnakeCase(entry.getKey()) : paramHelper.toCamelCase(entry.getKey());
                    map.put(key, entry.getValue());
                }
                this.setLtMap(map);
            }
            if (this.getLteMap() != null && !this.getLteMap().isEmpty()) {
                Map<String, Object> map = new HashMap<>(this.getLteMap().size());
                for(Map.Entry<String, Object> entry : this.getLteMap().entrySet()){
                    String key = isToSnakeCase ? paramHelper.toSnakeCase(entry.getKey()) : paramHelper.toCamelCase(entry.getKey());
                    map.put(key, entry.getValue());
                }
                this.setLteMap(map);
            }
            if (this.getLikeMap() != null && !this.getLikeMap().isEmpty()) {
                Map<String, Object> map = new HashMap<>(this.getLikeMap().size());
                for(Map.Entry<String, Object> entry : this.getLikeMap().entrySet()){
                    String key = isToSnakeCase ? paramHelper.toSnakeCase(entry.getKey()) : paramHelper.toCamelCase(entry.getKey());
                    map.put(key, entry.getValue());
                }
                this.setLikeMap(map);
            }
            if (this.getInMap() != null && !this.getInMap().isEmpty()) {
                Map<String, Object[]> map = new HashMap<>(this.getInMap().size());
                for(Map.Entry<String, Object[]> entry : this.getInMap().entrySet()){
                    String key = isToSnakeCase ? paramHelper.toSnakeCase(entry.getKey()) : paramHelper.toCamelCase(entry.getKey());
                    map.put(key, entry.getValue());
                }
                this.setInMap(map);
            }
            if (this.getNotInMap() != null && !this.getNotInMap().isEmpty()) {
                Map<String, Object[]> map = new HashMap<>(this.getNotInMap().size());
                for(Map.Entry<String, Object[]> entry : this.getNotInMap().entrySet()){
                    String key = isToSnakeCase ? paramHelper.toSnakeCase(entry.getKey()) : paramHelper.toCamelCase(entry.getKey());
                    map.put(key, entry.getValue());
                }
                this.setNotInMap(map);
            }
            if (this.getAggMap() != null && !this.getAggMap().isEmpty()) {
                Map<String, Aggregation> map = new HashMap<>(this.getAggMap().size());
                for(Map.Entry<String, Aggregation> entry : this.getAggMap().entrySet()){
                    String key = isToSnakeCase ? paramHelper.toSnakeCase(entry.getKey()) : paramHelper.toCamelCase(entry.getKey());
                    entry.getValue().setField(key);
                    map.put(key, entry.getValue());
                }
                this.setAggMap(map);
            }
        }
    }


    public interface ParamHelper {
        public boolean isNotEmpty(String val);

        public String toSnakeCase(String val);

        public String toCamelCase(String val);
    }

    /**
     * 聚合统计的查询参数
     */
    public class Aggregation {
        private String field;
        private boolean count = false;
        private boolean sum = false;
        private boolean max = false;
        private boolean min = false;
        private boolean avg = false;

        public Aggregation(String field){
            this.field = field;
        }

        /**
         * 统计记录数
         * @return
         */
        public void count(){
            this.count = true;
        }

        /**
         * 计算总和
         * @return
         */
        public void sum(){
            this.sum = true;
        }

        /**
         * 计算最大值
         * @return
         */
        public void max(){
            this.max = true;
        }

        /**
         * 计算平均值
         * @return
         */
        public void avg(){
            this.avg = true;
        }

        /**
         * 计算最小值
         * @return
         */
        public void min(){
            this.min = true;
        }

        public String getField() {
            return field;
        }

        public void setField(String field) {
            this.field = field;
        }

        public boolean getCount() {
            return count;
        }

        public void setCount(boolean count) {
            this.count = count;
        }

        public boolean getSum() {
            return sum;
        }

        public void setSum(boolean sum) {
            this.sum = sum;
        }

        public boolean getMax() {
            return max;
        }

        public void setMax(boolean max) {
            this.max = max;
        }

        public boolean getMin() {
            return min;
        }

        public void setMin(boolean min) {
            this.min = min;
        }

        public boolean getAvg() {
            return avg;
        }

        public void setAvg(boolean avg) {
            this.avg = avg;
        }
    }
}
