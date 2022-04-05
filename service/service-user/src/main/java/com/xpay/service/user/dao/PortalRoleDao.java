package com.xpay.service.user.dao;

import com.xpay.common.service.dao.MyBatisDao;
import com.xpay.common.statics.enums.user.RoleTypeEnum;
import com.xpay.common.statics.exception.BizException;
import com.xpay.common.utils.StringUtil;
import com.xpay.service.user.entity.PortalRole;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Description: 角色表数据访问层接口实现类.
 */
@Repository
public class PortalRoleDao extends MyBatisDao<PortalRole, Long> {

    /**
     * 列出所有角色，以供添加操作员时选择.
     *
     * @return roleList .
     */
    public List<PortalRole> listAll() {
        return super.listAll();
    }


    /**
     * 获取某个商户所有的角色
     *
     * @param merchantNo .
     * @return .
     */
    public List<PortalRole> listRoleByMerchantNo(String merchantNo) {
        if (StringUtil.isEmpty(merchantNo)) {
            throw new BizException(BizException.PARAM_INVALID, "merchantNo不能为空");
        }
        return super.listBy(Collections.singletonMap("mchNo", merchantNo));
    }

    /**
     * 获取所有的商户管理员角色
     * @param mchType   商户类型，如果此参数不为null，则只会查出该商户类型下的所有管理员角色
     * @return
     */
    public List<PortalRole> listAllAdminRoles(Integer mchType) {
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("roleType", RoleTypeEnum.ADMIN.getValue());
        if(mchType != null) {
            paramMap.put("mchType", mchType);
        }
        return super.listBy(paramMap);
    }

    /**
     * 根据角色名称获取角色记录（用于判断角色名是否已存在）.
     *
     * @param roleName 角色名.
     * @return PortalRole.
     */
    public PortalRole getByRoleName(String roleName) {
        if (roleName == null) {
            return null;
        }
        return super.getOne(Collections.singletonMap("roleName", roleName));
    }
}
