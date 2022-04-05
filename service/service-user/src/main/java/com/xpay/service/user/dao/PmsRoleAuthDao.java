package com.xpay.service.user.dao;

import com.xpay.common.service.dao.MyBatisDao;
import com.xpay.service.user.entity.PmsRoleAuth;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Author: Cmf
 * Date: 2019/10/10
 * Time: 14:59
 * Description:
 */
@Repository
public class PmsRoleAuthDao extends MyBatisDao<PmsRoleAuth, Long> {

    /**
     * 删除角色的所有功能关联
     *
     * @param roleId 角色id
     */
    public void deleteByRoleId(Long roleId) {
        super.deleteBy("deleteByRoleId", roleId);
    }

    public void deleteByAuthId(Long authId) {
        super.deleteBy("deleteByAuthId", authId);
    }

    public void deleteByAuthIds(List<Long> authIds) {
        super.deleteBy("deleteByAuthIdList", authIds);
    }
}