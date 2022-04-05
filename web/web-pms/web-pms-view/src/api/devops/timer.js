import axios from '../axios';

export const listScheduleJob = (reqData) => {
    return axios.request({
        method: 'post',
        url: '/devops/listScheduleJob',
        data: reqData
    });
};

export const addJob = (reqData) => {
    return axios.request({
        method: 'post',
        url: '/devops/addJob',
        data: reqData
    });
};

export const editJob = (reqData) => {
    return axios.request({
        method: 'post',
        url: '/devops/editJob',
        data: reqData
    });
};

export const pauseJob = (jobGroup, jobName) => {
    return axios.request({
        method: 'post',
        url: `/devops/pauseJob?jobGroup=${jobGroup}&jobName=${jobName}`,
    });
};

export const resumeJob = (jobGroup, jobName) => {
    return axios.request({
        method: 'post',
        url: `/devops/resumeJob?jobGroup=${jobGroup}&jobName=${jobName}`,
    });
};

export const triggerJob = (jobGroup, jobName) => {
    return axios.request({
        method: 'post',
        url: `/devops/triggerJob?jobGroup=${jobGroup}&jobName=${jobName}`,
    });
};

export const deleteJob = (jobGroup, jobName) => {
    return axios.request({
        method: 'post',
        url: `/devops/deleteJob?jobGroup=${jobGroup}&jobName=${jobName}`,
    });
};

export const notifyJob = (jobGroup, jobName) => {
    return axios.request({
        method: 'post',
        url: `/devops/notifyJob?jobGroup=${jobGroup}&jobName=${jobName}`,
    });
};

export const listInstance = (reqData) => {
    return axios.request({
        method: 'post',
        url: '/devops/listInstance',
        data: reqData
    });
};

export const adminInstance = (instanceId) => {
    return axios.request({
        method: 'post',
        url: `/devops/adminInstance?instanceId=${instanceId}`,
    });
};

export const listTimerOpLog = (reqData) => {
    return axios.request({
        method: 'post',
        url: '/devops/listTimerOpLog',
        data: reqData
    });
};
