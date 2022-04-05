import axios from '../axios';

export const listAllAuth = () => {
    return axios.request({
        method: 'post',
        url: '/auth/listAllAuth'
    });
};

export const listAuthTree = () => {
    return axios.request({
        method: 'post',
        url: '/auth/listAuthTree'
    });
};

export const addAuth = (reqData) => {
    return axios.request({
        data: reqData,
        method: 'post',
        url: '/auth/addAuth'
    });
};

export const editAuth = (reqData) => {
    return axios.request({
        data: reqData,
        method: 'post',
        url: '/auth/editAuth'
    });
};

export const deleteAuth = (authId) => {
    return axios.request({
        method: 'post',
        url: `/auth/deleteAuth?authId=${authId}`
    });
};



