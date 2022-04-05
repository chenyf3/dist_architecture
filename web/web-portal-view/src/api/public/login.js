import axios from '../axios';
import { getToken } from '@/tools/token';
const TOKEN = getToken();

/**
 * 获取验证码
 * @param {} phone
 */
export const getImgVerifyCode = (oldCodeKey) => {
  return axios.request({
    method: 'get',
    url: `/user/imgVerifyCode?oldCodeKey=${oldCodeKey}`
  });
};

/**
 * 登录
 * @param {*} username
 * @param {*} password
 * @param {*} codeKey 缓存验证码的key，从登录码获取接口得到
 * @param {*} captcha 验证码
 */
export const loginSys = reqData => {
  return axios.request({
    method: 'post',
    url: '/user/login',
    data: reqData
  });
};

/**
 * 获取已登录用户信息
 */
export function getSignedInInfo(){
  return axios.request({
    url: '/user/getSignedInInfo',
    method: 'post'
  })
}

/**
 * 登出
 */
export const logoutSys = () => {
  return axios.request({
    method: 'get',
    url: `/user/logout?token=${TOKEN}`
  });
};
