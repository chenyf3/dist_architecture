import axios from '../axios';

export const listUser = (reqData) => {
    return axios.request({
        data: reqData,
        method: 'post',
        url: '/portalUser/listUser'
    });
};

export const getUserById = (userId) => {
    return axios.request({
        method: 'get',
        url: `/portalUser/getUserById?userId=${userId}`
    });
};

export const userInfo = (userId) => {
    return axios.request({
        method: 'get',
        url: `/portalUser/userInfo?userId=${userId}`
    });
};

export const changeStatus = (userId) => {
    return axios.request({
        method: 'post',
        url: `/portalUser/changeStatus?userId=${userId}`
    });
};

export const searchMerchant = (mchName) => {
    return axios.request({
        method: 'get',
        url: `/portalUser/searchMerchant?mchName=${mchName}`
    });
};

export const addAdminUser = (reqData) => {
    return axios.request({
        data: reqData,
        method: 'post',
        url: '/portalUser/addAdminUser'
    });
};

export const editUser = (reqData) => {
    return axios.request({
        data: reqData,
        method: 'post',
        url: '/portalUser/editUser'
    });
};

export const assignAdminRoles = (userId, roleIds) => {
    return axios.request({
        data: {userId: userId, roleIds: roleIds},
        method: 'post',
        url: '/portalUser/assignAdminRoles'
    });
};

export const listOperateLog = (redData) => {
    return axios.request({
        data: redData,
        method: 'post',
        url: '/portalUser/listOperateLog'
    });
};


