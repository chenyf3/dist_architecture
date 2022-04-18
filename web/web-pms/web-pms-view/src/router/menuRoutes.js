/**
 * 菜单路由对象，要在点击菜单的时候需要渲染的路由，此对象的属性名与后端配置菜单的url相同
 */
export const menuRoutes = {
    '/system/userManage': () => import('@/views/system/user/UserList'),
    '/system/userOperateLog': () => import('@/views/system/user/OperateLogList'),
    '/system/roleManage': () => import('@/views/system/role/RoleList'),
    '/system/authManage': () => import('@/views/system/auth/AuthList'),
    '/devops/scheduleJobManage': () => import('@/views/devops/timer/ScheduleJobList'),
    '/devops/mqTraceManage': () => import('@/views/devops/mqtrace/MQTraceList'),
    '/devops/publishRecordManage': () => import('@/views/devops/publish/PublishRecordList'),
    '/merchant/base/merchantManage': () => import('@/views/merchant/base/MerchantList'),
    '/merchant/notify/notifyRecord': () => import('@/views/merchant/notify/NotifyRecordList'),
    '/portal/portalAuthManage': () => import('@/views/portal/auth/AuthList'),
    '/portal/portalRoleManage': () => import('@/views/portal/role/RoleList'),
    '/portal/portalUserManage': () => import('@/views/portal/user/UserList'),
    '/portal/portalOperateLog': () => import('@/views/portal/user/OperateLogList'),
    '/portal/portalAuthRevoke': () => import('@/views/portal/auth/RevokeAuthList'),
    '/baseConfig/productManage': () => import('@/views/base-config/product/ProductList'),
    '/baseConfig/productOpenManage': () => import('@/views/base-config/product/ProductOpenList'),
    '/baseConfig/mailManage': () => import('@/views/base-config/mail/MailGroupList'),
}
