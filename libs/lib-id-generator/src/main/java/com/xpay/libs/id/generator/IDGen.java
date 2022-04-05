package com.xpay.libs.id.generator;

import com.xpay.libs.id.common.IdGenException;

import java.util.ArrayList;
import java.util.List;

public interface IDGen {

    /**
     * 获取单个id，id最大值为long型数字的最大值
     * @param key
     * @return
     * @throws IdGenException
     */
    Long get(String key) throws IdGenException;

    /**
     * 获取单个id，并指定id的最大值
     * @param key
     * @param maxValue
     * @return
     * @throws IdGenException
     */
    Long get(String key, long maxValue) throws IdGenException;

    /**
     * 批量获取id，id最大值为long型数字的最大值
     * @param key
     * @param count
     * @return
     * @throws IdGenException
     */
    default List<Long> get(String key, int count) throws IdGenException {
        List<Long> ids = new ArrayList<>(count);
        for (int i=0; i<count; i++) {
            Long id = this.get(key);
            ids.add(id);
        }
        return ids;
    }

    boolean init();

    void destroy();
}
