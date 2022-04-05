package com.xpay.service.user.dao;

import com.xpay.common.service.dao.MyBatisDao;
import com.xpay.service.user.entity.PortalRoleAuth;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Author: Cmf
 * Date: 2019/10/10
 * Time: 14:59
 * Description:
 */
@Repository
public class PortalRoleAuthDao extends MyBatisDao<PortalRoleAuth, Long> {


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

    public void deleteByAuthIds(List<Long> functionIds) {
        super.deleteBy("deleteByAuthIdList", functionIds);
    }

    public void deleteByMchNoAndAuthIds(String mchNo, List<Long> functionIds) {
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("mchNo", mchNo);
        paramMap.put("authIdList", functionIds);
        super.deleteBy("deleteByMchNoAndAuthIds", paramMap);
    }

    public List<Long> listAuthIdByRoleIds(List<Long> roleIds){
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("roleIdList", roleIds);
        return listBy("listAuthIdByRoleIds", paramMap);
    }

    public List<Long> listAuthIdByMchNo(String mchNo){
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("mchNo", mchNo);
        return listBy("listAuthIdByMchNo", paramMap);
    }
}