package com.xpay.libs.id.generator.snowflake.holder;

import com.xpay.libs.id.common.IdGenException;
import com.xpay.libs.id.common.Utils;
import com.xpay.libs.id.config.SnowFlakeProperties;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.api.ACLBackgroundPathAndBytesable;
import org.apache.curator.retry.RetryUntilElapsed;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.data.ACL;
import org.apache.zookeeper.data.Id;
import org.apache.zookeeper.data.Stat;
import org.apache.zookeeper.server.auth.DigestAuthenticationProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * 使用Zookeeper来保存生成雪花算法的机器节点数据，相比起redis集群来，zookeeper集群的可靠性高
 * @author edit by chenyf
 */
public class SnowflakeZookeeperHolder {
    private static final Logger logger = LoggerFactory.getLogger(SnowflakeZookeeperHolder.class);
    private static final String PATH_SEPARATOR = "-";
    private final CuratorFramework curator;
    private String digestAuthEnc = null;
    private long lastUpdateTime;
    private final String instanceId; //实例id，要保证全局唯一，可以使用当前节点的 ip、instanceNum 来构成，比如：10.10.10.01:1
    private final int currMinWorkerId;//当前集群的最小workerId
    private final int currMaxWorkerId;//当前集群的最大workerId
    private int workerId = -1;//当前实例的workerId
    private final String pathForever; //保存雪花算法节点数据的根节点，如：/snowflake/serviceSequence/forever
    private String currNodePath; //当前实例在zk端的目录节点，比如：10.10.10.01:2050-000000001

    /**
     * 构造器
     * @param instanceId        实例id，每个应用实例唯一的id，比如：ip:port
     * @param clusterName
     * @param zkProperties
     */
    public SnowflakeZookeeperHolder(String instanceId, String clusterName, SnowFlakeProperties.Zookeeper zkProperties,
                                    Integer currMinWorkerId, Integer currMaxWorkerId) {
        this.currMinWorkerId = currMinWorkerId;
        this.currMaxWorkerId = currMaxWorkerId;
        this.instanceId = instanceId;
        this.pathForever = "/snowflake/" + clusterName + "/forever";
        String digestAuth = null;
        if (Utils.isNotEmpty(zkProperties.getUsername()) && Utils.isNotEmpty(zkProperties.getPassword())) {
            digestAuth = zkProperties.getUsername() + ":" + zkProperties.getPassword();
            try {
                this.digestAuthEnc = DigestAuthenticationProvider.generateDigest(digestAuth);
            } catch(Exception e) {
                throw new RuntimeException("zk digest 加密异常", e);
            }
        }

        String connectionString = zkProperties.getConnectionString();
        int connTimeout = zkProperties.getConnectionTimeout();
        int sessTimeout = zkProperties.getSessionTimeout();
        int maxElapsedTime = zkProperties.getMaxElapsedTime();
        int sleepMsBetweenRetries = zkProperties.getSleepMsBetweenRetries();
        RetryUntilElapsed retry = new RetryUntilElapsed(maxElapsedTime, sleepMsBetweenRetries);
        this.curator = createCuratorWithOptions(connectionString, digestAuth, connTimeout, sessTimeout, retry);
    }

    /**
     * 初始化
     * @return
     */
    public synchronized boolean init() {
        try {
            //1.初始化当前集群
            initCurrClusterIfNeed();

            //2.检查当前节点是否已存在，如果已存在，则解析出数据并校验，如果校验通过则设置到当前实例对象上
            boolean isExists = loadEndpointData();
            if (isExists) {
                logger.info("旧实例重启成功 currMinWorkerId={} currMaxWorkerId={} currWorkerId={} instanceId={} currNodePath={}",
                        this.currMinWorkerId, this.currMaxWorkerId, this.workerId, this.instanceId, this.currNodePath);
                return true;
            }

            //3、如果当前节点的数据还不存在，则为当前节点生成一个新的workerId
            initEndpointData();
            logger.info("新实例启动成功 currMinWorkerId={} currMaxWorkerId={} currWorkerId={} instanceId={} currNodePath={}",
                    this.currMinWorkerId, this.currMaxWorkerId, this.workerId, this.instanceId, this.currNodePath);
            return true;
        } catch (Exception e) {
            logger.error("Start node ERROR", e);
            return false;
        }
    }

