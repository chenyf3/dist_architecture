import axios from '../axios';

export const listRole = (reqData) => {
    return axios.request({
        data: reqData,
        method: 'post',
        url: '/portalRole/listRole'
    });
};

export const addAdminRole = (reqData) => {
    return axios.request({
        data: reqData,
        method: 'post',
        url: '/portalRole/addAdminRole'
    });
};

export const editAdminRole = (reqData) => {
    return axios.request({
        data: reqData,
        method: 'post',
        url: '/portalRole/editAdminRole'
    });
};

export const deleteAdminRole = (roleId) => {
    return axios.request({
        method: 'post',
        url: `/portalRole/deleteAdminRole?roleId=${roleId}`
    });
};

export const listRoleAuth = (roleId) => {
    return axios.request({
        method: 'get',
        url: `/portalRole/listRoleAuth?roleId=${roleId}`
    });
};

export const assignAdminRoleAuth = (roleId, authIds) => {
    return axios.request({
        data: authIds,
        method: 'post',
        url: `/portalRole/assignAdminRoleAuth?roleId=${roleId}`
    });
};

export const listAllAdminRoles = (mchUserId) => {
    return axios.request({
        method: 'post',
        url: `/portalRole/listAllAdminRoles?mchUserId=${mchUserId}`
    });
};


