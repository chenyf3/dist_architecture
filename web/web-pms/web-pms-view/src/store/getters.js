const getters = {
    sidebar: state => state.app.sidebar,
    size: state => state.app.size,
    device: state => state.app.device,
    visitedViews: state => state.tagsView.visitedViews,
    cachedViews: state => state.tagsView.cachedViews,
    errorLogs: state => state.errorLog.logs,

    signedInInfo: state => state.signedInInfo,
}

export default getters
