import axios from '../axios';

export const listProductPage = (reqData) => {
    return axios.request({
        method: 'post',
        url: '/baseConfig/listProductPage',
        data: reqData
    });
};

export const addProduct = (reqData) => {
    return axios.request({
        method: 'post',
        url: '/baseConfig/addProduct',
        data: reqData
    });
};

export const editProduct = (productId, remark) => {
    return axios.request({
        method: 'get',
        url: `/baseConfig/editProduct?productId=${productId}&remark=${remark}`,
    });
};

export const listProductOpenPage = (reqData) => {
    return axios.request({
        method: 'post',
        url: '/baseConfig/listProductOpenPage',
        data: reqData
    });
};

export const addProductOpen = (reqData) => {
    return axios.request({
        method: 'post',
        url: '/baseConfig/addProductOpen',
        data: reqData
    });
};

export const searchMerchant = (mchName) => {
    return axios.request({
        method: 'get',
        url: `/baseConfig/searchMerchant?mchName=${mchName}`,
    });
};

export const editProductOpen = (reqData) => {
    return axios.request({
        method: 'post',
        url: '/baseConfig/editProductOpen',
        data: reqData
    });
};
