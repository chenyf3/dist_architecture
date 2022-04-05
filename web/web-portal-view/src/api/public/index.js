import axios from '../axios';
import { getToken } from '@/tools/token';
const TOKEN = getToken();

/**
 * 忘记密码
 * @param {*} loginName
 * @param {*} type
 */
export const sendForgetLoginPwd = reqData => {
  return axios.request({
    method: 'get',
    url: `/user/forgetLoginPwdCode?${reqData}`
  });
};

/**
 * 找回密码
 * @param {*} loginName
 * @param {*} code
 * @param {*} newPwd
 * @param {*} confirmPwd
 */
export const retrieveLoginPwd = reqData => {
  return axios.request({
    method: 'post',
    url: '/user/retrieveLoginPwd',
    data: reqData
  });
};

/**
 * 获取用户菜单
 */
export const getMenuData = () => {
  return axios.request({
    method: 'get',
    url: `/user/info?token=${TOKEN}`
  });
};

/**
 * 获取产品开通权限
 */
export const getProductPermit = () => {
  return axios.request({
    method: 'get',
    url: '/product/getProductPermit'
  });
};
