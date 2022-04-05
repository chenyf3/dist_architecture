package com.xpay.service.user.dao;

import com.xpay.common.service.dao.MyBatisDao;
import com.xpay.common.utils.StringUtil;
import com.xpay.service.user.entity.PmsUser;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Description: 操作员表数据访问层接口实现类
 */
@Repository
public class PmsUserDao extends MyBatisDao<PmsUser, Long> {

    /**
     * 根据操作员登录名获取操作员信息.
     *
     * @param loginName     登录名
     */
    public PmsUser findByLoginName(String loginName) {
        if (StringUtil.isEmpty(loginName)) {
            return null;
        }
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("loginName", loginName);
        return getOne(paramMap);
    }

    /**
     * 查询角色关联的操作员
     * @param roleId    角色id
     */
    public List<PmsUser> listByRoleId(long roleId) {
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("roleId", roleId);
        return super.listBy("listByRoleId", paramMap);
    }
}