    /**
     * 初始化当前集群
     * @throws Exception
     */
    private void initCurrClusterIfNeed() throws Exception {
        curator.start();
        Stat stat = curator.checkExists().forPath(pathForever);
        if (stat == null) { //还不存在根路径，说明当前节点是整个集群中的第一个启动节点，则需要创建根路径
            curator.create()
                    .creatingParentsIfNeeded()
                    .withMode(CreateMode.PERSISTENT)
                    .forPath(pathForever);
        }

        //没有指定当前集群的minWorkerId，则说明默认最小workerId是0
        if(currMinWorkerId < 0){
            return;
        }

        //有指定当前集群的minWorkerId，则找出当前集群下已存在的最大自增序号
        List<String> needDeletePath = new ArrayList<>();
        String initPath = "ignore";//形如：/snowflake/serviceSequence/forever/ignore
        Integer currMaxId = 0;
        List<String> keys = this.curator.getChildren().forPath(this.pathForever);
        if(keys != null && keys.size() > 0){
            for (String childPath : keys) { //childPath 形如：10.10.10.01:1-000000001 或者 ignore-0000000001
                String currNodePath = childPath;
                String[] nodePathArr = splitCurrNodePath(currNodePath);
                Integer currId = Integer.valueOf(nodePathArr[1]);
                if(currId > currMaxId){
                    currMaxId = currId;
                }
                if(initPath.equals(nodePathArr[0])){
                    needDeletePath.add(this.pathForever + "/" + currNodePath);
                }
            }
        }

        //如果集群中当前最大自增序号还小于minWorkerId，则需要让其增长到等于 minWorkerId-1 为止
        boolean isNeedInitial = false;
        String initFullPath = this.pathForever + "/" + initPath + PATH_SEPARATOR;
        ACLBackgroundPathAndBytesable<String> aclBackground = curator.create()
                .creatingParentsIfNeeded()
                .withMode(CreateMode.PERSISTENT_SEQUENTIAL);
        while (currMaxId < currMinWorkerId-1) {
            isNeedInitial = true;
            String currNodePath = null;
            if (Utils.isEmpty(digestAuthEnc)) { //不需要acl权限
                currNodePath = aclBackground.forPath(initFullPath);
            } else {
                Id user = new Id("digest", digestAuthEnc);
                currNodePath = aclBackground
                        .withACL(Collections.singletonList(new ACL(ZooDefs.Perms.ALL, user)), false)
                        .forPath(initFullPath);
            }
            String[] nodePathArr = splitCurrNodePath(currNodePath);
            currMaxId = Integer.parseInt(nodePathArr[1]);
            needDeletePath.add(currNodePath);
            Thread.sleep(5);//休眠几毫秒，避免过于频繁请求
        }

        //删除刚才创建的初始化节点数据，因为只要自增序列号被zk服务端保留下来就可以了
        needDeletePath.forEach(nodePath -> {
            try {
                this.curator.delete().forPath(nodePath);
            } catch (Exception e){
                logger.warn("用来初始化集群的节点删除失败 nodePath={} errMessage={}", nodePath, e.getMessage());
            }
        });

        if (isNeedInitial) {
            logger.info("当前集群初始化成功 currMinWorkerId={} currMaxWorkerId={} currMaxId={}", currMinWorkerId, currMaxWorkerId, currMaxId);
        } else {
            logger.info("当前集群已无需初始化 currMinWorkerId={} currMaxWorkerId={} currMaxId={}", currMinWorkerId, currMaxWorkerId, currMaxId);
        }
    }

    /**
     * 加载当前节点数据
     * @return
     * @throws Exception
     */
    private boolean loadEndpointData() throws Exception {
        String currNodePath = null;
        Integer workerId = null;
        List<String> keys = this.curator.getChildren().forPath(this.pathForever);
        if(keys == null || keys.isEmpty()){
            return false;
        }

        for (String childPath : keys) { //childPath 形如：10.10.10.01:2050-000000001
            String[] nodePathArr = splitCurrNodePath(childPath);
            String instanceId = nodePathArr[0];
            if(this.instanceId.equals(instanceId)){
                currNodePath = childPath;
                workerId = Integer.valueOf(nodePathArr[1]);
                break;
            }
        }

        if(workerId == null || workerId < 0){ //还没有当前节点的数据，则直接返回
            return false;
        }

        //已经有当前节点的数据，则用服务端的数据设置到当前实例对象中
        this.currNodePath = currNodePath;
        this.workerId = workerId;
        String fullPath = this.pathForever + "/" + this.currNodePath;
        byte[] bytes = this.curator.getData().forPath(fullPath);
        Endpoint endpoint = Utils.jsonToBean(new String(bytes), Endpoint.class);
        if(System.currentTimeMillis() < endpoint.getTimestamp()){ //当前节点的时间应该大于等于最后一次上报的时间
            throw new RuntimeException("zk服务端的时间大于当前节点的时间，请排查当前节点是否发生了时钟回拨！");
        }

        this.checkWorkerId();
        //定时上报节点数据到zk节点中
        scheduledUploadData(this.curator, fullPath);
        return true;
    }

