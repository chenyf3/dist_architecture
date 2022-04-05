package com.xpay.libs.id.service;

import com.xpay.libs.id.common.Utils;
import com.xpay.libs.id.config.SnowFlakeProperties;
import com.xpay.libs.id.common.IdGenException;
import com.xpay.libs.id.generator.IDGen;
import com.xpay.libs.id.generator.zero.ZeroIDGen;
import com.xpay.libs.id.generator.snowflake.SnowflakeIDGenImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class SnowflakeService {
    private final static String JVM_INSTANCE_NUM = "instanceNum";//当前实例编号
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final static long twepoch = 1603882462498L;//雪花算法的起始时间：2020-10-28 18:54:22，初次运行前可修改，一旦确定就不要修改，避免出现id重复
    private IDGen idGen;
    private final SnowFlakeProperties snowFlake;

    public SnowflakeService(SnowFlakeProperties snowFlake){
        this.snowFlake = snowFlake;
        init();
    }

    public Long getId() throws IdGenException {
        return idGen.get("key");
    }

    public List<Long> getId(int count) throws IdGenException {
        return idGen.get("key", count);
    }

    public void destroy() {
        this.idGen.destroy();
    }

    private void init() {
        boolean flag = snowFlake != null && snowFlake.getEnabled();
        if (flag) {
            String instanceId = getInstanceId();
            String clusterName = snowFlake.getClusterName();

            idGen = new SnowflakeIDGenImpl(instanceId, twepoch, clusterName, snowFlake);

            if (idGen.init()) {
                logger.info("Snowflake Service Init Successfully");
            } else {
                throw new RuntimeException("Snowflake Service Init Fail, cause SnowflakeIDGenImpl init fail");
            }
        } else {
            idGen = new ZeroIDGen("SnowflakeService Use ZeroIDGen!");
        }
    }

    private String getInstanceId() {
        Integer instanceNum = snowFlake.getInstanceNum();
        String instanceNumStr = System.getProperty(JVM_INSTANCE_NUM, null);
        if(instanceNumStr != null && instanceNumStr.trim().length() > 0){
            instanceNum = Integer.valueOf(instanceNumStr);
        }

        if(SnowFlakeProperties.InstanceIdType.HOST.equals(snowFlake.getInstanceIdType())){
            return Utils.getLocalHost() + ":" + instanceNum;
        }else if(SnowFlakeProperties.InstanceIdType.IP.equals(snowFlake.getInstanceIdType())){
            return Utils.getLocalIp() + ":" +  instanceNum;
        }else{
            throw new RuntimeException("Snowflake Service Init Fail, Unknown InstanceIdType: " + snowFlake.getInstanceIdType());
        }
    }
}
