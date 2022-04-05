package com.xpay.demo.es;

import com.xpay.common.statics.query.EsQuery;
import com.xpay.common.statics.result.EsAggResult;
import com.xpay.common.statics.result.PageResult;
import com.xpay.common.utils.DateUtil;
import com.xpay.common.utils.JsonUtil;
import com.xpay.demo.es.order.Order;
import com.xpay.demo.es.order.OrderHelper;
import com.xpay.starter.plugin.client.ESClient;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.index.reindex.BulkByScrollResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * ES的一个index可类比成数据库的一张表，但ES不允许修改字段的类型，只允许新增字段，如果需要修改字段类型，则需要新建一个索引，然后把旧数据导到新索引(reindex)
 */
@RestController
@RequestMapping("demo")
public class DemoController {
    private Logger log = LoggerFactory.getLogger(this.getClass());
    @Autowired
    OrderHelper orderHelper;
    @Autowired
    ESClient esClient;

    @RequestMapping(value = "/createIndex")
    public boolean createIndex(){
        String index = orderHelper.getTableName();
        int shards = 1;
        int replicas = 1;
        Map<String, Map<String, Object>> mappingProp = orderHelper.getOrderMappingProp();
        boolean isOk = esClient.createIndex(index, shards, replicas, mappingProp);
        return isOk;
    }

    @RequestMapping(value = "/deleteIndex")
    public boolean deleteIndex(){
        String index = orderHelper.getTableName();
        return esClient.deleteIndex(index);
    }

    @RequestMapping(value = "/getMapping")
    public Map<String, Map<String, Object>> getMapping(){
        String index = orderHelper.getTableName();
        return esClient.getMapping(index);
    }

    @RequestMapping(value = "/addMapping")
    public Map<String, Object> addMapping() throws Exception {
        Map<String, Object> result = new HashMap<>();

        //1、先删除索引，避免被旧数据影响到
        deleteIndex();
        //2、再手动创建索引，避免ES自动生成mapping导致各字段的索引类型不符合要求
        createIndex();

        //3、先新增一条没有fee字段的记录
        String index = orderHelper.getTableName();
        Order order1 = orderHelper.createOrder();
        esClient.save(index, order1.getId(), JsonUtil.toJson(order1), (long)order1.getVersion());
        Thread.sleep(1000);//休眠一小段时间，等待索引可见
        EsQuery esQuery = EsQuery.buildNoneCase().from(index).eq("id", order1.getId());
        Map<String, Object> record1 = esClient.getOne(esQuery);
        result.put("record1", record1);

        //4、修改mapping，新增一个fee字段
        Map<String, Map<String, Object>> properties = new HashMap<>();
        properties.put("fee", Collections.singletonMap("type", "double"));//模拟新增一个手续费字段
        esClient.addMapping(index, properties);
        Thread.sleep(1000);//休眠一小段时间，等待索引可见
        //查询修改后的mapping
        Map<String, Map<String, Object>> newMapping = esClient.getMapping(index);
        result.put("mapping", newMapping);

        //5、再新增一个有fee字段的记录
        Order order2 = orderHelper.createOrder();
        order2.setFee(BigDecimal.valueOf(0.26));
        esClient.save(index, order2.getId(), JsonUtil.toJson(order2), (long)order2.getVersion());
        Thread.sleep(1000);//休眠一小段时间，等待索引可见
        Map record2 = esClient.getById(index, order2.getId());
        result.put("record2", record2);
        return result;
    }

