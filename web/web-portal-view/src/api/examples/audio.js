import axios from '../axios';

/**
 * 把文本内容转换成音频(base64之后的)
 * @param {*} oldPwd 旧密码
 * @param {*} newPwd 新密码
 * @param {*} confirmPwd 确认新密码
 */
export const transferAudio = (reqData) => {
  return axios.request({
    method: 'post',
    url: '/examples/audio/transferAudio',
    data: reqData
  });
};