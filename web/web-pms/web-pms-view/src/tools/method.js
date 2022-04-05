import store from '@/store'
import dayjs from 'dayjs'

/**
 * 全局方法，使用方式：
 * 1、在<script />标签内使用，如：this.$dayStart(new Date())
 * 2、在html标签上使用，如：<el-table-column label="商户类型" prop="type" :formatter="row=>$dictName('MchTypeEnum', row.mchType)"/>
 */
export default {
    install(Vue /*, options*/) {
        Vue.prototype.$dayStart = function (date) {
            return date ? dayjs(date).format('YYYY-MM-DD 00:00:00') : '-'
        }
        Vue.prototype.$dayEnd = function (date) {
            return date ? dayjs(date).format('YYYY-MM-DD 23:59:59') : '-'
        }
        Vue.prototype.$dictArray = (dataName) => {
            const dictionary = store.getters.signedInInfo.dictionary
            if (dictionary && dictionary[dataName]) {
                return dictionary[dataName]
            } else {
                return []
            }
        }
        Vue.prototype.$dictItem = (dataName, dataCode) => {
            const dictionary = store.getters.signedInInfo.dictionary
            if (dictionary && dictionary[dataName]) {
                for (const item of dictionary[dataName]) {
                    if (item.code === String(dataCode)) {
                        return item
                    }
                }
            }
            return {}
        }
        Vue.prototype.$dictName = (dataName, dataCode) => {
            const dictionary = store.getters.signedInInfo.dictionary
            if (dictionary && dictionary[dataName]) {
                for (const item of dictionary[dataName]) {
                    if (item.code === String(dataCode)) {
                        return item.desc
                    }
                }
            }
            return ''
        }
    }
}
