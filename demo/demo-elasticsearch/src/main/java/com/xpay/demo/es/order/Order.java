package com.xpay.demo.es.order;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 订单表的实体类
 */
public class Order implements Serializable {
    /**
     CREATE TABLE `tbl_order` (
     `ID` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '自增主键',
     `CREATE_TIME` datetime NOT NULL COMMENT '创建时间',
     `CREATE_DATE` date NOT NULL COMMENT '创建日期',
     `VERSION` int NOT NULL COMMENT '版本号',
     `ORDER_NO` char(32) NOT NULL COMMENT '订单号',
     `PRODUCT_ID` bigint NOT NULL COMMENT '商品ID',
     `PRODUCT_NAME` varchar(256) NOT NULL COMMENT '商品名称',
     `PRODUCT_CATEGORY` int NOT NULL COMMENT '商品类目',
     `PRICE` decimal(18,2) NOT NULL COMMENT '价格',
     `QUANTITY` smallint NOT NULL COMMENT '数量',
     `AMOUNT` decimal(18,2) NOT NULL COMMENT '总额',
     `MCH_NO` varchar(32) NOT NULL COMMENT '商家编号',
     `USER_ID` bigint NOT NULL COMMENT '用户ID',
     `EXPRESS_ADDRESS` varchar(512) NOT NULL COMMENT '物流配送地址',
     `PAY_STATUS` tinyint(1) NOT NULL COMMENT '支付状态(1=未支付 2=已支付 3=已取消 4=已退款)',
     PRIMARY KEY (`ID`)
     ) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='商品订单表';
     */
    private Long id;
    private Date createTime;
    private Date createDate;
    private Integer version;
    private String orderNo;
    private Long productId;
    private String productName;
    private Integer productCategory;
    private BigDecimal price;
    private Integer quantity;
    private BigDecimal amount;
    private String mchNo;
    private Long userId;
    private String expressAddress;
    private Integer payStatus;

    private BigDecimal fee;//用以模拟新增的字段

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public String getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public Integer getProductCategory() {
        return productCategory;
    }

    public void setProductCategory(Integer productCategory) {
        this.productCategory = productCategory;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getMchNo() {
        return mchNo;
    }

    public void setMchNo(String mchNo) {
        this.mchNo = mchNo;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getExpressAddress() {
        return expressAddress;
    }

    public void setExpressAddress(String expressAddress) {
        this.expressAddress = expressAddress;
    }

    public Integer getPayStatus() {
        return payStatus;
    }

    public void setPayStatus(Integer payStatus) {
        this.payStatus = payStatus;
    }

    public BigDecimal getFee() {
        return fee;
    }

    public void setFee(BigDecimal fee) {
        this.fee = fee;
    }
}
