/**
 * Copyright (C) 2008 Happy Fish / YuQing
 * <p>
 * FastDFS Java Client may be copied only under the terms of the GNU Lesser
 * General Public License (LGPL).
 * Please visit the FastDFS Home Page https://github.com/happyfish100/fastdfs for more detail.
 */

package com.xpay.libs.fdfs.fasfdfs;

import com.xpay.libs.fdfs.common.MyException;
import com.xpay.libs.fdfs.fasfdfs.pool.Connection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Tracker server group
 *
 * @author Happy Fish / YuQing
 * @version Version 1.17
 */
public class TrackerGroup {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    public int tracker_server_index;
    public InetSocketAddress[] tracker_servers;
    protected Integer lock;
    private static Monitor monitor;

    /**
     * Constructor
     *
     * @param tracker_servers tracker servers
     */
    public TrackerGroup(InetSocketAddress[] tracker_servers) {
        this.tracker_servers = tracker_servers;
        this.lock = new Integer(0);
        this.tracker_server_index = 0;
        this.monitor = new Monitor();
    }

    /**
     * return connected tracker server
     *
     * @return connected tracker server, null for fail
     */
    public TrackerServer getTrackerServer(int serverIndex) throws IOException {
        if (monitor.isAlive(serverIndex)) {
            return new TrackerServer(this.tracker_servers[serverIndex]);
        } else {
            throw new IOException("The TrackerServer is invalid for now : " + this.tracker_servers[serverIndex].getAddress().getHostAddress());
        }
    }

    /**
     * return connected tracker server
     *
     * @return connected tracker server, null for fail
     */
    public TrackerServer getTrackerServer() {
        int current_index;

        //通过轮询方式获取tracker，达到负载均衡的目的
        synchronized (this.lock) {
            this.tracker_server_index++;
            if (this.tracker_server_index >= this.tracker_servers.length) {
                this.tracker_server_index = 0;
            }

            current_index = this.tracker_server_index;
        }
        try {
            return this.getTrackerServer(current_index);
        } catch (IOException ex) {
            logger.error("connect to server " + this.tracker_servers[current_index].getAddress().getHostAddress() + ":" + this.tracker_servers[current_index].getPort() + " fail, Message : " + ex.getMessage());
        }

        //运行到此处说明上一步获取失败了，则从头开始找，直到把所有server找完
        for (int i = 0; i < this.tracker_servers.length; i++) {
            if (i == current_index) {
                continue;
            }

            try {
                TrackerServer trackerServer = this.getTrackerServer(i);

                synchronized (this.lock) {
                    if (this.tracker_server_index == current_index) {
                        this.tracker_server_index = i;
                    }
                }

                return trackerServer;
            } catch (IOException ex) {
                logger.error("connect to server " + this.tracker_servers[current_index].getAddress().getHostAddress() + ":" + this.tracker_servers[current_index].getPort() + " fail, Message : " + ex.getMessage());
            }
        }

        return null;
    }

    public Object clone() {
        InetSocketAddress[] trackerServers = new InetSocketAddress[this.tracker_servers.length];
        for (int i = 0; i < trackerServers.length; i++) {
            trackerServers[i] = new InetSocketAddress(this.tracker_servers[i].getAddress().getHostAddress(), this.tracker_servers[i].getPort());
        }

        return new TrackerGroup(trackerServers);
    }

    /**
     * tracker server 监控器
     */
    private class Monitor {
        private int testInternal = 20;//检测tracker server的间隔
        private AtomicBoolean[] tracker_status;//每个tracker server的状态
        private ScheduledExecutorService scheduledExecutor;

        private Monitor() {
            this.tracker_status = new AtomicBoolean[tracker_servers.length];
            initMonitor();

            logger.info("TrackerServer Monitor initial finish tracker_servers");
        }

        private boolean isAlive(int serverIndex) {
            return this.tracker_status[serverIndex].get();
        }

        private void initMonitor() {
            //首先把所有tracker都设置为alive
            for (int i = 0; i < tracker_servers.length; i++) {
                this.tracker_status[i] = new AtomicBoolean(true);
            }

            //安排线程定期检测tracker是否alive
            scheduledExecutor = Executors.newScheduledThreadPool(1, r -> {
                Thread t = new Thread(r);
                t.setDaemon(true);
                return t;
            });
            scheduledExecutor.scheduleAtFixedRate(() -> {
                for (int i = 0; i < tracker_servers.length; i++) {
                    try {
                        if (tracker_status[i].get() && !testTrackerAlive(i)) {
                            tracker_status[i].compareAndSet(true, false);
                            logger.error("tracker server offline -> " + tracker_servers[i].getAddress().getHostAddress() + ":" + tracker_servers[i].getPort());
                        }
                        if (!tracker_status[i].get() && testTrackerAlive(i)) {
                            tracker_status[i].compareAndSet(false, true);
                            logger.info("tracker server recover <- " + tracker_servers[i].getAddress().getHostAddress() + ":" + tracker_servers[i].getPort());
                        }
                    } catch (Throwable e) {
                        if (tracker_status[i].get()) {
                            tracker_status[i].compareAndSet(true, false);
                            logger.error("tracker server offline -> ServerAddress: {}:{}, ExceptionMsg: {}", tracker_servers[i].getAddress().getHostAddress(), tracker_servers[i].getPort(), e.getMessage());
                        }
                    }
                }
            }, 1, testInternal, TimeUnit.SECONDS);
        }

        private boolean testTrackerAlive(int serverIndex) throws IOException, MyException {
            TrackerServer trackerServer = new TrackerServer(tracker_servers[serverIndex]);
            Connection connection = null;

            try {
                connection = trackerServer.getConnection();
                return connection.activeTest();
            } catch (IOException ex) {
                try {
                    if (connection != null) {
                        connection.close();
                    }
                } catch (IOException ex1) {
                    ex1.printStackTrace();
                } finally {
                    connection = null;
                }
                throw ex;
            } finally {
                if (connection != null) {
                    try {
                        connection.release();
                    } catch (IOException ex1) {
                        ex1.printStackTrace();
                    }
                }
            }
        }
    }
}
