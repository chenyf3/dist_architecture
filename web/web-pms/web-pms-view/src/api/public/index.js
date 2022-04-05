import axios from '../axios';
import {getToken} from '@/tools/token';

const TOKEN = getToken();

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
 * 获取图形验证码
 * @param {} phone
 */
export const getImgVerifyCode = (oldCodeKey) => {
    return axios.request({
        method: 'get',
        url: `/user/imgVerifyCode?oldCodeKey=${oldCodeKey}`
    });
};

/**
 * 登出
 */
export const logoutSys = () => {
    return axios.request({
        method: 'get',
        url: `/user/logout?token=${TOKEN}`
    });
};

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
 * 获取数据字典
 */
export const getDictionary = () => {
    return axios.request({
        method: 'post',
        url: '/dictionary/listAllDictionary'
    });
};

/**
 * 获取产品枚举
 * @returns {AxiosPromise<any>}
 */
export const getProductInfo = () => {
    return axios.request({
        method: 'get',
        url: '/signInPublic/getProductInfo'
    });
};
