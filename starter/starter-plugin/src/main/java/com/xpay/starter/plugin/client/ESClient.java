package com.xpay.starter.plugin.client;

import com.google.common.cache.Cache;
import com.xpay.common.statics.dto.es.EsAgg;
import com.xpay.common.statics.query.EsQuery;
import com.xpay.common.statics.result.EsAggResult;
import com.xpay.common.statics.result.PageResult;
import com.xpay.starter.plugin.util.Utils;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.DocWriteResponse;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.*;
import org.elasticsearch.action.support.ActiveShardCount;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.action.support.replication.ReplicationResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.core.CountRequest;
import org.elasticsearch.client.core.CountResponse;
import org.elasticsearch.client.indices.*;
import org.elasticsearch.cluster.metadata.MappingMetaData;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.VersionType;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.index.reindex.BulkByScrollResponse;
import org.elasticsearch.index.reindex.ReindexRequest;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.*;
import org.elasticsearch.search.aggregations.support.ValuesSourceAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.FetchSourceContext;
import org.elasticsearch.search.sort.SortOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * elasticsearch客户端，提供一些常规的方法，如果需要更复杂的操作，可通过 {@link #getRestEsClient()}方法取得ES的原生客户端来处理，关于ES的
 * 一些查询语法可以参考下面的内容，可以拿 ES 和 MYSQL 的语法类比如下：

 team 等效于 =

 teams 等效于 in

 range 等效于 between、>、>=、<、<=
   gt 等效于 >
   gte 等效于 >=
   lt 等效于 <
   lte 等效于 <=
   gte加lte 等效于 between 或者等效于类似sql语句 a >= 1 and a <= 5

 match 类似于 like，不过ES是基于分词来匹配，而 MYSQL 是基于前缀、后缀来匹配

 //以下是sql查询和ES查询一些示例
 //1.不需要嵌套查询时must或filter的用法
 select * from table where a=1 and b=2 and c=3 and d not in(5,6)
 POST _search
 {
   "query": {
     "bool" : {
       "filter" : {
         "term" : { "a" : "1" },
         "term" : { "b" : "2" },
         "term" : { "c" : "3" }
       },
       "must_not": {
         "terms" : { "d" : [5,6] }
       }
     }
   }
 }

 //2.不需要嵌套查询时should的用法
 select * from table where a=1 or b=2 or c=3
 POST _search
 {
   "query": {
     "bool" : {
       "should" : {
         "term" : { "a" : "1" },
         "term" : { "b" : "2" }
         "term" : { "c" : "3" }
       }
     }
   }
 }

 //3.需要嵌套查询时should的用法
 select * from table where a=1 or (b=2 and c=3)
 POST _search
 {
   "query": {
     "bool" : {
       "should" : {
         "term" : { "a" : "1" },
         "bool" : {
           "must" : {
             "term" : { "b" : "2" },
             "term" : { "c" : "3" }
           }
         }
       }
     }
   }
 }

 //4.组合嵌套查询的用法
 select * from where a=1 and b=2 and (c=3 or d=4) and e not in(5,6)
 POST _search
 {
   "query": {
     "bool" : {
       "filter" : {
         "term" : { "a" : "1" },
         "term" : { "b" : "2" },
         "bool" : {
           "should" : {
             "term" : { "c" : "3" },
             "term" : { "d" : "4" }
           }
         }
       },
       "must_not": {
         "terms" : { "e" : [5,6] }
       }
     }
   }
 }

 * @author chenyf
 */
public class ESClient {
    public static final int MAX_GROUP_SIZE = 1000;//最大分组数量
    private static final String DEFAULT_GROUP_VALUE = "_DEFAULT_GROUP_VALUE_";
    private final InnerParamHelper paramHelper = new InnerParamHelper();
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final RestHighLevelClient restEsClient;
    private final Cache<String, Map<String, String>> cache;

    public ESClient(RestHighLevelClient restEsClient){
        this(restEsClient, null);
    }

    public ESClient(RestHighLevelClient restEsClient, Cache<String, Map<String, String>> cache){
        this.restEsClient = restEsClient;
        this.cache = cache;
    }

    /**
     * 获取es原生客户端，用以处理比较复杂的需求
     * @return
     */
    public RestHighLevelClient getRestEsClient() {
        return restEsClient;
    }

    /**
     * 取得单个实体
     * @param esQuery   es查询参数
     * @param <T>       返回的实体类型
     * @return
     */
    public <T> T getOne(EsQuery esQuery){
        Class<T> clz = getReturnClass(esQuery);

        SearchResponse response = executeQuery(esQuery);
        if(response.getHits().getTotalHits().value > 0){
            SearchHit searchHit = response.getHits().getHits()[0];
            if(esQuery.isWordCase()){
                Map<String, Object> resultMap = resultWordCase(searchHit.getSourceAsMap(), esQuery.isCamelCase());
                if(isHashMap(clz)){
                    return (T) resultMap;
                }else if(isString(clz)){
                    return (T) Utils.toJson(resultMap);
                }else{
                    return Utils.jsonToBean(Utils.toJson(resultMap), clz);
                }
            }else{
                if(isHashMap(clz)){
                    return (T) searchHit.getSourceAsMap();
                }else if(isString(clz)){
                    return (T) searchHit.getSourceAsString();
                }else{
                    return Utils.jsonToBean(searchHit.getSourceAsString(), clz);
                }
            }
        }else{
            return null;
        }
    }

    /**
     * 根据id查询单个记录
     * @param index     索引名
     * @param id        记录id
     * @return
     */
    public Map<String, Object> getById(String index, Long id){
        if(isEmpty(index)) {
            throw new RuntimeException("index不能为空");
        }else if(id == null){
            throw new RuntimeException("id不能为空");
        }

        GetRequest getRequest = new GetRequest(index, id.toString());
        try {
            GetResponse response = restEsClient.get(getRequest, RequestOptions.DEFAULT);
            return response.getSourceAsMap();
        } catch (Exception e) {
            throw new RuntimeException("getById异常", e);
        }
    }

    /**
     * 根据多个id查询多条记录
     * @param index     索引名
     * @param ids       id列表
     * @return
     */
    public List<Map<String, Object>> listById(String index, List<Long> ids){
        if(isEmpty(index)){
            throw new RuntimeException("index不能为空");
        }else if(ids == null || ids.isEmpty()){
            throw new RuntimeException("ids不能为空");
        }

        String[] idsArr = new String[ids.size()];
        for(int i=0; i<ids.size(); i++){
            idsArr[i] = ids.get(i).toString();
        }

        SearchSourceBuilder builder = new SearchSourceBuilder();
        builder.from(0)
                .size(ids.size())
                .query(QueryBuilders.idsQuery().addIds(idsArr));

        SearchRequest request = new SearchRequest(index);
        request.source(builder);

        SearchResponse response;
        try {
            response = restEsClient.search(request, RequestOptions.DEFAULT);
        } catch (Exception e) {
            throw new RuntimeException("listById异常", e);
        }

        List<Map<String, Object>> entityList = new ArrayList<>(ids.size());
        SearchHit[] hits = response.getHits().getHits();
        for(int i=0; i<hits.length; i++){
            entityList.add(hits[i].getSourceAsMap());
        }
        return entityList;
    }

    /**
     * 列表查询
     * @param esQuery   es查询参数
     * @param <T>       返回的实体类型
     * @return
     */
    public <T> List<T> listBy(EsQuery esQuery){
        Class<T> clz = getReturnClass(esQuery);

        SearchResponse response = executeQuery(esQuery);
        if(response.getHits().getTotalHits().value <= 0){
            return new ArrayList<>();
        }

        List<T> entityList = getEntityList(response, clz, esQuery);
        if((entityList == null || entityList.isEmpty()) && Utils.isNotEmpty(response.getScrollId())){
            clearScrollAsync(response.getScrollId());
        }
        return entityList;
    }

    /**
     * 多条件统计记录数
     * @param esQuery   es查询参数
     * @return
     */
    public long countBy(EsQuery esQuery){
        //参数检查
        paramCheck(esQuery, false);
        //查询时参数名转换
        esQuery.doQueryParamCase(paramHelper);

        String index = esQuery.getIndex();
        CountRequest request = new CountRequest(index);
        request.query(getQueryBuilder(esQuery));//构造查询条件
        CountResponse response;
        try {
            response = restEsClient.count(request, RequestOptions.DEFAULT);
            if (RestStatus.OK.equals(response.status())
                    && (response.isTerminatedEarly() == null || Boolean.FALSE.equals(response.isTerminatedEarly()))
                    && response.getFailedShards() <= 0) {
                return response.getCount();
            }
        } catch (Exception e) {
            throw new RuntimeException("countBy异常", e);
        }

        StringBuilder failMsg = new StringBuilder();
        for (ShardSearchFailure failure : response.getShardFailures()) {
            if(failMsg.length() > 0) {
                failMsg.append(",");
            }
            failMsg.append("{nodeId:").append(failure.shard().getNodeId()).append(",")
                    .append("shardId:").append(failure.shard().getShardId()).append(",")
                    .append("reason:").append(failure.reason()).append("}");
        }
        logger.warn("countBy时出现了失败的节点或分片 index: {} failMsg: {}", index, failMsg.toString());
        throw new RuntimeException("部分节点或分片统计失败，无法得到准确的统计值");
    }

    /**
     * 分页查询，需要返回分页结果
     * @param esQuery   es查询参数
     * @param <T>       返回的实体类型
     * @return
     */
    public <T> PageResult<List<T>> listPage(EsQuery esQuery){
        Class<T> clz = getReturnClass(esQuery);
        SearchResponse response = executeQuery(esQuery);
        long totalRecord = response.getHits().getTotalHits().value;
        if(totalRecord <= 0){
            return PageResult.newInstance(new ArrayList<>(), esQuery.getPageCurrent(), esQuery.getPageSize());
        }

        String scrollId = response.getScrollId();
        List<T> entityList = getEntityList(response, clz, esQuery);
        if(Utils.isNotEmpty(scrollId) && (entityList == null || entityList.isEmpty() || entityList.size() < esQuery.getPageSize())){
            clearScrollAsync(scrollId);
            scrollId = null;//ES中的快照已被清除，故scrollId也不应该再返回
        }
        PageResult<List<T>> result = PageResult.newInstance(entityList, esQuery.getPageCurrent(), esQuery.getPageSize(), totalRecord);
        result.setScrollId(scrollId);
        return result;
    }

    /**
     * 统计，可统计多个字段的多个维度，每个字段都可有：count、sum、min、max、avg 等维度统计
     * @param esQuery   es查询参数
     * @return
     */
    public EsAggResult aggregation(EsQuery esQuery){
        SearchResponse response = executeAggregation(esQuery);

        AggResult aggResult = new AggResult();
        aggResult.setGroupField(esQuery.getGroupBy());
        if(response.getHits().getTotalHits().value > 0){
            if (isEmpty(esQuery.getGroupBy())){
                fillEsAggResult(aggResult, DEFAULT_GROUP_VALUE, response.getAggregations().iterator(), esQuery);
            }else{
                Iterator<Aggregation> iterator = response.getAggregations().iterator();
                while (iterator.hasNext()){
                    Aggregation agg = iterator.next();
                    ParsedTerms terms = (ParsedTerms) agg;

                    if(terms.getBuckets().isEmpty()){
                        continue;
                    }

                    for(Terms.Bucket bucket : terms.getBuckets()){
                        String groupValue = bucket.getKeyAsString();
                        Aggregations bucketAgg = bucket.getAggregations();
                        fillEsAggResult(aggResult, groupValue, bucketAgg.iterator(), esQuery);
                    }
                }
            }
        }
        return aggResult.toEsAggResult();
    }

    /**
     * 从查询结果中转换成List<T>返回
     * @param response      检索响应体
     * @param clz           返回实体的Class对象
     * @param esQuery       es查询参数
     * @param <T>           返回实体的类型
     * @return
     */
    public <T> List<T> getEntityList(SearchResponse response, Class<T> clz, EsQuery esQuery){
        List<T> entityList = new ArrayList<>();
        boolean isHashMap = isHashMap(clz);
        boolean isString = isString(clz);
        if(response.getHits().getHits().length <= 0){
            return entityList;
        }

        SearchHit[] hits = response.getHits().getHits();
        for(int i=0; i<hits.length; i++){
            SearchHit searchHit = hits[i];
            if(esQuery.isWordCase()){
                Map<String, Object> resultMap = resultWordCase(searchHit.getSourceAsMap(), esQuery.isCamelCase());
                if(isHashMap){
                    entityList.add((T) resultMap);
                }else if(isString){
                    entityList.add((T) Utils.toJson(resultMap));
                }else{
                    entityList.add(Utils.jsonToBean(Utils.toJson(resultMap), clz));
                }
            }else{
                if(isHashMap){
                    entityList.add((T) searchHit.getSourceAsMap());
                }else if(isString){
                    entityList.add((T) searchHit.getSourceAsString());
                }else{
                    entityList.add(Utils.jsonToBean(searchHit.getSourceAsString(), clz));
                }
            }
        }
        return entityList;
    }

    /**
     * 判断一条记录是否存在
     * @param index     索引名
     * @param id        记录id
     * @return
     */
    public boolean exists(String index, Long id) {
        if (isEmpty(index) || id == null) {
            throw new RuntimeException("index和id不能为空");
        }

        GetRequest getRequest = new GetRequest(index, id.toString());
        getRequest.fetchSourceContext(new FetchSourceContext(false));
        getRequest.storedFields("_none_");
        try {
            boolean exists = restEsClient.exists(getRequest, RequestOptions.DEFAULT);
            return exists;
        } catch (Exception e) {
            throw new RuntimeException("判断记录是否存在时异常 index="+index+",id="+id, e);
        }
    }

    /**
     * 删除一条记录
     * @param index     索引名
     * @param id        记录的id
     * @param version   版本号
     * @return
     */
    public boolean delete(String index, Long id, Long version){
        if (isEmpty(index) || id == null) {
            throw new RuntimeException("index和id不能为空");
        }

        DeleteRequest request = new DeleteRequest(index, id.toString());
        if (version != null) {
            request.version(version);
        }

        DeleteResponse response;
        try {
            response = restEsClient.delete(request, RequestOptions.DEFAULT);
            if (DocWriteResponse.Result.DELETED.equals(response.getResult())
                    || DocWriteResponse.Result.NOT_FOUND.equals(response.getResult())) {
                return true;//记录删除成功或者记录已不存在，直接返回true即可
            }
        } catch (ElasticsearchException exception) {
            if (exception.status() == RestStatus.CONFLICT) {
                logger.error("版本冲突，记录删除失败 index:{} id:{}", index, id);
            }
            return false;
        } catch (Exception e) {
            logger.error("删除记录时异常 index:{} id:{} version:{}", index, id, version, e);
            return false;
        }

        StringBuilder failMsg = new StringBuilder();
        ReplicationResponse.ShardInfo shardInfo = response.getShardInfo();
        if (shardInfo.getTotal() != shardInfo.getSuccessful()) {
            failMsg.append("totalShard=").append(shardInfo.getTotal())
                    .append("successShard=").append(shardInfo.getSuccessful());
        }
        if (shardInfo.getFailed() > 0) {
            failMsg.append("failures are: [");
            for (ReplicationResponse.ShardInfo.Failure failure : shardInfo.getFailures()) {
                failMsg.append("{nodeId:").append(failure.nodeId()).append(",")
                        .append("reason:").append(failure.reason()).append("},");
            }
            failMsg.append("]");
        }
        logger.error("删除记录失败 index:{} id:{} message:{}", index, id, failMsg.toString());
        return false;
    }

    /**
     * 批量保存记录(新增或修改)
     * @param index          索引名称
     * @param idSourceMap    id和JSON格式的文档内容
     * @param idVersionMap   id和版本号，可选
     * @return  返回保存失败的记录id
     */
    public List<Long> batchSave(String index, Map<Long, String> idSourceMap, Map<Long, Long> idVersionMap){
        if(isEmpty(index)) {
            throw new RuntimeException("index不能为空");
        }else if (idSourceMap == null || idSourceMap.isEmpty()) {
            throw new RuntimeException("idMapSource不能为空");
        }

        BulkRequest request = new BulkRequest();
        idSourceMap.forEach((k,v) -> {
            Long version = idVersionMap != null ? idVersionMap.get(k) : null;
            IndexRequest idxRequest = new IndexRequest(index).id(k.toString()).source(v, XContentType.JSON);
            if(version != null){
                idxRequest.version(version).versionType(VersionType.EXTERNAL);
            }
            request.add(idxRequest);
        });

        BulkResponse responses;
        try {
            responses = restEsClient.bulk(request, RequestOptions.DEFAULT);
            if (! responses.hasFailures()) {
                return new ArrayList<>();
            }
        } catch (Exception e) {
            logger.error("批量保存记录异常 index:{} batchSize:{} idSourceMap:{}", index, idSourceMap.size(), Utils.toJson(idSourceMap), e);
            throw new RuntimeException("批量保存记录异常", e);
        }

        StringBuilder failMsg = new StringBuilder();
        List<Long> failIds = new ArrayList<>();
        for (BulkItemResponse itemResponse : responses.getItems()) {
            IndexResponse indexResponse = itemResponse.getResponse();
            if (!itemResponse.isFailed()
                    || DocWriteResponse.Result.CREATED.equals(indexResponse.getResult())
                    || DocWriteResponse.Result.UPDATED.equals(indexResponse.getResult())) {
                continue;
            }

            failIds.add(Long.valueOf(indexResponse.getId()));

            if(failMsg.length() > 0) {
                failMsg.append(",");
            }
            failMsg.append("{id:").append(indexResponse.getId()).append(",")
                    .append("cause:").append(itemResponse.getFailure().getCause()).append("}");
        }
        logger.error("批量保存记录失败的记录为 index:{} message:{}", index, failMsg.toString());
        return failIds;
    }

    /**
     * 保存一条索引记录(类比成数据库的话相当于往表中新增了一条记录)
     * 1、如果记录还不存在，则会新增一条记录
     * 2、如果记录已存在，且version不冲突，则会替换成新的内容
     * 3、如果记录已存在，且发生version冲突，则会替换失败
     * @param index         索引名称
     * @param id            id
     * @param sourceJson    JSON格式的文档内容
     * @param version       版本号，可选
     * @return
     */
    public boolean save(String index, Long id, String sourceJson, Long version){
        if(isEmpty(index) || id == null || isEmpty(sourceJson)) {
            throw new RuntimeException("index、id、sourceJson 不能为空");
        }

        IndexRequest request = new IndexRequest(index)
                .id(id.toString())
                .source(sourceJson, XContentType.JSON);
        if (version != null) {
            request.version(version).versionType(VersionType.EXTERNAL);
        }

        IndexResponse response;
        try {
            response = restEsClient.index(request, RequestOptions.DEFAULT);
            if (DocWriteResponse.Result.CREATED.equals(response.getResult())
                    || DocWriteResponse.Result.UPDATED.equals(response.getResult())) {
                return true;
            }
        } catch (Exception e) {
            logger.error("保存记录异常 index:{} id:{} sourceJson:{}", index, id, sourceJson, e);
            return false;
        }

        StringBuilder sbf = new StringBuilder();
        ReplicationResponse.ShardInfo shardInfo = response.getShardInfo();
        if (shardInfo.getTotal() != shardInfo.getSuccessful()) {
            sbf.append("totalShard=").append(shardInfo.getTotal())
                    .append("successShard=").append(shardInfo.getSuccessful());
        }
        if (shardInfo.getFailed() > 0) {
            sbf.append("failures are: [");
            for (ReplicationResponse.ShardInfo.Failure failure : shardInfo.getFailures()) {
                sbf.append("{nodeId:").append(failure.nodeId()).append(",")
                        .append("reason:").append(failure.reason()).append("},");
            }
            sbf.append("]");
        }
        logger.error("保存记录失败 index:{} id:{} message:{}", index, id, sbf.toString());
        return false;
    }

    /**
     * 创建索引（相当于索引数据拷贝）
     * @param sourceIndex       源索引
     * @param targetIndex       目标索引
     * @param externalVersion   目标索引是否使用外部版本号
     * @param listener          回调监听器，可选
     */
    public void reindexAsync(String sourceIndex, String targetIndex, boolean externalVersion, ActionListener<BulkByScrollResponse> listener){
        if (!exists(sourceIndex)) {
            throw new RuntimeException("sourceIndex不存在");
        } else if (!exists(targetIndex)) {
            throw new RuntimeException("targetIndex不存在");
        }

        if(listener == null) {
            listener = new ActionListener<BulkByScrollResponse>() {
                @Override
                public void onResponse(BulkByScrollResponse bulkByScrollResponse) {
                    logger.info("reindex完成 sourceIndex={} targetIndex={}", sourceIndex, targetIndex);
                }
                @Override
                public void onFailure(Exception e) {
                    logger.error("reindex失败 sourceIndex={} targetIndex={}", sourceIndex, targetIndex, e);
                }
            };
        }

        ReindexRequest request = new ReindexRequest();
        request.setRefresh(true);
        request.setSourceIndices(sourceIndex);
        request.setDestIndex(targetIndex);
        if (externalVersion) {
            request.setDestVersionType(VersionType.EXTERNAL);
        }
        restEsClient.reindexAsync(request, RequestOptions.DEFAULT, listener);
    }

    /**
     * 创建索引(类比成数据库的话相当于创建一张表)
     * @param index     索引名称
     * @param shards    分片数
     * @param replicas  每个分片的副本数
     * @param mappingProp   mappingProp
     */
    public boolean createIndex(String index, int shards, int replicas, Map<String, Map<String, Object>> mappingProp){
        if(isEmpty(index)) {
            throw new RuntimeException("index不能为空");
        }else if(shards <= 0 || replicas <= 0){
            throw new RuntimeException("shards、replicas须大于0");
        }else if(mappingProp == null || mappingProp.isEmpty()){
            throw new RuntimeException("mappingProp不能为空");
        }

        if (exists(index)) {
            return true;
        }

        CreateIndexRequest request = new CreateIndexRequest(index);
        request.settings(Settings.builder()
                .put("index.number_of_shards", shards)
                .put("index.number_of_replicas", replicas))
                .mapping(Collections.singletonMap("properties", mappingProp))
                .waitForActiveShards(ActiveShardCount.from(shards));
        try {
            CreateIndexResponse response = restEsClient.indices().create(request, RequestOptions.DEFAULT);
            return response.isShardsAcknowledged();
        } catch (Exception e) {
            logger.error("创建索引时异常 index:{} shards:{} replicas:{}", index, shards, replicas, e);
            return false;
        }
    }

    /**
     * 删除索引（此操作会删除整个索引的数据，慎重）
     * @param index     索引名
     * @return
     */
    public boolean deleteIndex(String index){
        if(isEmpty(index)) {
            throw new RuntimeException("index不能为空");
        }

        if(!exists(index)) {
            return true;
        }

        DeleteIndexRequest request = new DeleteIndexRequest(index);
        try {
            AcknowledgedResponse response = restEsClient.indices().delete(request, RequestOptions.DEFAULT);
            return response.isAcknowledged();
        } catch (Exception e) {
            logger.error("删除索引时异常 index:{}", index, e);
            return false;
        }
    }

    /**
     * 判断索引是否存在
     * @param index     索引名
     * @return
     */
    public boolean exists(String index) {
        if (isEmpty(index)) return false;

        try {
            GetIndexRequest request = new GetIndexRequest(index);
            boolean exists = restEsClient.indices().exists(request, RequestOptions.DEFAULT);
            return exists;
        } catch (Exception e) {
            throw new RuntimeException("判断索引是否存在时异常 index=" + index, e);
        }
    }

    /**
     * 获取index在ES中的mapping
     * @param index     索引名
     * @return
     */
    public Map<String, Map<String, Object>> getMapping(String index) {
        if(isEmpty(index)) {
            throw new RuntimeException("index不能为空");
        }

        Map<String, MappingMetaData> mappings;
        try {
            GetMappingsResponse mapping = restEsClient.indices().getMapping(new GetMappingsRequest().indices(index), RequestOptions.DEFAULT);
            mappings = mapping.mappings();
            if (mappings == null){
                return new HashMap<>();
            }
        } catch(Exception e) {
            throw new RuntimeException("获取当前index的mapping时异常, index:" + index, e);
        }

        Map<String, Map<String, Object>> mapping = new HashMap<>();
        for(Map.Entry<String, MappingMetaData> entry : mappings.entrySet()){
            Map<String, Object> res = (Map<String, Object>) entry.getValue().sourceAsMap().get("properties");
            if(res == null) {
                continue;
            }

            for(Map.Entry<String, Object> entry1 : res.entrySet()){
                LinkedHashMap<String, Object> fieldMap = (LinkedHashMap<String, Object>) entry1.getValue();
                mapping.put(entry1.getKey(), fieldMap);
            }
        }
        return mapping;
    }

    /**
     * 新增字段mapping（仅用以新增字段，不会修改原有字段的类型）
     * @param index         索引名
     * @param mappingProp   字段名和字段类型
     * @return
     */
    public boolean addMapping(String index, Map<String, Map<String, Object>> mappingProp) {
        if(isEmpty(index)) {
            throw new RuntimeException("index不能为空");
        }else if(mappingProp == null || mappingProp.isEmpty()){
            throw new RuntimeException("mappingProp不能为空");
        }

        Map<String, Map<String, Object>> filedTypeMapping = getMapping(index);
        if (filedTypeMapping == null) {
            filedTypeMapping = new HashMap<>();
        }

        boolean isNeedAddField = false;
        for (Map.Entry<String, Map<String, Object>> entry : mappingProp.entrySet()) {
            if (! filedTypeMapping.containsKey(entry.getKey())) {//只处理新增字段的mapping
                isNeedAddField = true;
                filedTypeMapping.put(entry.getKey(), entry.getValue());
            }
        }

        if(!isNeedAddField) {
            return false;
        }

        try {
            PutMappingRequest request = new PutMappingRequest(index);
            request.source(Collections.singletonMap("properties", filedTypeMapping));
            AcknowledgedResponse response = restEsClient.indices().putMapping(request, RequestOptions.DEFAULT);
            return response.isAcknowledged();
        } catch(Exception e) {
            logger.error("添加mapping异常 index:{}", index, e);
            return false;
        }
    }

    private SearchResponse executeQuery(EsQuery esQuery){
        paramCheck(esQuery, false);

        esQuery.doQueryParamCase(paramHelper);//查询时参数名转换

        //如果是滚动查询，则查询后直接返回即可
        if(esQuery.getScrollMode() && Utils.isNotEmpty(esQuery.getScrollId())){
            SearchScrollRequest scrollRequest = new SearchScrollRequest(esQuery.getScrollId());
            scrollRequest.scroll(TimeValue.timeValueSeconds(esQuery.getScrollExpireSec()));
            try {
                return restEsClient.scroll(scrollRequest, RequestOptions.DEFAULT);
            } catch(Exception e) {
                throw new RuntimeException("执行滚动查询时异常", e);
            }
        }

        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        sourceBuilder.timeout(new TimeValue(esQuery.getTimeout(), TimeUnit.MILLISECONDS));
        //设置需要返回的字段
        if(esQuery.getSelectFields() != null && esQuery.getSelectFields().length > 0){
            sourceBuilder.fetchSource(esQuery.getSelectFields(), null);
        }
        //构造查询条件
        sourceBuilder.query(getQueryBuilder(esQuery));
        //增加排序字段
        addSort(sourceBuilder, esQuery.getOrderBy());
        //构建查询请求对象，并指定要查询的 index、type
        SearchRequest searchRequest = new SearchRequest(esQuery.getIndex());
        searchRequest.source(sourceBuilder);
        //处理分页查询
        if(esQuery.getScrollMode()){
            searchRequest.scroll(TimeValue.timeValueSeconds(esQuery.getScrollExpireSec()));
            sourceBuilder.size(esQuery.getPageSize());
        }else{
            int offset = (esQuery.getPageCurrent() - 1) * esQuery.getPageSize();
            sourceBuilder.from(offset).size(esQuery.getPageSize());
        }

        try {
            return restEsClient.search(searchRequest, RequestOptions.DEFAULT);
        } catch(Exception e) {
            throw new RuntimeException("执行查询时异常", e);
        }
    }

    private SearchResponse executeAggregation(EsQuery esQuery){
        paramCheck(esQuery, true);
        //查询时参数名转换
        esQuery.doQueryParamCase(paramHelper);

        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        //设置聚合维度
        appendAggregation(sourceBuilder, esQuery);
        //设置查询过滤条件
        sourceBuilder.query(getQueryBuilder(esQuery));

        SearchRequest searchRequest = new SearchRequest(esQuery.getIndex());
        searchRequest.source(sourceBuilder);

        try {
            return restEsClient.search(searchRequest, RequestOptions.DEFAULT);
        } catch(Exception e ){
            throw new RuntimeException("执行聚合查询时异常", e);
        }
    }

    private QueryBuilder getQueryBuilder(EsQuery esQuery){
        Map<String, String> fieldMap = getESFieldTypeMapping(esQuery.getIndex());
        if(fieldMap == null || fieldMap.isEmpty()){
            throw new RuntimeException("es mapping not exist of index: " + esQuery.getIndex());
        }else if(! isEmpty(esQuery.getGroupBy()) && ! fieldMap.containsKey(esQuery.getGroupBy())){
            throw new RuntimeException("cannot use an not exist field to group by : " + esQuery.getGroupBy());
        }

        //如果没有任何的查询条件，则直接返回MatchAllQueryBuilder
        if (isMatchAll(esQuery)) {
            return QueryBuilders.matchAllQuery();
        }

        //布尔查询可以把多个子查询组合（combine）成一个布尔表达式，所有子查询之间的逻辑关系是与，即等同于sql中的 and 查询
        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();

        //精确匹配(等于)
        if(isNotBlank(esQuery.getEqMap())){
            for(Map.Entry<String, Object> entry : esQuery.getEqMap().entrySet()){
                if(isNotEmpty(entry.getKey(), entry.getValue()) && fieldMap.containsKey(entry.getKey())){
                    queryBuilder.filter(QueryBuilders.termQuery(entry.getKey(), entry.getValue()));
                }
            }
        }

        //精确匹配(不等于)
        if(isNotBlank(esQuery.getNeqMap())) {
            for (Map.Entry<String, Object> entry : esQuery.getNeqMap().entrySet()) {
                if (isNotEmpty(entry.getKey(), entry.getValue()) && fieldMap.containsKey(entry.getKey())) {
                    queryBuilder.mustNot(QueryBuilders.termQuery(entry.getKey(), entry.getValue()));
                }
            }
        }

        //范围列表精确匹配(in 查询)
        if(isNotBlank(esQuery.getInMap())){
            for(Map.Entry<String, Object[]> entry : esQuery.getInMap().entrySet()){
                if(isNotEmpty(entry.getKey(), entry.getValue()) && fieldMap.containsKey(entry.getKey())){
                    queryBuilder.filter(QueryBuilders.termsQuery(entry.getKey(), entry.getValue()));
                }
            }
        }

        //精确匹配(not in 查询)
        if(isNotBlank(esQuery.getNotInMap())) {
            for (Map.Entry<String, Object[]> entry : esQuery.getNotInMap().entrySet()) {
                if (isNotEmpty(entry.getKey(), entry.getValue()) && fieldMap.containsKey(entry.getKey())) {
                    queryBuilder.mustNot(QueryBuilders.termsQuery(entry.getKey(), entry.getValue()));
                }
            }
        }

        //范围区间查询
        if(isNotBlank(esQuery.getGtMap()) || isNotBlank(esQuery.getGteMap())
                || isNotBlank(esQuery.getLtMap()) || isNotBlank(esQuery.getLteMap())){
            Set<String> keys = new HashSet<>();

            if(isNotBlank(esQuery.getGtMap())){
                keys.addAll(esQuery.getGtMap().keySet());
            }
            if(isNotBlank(esQuery.getGteMap())){
                keys.addAll(esQuery.getGteMap().keySet());
            }
            if(isNotBlank(esQuery.getLtMap())){
                keys.addAll(esQuery.getLtMap().keySet());
            }
            if(isNotBlank(esQuery.getLteMap())){
                keys.addAll(esQuery.getLteMap().keySet());
            }

            for(String key : keys){
                if(isEmpty(key) || ! fieldMap.containsKey(key)){
                    continue;
                }

                RangeQueryBuilder query = QueryBuilders.rangeQuery(key);
                Object valueGt = esQuery.getGtMap() == null ? null : esQuery.getGtMap().get(key);
                Object valueGte = esQuery.getGteMap() == null ? null : esQuery.getGteMap().get(key);
                Object valueLt = esQuery.getLtMap() == null ? null : esQuery.getLtMap().get(key);
                Object valueLte = esQuery.getLteMap() == null ? null : esQuery.getLteMap().get(key);

                if(valueGte != null && valueLte != null){
                    query.from(valueGte).to(valueLte);
                }else{
                    if(valueGt != null){
                        query.gt(valueGt);
                    }
                    if(valueGte != null){
                        query.gte(valueGte);
                    }
                    if(valueLt != null){
                        query.lt(valueLt);
                    }
                    if(valueLte != null){
                        query.lte(valueLte);
                    }
                }

                queryBuilder.filter(query);
            }
        }

        //全文搜索(ES服务端需安装有中文分词器)
        if(isNotBlank(esQuery.getLikeMap())){
            for(Map.Entry<String, Object> entry : esQuery.getLikeMap().entrySet()){
                if (isNotEmpty(entry.getKey(), entry.getValue()) && fieldMap.containsKey(entry.getKey())) {
                    queryBuilder.filter(QueryBuilders.matchQuery(entry.getKey(), entry.getValue()));
                }
            }
        }

        //null查询，等同于sql语句：where field is null
        if(isNotBlank(esQuery.getNullMap())){
            for(Map.Entry<String, Object> entry : esQuery.getNullMap().entrySet()){
                if (isNotEmpty(entry.getKey(), entry.getValue()) && fieldMap.containsKey(entry.getKey())) {
                    queryBuilder.mustNot(QueryBuilders.existsQuery(entry.getKey()));
                }
            }
        }

        //not null查询，等同于sql语句：where field is not null
        if(isNotBlank(esQuery.getNotNullMap())){
            for(Map.Entry<String, Object> entry : esQuery.getNotNullMap().entrySet()){
                if (isNotEmpty(entry.getKey(), entry.getValue()) && fieldMap.containsKey(entry.getKey())) {
                    queryBuilder.filter(QueryBuilders.existsQuery(entry.getKey()));
                }
            }
        }

        return queryBuilder;
    }

    private void appendAggregation(SearchSourceBuilder sourceBuilder, EsQuery esQuery){
        Map<String, String> fieldMap = getESFieldTypeMapping(esQuery.getIndex());
        boolean isNeedTerms = Utils.isNotEmpty(esQuery.getGroupBy());
        TermsAggregationBuilder termsAggBuilder = null;
        if (isNeedTerms) {
            termsAggBuilder = AggregationBuilders.terms(esQuery.getGroupBy())
                    .field(esQuery.getGroupBy())
                    .size(MAX_GROUP_SIZE);//聚合不能够分页，只能取指定的条数
        }

        Field[] fields = EsQuery.Aggregation.class.getDeclaredFields();
        for(Map.Entry<String, EsQuery.Aggregation> entry : esQuery.getAggMap().entrySet()){
            String aggField = entry.getKey();
            if(! fieldMap.containsKey(aggField)){ //ES中不存在的字段将直接忽略
                continue;
            }

            EsQuery.Aggregation agg = entry.getValue();
            for(Field field : fields){
                field.setAccessible(true);

                String name = field.getName();
                if(name.contains("this$") || "field".equals(name)){
                    continue;
                }

                Boolean value;
                try{
                    value = field.getBoolean(agg);
                }catch(Throwable e){
                    throw new RuntimeException("EsQuery.Aggregation 获取"+name+"的属性值出现异常：", e);
                }
                if(!value){
                    continue;
                }

                ValuesSourceAggregationBuilder aggBuilder;
                switch(name){
                    case "count":
                        aggBuilder = AggregationBuilders.count(fillFieldName(aggField, name)).field(aggField);
                        break;
                    case "sum":
                        aggBuilder = AggregationBuilders.sum(fillFieldName(aggField, name)).field(aggField);
                        break;
                    case "min":
                        aggBuilder = AggregationBuilders.min(fillFieldName(aggField, name)).field(aggField);
                        break;
                    case "max":
                        aggBuilder = AggregationBuilders.max(fillFieldName(aggField, name)).field(aggField);
                        break;
                    case "avg":
                        aggBuilder = AggregationBuilders.avg(fillFieldName(aggField, name)).field(aggField);
                        break;
                    default:
                        throw new RuntimeException("EsQuery.Aggregation 未预期的属性名称：" + name);
                }

                if(isNeedTerms){
                    termsAggBuilder.subAggregation(aggBuilder);
                }else{
                    sourceBuilder.aggregation(aggBuilder);
                }
            }
        }

        if(isNeedTerms){
            sourceBuilder.aggregation(termsAggBuilder);
        }
    }

    private void fillEsAggResult(AggResult aggResult, String groupValue, Iterator<Aggregation> iterator, EsQuery esQuery){
        while(iterator.hasNext()) {
            Aggregation aggEs = iterator.next();
            String fieldName = splitFieldName(aggEs.getName());
            if(esQuery.isWordCase()){
                fieldName = esQuery.isCamelCase() ? Utils.toCamelCase(fieldName) : Utils.toSnakeCase(fieldName);
            }

            EsAgg agg;
            Map<String, EsAgg> groupAggMap = aggResult.getAggDataMap().get(fieldName);//分组后的统计结果
            if(groupAggMap == null){
                groupAggMap = new HashMap<>();
                agg = new EsAgg();
                agg.setGroupValue(groupValue);
                groupAggMap.put(groupValue, agg);
                aggResult.getAggDataMap().put(fieldName, groupAggMap);
            }else if((agg = groupAggMap.get(groupValue)) == null){
                agg = new EsAgg();
                agg.setGroupValue(groupValue);
                groupAggMap.put(groupValue, agg);
            }

            switch(aggEs.getType()){
                case ValueCountAggregationBuilder.NAME:
                    agg.setCount(((ParsedValueCount) aggEs).getValue());
                    break;
                case MaxAggregationBuilder.NAME:
                    agg.setMax(BigDecimal.valueOf(((ParsedMax) aggEs).getValue()));
                    break;
                case MinAggregationBuilder.NAME:
                    agg.setMin(BigDecimal.valueOf(((ParsedMin) aggEs).getValue()));
                    break;
                case SumAggregationBuilder.NAME:
                    agg.setSum(BigDecimal.valueOf(((ParsedSum) aggEs).getValue()));
                    break;
                case AvgAggregationBuilder.NAME:
                    agg.setAvg(BigDecimal.valueOf(((ParsedAvg) aggEs).getValue()));
                    break;
                default:
                    throw new RuntimeException("未支持的聚合类型：" + aggEs.getType());
            }
        }
    }

    /**
     * 取得查询结构返回的实体类
     * @param esQuery
     * @return
     */
    private Class getReturnClass(EsQuery esQuery){
        if(Utils.isEmpty(esQuery.getReturnClassName())){
            return HashMap.class;
        }else{
            try{
                return Utils.getClass(esQuery.getReturnClassName());
            }catch (ClassNotFoundException e){
                throw new RuntimeException("ClassNotFoundException " + e.getMessage());
            }
        }
    }

    /**
     * 添加排序字段
     * @param searchBuilder
     * @param sortColumns
     */
    protected void addSort(SearchSourceBuilder searchBuilder, String sortColumns){
        if(Utils.isEmpty(sortColumns)){
            return;
        }

        String[] sortColumnArray = sortColumns.split(",");
        for(int i=0; i<sortColumnArray.length; i++){
            String[] sortColumn = sortColumnArray[i].split(" ");
            if(sortColumn.length > 1){
                searchBuilder.sort(sortColumn[0], SortOrder.fromString(sortColumn[sortColumn.length - 1]));
            }else{
                searchBuilder.sort(sortColumn[0], SortOrder.DESC);
            }
        }
    }

    private void paramCheck(EsQuery esQuery, boolean aggMapMust){
        if(esQuery == null){
            throw new RuntimeException("esQuery不能为空");
        }else if(Utils.isEmpty(esQuery.getIndex())){
            throw new RuntimeException("index不能为空");
        }else if(esQuery.getPageSize() <= 0 || esQuery.getPageCurrent() <= 0){
            throw new RuntimeException("pageCurrent和pageSize都需大于0");
        }else if(aggMapMust && (esQuery.getAggMap() == null || esQuery.getAggMap().isEmpty())){
            throw new RuntimeException("aggMap不能为空");
        }
    }

    private boolean isNotEmpty(String key, Object value){
        return ! (isEmpty(key) || (value == null || value.toString().trim().length() <= 0));
    }
    private boolean isNotEmpty(String key, Object[] values){
        return ! (isEmpty(key) || (values == null || values.length <= 0));
    }
    private boolean isEmpty(String key){
        return (key == null || key.trim().length() <= 0);
    }
    private boolean isNotBlank(Map<String, ?> map){
        return !isBlank(map);
    }
    private boolean isBlank(Map<String, ?> map){
        return map == null || map.isEmpty();
    }

    public boolean isMatchAll(EsQuery esQuery) {
        return isBlank(esQuery.getEqMap()) && isBlank(esQuery.getNeqMap())
                && isBlank(esQuery.getGtMap()) && isBlank(esQuery.getGteMap())
                && isBlank(esQuery.getLtMap()) && isBlank(esQuery.getLteMap())
                && isBlank(esQuery.getLikeMap()) && isBlank(esQuery.getInMap())
                && isBlank(esQuery.getNotInMap());
    }

    private boolean isHashMap(Class clz){
        return HashMap.class.getName().equals(clz.getName());
    }

    private boolean isString(Class clz){
        return String.class.getName().equals(clz.getName());
    }

    private String fillFieldName(String field, String suffix){
        return field + "|" + suffix;
    }

    private String splitFieldName(String field){
        return field.split("\\|")[0];
    }

    /**
     * key的驼峰和下划线互转
     * @param entryMap
     * @param isCamelCase
     * @return
     */
    private Map<String, Object> resultWordCase(Map<String, Object> entryMap, boolean isCamelCase){
        Map<String, Object> resultMap = new HashMap<>();
        for(Map.Entry<String, Object> entry : entryMap.entrySet()) {
            String key = isCamelCase ? Utils.toCamelCase(entry.getKey()) : Utils.toSnakeCase(entry.getKey());
            resultMap.put(key, entry.getValue());
        }
        return resultMap;
    }

    /**
     * 返回index在ES中的字段类型，其中key为字段名，value为该字段的数据类型
     * @param index
     * @return
     */
    private Map<String, String> getESFieldTypeMapping(String index){
        if(cache != null && cache.getIfPresent(index) != null){
            return cache.getIfPresent(index);
        }

        Map<String, Map<String, Object>> mapping = getMapping(index);

        Map<String, String> fieldTypeMap = new HashMap<>();
        for(Map.Entry<String, Map<String, Object>> entry : mapping.entrySet()){
            if(Utils.isNotEmpty(entry.getKey())){
                fieldTypeMap.put(entry.getKey(), (String) entry.getValue().get("type"));
            }
        }
        cache.put(index, fieldTypeMap);
        return fieldTypeMap;
    }

    private void clearScrollAsync(String scrollId){
        ClearScrollRequest clearScrollRequest = new ClearScrollRequest();
        clearScrollRequest.addScrollId(scrollId);
        getRestEsClient().clearScrollAsync(clearScrollRequest, RequestOptions.DEFAULT, new ActionListener<ClearScrollResponse>() {
            @Override
            public void onResponse(ClearScrollResponse clearScrollResponse) {
                if(clearScrollResponse.isSucceeded()){
                    logger.info("异步清除成功 scrollId={} ", scrollId);
                }else{
                    logger.warn("异步清除失败 scrollId={} status={} ", scrollId, clearScrollResponse.status().name());
                }
            }
            @Override
            public void onFailure(Exception e) {
                logger.error("异步清除异常 scrollId={} Exception={} ", scrollId, e.getMessage());
            }
        });
    }

    public void destroy(){
        try{
            this.getRestEsClient().close();
        }catch(Throwable e){
        }
    }

    private class InnerParamHelper implements EsQuery.ParamHelper {
        public boolean isNotEmpty(String val){
            return Utils.isNotEmpty(val);
        }

        public String toSnakeCase(String val){
            return Utils.toSnakeCase(val);
        }

        public String toCamelCase(String val){
            return Utils.toCamelCase(val);
        }
    }

    /**
     * 统计结果的临时存放对象
     */
    private class AggResult {
        /**
         * 分组字段
         */
        private String groupField;
        /**
         * 统计结果，第一层key为字段名，第二层key为group by之后的具体分组值，第二层value为各个维度的统计结果
         */
        Map<String, Map<String, EsAgg>> aggDataMap = new HashMap<>();

        public String getGroupField() {
            return groupField;
        }

        public void setGroupField(String groupField) {
            this.groupField = groupField;
        }

        public Map<String, Map<String, EsAgg>> getAggDataMap() {
            return aggDataMap;
        }

        public void setAggDataMap(Map<String, Map<String, EsAgg>> aggDataMap) {
            this.aggDataMap = aggDataMap;
        }

        public EsAggResult toEsAggResult(){
            EsAggResult result = new EsAggResult();
            result.setGroupField(this.groupField);
            for(Map.Entry<String, Map<String, EsAgg>> entry : aggDataMap.entrySet()){
                String key = entry.getKey();//字段名
                Map<String, EsAgg> valueMap = entry.getValue();//字段的分组统计结果

                List<EsAgg> aggList = new ArrayList<>();
                for(Map.Entry<String, EsAgg> aggEntry : valueMap.entrySet()){
                    EsAgg aggDto = aggEntry.getValue();
                    if (DEFAULT_GROUP_VALUE.equals(aggDto.getGroupValue())) {
                        aggDto.setGroupValue(null);
                    }
                    aggList.add(aggDto);
                }
                result.getAggResults().put(key, aggList);
            }
            return result;
        }
    }
}

