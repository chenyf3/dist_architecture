package com.xpay.service.user.dao;

import com.xpay.common.service.dao.MyBatisDao;
import com.xpay.common.statics.exception.BizException;
import com.xpay.common.utils.StringUtil;
import com.xpay.service.user.entity.PmsAuth;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class PmsAuthDao extends MyBatisDao<PmsAuth, Long> {

    public List<PmsAuth> listByRoleIds(List<Long> roleIds, String sortColumn) {
        if (roleIds == null || roleIds.size() == 0) {
            return Collections.emptyList();
        }
        if(StringUtil.isEmpty(sortColumn)){
            sortColumn = "NUMBER ASC";
        }
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("roleIds", roleIds);
        paramMap.put(SORT_COLUMNS, sortColumn);
        return super.listBy("listByRoleIds", paramMap);
    }

    public List<PmsAuth> listByParentId(Long parentId) {
        if (parentId == null) {
            throw new BizException(BizException.BIZ_INVALID, "parentId不能为null");
        }
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("parentId", parentId);
        paramMap.put(SORT_COLUMNS, "NUMBER ASC");
        return super.listBy("listByParentId", paramMap);
    }

    public PmsAuth getByPermissionFlag(String permissionFlag){
        Map<String, Object> map = new HashMap<>();
        map.put("permissionFlag", permissionFlag);
        return getOne(map);
    }

    public String getBrotherMaximalNumber(Long parentId){
        return getOne("getBrotherMaximalNumber", Collections.singletonMap("parentId", parentId));
    }
}
