package com.xpay.starter.plugin.client;

import com.xpay.starter.plugin.properties.ZookeeperProperties;
import com.xpay.starter.plugin.util.Utils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.api.CuratorWatcher;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.apache.curator.framework.recipes.locks.InterProcessSemaphoreMutex;
import org.apache.curator.retry.RetryNTimes;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 操作zookeeper的客户端
 * @author chenyf
 */
public class ZKClient {
    private CuratorFramework client;

    public ZKClient(ZookeeperProperties zkProperties) {
        CuratorFrameworkFactory.Builder builder = CuratorFrameworkFactory.builder();
        builder.connectString(zkProperties.getUrls())
                .sessionTimeoutMs(zkProperties.getSessionTimeoutMs())
                .connectionTimeoutMs(zkProperties.getConnectionTimeoutMs())
                .retryPolicy(new RetryNTimes(zkProperties.getMaxRetry(), zkProperties.getRetryIntervalMs()));

        if (Utils.isNotEmpty(zkProperties.getUsername()) && Utils.isNotEmpty(zkProperties.getPassword())) {
            String authenticationString = zkProperties.getUsername() + ":" + zkProperties.getPassword();
            builder.authorization("digest", authenticationString.getBytes());
        }

        client = builder.build();
        client.start();
    }

    /**
     * 创建节点
     * @param path
     * @param value
     * @param mode
     * @return
     */
    public String createNode(String path, String value, CreateMode mode) {
        try {
            path = paddingPath(path);
            if(checkExists(path)){
                throw new RuntimeException("zk节点已存在 path=" + path);
            }

            String opResult;
            if (Utils.isEmpty(value)) { //节点数据为空，不为节点添加数据
                opResult = client.create().creatingParentsIfNeeded().withMode(mode).forPath(path);
            } else { //节点数据不为空，则设置节点的数据值
                opResult = client.create().creatingParentsIfNeeded().withMode(mode).forPath(path, value.getBytes(StandardCharsets.UTF_8));
            }
            return opResult;
        } catch(Exception e) {
            throw new RuntimeException("添加zk节点异常 path=" + path, e);
        }
    }

    /**
     * 更新节点数据
     * @param path
     * @param value
     * @return
     */
    public boolean updateNode(String path, String value) {
        try {
            path = paddingPath(path);
            if(! checkExists(path)){
                throw new RuntimeException("zk节点不存在 path=" + path);
            }

            //存在就开始更新节点数据
            Stat returnResult = client.setData().forPath(path, value != null ? value.getBytes(StandardCharsets.UTF_8) : null);
            return returnResult != null;
        } catch (Exception e) {
            throw new RuntimeException("更新zk节点数据异常 path=" + path, e);
        }
    }

    /**
     * 删除节点
     * @param path
     */
    public void deleteNode(String path) {
        try {
            path = paddingPath(path);
            client.delete().deletingChildrenIfNeeded().forPath(path);
        } catch (Exception e) {
            throw new RuntimeException("删除zk节点异常 path=" + path, e);
        }
    }

    /**
     * 获取某个节点的数据
     * @param path
     * @return
     */
    public String getData(String path){
        try {
            path = paddingPath(path);
            byte[] data = client.getData().forPath(path);
            return data == null ? null : new String(data, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("获取zk节点数据时异常 path=" + path, e);
        }
    }

    /**
     * 获取某个节点下的所有子节点
     * @param path
     * @return
     */
    public List<String> getChildren(String path) {
        try {
            path = paddingPath(path);
            return client.getChildren().forPath(path);
        } catch (Exception e) {
            throw new RuntimeException("获取zk节点的所有子节点时异常 path=" + path, e);
        }
    }

    /**
     * 检查节点是否已存在
     * @param path
     * @return
     */
    public boolean checkExists(String path){
        try {
            path = paddingPath(path);
            Stat stat = client.checkExists().forPath(path);
            return stat != null;
        } catch (Exception e) {
            throw new RuntimeException("检查Zookeeper节点是否存在出现异常 path=" + path, e);
        }
    }

    /**
     * 为节点添加监听器
     * @param path
     * @param watcher
     * @throws Exception
     */
    public void addWatch(String path, CuratorWatcher watcher) {
        try {
            path = paddingPath(path);
            client.getData().usingWatcher(watcher).forPath(path);
        } catch (Exception e) {
            throw new RuntimeException("添加监听器时出现异常 path=" + path, e);
        }
    }

    /**
     * 监听所有子节点
     * @param path
     * @param watcher
     * @throws Exception
     */
    public void addChildrenWatch(String path, CuratorWatcher watcher) {
        try {
            path = paddingPath(path);
            client.getChildren().usingWatcher(watcher).forPath(path);
        } catch (Exception e) {
            throw new RuntimeException("添加子节点监听器时出现异常 path=" + path, e);
        }
    }

    /**
     * 分布式可重入锁，InterProcessMutex的实例对象是可重用的，外部可看情况是否需要缓存起来重用
     * @param path
     * @return
     */
    public InterProcessMutex getReentrantLock(String path){
        return new InterProcessMutex(getCuratorClient(), paddingPath(path));
    }

    /**
     * 分布式共享锁（不可重入）
     * @param path
     * @return
     */
    public InterProcessSemaphoreMutex getShareLock(String path){
        return new InterProcessSemaphoreMutex(getCuratorClient(), paddingPath(path));
    }

    public CuratorFramework getCuratorClient(){
        return client;
    }

    public void destroy(){
        if(client != null){
            client.close();
        }
    }

    private String paddingPath(String path){
        if(! path.startsWith("/")){
            path = "/" + path;
        }
        return path;
    }
}
