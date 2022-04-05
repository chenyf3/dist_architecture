package com.xpay.libs.id.generator.segment.dao;

import com.xpay.libs.id.generator.segment.model.IDAlloc;

import java.util.List;

public interface IDAllocDao {

     int addIDAlloc(IDAlloc idAlloc);

     List<IDAlloc> listAllIDAlloc();

     List<String> listAllKeys();

     IDAlloc getIDAllocByKey(String key);

     IDAlloc increaseMaxIdAndGetIDAlloc(String key);

     IDAlloc increaseMaxIdAndGetIDAlloc(String key, int step);
}
