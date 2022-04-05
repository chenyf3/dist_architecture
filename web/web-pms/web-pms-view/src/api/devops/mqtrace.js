import axios from '../axios';

export const listMQTrace = reqData => {
    return axios.request({
        method: 'post',
        url: '/devops/listMQTrace',
        data: reqData
    });
};

export const getTopicInfo = () => {
    return axios.request({
        method: 'get',
        url: '/devops/getTopicInfo'
    });
};

export const resendOriMsg = (recordId) => {
    return axios.request({
        method: 'post',
        url: `/devops/resendOriMsg?recordId=${recordId}`
    });
};

export const resendOriMsgBatch = (recordIdStr) => {
    return axios.request({
        method: 'post',
        url: `/devops/resendOriMsgBatch?recordIdStr=${recordIdStr}`
    });
};

export const sendCompensate = (reqData) => {
    return axios.request({
        method: 'post',
        url: '/devops/sendCompensate',
        data: reqData
    });
};

