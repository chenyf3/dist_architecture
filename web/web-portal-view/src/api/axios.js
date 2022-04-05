import axios from 'axios';
import { getToken, removeToken } from '../tools/token';
import { Message } from 'element-ui';

const axiosRequest = axios.create({
  baseURL: process.env.VUE_APP_BASE_API,
  timeout: 30000,
  responseType: 'json'
});

// 请求
axiosRequest.interceptors.request.use(
  config => {
    const token = getToken();
    if (token) {
      config.headers['X-TOKEN'] = token;
    }
    return config;
  },
  error => Promise.reject(error)
);

// 响应
axiosRequest.interceptors.response.use(
  response => {
    const { config, data: res } = response;

    if (res.code === 200) {
      return res;
    } else if (res.code === 201) {
      Message.error({
        message: res.msg || '登录凭证无效或已过期，请重新登录',
        type: 'error',
        duration: 1000,
        center: true
      });
      setTimeout(() => {
        removeToken();
        sessionStorage.removeItem('vuex');
        const loginPath =
          window.location.origin +
          (window.location.pathname.includes('index.html')
            ? window.location.pathname.replace('index', 'login')
            : window.location.pathname.includes('login.html')
            ? window.location.pathname
            : window.location.pathname + 'login.html') +
          '#/';
        window.location.href = loginPath;
      }, 1000);
      throw new Error('登录凭证无效或已过期，请重新登录');
    } else if (res.code === 202) {
      Message.error({
        message: res.msg || '业务异常',
        type: 'error',
        duration: 3000,
        center: true
      });
      return res;
    } else if (res.code === 203) {
      Message.error({
        message: res.msg || '业务警告',
        type: 'warning',
        duration: 3000,
        center: true
      });
      return res;
    } else if (res.code === 204) {
      Message.error({
        message: res.msg || '无访问权限',
        type: 'error',
        duration: 3000,
        center: true
      });
      return Promise.reject('Error');
    } else if (res.code === 205) {
      Message.error({
        message: res.msg || '系统异常',
        type: 'error',
        duration: 3000,
        center: true
      });
      return Promise.reject('Error');
    } else if (!res.code) {
      return config.responseType === 'arraybuffer' ? response : res;
    } else {
      Message.error(res.msg || 'Error');
      return Promise.reject(res.msg || 'Error');
    }
  },
  error => {
    const status = error.response && error.response.status ? error.response.status : undefined
    const respData = error.response && error.response.data ? error.response.data : undefined
    let message = error.message ? error.message : '请求无响应'

    if (status === 401 || status === 404) {
      Message({
        message: status === 401 ? '无访问权限！' : '请求路径不存在！',
        type: 'error',
        duration: 2000,
        center: true
      });
    } else if (message.includes('timeout')) {
      Message({
        message: '请求超时',
        type: 'error',
        duration: 2000,
        center: true
      });
    } else if (message.includes('Network Error')) {
      Message({
        message: '服务器无响应或网络连接错误',
        type: 'error',
        duration: 2000,
        center: true
      });
    } else {
      if (respData && respData.msg) {
        message = respData.msg
      }
      Message({
        message: message,
        type: 'error',
        duration: 2000,
        center: true
      });
    }
    return Promise.reject(error);
  }
);

export default axiosRequest;
