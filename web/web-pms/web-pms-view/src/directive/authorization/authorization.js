import store from '@/store'

function checkPermission(el, binding) {
    const {value} = binding
    const permitFlags = store.getters.signedInInfo.permitFlags
    if (value && (!permitFlags || permitFlags.indexOf(value) < 0)) {
        Promise.resolve().then(() => {
            el.parentNode && el.parentNode.removeChild(el)
        })
    }
}

export default {
    inserted(el, binding) {
        checkPermission(el, binding)
    },
    update(el, binding) {
        checkPermission(el, binding)
    }
}
