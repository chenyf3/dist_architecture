package com.xpay.demo.sharding.dao;

import com.xpay.common.service.dao.MyBatisDao;
import com.xpay.demo.sharding.entity.Product;
import org.springframework.stereotype.Repository;

@Repository
public class ProductDao extends MyBatisDao<Product, Long> {

}
