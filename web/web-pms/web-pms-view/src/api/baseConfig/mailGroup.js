import axios from '../axios';

export const listMailGroup = reqData => {
    return axios.request({
        method: 'post',
        url: '/baseConfig/listMailGroup',
        data: reqData
    });
};

export const getMailSender = () => {
    return axios.request({
        method: 'get',
        url: '/baseConfig/getMailSender',
    });
};

export const addMailGroup = (reqData) => {
    return axios.request({
        method: 'post',
        url: '/baseConfig/addMailGroup',
        data: reqData
    });
};

export const editMailGroup = (reqData) => {
    return axios.request({
        method: 'post',
        url: '/baseConfig/editMailGroup',
        data: reqData
    });
};

export const deleteMailGroup = (recordId) => {
    return axios.request({
        method: 'post',
        url: `/baseConfig/deleteMailGroup?recordId=${recordId}`
    });
};
