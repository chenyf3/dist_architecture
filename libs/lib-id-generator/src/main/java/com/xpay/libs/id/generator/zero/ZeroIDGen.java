package com.xpay.libs.id.generator.zero;

import com.xpay.libs.id.generator.IDGen;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;

public class ZeroIDGen implements IDGen {
    private Logger logger = LoggerFactory.getLogger(ZeroIDGen.class);

    public ZeroIDGen(){}
    public ZeroIDGen(String msg){
        logger.info(msg);
    }

    public Long get(String key) {
        return 0L;
    }

    @Override
    public List<Long> get(String key, int count) {
        return Collections.singletonList(0L);
    }

    @Override
    public Long get(String key, long maxValue) {
        return 0L;
    }

    public boolean init() {
        return true;
    }

    public void destroy() {

    }
}
