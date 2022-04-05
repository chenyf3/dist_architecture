import axios from '@/api/axios';

export const listMchNotifyPage = (reqData) => {
	return axios.request({
		method: 'post',
		url: '/mchNotify/listMchNotifyPage',
		data: reqData
	});
};

export const getNotifyRecord = (recordId) => {
	return axios.request({
		method: 'get',
		url: `/mchNotify/getNotifyRecord?recordId=${recordId}`,
	});
};

export const notifyLog = (recordId) => {
	return axios.request({
		method: 'get',
		url: `/mchNotify/notifyLog?recordId=${recordId}`,
	});
};

export const notifyAgain = (recordId) => {
	return axios.request({
		method: 'get',
		url: `/mchNotify/notifyAgain?recordId=${recordId}`,
	});
};

export const notifyAgainBatch = (recordIdStr) => {
    return axios.request({
        method: 'get',
        url: `/mchNotify/notifyAgainBatch?recordIdStr=${recordIdStr}`,
    });
};
