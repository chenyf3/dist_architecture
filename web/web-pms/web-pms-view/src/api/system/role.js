import axios from '../axios';

export const listRole = (reqData) => {
    return axios.request({
        data: reqData,
        method: 'post',
        url: '/role/listRole'
    });
};

export const listAllRoles = () => {
    return axios.request({
        method: 'get',
        url: '/role/listAllRoles'
    });
};

export const addRole = (reqData) => {
    return axios.request({
        data: reqData,
        method: 'post',
        url: '/role/addRole'
    });
};

export const editRole = (reqData) => {
    return axios.request({
        data: reqData,
        method: 'post',
        url: '/role/editRole'
    });
};

export const deleteRole = (roleId) => {
    return axios.request({
        method: 'post',
        url: `/role/deleteRole?roleId=${roleId}`
    });
};

export const listRoleAuth = (roleId) => {
    return axios.request({
        method: 'get',
        url: `/role/listRoleAuth?roleId=${roleId}`
    });
};

export const assignRoleAuth = (roleId, authIds) => {
    return axios.request({
        data: authIds,
        method: 'post',
        url: `/role/assignRoleAuth?roleId=${roleId}`
    });
};

