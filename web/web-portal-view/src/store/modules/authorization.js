const state = {
	list: new Array()
};

const getters = {
	checkAuthorization: state => flag => {
		return state.list.some(e => e.permissionFlag === flag);
	}
};

const mutations = {
	SET_AUTHORIZATION: (state, list) => {
		state.list = [...list];
	}
};

const actions = {
	setAuthorization({commit}, list) {
		return new Promise(resolve => {
			commit('SET_AUTHORIZATION', list);
			resolve();
		});
	}
};

export default {
	state,
	getters,
	mutations,
	actions
};
