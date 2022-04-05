import axios from 'axios';
import {Message} from 'element-ui';
import router from '@/router'
import {getToken} from '@/tools/token';
import {logout} from '@/tools/logout';

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
        const {config, data: res} = response;

        if (res.code === 200) {
            return res;
        } else if (res.code === 201) {
            Message.error({
                message: res.msg || '登录凭证无效或已过期，请重新登录',
                type: 'error',
                duration: 1000,
                center: true
            });
            logout()//清除登录信息并跳转到登录页面
            return Promise.reject('登录凭证无效或已过期，请重新登录');
        } else if (res.code === 202) {
            Message.error({
                message: res.msg || '业务异常',
                type: 'error',
                duration: 3000,
                center: true
            });
            return Promise.reject('Error');
        } else if (res.code === 203) {
            Message.error({
                message: res.msg || '业务警告',
                type: 'warning',
                duration: 3000,
                center: true
            });
            return Promise.reject('Error');
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
            Message.error({
                message: res.msg || '返回参数格式错误',
                type: 'error',
                duration: 3000,
                center: true
            });
            return Promise.reject(res.msg || 'Error');
        }
    },
    error => {
        const status = error.response && error.response.status ? error.response.status : undefined
        const respData = error.response && error.response.data ? error.response.data : undefined
        let message = error.message ? error.message : '请求无响应'

        if (status === 401 || status === 404) {
            router.push(`/${status}`)
        } else if (message.includes('timeout')) {
            Message.error({
                message: '请求超时',
                type: 'error',
                duration: 2000,
                center: true
            });
        } else if (message.includes('Network Error')) {
            Message.error({
                message: '服务器无响应或网络连接错误',
                type: 'error',
                duration: 2000,
                center: true
            });
        } else {
            if (respData && respData.msg) {
                message = respData.msg
            }
            Message.error({
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
