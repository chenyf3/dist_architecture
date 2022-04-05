import axios from '../axios';

/* ----------- 油品挂牌价管理 -------------*/
// 油品挂牌价管理 查询
export const listOilManageOilPrice = reqParams => {
  return axios.request({
    method: 'post',
    url: 'oilManage/oilPrice/list',
    data: reqParams
  });
};
// 油品挂牌价管理 添加
export const addOilManageOilPrice = reqParams => {
  return axios.request({
    method: 'post',
    url: 'oilManage/oilPrice/add',
    data: reqParams
  });
};
// 油品挂牌价管理 编辑
export const editOilManageOilPrice = reqParams => {
  return axios.request({
    method: 'post',
    url: 'oilManage/oilPrice/edit',
    data: reqParams
  });
};
// 油品挂牌价管理 删除
export const deleteOilManageOilPrice = reqParams => {
  return axios.request({
    method: 'post',
    url: 'oilManage/oilPrice/delete',
    data: reqParams
  });
};
// 油品挂牌价管理 集团查询
export const listOilManageGroup = reqParams => {
  return axios.request({
    method: 'post',
    url: 'oilManage/group/list',
    data: reqParams
  });
};
// 油品挂牌价管理 集团查看
export const viewOilManageOilPriceGroup = reqParams => {
  return axios.request({
    method: 'post',
    url: 'oilManage/oilPrice/group/view',
    data: reqParams
  });
};

/* ----------- 油枪管理 -------------*/
// 油枪管理
export const listOilManageOilGun = reqParams => {
  return axios.request({
    method: 'post',
    url: 'oilManage/oilGun/list',
    data: reqParams
  });
};
// 油枪管理
export const addOilManageOilGun = reqParams => {
  return axios.request({
    method: 'post',
    url: 'oilManage/oilGun/add',
    data: reqParams
  });
};
// 油枪管理
export const editOilManageOilGun = reqParams => {
  return axios.request({
    method: 'post',
    url: 'oilManage/oilGun/edit',
    data: reqParams
  });
};
// 油枪管理
export const deleteOilManageOilGun = reqParams => {
  return axios.request({
    method: 'post',
    url: 'oilManage/oilGun/delete',
    data: reqParams
  });
};
// 油枪管理
export const viewOilManageOilGunGroup = reqParams => {
  return axios.request({
    method: 'post',
    url: 'oilManage/oilGun/group/view',
    data: reqParams
  });
};

/* ----------- 合作渠道管理 -------------*/
/**
 * 商户渠道管理 - 查看列表
 * @param {*} pageNum
 * @param {*} numPerPage
 */
export const mchCoopChannelList = reqParams => {
  return axios.request({
    method: 'post',
    url: 'mchCoopChannel/list',
    data: reqParams
  });
};

/**
 * 获取可配置的渠道列表
 * @param {*} pageNum
 * @param {*} numPerPage
 */
export const listCoopChannel = () => {
  return axios.request({
    method: 'get',
    url: 'mchCoopChannel/listCoopChannel'
  });
};

/**
 * 添加商户渠道
 * @param {*} coopChannelCode
 * @param {*} comment
 */
export const addMchCoopChannel = reqParams => {
  return axios.request({
    method: 'post',
    url: 'mchCoopChannel/add',
    data: reqParams
  });
};

/**
 * 修改商户渠道
 * @param {*} coopChannelCode
 * @param {*} comment
 * @param {*} id
 */
export const editMchCoopChannel = reqParams => {
  return axios.request({
    method: 'post',
    url: 'mchCoopChannel/edit',
    data: reqParams
  });
};

/**
 * 获取商户渠道详情
 */
export const getMchCoopChannel = id => {
  return axios.request({
    method: 'get',
    url: `mchCoopChannel/get?id=${id}`
  });
};

/**
 * 删除商户渠道
 * @param {*} id
 */
export const deleteMchCoopChannel = id => {
  return axios.request({
    method: 'get',
    url: `mchCoopChannel/delete?id=${id}`
  });
};
