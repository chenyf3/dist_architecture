import axios from '../axios';

export const listAllAuth = () => {
    return axios.request({
        method: 'post',
        url: '/portalAuth/listAllAuth'
    });
};

export const listAuthTree = () => {
    return axios.request({
        method: 'post',
        url: '/portalAuth/listAuthTree'
    });
};

export const addAuth = (reqData) => {
    return axios.request({
        data: reqData,
        method: 'post',
        url: '/portalAuth/addAuth'
    });
};

export const editAuth = (reqData) => {
    return axios.request({
        data: reqData,
        method: 'post',
        url: '/portalAuth/editAuth'
    });
};

export const deleteAuth = (authId) => {
    return axios.request({
        method: 'post',
        url: `/portalAuth/deleteAuth?authId=${authId}`
    });
};

export const listRevokeAuth = (reqData) => {
    return axios.request({
        method: 'post',
        data: reqData,
        url: '/portalAuth/listRevokeAuth'
    });
};

export const doRevokeAuth = (revokeId) => {
    return axios.request({
        method: 'post',
        url: `/portalAuth/doRevokeAuth?revokeId=${revokeId}`
    });
};

