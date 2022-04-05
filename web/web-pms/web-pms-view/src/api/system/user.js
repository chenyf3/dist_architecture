import axios from '../axios';

/**
 * 获取用户信息
 * @param token
 * @returns {*}
 */
export function getUserInfo() {
    return axios.request({
        method: 'get',
        url: '/user/self'
    })
}

/**
 * 获取已登录后的信息
 * @returns {*}
 */
export function getSignedInInfo(){
    return axios.request({
        method: 'post',
        url: '/user/getSignedInInfo'
    })
}

export const listUser = (reqData) => {
    return axios.request({
        data: reqData,
        method: 'post',
        url: '/user/listUser'
    });
};

export const getUserById = (userId) => {
    return axios.request({
        method: 'get',
        url: `/user/getUserById?userId=${userId}`
    });
};

export const changeUserStatus = (userId) => {
    return axios.request({
        method: 'get',
        url: `/user/changeUserStatus?userId=${userId}`
    });
};

export const addUser = (reqData) => {
    return axios.request({
        data: reqData,
        method: 'post',
        url: '/user/addUser'
    });
};

export const editUser = (reqData) => {
    return axios.request({
        data: reqData,
        method: 'post',
        url: '/user/editUser'
    });
};

export const deleteUser = (userId) => {
    return axios.request({
        method: 'get',
        url: `/user/deleteUser?userId=${userId}`
    });
};

export const assignRoles = (userId, roleIds) => {
    return axios.request({
        data: {userId: userId, roleIds: roleIds},
        method: 'post',
        url: '/user/assignRoles'
    });
};

export const resetUserPwd = (userId, newPwd) => {
    return axios.request({
        data: {userId: userId, newPwd: newPwd},
        method: 'post',
        url: '/user/resetUserPwd'
    });
};

export const changePwd = (oldPwd, newPwd, confirmPwd) => {
    return axios.request({
        data: {oldPwd: oldPwd, newPwd: newPwd, confirmPwd: confirmPwd},
        method: 'post',
        url: '/user/changePwd'
    });
};

export const listOperateLog = (redData) => {
    return axios.request({
        data: redData,
        method: 'post',
        url: '/user/listOperateLog'
    });
};

