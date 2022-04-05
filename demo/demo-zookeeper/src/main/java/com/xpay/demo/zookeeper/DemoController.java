package com.xpay.demo.zookeeper;

import com.xpay.starter.plugin.client.ZKClient;
import com.xpay.starter.plugin.plugins.DistributedLock;
import jakarta.validation.constraints.NotNull;
import org.apache.curator.framework.api.CuratorWatcher;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.WatchedEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/demo")
public class DemoController {
    @Autowired
    ZKClient zkClient;
    @Autowired
    DistributedLock zookeeperLock;

    @RequestMapping("create")
    public String create(@NotNull String path, String data){
        return zkClient.createNode(path.startsWith("/") ? path : "/"+path, data, CreateMode.PERSISTENT);
    }

    @RequestMapping("update")
    public String update(@NotNull String path, String data){
        boolean isOk = zkClient.updateNode(path.startsWith("/") ? path : "/"+path, data);
        return String.valueOf(isOk);
    }

    @RequestMapping("getData")
    public String getData(@NotNull String path){
        return zkClient.getData(path.startsWith("/") ? path : "/"+path);
    }

    @RequestMapping("getChildren")
    public List<String> getChildren(@NotNull String path){
        return zkClient.getChildren(path.startsWith("/") ? path : "/"+path);
    }

    @RequestMapping("addWatch")
    public void addWatch(@NotNull String path) throws Exception {
        zkClient.addWatch(path.startsWith("/") ? path : "/" + path, new CuratorWatcher() {
            @Override
            public void process(WatchedEvent event) throws Exception {
                System.out.println("addWatch path: " + event.getPath() + " eventType: " + event.getWrapper().getType());
            }
        });
    }

    @RequestMapping("addChildrenWatch")
    public void addChildrenWatch(@NotNull String path) throws Exception {
        zkClient.addChildrenWatch(path.startsWith("/") ? path : "/" + path, new CuratorWatcher() {
            @Override
            public void process(WatchedEvent event) throws Exception {
                System.out.println("addChildrenWatch path: " + event.getPath() + " eventType: " + event.getWrapper().getType());
            }
        });
    }

    @RequestMapping("delete")
    public String delete(@NotNull String path){
        zkClient.deleteNode(path.startsWith("/") ? path : "/"+path);
        return "ok";
    }

    @RequestMapping("tryLock")
    public String tryLock(String lockName){
        Object lock = zookeeperLock.tryLock(lockName, 60000, 60000);
        if(lock == null){
            return "fail";
        }else{
            zookeeperLock.unlock(lock);
            return "ok";
        }
    }
}
