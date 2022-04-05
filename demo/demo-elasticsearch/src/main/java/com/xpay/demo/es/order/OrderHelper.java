package com.xpay.demo.es.order;

import com.xpay.common.utils.AmountUtil;
import com.xpay.common.utils.DateUtil;
import com.xpay.common.utils.RandomUtil;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 订单帮助类
 */
@Component
public class OrderHelper {
    private List<Category> categories = new ArrayList<>();//模拟所有类目
    private List<Merchant> merchants = new ArrayList<>();//模拟所有商户列表
    private List<Product> products = new ArrayList<>();//模拟所有商品列表
    private List<User> users = new ArrayList<>();//模拟所有用户列表
    private AtomicLong orderId = new AtomicLong(0);
    private AtomicLong orderNo = new AtomicLong(RandomUtil.getInt(1, 100));

    @PostConstruct
    public void init() {
        for (int i=1; i<=10; i++) {
            Category category = new Category();
            category.setNum(i);
            category.setDesc("类目_" + i);
            categories.add(category);
        }

        for (int i=1; i<=20; i++) {
            String prefix = "111";
            String suffix = String.format("%06d", i);
            String mchNo = prefix + suffix;

            Merchant merchant = new Merchant();
            merchant.setMchNo(mchNo);
            merchant.setName("商户_" + suffix);
            merchants.add(merchant);
        }

        for (int i=1; i<=6; i++) {
            User user = new User();
            user.setId(Long.valueOf(i));
            user.setExpressAddress("广东省深圳市福田区xxx路xxx号xxx房_" + i);
            users.add(user);
        }

        for (int i=1; i<=100; i++) {
            int intBit = RandomUtil.getInt(5, 100);//整数位
            int floatBit = RandomUtil.getInt(0, 10);//小数位
            String priceStr = intBit + "." + floatBit;

            int category = categories.get(RandomUtil.getInt(0, categories.size()-1)).getNum();
            String mchNo = merchants.get(RandomUtil.getInt(0, merchants.size()-1)).getMchNo();

            Product product = new Product();
            product.setId(Long.valueOf(i));
            product.setName("商品_" + i);
            product.setPrice(new BigDecimal(priceStr));
            product.setCategory(category);
            product.setMchNo(mchNo);
            products.add(product);
        }
    }

    public String getTableName(){
        return "tbl_order";
    }

    /**
     * https://www.elastic.co/guide/en/elasticsearch/reference/7.6/mapping-types.html
     * @return
     */
    public Map<String, Map<String, Object>> getOrderMappingProp() {
        Map<String,Object> dateTimeFormat = new HashMap<>();//时间格式，包含：年月日时分秒
        dateTimeFormat.put("type", "date");
        dateTimeFormat.put("format", "yyyy-MM-dd HH:mm:ss||epoch_millis");

        Map<String,Object> dateFormat = new HashMap<>();//日期格式，包含：年月日
        dateFormat.put("type", "date");
        dateFormat.put("format", "yyyy-MM-dd||epoch_millis");

        Map<String,Object> floatField = new HashMap<>();//浮点型数据，解决精度问题
        floatField.put("type", "scaled_float");
        floatField.put("scaling_factor", "100");

        //ik分词器，分为 ik_max_word、ik_smart 两种模式，前者分词粒度小，分词数量多，后者分词力度较大，分词数量较少
        Map<String,Object> ikAnalyzer = new HashMap<>();
        ikAnalyzer.put("type", "text");
        ikAnalyzer.put("analyzer", "ik_smart");//创建索引时使用的分词器，与search_analyzer最好保持一致
        ikAnalyzer.put("search_analyzer", "ik_smart");//检索时使用的分词器，与analyzer最好保持一致

        Map<String, Map<String, Object>> properties = new HashMap<>();
        properties.put("id", Collections.singletonMap("type", "long"));
        properties.put("createTime", dateTimeFormat);
        properties.put("createDate", dateFormat);
        properties.put("version", Collections.singletonMap("type", "integer"));
        properties.put("orderNo", Collections.singletonMap("type", "keyword"));//使用keyword类型，搜索时不会执行分词，而直接精确匹配
        properties.put("productId", Collections.singletonMap("type", "long"));
        properties.put("productName", ikAnalyzer);
        properties.put("productCategory", Collections.singletonMap("type", "short"));
        properties.put("price", floatField);
        properties.put("quantity", Collections.singletonMap("type", "short"));
        properties.put("amount", floatField);
        properties.put("mchNo", Collections.singletonMap("type", "keyword"));//使用keyword类型，搜索时不会执行分词，而直接精确匹配
        properties.put("userId", Collections.singletonMap("type", "long"));
        properties.put("expressAddress", ikAnalyzer);
        properties.put("payStatus", Collections.singletonMap("type", "byte"));
        return properties;
    }

    public Order createOrder(){
        Product product = products.get(RandomUtil.getInt(0, products.size()-1));
        User user = users.get(RandomUtil.getInt(0, users.size()-1));

        Order order = new Order();
        order.setId(orderId.incrementAndGet());
        order.setCreateTime(new Date());
        order.setCreateDate(DateUtil.convertDate(new Date()));
        order.setVersion(RandomUtil.getInt(1, 6));
        String suffix = String.format("%08d", orderNo.incrementAndGet());
        order.setOrderNo("OR" + suffix);
        order.setProductId(product.getId());
        order.setProductName(product.getName());
        order.setProductCategory(product.getCategory());
        order.setPrice(product.getPrice());
        order.setQuantity(RandomUtil.getInt(1, 5));
        order.setAmount(AmountUtil.mul(order.getPrice(), BigDecimal.valueOf(order.getQuantity())));
        order.setMchNo(product.getMchNo());
        order.setUserId(user.getId());
        order.setExpressAddress(user.getExpressAddress());
        order.setPayStatus(RandomUtil.getInt(1, 4));
        return order;
    }
}
