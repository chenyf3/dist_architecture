import axios from '../axios';

/**
 * 修改登录密码
 * @param {*} oldPwd 旧密码
 * @param {*} newPwd 新密码
 * @param {*} confirmPwd 确认新密码
 */
export const changeLoginPwd = reqData => {
  return axios.request({
    method: 'post',
    url: '/user/changePwd',
    data: reqData
  });
};

/**
 * 获取修改支付密码的短信验证码
 */
export const sendTradePwdCode = () => {
  return axios.request({
    method: 'post',
    url: '/merchantSecurity/sendTradePwdCode'
  });
};

/**
 * 修改支付密码
 * @param {*} oldPwd 旧密码
 * @param {*} newPwd 新密码
 * @param {*} confirmPwd 确认密码
 * @param {*} smsCode 短信验证码
 */
export const changeTradePwd = reqData => {
  return axios.request({
    method: 'post',
    url: '/merchantSecurity/changeTradePwd',
    data: reqData
  });
};

/**
 * 重置支付密码
 * @param reqData
 * @returns {Promise<AxiosResponse<any>>}
 */
export const resetTradePwd = reqData => {
  return axios.request({
    method: 'post',
    url: '/merchantSecurity/resetTradePwd',
    data: reqData
  });
};

/**
 * 获取商户公钥
 * @returns {Promise<AxiosResponse<any>>}
 */
export const getSecretPublicKey = () => {
  return axios.request({
    url: 'merchantSecurity/getSecretPublicKey'
  });
};

/**
 * 发送修改公钥的短信验证码
 * @returns {Promise<AxiosResponse<any>>}
 */
export const sendChangeSecKeyCode = () => {
  return axios.request({
    method: 'post',
    url: '/merchantSecurity/sendChangeSecKeyCode'
  });
};

/**
 * 修改公钥
 * @param reqData
 * @returns {Promise<AxiosResponse<any>>}
 */
export const changeSecretKey = (reqData) => {
  return axios.request({
    method: 'post',
    url: '/merchantSecurity/changeSecretKey',
    data: reqData
  });
};