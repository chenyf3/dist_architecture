import axios from '../axios';

/**
 * 获取操作员列表
 * @param reqData
 * @param {*} loginName 登录名
 * @param {*} realName 姓名
 * @param {*} status 状态
 * @param pageData
 * @param {*} pageCurrent 当前页码
 * @param {*} pageSize 页面容量
 */
export const listUser = (reqData) => {
  return axios.request({
    method: 'post',
    url: `/user/listUser`,
    data: reqData
  });
};

/**
 * 添加操作员
 * @param {*} loginName 登录名
 * @param {*} loginPwd 登录密码
 * @param {*} realName 用户名
 * @param {*} mobileNo  手机号
 * @param {*} email 邮箱
 * @param {*} remark 备注
 * @param {*} roleIds 分配ID
 */
export const addUser = reqData => {
  return axios.request({
    method: 'post',
    url: '/user/addUser',
    data: reqData
  });
};

/**
 * 编辑操作员
 * @param {*} id 用户ID
 * @param {*} realName 用户名
 * @param {*} mobileNo  手机号
 * @param {*} email 邮箱
 * @param {*} remark 备注
 * @param {*} roleIds 分配ID
 */
export const editUser = reqData => {
  return axios.request({
    method: 'post',
    url: '/user/editUser',
    data: reqData
  });
};

/**
 * 查看操作员信息
 * @param {*} userId 操作员Id
 */
export const getUserById = userId => {
  return axios.request({
    method: 'get',
    url: `/user/getUserById?userId=${userId}`
  });
};

/**
 * 删除操作员信息
 * @param {*} userId 操作员Id
 */
export const deleteUser = userId => {
  return axios.request({
    method: 'get',
    url: `/user/deleteUser?userId=${userId}`
  });
};

/**
 * 更改用户状态
 * @param {*} userId 操作员Id
 */
export const changeUserStatus = userId => {
  return axios.request({
    method: 'get',
    url: `/user/changeUserStatus?userId=${userId}`
  });
};

/**
 * 重置操作员密码
 * @param {*} userId 操作员Id
 * @param {*} newPwd 新密码
 */
export const resetUserPwd = reqData => {
  return axios.request({
    method: 'post',
    url: '/user/resetUserPwd',
    data: reqData
  });
};

/**
 * 为用户分配角色
 * @param {*} id 操作员Id
 * @param {*} roleIds 角色ids
 */
export const assignRoles = reqData => {
  return axios.request({
    method: 'post',
    url: '/user/assignRoles',
    data: reqData
  });
};

// 角色管理
/**
 * 获取角色列表
 * @param reqData
 * @param {*} roleType 登录名
 * @param {*} roleName 姓名
 * @param pageData
 * @param {*} pageCurrent 当前页码
 * @param {*} pageSize 页面容量
 */
export const listRole = (reqData) => {
  return axios.request({
    method: 'post',
    url: `/role/listRole`,
    data: reqData
  });
};

/**
 * 获取所有角色
 */
export const listAllRoles = () => {
  return axios.request({
    method: 'get',
    url: '/role/listAllRoles'
    // data: reqData
  });
};

/**
 * 添加角色列表
 * @param {*} roleName 角色名称
 * @param {*} remark 角色描述
 */
export const addRole = reqData => {
  return axios.request({
    method: 'post',
    url: '/role/addRole',
    data: reqData
  });
};

/**
 * 编辑角色列表
 * @param {*} roleType 登录名
 * @param {*} roleName 姓名
 */
export const editRole = reqData => {
  return axios.request({
    method: 'post',
    url: '/role/editRole',
    data: reqData
  });
};

/**
 * 删除角色列表
 * @param {*} roleId 角色ID
 */
export const deleteRole = roleId => {
  return axios.request({
    method: 'post',
    url: `/role/deleteRole?roleId=${roleId}`
  });
};

/**
 *  获取角色关联权限
 * @param {*} roleId 角色ID
 */
export const listRoleAuth = roleId => {
  return axios.request({
    method: 'get',
    url: `/role/listRoleAuth?roleId=${roleId}`
  });
};

/**
 * 分配权限
 * @param {*} roleId 角色ID
 * @param {*} authIds 权限id数组
 */
export const assignRoleAuth = (roleId, authIds) => {
  return axios.request({
    method: 'post',
    url: `/role/assignRoleAuth?roleId=${roleId}`,
    data: authIds
  });
};

/**
 * 权限列表
 */
export const listAllAuth = () => {
  return axios.request({
    method: 'get',
    url: '/auth/listAllAuth'
  });
};

/**
 * 权限树
 */
export const listAllAuthTree = () => {
  return axios.request({
    method: 'get',
    url: '/auth/listAuthTree'
  });
};

// 操作员日志
/**
 * 查询操作员日志
 * @param reqData
 * @param {} createTimeBegin
 * @param {} createTimeEnd
 * @param {} operateType
 * @param {} loginName
 * @param params
 * @param {} pageCurrent
 * @param {} pageSize
 */
export const listOperateLog = (reqData, params) => {
  return axios.request({
    method: 'post',
    url: `/user/listOperateLog?${params}`,
    data: reqData
  });
};