    @RequestMapping(value = "/reindex")
    public Map<String, Object> reindex() throws Exception {
        String index = orderHelper.getTableName();
        String indexNew = index + "_new";

        //1.先删除新的index，避免数据污染
        esClient.deleteIndex(indexNew);

        //2.在旧index上插入数据并新增一个fee字段
        addMapping();

        //3.创建新index
        int shards = 1;
        int replicas = 1;
        Map<String, Map<String, Object>> mappingProp = orderHelper.getOrderMappingProp();
        mappingProp.put("fee", Collections.singletonMap("type", "text"));//模拟修改字段类型
        boolean isOk = esClient.createIndex(indexNew, shards, replicas, mappingProp);
        if(!isOk) {
            System.out.println("创建新index失败 indexNew = " + indexNew);
            return null;
        }

        //4.把数据从旧index导入到新index中去
        CountDownLatch countDown = new CountDownLatch(1);
        List<HashMap> allRecords = new ArrayList<>();
        esClient.reindexAsync(index, indexNew, true, new ActionListener<BulkByScrollResponse>() {
            @Override
            public void onResponse(BulkByScrollResponse bulkByScrollResponse) {
                System.out.println("reindex完成");

                CompletableFuture.runAsync(() -> {
                    try {
                        //4.1 往新index中插入一条数据（fee字段的内容变成了字符串，而不再是double类型的数字）
                        Order order = orderHelper.createOrder();
                        Map<String, Object> newOrder = JsonUtil.toBean(JsonUtil.toJson(order), HashMap.class);
                        newOrder.put("fee", "ok了，换成了字符串");
                        boolean isOK = esClient.save(indexNew, order.getId(), JsonUtil.toJson(newOrder), (long)order.getVersion());
                        System.out.println("save new record: " + isOK);
                        try {
                            Thread.sleep(1000);//休眠一小段时间，等待索引可见
                        } catch (Exception e){
                        }

                        //4.2 查出新index下的所有记录（因为数据量少，所以可以直接取出）
                        EsQuery esQuery = EsQuery.buildNoneCase().from(indexNew);
                        List<HashMap> records = esClient.listBy(esQuery);
                        if(records != null) {
                            allRecords.addAll(records);
                        }
                    } finally {
                        //4.3 唤醒等待的主线程
                        countDown.countDown();
                    }
                });
            }

            @Override
            public void onFailure(Exception e) {
                System.out.println("reindex失败,errorMsg = " + e.getMessage());
                countDown.countDown();//4.3 唤醒等待的主线程
            }
        });

        //5.等待reindex完成后被唤醒
        countDown.await();

        //6.取得新index的mapping
        Map<String, Map<String, Object>> mappingNew = esClient.getMapping(indexNew);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("mappingNew", mappingNew);
        result.put("recordsNew", allRecords);
        return result;
    }

    @RequestMapping(value = "/insert")
    public boolean insert(){
        createIndex();//如果index还不存在，则先创建，如果index已存在，则忽略本次创建请求

        String index = orderHelper.getTableName();
        Order order = orderHelper.createOrder();
        boolean isOk = esClient.save(index, order.getId(), JsonUtil.toJson(order), (long)order.getVersion());
        return isOk;
    }

    @RequestMapping(value = "/batchInsert")
    public String batchInsert(@Nullable String index){
        if (index == null) {
            index = orderHelper.getTableName();
        }

        createIndex();//如果index还不存在，则先创建，如果index已存在，则忽略本次创建请求

        Map<Long, String> orders = new HashMap<>();
        Map<Long, Long> orderVersion = new HashMap<>();
        for (int i=0; i<100; i++){
            Order order = orderHelper.createOrder();
            orders.put(order.getId(), JsonUtil.toJson(order));
            orderVersion.put(order.getId(), (long)order.getVersion());
        }
        List<Long> failIds = esClient.batchSave(index, orders, orderVersion);
        return JsonUtil.toJson(failIds);
    }

    @ResponseBody
    @RequestMapping(value = "/getById")
    public Map getById(Long id) {
        String index = orderHelper.getTableName();
        Map result = esClient.getById(index, id);
        return result;
    }

    @ResponseBody
    @RequestMapping(value = "/getByOrderNo")
    public HashMap getByOrderNo(String orderNo) {
        String index = orderHelper.getTableName();
        EsQuery esQuery = EsQuery.buildNoneCase()
                .from(index)
                .eq("orderNo", orderNo);
        HashMap result = esClient.getOne(esQuery);
        return result;
    }

    @ResponseBody
    @RequestMapping(value = "/listByUserId")
    public List<HashMap> listByUserId(Long userId) {
        String index = orderHelper.getTableName();

        EsQuery esQuery = EsQuery.buildNoneCase()
                .from(index)
                .eq("userId", userId);
        List<HashMap> results = esClient.listBy(esQuery);
        return results;
    }

    @ResponseBody
    @RequestMapping(value = "/listByMchNo")
    public List<HashMap> listByMchNo(Long mchNo) {
        String index = orderHelper.getTableName();

        EsQuery esQuery = EsQuery.buildNoneCase()
                .from(index)
                .eq("mchNo", mchNo);
        List<HashMap> results = esClient.listBy(esQuery);
        return results;
    }

