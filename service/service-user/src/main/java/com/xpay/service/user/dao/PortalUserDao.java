package com.xpay.service.user.dao;

import com.xpay.common.service.dao.MyBatisDao;
import com.xpay.common.utils.StringUtil;
import com.xpay.service.user.entity.PortalUser;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Description: 操作员表数据访问层接口实现类
 */
@Repository
public class PortalUserDao extends MyBatisDao<PortalUser, Long> {

    /**
     * 根据操作员登录名获取操作员信息
     * @param loginName
     * @return
     */
    public PortalUser findByLoginName(String loginName) {
        if (StringUtil.isEmpty(loginName)) {
            return null;
        }
        return getOne(Collections.singletonMap("loginName", loginName));
    }

    public List<PortalUser> listByRoleId(long roleId) {
        return super.listBy("listByRoleId", Collections.singletonMap("roleId", roleId));
    }

    public List<Long> listUserIdByMap(String mchNo, Map<String, Object> paramMap){
        if(paramMap == null){
            paramMap = new HashMap<>();
        }
        paramMap.put("mchNo", mchNo);
        return listBy("listUserIdByMap", paramMap);
    }

    public List<String> listMchNoByUserIdList(List<Long> userIdList){
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("userIdList", userIdList);
        return listBy("listMchNoByUserIdList", paramMap);
    }
}
