import axios from '../axios';

export const listMailReceiver = reqData => {
    return axios.request({
        method: 'post',
        url: '/baseConfig/listMailReceiver',
        data: reqData
    });
};

export const getMailSender = () => {
    return axios.request({
        method: 'get',
        url: '/baseConfig/getMailSender',
    });
};

export const addMailReceiver = (reqData) => {
    return axios.request({
        method: 'post',
        url: '/baseConfig/addMailReceiver',
        data: reqData
    });
};

export const editMailReceiver = (reqData) => {
    return axios.request({
        method: 'post',
        url: '/baseConfig/editMailReceiver',
        data: reqData
    });
};

export const deleteMailReceiver = (recordId) => {
    return axios.request({
        method: 'post',
        url: `/baseConfig/deleteMailReceiver?recordId=${recordId}`
    });
};
