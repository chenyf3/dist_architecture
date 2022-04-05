// 首页交易折线图
import axios from '../axios';

export const getTradeDataStatics = reqData => {
  return axios.request({
    method: 'post',
    url: '/tradeInfo/getTradeDataStatics',
    data: reqData
  });
};
