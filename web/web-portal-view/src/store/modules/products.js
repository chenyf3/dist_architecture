const state = {
	// 一键支付
	ONE_KEY_PAY: true,
	// 收款码
	PAY_CODE: false,
	// 扫一扫
	SCAN: false,
};

const getters = {
	getProductByKey: state => key => {
		return state[key];
	}
};

const mutations = {
	SET_PRODUCTS_PERMISSION: (state, data) => {
		state = Object.assign(state, data);
	}
};

const actions = {
	setProducts({commit}, data) {
		return new Promise(resolve => {
			commit('SET_PRODUCTS_PERMISSION', data);
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
