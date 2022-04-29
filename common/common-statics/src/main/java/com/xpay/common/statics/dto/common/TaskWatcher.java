package com.xpay.common.statics.dto.common;

/**
 * 任务观测器，用以记录任务执行过程中的一些数据
 */
public class TaskWatcher {
    private String name;
    private long begin;
    private long end;
    private int totalCount;
    private int successCount;
    private int failCount;

    public TaskWatcher(String name){
        this.name = name;
    }

    public TaskWatcher begin(){
        if(begin <= 0){
            begin = System.currentTimeMillis();
        }
        return this;
    }

    public TaskWatcher end(){
        if(end <= 0){
            end = System.currentTimeMillis();
        }
        return this;
    }

    public TaskWatcher addSuccess(int count){
        this.successCount += count;
        this.totalCount += count;
        return this;
    }

    public TaskWatcher addFail(int count){
        this.failCount += count;
        this.totalCount += count;
        return this;
    }

    public TaskWatcher addCount(int success, int fail){
        addSuccess(success);
        addFail(fail);
        return this;
    }

    public TaskWatcher reset(String name) {
        this.name = name;
        this.begin = 0;
        this.end = 0;
        this.totalCount = 0;
        this.successCount = 0;
        this.failCount = 0;
        return this;
    }

    public String toString(){
        long endTime = this.end > 0 ? this.end : System.currentTimeMillis();
        return "{" + "name:" + name + ", " +
                "totalCount:" + totalCount + ", " +
                "successCount:" + successCount + ", " +
                "failCount:" + failCount + ", " +
                "begin:" + begin + ", " +
                "end:" + endTime + ", " +
                "timeCost:" + (endTime - begin) +
                "}";
    }
}
