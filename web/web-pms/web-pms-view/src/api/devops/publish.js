import axios from '../axios';

export const publishRecordList = reqData => {
    return axios.request({
        method: 'post',
        url: '/devops/publishRecordList',
        data: reqData
    });
};

export const publish = reqData => {
    return axios.request({
        method: 'post',
        url: '/devops/publish',
        data: reqData
    });
};

export const republish = (id,relayApp,remark) => {
    return axios.request({
        method: 'get',
        url: `/devops/republish?id=${id}&relayApp=${relayApp}&remark=${remark}`,
    });
};

export const cancelPublish = (id) => {
    return axios.request({
        method: 'get',
        url: `/devops/cancelPublish?id=${id}`,
    });
};

export const audit = (id,auditStatus,remark) => {
    return axios.request({
        method: 'get',
        url: `/devops/audit?id=${id}&auditStatus=${auditStatus}&remark=${remark}`,
    });
};

export const getPublishInfo = () => {
    return axios.request({
        method: 'get',
        url: '/devops/getPublishInfo'
    });
};

export const getCurrIdcFlow = () => {
    return axios.request({
        method: 'get',
        url: '/devops/getCurrIdcFlow'
    });
};

export const flowSwitch = (toIdcs,checkPublishing) => {
    return axios.request({
        method: 'get',
        url: `/devops/flowSwitch?toIdcs=${toIdcs}&checkPublishing=${checkPublishing}`
    });
};

export const syncIdcPublish = (toIdc,publishIds,syncMsg,timerResumeIdc) => {
    return axios.request({
        method: 'get',
        url: `/devops/syncIdcPublish?toIdc=${toIdc}&publishIds=${publishIds}&syncMsg=${syncMsg}&timerResumeIdc=${timerResumeIdc}`
    });
};


