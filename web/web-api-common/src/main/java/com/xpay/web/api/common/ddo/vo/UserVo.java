package com.xpay.web.api.common.ddo.vo;

import com.xpay.common.statics.exception.BizException;
import com.xpay.common.utils.StringUtil;
import com.xpay.common.utils.ValidateUtil;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

public class UserVo implements Serializable {
    private Long id;
    private Date createTime = new Date();
    private Integer version;
    private String loginName;// 登录名
    private String loginPwd; // 登录密码
    private String realName; // 姓名
    private String mobileNo; // 手机号
    private String email;    //邮箱
    private Integer status; // 状态
    private Integer type; // 操作员类型（1:商户管理员，2:商户操作员）
    private String mchNo; //商户编号
    private Integer mchType; //商户类型
    private String orgNo;   //集团编号
    private String creator; // 创建人
    private String modifier;// 修改者
    private String remark; // 描述
    private Integer pwdErrorCount; // 连续输错密码次数（连续5次输错就冻结帐号）

    private List<Long> roleIds;//角色Id集合
    private String otherParam;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public String getLoginName() {
        return loginName;
    }

    public void setLoginName(String loginName) {
        this.loginName = loginName;
    }

    public String getLoginPwd() {
        return loginPwd;
    }

    public void setLoginPwd(String loginPwd) {
        this.loginPwd = loginPwd;
    }

    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    public String getMobileNo() {
        return mobileNo;
    }

    public void setMobileNo(String mobileNo) {
        this.mobileNo = mobileNo;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public String getMchNo() {
        return mchNo;
    }

    public void setMchNo(String mchNo) {
        this.mchNo = mchNo;
    }

    public Integer getMchType() {
        return mchType;
    }

    public void setMchType(Integer mchType) {
        this.mchType = mchType;
    }

    public String getOrgNo() {
        return orgNo;
    }

    public void setOrgNo(String orgNo) {
        this.orgNo = orgNo;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public String getModifier() {
        return modifier;
    }

    public void setModifier(String modifier) {
        this.modifier = modifier;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public Integer getPwdErrorCount() {
        return pwdErrorCount;
    }

    public void setPwdErrorCount(Integer pwdErrorCount) {
        this.pwdErrorCount = pwdErrorCount;
    }

    public List<Long> getRoleIds() {
        return roleIds;
    }

    public void setRoleIds(List<Long> roleIds) {
        this.roleIds = roleIds;
    }

    public String getOtherParam() {
        return otherParam;
    }

    public void setOtherParam(String otherParam) {
        this.otherParam = otherParam;
    }


    /**
     * 校验输入的操作员数据
     *
     * @param userVo .
     * @throws BizException .
     */
    public static void validateUser(UserVo userVo) throws BizException {
        if (!ValidateUtil.isStrLengthValid(userVo.getRealName(), 2, 15)) {
            throw new BizException(BizException.PARAM_INVALID, "真实姓名长度必须为2--15");
        } else if (!userVo.getRealName().matches("[^\\x00-\\xff]+")) {
            throw new BizException(BizException.PARAM_INVALID, "真实姓名必须为中文");
        } else if (!ValidateUtil.isStrLengthValid(userVo.getLoginName(), 3, 50)) {
            throw new BizException(BizException.PARAM_INVALID, "登录名长度必须为3--50");
        } else if (!ValidateUtil.isMobile(userVo.getMobileNo())) {
            throw new BizException(BizException.PARAM_INVALID, "手机号无效");
        } else if (StringUtil.isNotEmpty(userVo.getEmail()) && !ValidateUtil.isEmail(userVo.getEmail())) {
            throw new BizException(BizException.PARAM_INVALID, "邮箱格式错误");
        }
    }
}
