import store from '@/store'
import router from '@/router'
import {resetRouter} from '@/router'
import {removeToken} from '@/tools/token'

export function logout(){
    removeToken()
    resetRouter()
    store.dispatch('signedInInfo/resetSignedInInfo')
    router.replace({path: `/login?redirect=${router.fullPath}`})
}
