package com.xpay.starter.sharding.utils;

import com.xpay.starter.sharding.properties.DBRule;
import com.xpay.starter.sharding.properties.GroupRule;
import com.xpay.starter.sharding.properties.TableRule;

import java.util.*;

/**
 * sharding工具类，根据当前id、分库规则、分表规则，计算出当前id应该分到哪个库、哪张表
 * @author chenyf
 */
public class ShardingRuleUtil {
    private final static Map<String, List<GroupRule>> GROUP_RULES = new LinkedHashMap<>();
    private final static Map<String, Map<String, Integer>> GROUP_MAP_TOTAL_TABLE_NUM = new LinkedHashMap<>();//表名:分组名:当前分组下的总表数
    private final static Map<String, Map<String, Map<Integer, DBRule>>> GROUP_HASH_VAL_MAP_DB_RULE = new LinkedHashMap<>();//表名:分组名:hash值:库规则

    /**
     * 初始化表的分片规则
     * @param groupRules
     */
    public static void initShardingRule(Map<String, List<GroupRule>> groupRules){
        for(Map.Entry<String, List<GroupRule>> entry : groupRules.entrySet()){
            GROUP_RULES.put(entry.getKey(), entry.getValue());
        }
        initRules();
    }

    /**
     * 获取指定逻辑表的分组规则
     * @param logicTable
     * @param groupName
     * @return
     */
    public static GroupRule getGroupRule(String logicTable, String groupName){
        for(GroupRule groupRule : GROUP_RULES.get(logicTable)){
            if(groupRule.getName().equals(groupName)){
                return groupRule;
            }
        }
        return null;
    }

    /**
     * 定位分组规则
     * @param logicTable
     * @param currId
     * @return
     */
    public static GroupRule matchGroupRule(String logicTable, Long currId){
        GroupRule currGroup = null;
        for(GroupRule groupRule : GROUP_RULES.get(logicTable)){
            if(isBetween(currId, groupRule.getStartId(), groupRule.getEndId())){
                currGroup = groupRule;
                break;
            }
        }
        return currGroup;
    }

    /**
     * 定位库
     * @param logicTable
     * @param currId
     * @return
     */
    public static DBRule matchDBRule(String logicTable, Long currId){
        GroupRule currGroup = matchGroupRule(logicTable, currId);
        int totalTableNum = GROUP_MAP_TOTAL_TABLE_NUM.get(logicTable).get(currGroup.getName());
        int hashValue = (int) (currId % totalTableNum);
        DBRule dbRule = GROUP_HASH_VAL_MAP_DB_RULE.get(logicTable).get(currGroup.getName()).get(hashValue);
        return dbRule;
    }

    /**
     * 定位表
     * @param logicTable
     * @param currId
     * @return
     */
    public static TableRule matchTableRule(String logicTable, Long currId){
        DBRule currDb = matchDBRule(logicTable, currId);
        TableRule currTable = null;

        if(DBRule.Mode.RANGE.equals(currDb.getMode())){
            for(TableRule tableRule : currDb.getTableRules()){
                if(isBetween(currId, tableRule.getStartId(), tableRule.getEndId())){
                    currTable = tableRule;
                    break;
                }
            }
        }else if(DBRule.Mode.MOD.equals(currDb.getMode())){
            int idx = (int) (currId % currDb.getTableRules().size());
            currTable = currDb.getTableRules().get(idx);
        }
        return currTable;
    }

    private static void initRules(){
        for(Map.Entry<String, List<GroupRule>> entry : GROUP_RULES.entrySet()){
            String logicTable = entry.getKey();
            List<GroupRule> groupRules = entry.getValue();

            Map<String, Integer> groupMapTotalNum = new HashMap<>();//分组名:表总数量
            Map<String, Map<Integer, DBRule>> groupMapDBRule = new HashMap<>();//分组名:hash值:db规则
            for(GroupRule groupRule : groupRules){
                int groupTableCount = 0;//当前分组下的表数量
                Map<Integer, DBRule> hashValueMapDbRule = new HashMap<>();//当前分组下每一个hash值对应的DB

                for(DBRule dbRule : groupRule.getDbRules()){
                    //如果没有配置表的规则，则进行计算
                    if(dbRule.getTableRules() == null || dbRule.getTableRules().isEmpty()){
                        dbRule.calcTableRules(logicTable, groupRule.getStartId(), groupRule.getEndId(), groupRule.getPerTableRow());
                    }

                    groupTableCount += dbRule.getTableRules().size();
                    String[] hashValues = dbRule.getHashValue().split(DBRule.HASH_SEPARATOR);
                    for(String hashValue : hashValues){
                        hashValueMapDbRule.put(Integer.valueOf(hashValue), dbRule);
                    }
                }
                groupMapTotalNum.put(groupRule.getName(), groupTableCount);
                groupMapDBRule.put(groupRule.getName(), hashValueMapDbRule);
            }

            GROUP_MAP_TOTAL_TABLE_NUM.put(logicTable, groupMapTotalNum);
            GROUP_HASH_VAL_MAP_DB_RULE.put(logicTable, groupMapDBRule);
        }
    }

    private static boolean isBetween(Long currId, Long startId, Long endId){
        return startId <= currId && (currId <= endId || endId == -1);
    }
}
