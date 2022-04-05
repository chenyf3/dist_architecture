import axios from '@/api/axios';

export const listMerchant = (reqData) => {
	return axios.request({
		method: 'post',
		url: '/merchant/listMerchantPage',
		data: reqData
	});
};

export const addMerchant = (reqData) => {
	return axios.request({
		method: 'post',
		url: '/merchant/addMerchant',
		data: reqData
	});
};

export const getMerchantInfo = (mchNo) => {
	return axios.request({
		method: 'get',
		url: `/merchant/getMerchantInfo?mchNo=${mchNo}`,
	});
};

export const getTradePwdResetInfo = (mchNo) => {
    return axios.request({
        method: 'get',
        url: `/merchant/getTradePwdResetInfo?mchNo=${mchNo}`,
    });
}

export const sendTradePwdResetCode = (mchNo) => {
	return axios.request({
		method: 'get',
		url: `/merchant/sendTradePwdResetCode?mchNo=${mchNo}`,
	});
};