    /**
     * 列出所有记录（此方式只适用于数据量比较小的情况，此处是为了查看到所有的数据）
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/listAll")
    public List<HashMap> listAll(@Nullable String index){
        if(index == null) {
            index = orderHelper.getTableName();
        }
        int currRecordCount = 0;
        Integer pageSize = 10;
        String scrollId = null;

        EsQuery esQuery = EsQuery.buildNoneCase().from(index);

        List<HashMap> records = new ArrayList<>();
        while (true) {
            esQuery.scroll( 60, pageSize, scrollId);

            PageResult<List<HashMap>> result = esClient.listPage(esQuery);
            scrollId = result.getScrollId();
            currRecordCount = result.getData() != null ? result.getData().size() : 0;
            if(currRecordCount > 0) {
                records.addAll(result.getData());
            }
            if(currRecordCount < pageSize) {
                break;
            }
        }
        return records;
    }

    /**
     * 按多个条件一次性查出所有数据（默认不能超出10000条记录）
     * @param pagSize
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/listBy")
    public List<Order> listBy(Integer pagSize) {
        String index = orderHelper.getTableName();

        EsQuery esQuery = EsQuery.buildNoneCase()
                .from(index)
                .eq("payStatus", "1")
                .size(pagSize)
                .resultClass(Order.class);
        List<Order> result = esClient.listBy(esQuery);
        return result;
    }

    /**
     * 多条件统计记录数
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/countBy")
    public Long countBy(@Nullable Integer payStatus){
        String index = orderHelper.getTableName();
        EsQuery esQuery = EsQuery.buildNoneCase().from(index);
        if (payStatus != null) {
            esQuery.eq("payStatus", payStatus.toString());
        }
        return esClient.countBy(esQuery);
    }

    /**
     * 多条件分页查询（为了避免深分页问题，默认不能超出10000条记录，如果需要查出所有符合条件的记录，请使用滚动查询）
     * @param pageCurrent
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/listPage")
    public PageResult<List<Order>> listPage(Integer pageCurrent) {
        String index = orderHelper.getTableName();
        EsQuery esQuery = EsQuery.buildNoneCase()
                .from(index)
//                .eq("userId", "1")
                .eq("mchNo", "111000013")
                .in("payStatus", "1,2,3,4".split(","))
                .page(pageCurrent, 3)
                .resultClass(Order.class)
                ;
        PageResult<List<Order>> result = esClient.listPage(esQuery);
        return result;
    }

    /**
     * 全文检索
     * @param name
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/fullText")
    public PageResult<List<Order>> fullText(String name, Integer pageCurrent) {
        String index = orderHelper.getTableName();
        EsQuery esQuery = EsQuery.buildNoneCase()
                .from(index)
                .fullText("productName", name)
                .page(pageCurrent, 10)
                .resultClass(Order.class);
        PageResult<List<Order>> result = esClient.listPage(esQuery);
        return result;
    }

    /**
     * is null 和 is not null 的检索
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/nullSearch")
    public Map<String, Object> nullSearch() {
        String index = orderHelper.getTableName();

        EsQuery esQuery1 = EsQuery.buildNoneCase()
                .from(index)
                .isNull("fee")
                .size(10);
        List<HashMap> records1 = esClient.listBy(esQuery1);

        EsQuery esQuery2 = EsQuery.buildNoneCase()
                .from(index)
                .isNotNull("fee")
                .size(10);
        List<HashMap> records2 = esClient.listBy(esQuery2);

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("isNull", records1);
        resultMap.put("NotNull", records2);
        return resultMap;
    }

    /**
     * 多条件滚动分页查询（当需要查询出所有符合条件的记录时使用，此方式相当于是第一次查询时生成一个快照，如果在分页查询过程中有记录被新增/修改，将不会被查询到）
     * @param scrollId
     * @param pagSize
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/scrollPage")
    public PageResult<List<HashMap>> scrollPage(String scrollId, Integer pagSize) {
        String index = orderHelper.getTableName();

        String dataStart = DateUtil.formatDateTime(DateUtil.getDayStart(DateUtil.addDay(new Date(), -1)));
        String dateEnd = DateUtil.formatDateTime(DateUtil.getDayEnd(new Date()));

        EsQuery esQuery = EsQuery.buildNoneCase()
                .from(index)
                .between("createTime", dataStart, dateEnd)
                .scroll( 60, pagSize, scrollId);
        PageResult<List<HashMap>> result = esClient.listPage(esQuery);
        return result;
    }

    /**
     * 聚合查询，用以数据统计（本方式仅适合一些简单的统计，复杂的统计，ES并不擅长，客户端也不好封装，有复杂的统计时，需要根据官方文档的指导，使用RestHighLevelClient自行开发了）
     * @param groupBy
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/aggregation")
    public EsAggResult aggregation(@Nullable String groupBy) {
        String index = orderHelper.getTableName();
        EsQuery esQuery = EsQuery.buildNoneCase().from(index);

        esQuery.count("id") //统计总订单数
                .sum("amount")//统计总金额
                .avg("price")//统计均价
                .min("price")//统计最低单价
                .max("quantity")//统计每笔单最大购买数量
//                .eq("productId", "1")//过滤条件：商品id
//                .eq("payStatus", "1")//过滤条件：支付状态
                .groupBy(groupBy)//分组字段
        ;

        EsAggResult result = esClient.aggregation(esQuery);
        return result;
    }

    /**
     * 测试单笔写入的性能(简单的测试)
     *
     * 使用阿里云实例 2C4G，1M带宽 部署单机ES服务端的测试结果如下：
     * testMultiThreadWriteTPS totalCount = 80000 timeCost = 199593(ms) tps = 400.8156598678311 failCount = 0
     */
    @RequestMapping(value = "/writeTPS")
    public String writeTPS(){
        deleteIndex();//先清空index内的值

        createIndex();//创建index

        long start = System.currentTimeMillis();
        int threadCount = 8, maxPerThread = 10000;
        CountDownLatch countDown = new CountDownLatch(threadCount);
        AtomicInteger totalCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);
        while(threadCount-- > 0){
            new Thread(() -> {
                int i = 0;
                while(++i <= maxPerThread){
                    try{
                        String index = orderHelper.getTableName();
                        Order order = orderHelper.createOrder();
                        boolean isOk = esClient.save(index, order.getId(), JsonUtil.toJson(order), (long)order.getVersion());
                        if(!isOk){
                            failCount.incrementAndGet();
                        }
//                        System.out.println("totalCount=" + totalCount + ",isOk="+isOk);//println语句比较耗时，对测试影响很大
                    }catch(Exception e){
                        failCount.incrementAndGet();
//                        e.printStackTrace();
                    }
                    totalCount.incrementAndGet();
                }
                countDown.countDown();
            }).start();
        }

