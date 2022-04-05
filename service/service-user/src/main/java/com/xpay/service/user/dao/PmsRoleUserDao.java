package com.xpay.service.user.dao;

import com.xpay.common.service.dao.MyBatisDao;
import com.xpay.service.user.entity.PmsRoleUser;
import org.springframework.stereotype.Repository;

import java.util.*;

/**
 *
 */
@Repository
public class PmsRoleUserDao extends MyBatisDao<PmsRoleUser, Long> {

    /**
     * 根据操作员ID查找该操作员关联的角色.
     *
     * @param userId .
     * @return list .
     */
    public List<PmsRoleUser> listByUserId(Long userId) {
        if (userId == null) {
            return new ArrayList<>();
        }
        return super.listBy(Collections.singletonMap("userId", userId));
    }

    /**
     * 根据角色ID查找该操作员关联的操作员.
     *
     * @param roleId
     * @return
     */
    public List<PmsRoleUser> listByRoleId(Long roleId) {
        if (roleId == null) {
            return new ArrayList<>();
        }
        return super.listBy(Collections.singletonMap("roleId", roleId));

    }

    /**
     * 根据操作员ID删除与角色的关联记录.
     *
     * @param userId .
     */
    public void deleteByUserId(long userId) {
        super.deleteBy("deleteByUserId", userId);
    }

    /**
     * 根据角色ID删除操作员与角色的关联关系.
     *
     * @param roleId .
     */
    public void deleteByRoleId(long roleId) {
        super.deleteBy("deleteByRoleId", roleId);
    }

    /**
     * 根据角色ID和操作员ID删除关联数据(用于更新操作员的角色).
     *
     * @param roleId     角色ID.
     * @param userId     用户ID.
     */
    public void deleteByRoleIdAndUserId(long roleId, long userId) {
        Map<String, Object> paramMap = new HashMap<String, Object>();
        paramMap.put("roleId", roleId);
        paramMap.put("userId", userId);
        super.deleteBy("deleteByRoleIdAndUserId", paramMap);
    }
}
