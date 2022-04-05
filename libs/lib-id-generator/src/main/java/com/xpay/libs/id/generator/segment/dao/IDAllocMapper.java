package com.xpay.libs.id.generator.segment.dao;

import com.xpay.libs.id.generator.segment.model.IDAlloc;
import org.apache.ibatis.annotations.*;

import java.util.List;

public interface IDAllocMapper {
    String TABLE_NAME = " id_alloc ";

    @Insert("INSERT INTO "+TABLE_NAME+" (biz_key,max_id,min_step) values (#{key},#{maxId},#{minStep})")
    int addIDAlloc(IDAlloc idAlloc);

    @Select("SELECT id, biz_key, max_id, min_step, update_time FROM " + TABLE_NAME)
    @Results(value = {
            @Result(column = "id", property = "id"),
            @Result(column = "biz_key", property = "key"),
            @Result(column = "max_id", property = "maxId"),
            @Result(column = "min_step", property = "minStep"),
            @Result(column = "update_time", property = "updateTime")
    })
    List<IDAlloc> listAllIDAlloc();

    @Select("SELECT biz_key FROM " + TABLE_NAME + " ORDER BY id ASC")
    List<String> listAllKeys();

    @Select("SELECT biz_key, max_id, min_step, update_time FROM "+TABLE_NAME+" WHERE biz_key = #{key}")
    @Results(value = {
            @Result(column = "id", property = "id"),
            @Result(column = "biz_key", property = "key"),
            @Result(column = "max_id", property = "maxId"),
            @Result(column = "min_step", property = "minStep"),
            @Result(column = "update_time", property = "updateTime")
    })
    IDAlloc getIDAllocByKey(@Param("key") String key);

    @Update("UPDATE "+TABLE_NAME+" SET max_id = max_id + min_step WHERE biz_key = #{key}")
    void increaseMaxId(@Param("key") String key);

    @Update("UPDATE "+TABLE_NAME+" SET max_id = max_id + #{step} WHERE biz_key = #{key}")
    void increaseMaxIdByDynamicStep(@Param("key") String key, @Param("step") int step);
}