        try{
            countDown.await();
        }catch(Exception e){
            e.printStackTrace();
        }

        long timeCost = System.currentTimeMillis() - start;
        double tps = totalCount.get() / (timeCost/1000d);
        System.out.println("testMultiThreadWriteTPS totalCount = " + totalCount.get() + " timeCost = " + timeCost + "(ms) tps = " + tps + " failCount = " + failCount.get());
        return "finish";
    }

    /**
     * 测试批量写入的性能(简单的测试)
     *
     * 使用阿里云实例 2C4G，1M带宽 部署单机ES服务端的测试结果如下：
     * testMultiThreadBatchWriteTPS totalCount = 80800 timeCost = 115946(ms) tps = 696.8761319924793 failCount = 0
     */
    @RequestMapping(value = "/batchWriteTPS")
    public String batchWriteTPS(){
        deleteIndex();//先清空index内的值

        createIndex();//创建index

        String index = orderHelper.getTableName();
        long start = System.currentTimeMillis();
        int threadCount = 8, maxPerThread = 10000;
        CountDownLatch countDown = new CountDownLatch(threadCount);
        AtomicInteger totalCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);
        while(threadCount-- > 0){
            new Thread(() -> {
                int currCount = 0;
                int batchSize = 100;
                while(currCount <= maxPerThread){
                    try {
                        Map<Long, String> orders = new HashMap<>();
                        Map<Long, Long> orderVersion = new HashMap<>();
                        for (int j=0; j<batchSize; j++){
                            Order order = orderHelper.createOrder();
                            orders.put(order.getId(), JsonUtil.toJson(order));
                            orderVersion.put(order.getId(), (long)order.getVersion());
                        }
                        currCount += batchSize;
                        List<Long> failIds = esClient.batchSave(index, orders, orderVersion);
                        failCount.addAndGet(failIds.size());
                    } catch (Exception e) {
                        failCount.addAndGet(batchSize);
                    }
                    totalCount.addAndGet(batchSize);
                }
                countDown.countDown();
            }).start();
        }

        try{
            countDown.await();
        }catch(Exception e){
            e.printStackTrace();
        }

        long timeCost = System.currentTimeMillis() - start;
        double tps = totalCount.get() / (timeCost/1000d);
        System.out.println("testMultiThreadBatchWriteTPS totalCount = " + totalCount.get() + " timeCost = " + timeCost + "(ms) tps = " + tps + " failCount = " + failCount.get());
        return "finish";
    }
}