    /**
     * 初始化当前节点的数据
     *
     * zk中的目录结构可能如下所示：
     * /snowflake/serviceSequence/forever/
     *      10.10.10.01:1-000000000
     *      10.10.10.02:1-000000001
     *      10.10.10.03:1-000000002
     *
     * @throws Exception
     */
    private void initEndpointData() throws Exception {
        ACLBackgroundPathAndBytesable<String> aclBackground = curator.create()
                .creatingParentsIfNeeded()
                .withMode(CreateMode.PERSISTENT_SEQUENTIAL);
        //currNodePath 样例如：/snowflake/serviceSequence/forever/10.10.10.01:1-000000001
        byte[] data = buildEndpointData().getBytes();
        String fullPath = pathForever + "/" + instanceId + PATH_SEPARATOR;
        if (Utils.isEmpty(digestAuthEnc)) { //不需要acl权限
            this.currNodePath = aclBackground.forPath(fullPath, data);
        } else { //需要设置acl权限
            Id user = new Id("digest", digestAuthEnc);
            this.currNodePath = aclBackground
                    .withACL(Collections.singletonList(new ACL(ZooDefs.Perms.ALL, user)), false)
                    .forPath(fullPath, data);
        }
        String[] nodePathArr = splitCurrNodePath(this.currNodePath);
        this.workerId = Integer.parseInt(nodePathArr[1]);
        this.checkWorkerId();
        //把workerId数据更新到服务端，如果保存失败，则可能会因此浪费掉一个workerId
        updateEndpointData(curator, this.currNodePath);
        //定时上报节点数据到zk节点中
        scheduledUploadData(curator, this.currNodePath);
    }

    /**
     * 定时上报当前节点数据
     * @param curator
     * @param fullPath
     */
    private void scheduledUploadData(final CuratorFramework curator, final String fullPath) {
        Executors.newSingleThreadScheduledExecutor(r -> {
            Thread thread = new Thread(r, "schedule-upload-time");
            thread.setDaemon(true);
            return thread;
        }).scheduleWithFixedDelay(() -> {
            try {
                updateEndpointData(curator, fullPath);
            } catch (Exception e){
                logger.error("节点数据上报异常", e);
            }
        }, 0, 3L, TimeUnit.SECONDS); //每3s上报数据
    }

    /**
     * 更新
     * @param curator
     * @param fullPath
     * @return
     */
    private boolean updateEndpointData(final CuratorFramework curator, final String fullPath){
        try {
            long currTime = System.currentTimeMillis();
            if (currTime < lastUpdateTime) {
                return false;
            }
            byte[] data = buildEndpointData().getBytes();
            curator.setData().forPath(fullPath, data);
            lastUpdateTime = currTime;
            return true;
        } catch (Exception e) {
            throw new RuntimeException("节点数据更新异常 path="+fullPath, e);
        }
    }

    private void checkWorkerId(){
        if(! (this.workerId >= this.currMinWorkerId && this.workerId <= currMaxWorkerId)){
            throw new IdGenException("zk分配的workerId: " + this.workerId + "必须在[" + currMinWorkerId + "," + currMaxWorkerId + "]之间");
        }
    }

    /**
     * 构建需要上传的数据，返回的数据样例为：{instanceId:10.10.10.01:2050,timestamp:1603167141012}
     * @return
     */
    private String buildEndpointData() {
        Endpoint endpoint = new Endpoint(instanceId, workerId, System.currentTimeMillis());
        return Utils.beanToJson(endpoint);
    }

    /**
     * 构建Curator客户端实例对象
     * @param connectionString
     * @param digestAuth
     * @param connectionTimeoutMs
     * @param sessionTimeoutMs
     * @param retryPolicy
     * @return
     */
    private CuratorFramework createCuratorWithOptions(String connectionString, String digestAuth, int connectionTimeoutMs,
                                                      int sessionTimeoutMs, RetryPolicy retryPolicy) {
        CuratorFrameworkFactory.Builder builder = CuratorFrameworkFactory.builder();
        if(Utils.isNotEmpty(digestAuth)){
            builder.authorization("digest", digestAuth.getBytes());
        }
        return builder.connectString(connectionString)
                .retryPolicy(retryPolicy)
                .connectionTimeoutMs(connectionTimeoutMs)
                .sessionTimeoutMs(sessionTimeoutMs)
                .build();
    }

    /**
     * 拆分节点路径成数组，currNodePath形如：10.10.10.01:2050-000000001
     * @param currNodePath
     * @return
     */
    private String[] splitCurrNodePath(String currNodePath) {
        int index = currNodePath.lastIndexOf(PATH_SEPARATOR);
        String instanceId = currNodePath.substring(0, index);
        String workerId = currNodePath.substring(index+1);
        return new String[]{instanceId, workerId};
    }

    /**
     * 获取workerId
     * @return
     */
    public int getWorkerId() {
        return this.workerId;
    }

    /**
     * 销毁当前实例
     */
    public void destroy(){
        if(this.curator != null){
            this.curator.close();
        }
    }

    /**
     * 上报数据结构
     */
    static class Endpoint {
        private String instanceId;
        private Integer workerId;
        private long timestamp;

        public Endpoint(){
        }

        public Endpoint(String instanceId, Integer workerId, long timestamp) {
            this.instanceId = instanceId;
            this.workerId = workerId;
            this.timestamp = timestamp;
        }

        public String getInstanceId() {
            return instanceId;
        }

        public void setInstanceId(String instanceId) {
            this.instanceId = instanceId;
        }

        public Integer getWorkerId() {
            return workerId;
        }

        public void setWorkerId(Integer workerId) {
            this.workerId = workerId;
        }

        public long getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(long timestamp) {
            this.timestamp = timestamp;
        }
    }
}
