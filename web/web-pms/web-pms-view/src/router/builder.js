//页面组件名称和实例的映射
import Layout from '@/layout';

/**
 * 构造前端的组件路由，参考：https://router.vuejs.org/zh/api/#router-%E6%9E%84%E5%BB%BA%E9%80%89%E9%A1%B9
 * @param userAuths
 * @param menuRoutes
 * @returns {any}
 */
export const routeBuilder = function(userAuths, menuRoutes) {
    const menuRoute = { path: '/root', component: Layout, children: [] }
    userAuths.filter(f => f.authType === 1).forEach(f => {
        menuRoute.children.push({
            path: f.url,
            component: menuRoutes[f.url], //组件名称
            // name: f.name,//不设置name，避免重名时vue-router会报错，tagsView中需要使用的话从meta.title中获取
            meta: {id:f.id, title:f.name, icon:f.icon, affix:false, permissionFlag:f.permissionFlag}
        })
    })
    return [menuRoute]
}

/**
 * 构建菜单树
 * @param userFunctions
 * @returns {[]}
 */
export const menuBuilder = function(userAuths) {
    const pidGroup = {}
    userAuths.filter(f => f.authType === 1).forEach(f => {
        if (!pidGroup[f.parentId]) {
            pidGroup[f.parentId] = [f]
        } else {
            pidGroup[f.parentId].push(f)
        }
    })

    const resultArr = []
    buildMenuTree(0, pidGroup, resultArr)
    return resultArr
}

export const permitBuilder = function(userAuths){
    let permits = []
    userAuths.forEach(f => {
        permits.push(f.permissionFlag)
    })
    return permits
}

function buildMenuTree(pid, pidGroup, resultArr) {
    const group = pidGroup[pid]
    if (!group) {
        return
    }
    group.forEach((p) => {
        const menu = {}
        menu.meta = { title: p.name, icon: p.icon }
        menu.path = p.url
        if (pidGroup[p.id]) {
            menu.children = []
            buildMenuTree(p.id, pidGroup, menu.children)
        }
        resultArr.push(menu)
    })
}
