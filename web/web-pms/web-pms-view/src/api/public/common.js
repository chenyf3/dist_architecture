import axios from '../axios';

//文件下载
export const downloadFile = (fileNameEnc) => {
    return axios.request({
        url: `signInPublic/download?fileNameEnc=${fileNameEnc}`,
        method: 'get'
    });
};

//图片查看
export const viewPic = (picNameEnc) => {
    return axios.request({
        url: `signInPublic/viewPic?picNameEnc=${picNameEnc}`,
        method: 'get'
    });
};

//获取基本信息
// 获取省份
export const getProvinceList = () => {
    return axios.request({
        url: 'area/getProvinceList',
        method: 'post'
    });
};
// 获取城市
export const getCityList = province => {
    return axios.request({
        url: 'area/getCityList',
        method: 'post',
        data: province
    });
};
// 获取银行
export const getBankCodeMap = () => {
    return axios.request({
        url: 'bankInfo/getBankCodeMap',
        method: 'post'
    });
};
// 获取支行信息
export const getBankInfoList = reqData => {
    return axios.request({
        url: 'bankInfo/getBankInfoList',
        method: 'post',
        data: reqData
    });
};

// 银行卡校验
export const bankAccountNoValidate = reqData => {
    return axios.request({
        url: 'cardbin/bankAccountNoValidate',
        method: 'post',
        data: reqData
    });
};
