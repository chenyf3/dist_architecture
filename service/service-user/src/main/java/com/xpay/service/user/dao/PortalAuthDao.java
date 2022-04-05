package com.xpay.service.user.dao;

import com.xpay.common.service.dao.MyBatisDao;
import com.xpay.common.statics.exception.BizException;
import com.xpay.common.utils.StringUtil;
import com.xpay.service.user.entity.PortalAuth;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Author: Cmf
 * Date: 2019/10/22
 * Time: 20:22
 * Description:
 */
@Repository
public class PortalAuthDao extends MyBatisDao<PortalAuth, Long> {

    public List<PortalAuth> listByRoleIds(List<Long> roleIds, String sortColumn) {
        if (roleIds == null || roleIds.size() == 0) {
            return Collections.emptyList();
        }
        if(StringUtil.isEmpty(sortColumn)){
            sortColumn = "NUMBER ASC";
        }
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put(SORT_COLUMNS, sortColumn);
        paramMap.put("roleIds", roleIds);
        return super.listBy("listByRoleIds", paramMap);
    }

    public List<PortalAuth> listByParentId(Long parentId) {
        if (parentId == null) {
            throw new BizException(BizException.PARAM_INVALID, "parentId不能为null");
        }
        return super.listBy("listByParentId", Collections.singletonMap("parentId", parentId));
    }

    public PortalAuth getByPermissionFlag(String permissionFlag){
        Map<String, Object> map = new HashMap<>();
        map.put("permissionFlag", permissionFlag);
        return getOne(map);
    }

    public String getBrotherMaximalNumber(Long parentId){
        return getOne("getBrotherMaximalNumber", parentId);
    }
}
