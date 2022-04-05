const state = {
	userId: null,
	username: '',
	loginName: '', // 登录名
	mobileNo: '', // 手机号
	email: '', // 邮箱
	mchNo: '', // 商户号
	mchType: null, // 商户类型 1:收款商户 2:集团商户
	type: null, // 操作员类型（1:商户管理员，2:商户操作员）
	status: null // 状态
};

// const mutationsType = {
//   SET_USERINFO_BY_KEY: 'SET_USERINFO_BY_KEY'
// }

const getters = {
	getUserInfoByKey: state => key => {
		return state[key];
	}
};

const mutations = {
	SET_USERINFO: (state, data) => {
		state = Object.assign(state, data);
	}
};

const actions = {
	setUserInfo({commit}, data) {
		return new Promise(resolve => {
			commit('SET_USERINFO', data);
			resolve({data});
		});
	}
};

export default {
	state,
	getters,
	mutations,
	actions
};
