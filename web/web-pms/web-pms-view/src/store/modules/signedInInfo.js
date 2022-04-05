/**
 * 保存用户登陆后的信息，包活：
 *  1、用户id、姓名、邮箱等基础信息；
 *  2、数据字典；
 *  3、用户允许访问的路由、菜单、权限标识等
 */
const state = {
    userInfo: {},
    dictionary: [],
    permitRoutes: [],
    menuTree: [],
    permitFlags: [],
    isInfoReady: false,
}

const mutations = {
    SET_USER_INFO: (state, userInfo) => {
        state.userInfo = userInfo
    },
    SET_DICTIONARY: (state, dictionary) => {
        state.dictionary = dictionary
        sessionStorage.setItem('dictionary', JSON.stringify(dictionary))
    },
    SET_PERMIT_ROUTES: (state, permitRoutes) => {
        state.permitRoutes = permitRoutes
    },
    SET_MENU_TREE: (state, menuTree) => {
        state.menuTree = menuTree
    },
    SET_PERMIT_FLAGS: (state, permitFlags) => {
        state.permitFlags = permitFlags
    },
    SET_INFO_READY: (state, isReady) => {
        state.isInfoReady = isReady
    }
}

const actions = {
    saveSignedInInfo({commit}, signedInInfo){
        return new Promise(resolve => {
            const {userInfo, dictionary, routeInfo} = signedInInfo
            const {accessedRoutes, accessedMenus, accessPermits} = routeInfo

            commit('SET_USER_INFO', userInfo)
            commit('SET_DICTIONARY', dictionary)
            commit('SET_PERMIT_ROUTES', accessedRoutes)
            commit('SET_MENU_TREE', accessedMenus)
            commit('SET_PERMIT_FLAGS', accessPermits)
            commit('SET_INFO_READY', true)
            resolve(true)
        })
    },
    resetSignedInInfo({commit, dispatch}) {
        return new Promise(resolve => {
            commit('SET_USER_INFO', {})
            commit('SET_DICTIONARY', {})
            commit('SET_PERMIT_ROUTES', [])
            commit('SET_MENU_TREE', [])
            commit('SET_PERMIT_FLAGS', [])
            commit('SET_INFO_READY', false)

            // reset visited views and cached views
            // to fixed https://github.com/PanJiaChen/vue-element-admin/issues/2485
            dispatch('tagsView/delAllViews', null, {root: true})
            resolve(true)
        })
    }
}

export default {
    namespaced: true,
    state,
    mutations,
    actions
}
