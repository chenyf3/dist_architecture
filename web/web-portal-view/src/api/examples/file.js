import axios from '../axios';

/**
 * 获取图片记录
 * @param {*} oldPwd 旧密码
 * @param {*} newPwd 新密码
 * @param {*} confirmPwd 确认新密码
 */
export const getImageRecord = () => {
  return axios.request({
    url: '/examples/file/getImageRecord',
    method: 'post',
  });
};

/**
 * 删除图片
 * @param fileNameEnc
 * @returns {*}
 */
export const deleteImage = (fileNameEnc) => {
  return axios.request({
    url: `/examples/file/deleteImage?fileNameEnc=${fileNameEnc}`,
    method: 'get',
  });
};