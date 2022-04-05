<template>
    <el-dialog
        :visible.sync="show"
        :close-on-click-modal="false"
        title="查看商户通知"
        append-to-body
        width="600px"
        @open="openForm"
        @close="closeForm"
    >
        <el-form ref="form" :inline="true" :model="form" size="mini" label-width="100px">
            <el-form-item label="创建时间">
                <el-date-picker v-model="form.createTime" :readonly="true" type="datetime"
                                value-format="yyyy-MM-dd HH:mm:ss"/>
            </el-form-item>
            <el-form-item label="商户编号">
                <el-input v-model="form.mchNo" :readonly="true"/>
            </el-form-item>
            <el-form-item label="系统流水号">
                <el-input v-model="form.trxNo" :readonly="true"/>
            </el-form-item>
            <el-form-item label="商户流水号">
                <el-input v-model="form.mchTrxNo" :readonly="true"/>
            </el-form-item>
            <el-form-item label="通知次数">
                <el-input v-model="form.currTimes" :readonly="true"/>
            </el-form-item>
            <el-form-item label="通知状态">
                <el-input :value="statusName" :readonly="true"/>
            </el-form-item>
            <el-form-item label="业务线">
                <el-input :value="productTypeName" :readonly="true"/>
            </el-form-item>
            <el-form-item label="产品名称">
                <el-input :value="productCodeName" :readonly="true"/>
            </el-form-item>
            <el-form-item label="回调地址">
                <el-input v-model="form.url" :readonly="true"/>
            </el-form-item>
            <el-form-item label="通知内容">
                <el-input v-model="form.oriMsg" type="textarea" :readonly="true" :autosize="{minRows: 5, maxRows: 9}"/>
            </el-form-item>
        </el-form>
        <div slot="footer" class="dialog-footer">
            <el-button @click="closeForm">关闭</el-button>
        </div>
    </el-dialog>
</template>

<script>
import {getProductInfo} from '@/api/public/index';
import {getNotifyRecord} from '@/api/merchant/notify';

export default {
    name: 'ViewNotifyRecord',
    data() {
        return {
            show: false,
            recordId: '',
            productTypeMap: {},
            productCodeArrMap: [],
            form: {}
        };
    },
    computed: {
        statusName(){
            return this.form.status === '1' ? '成功' : this.form.status === 2 ? '失败' : ''
        },
        productTypeName() {
            return this.productTypeMap[this.form.productType] ? this.productTypeMap[this.form.productType] : this.form.productType;
        },
        productCodeName() {
            if (this.productCodeArrMap[this.form.productType]) {
                let productCodeMap = this.productCodeArrMap[this.form.productType];
                return productCodeMap[this.form.productCode] ? productCodeMap[this.form.productCode] : this.form.productCode
            } else {
                return this.form.productCode;
            }
        },
    },
    methods: {
        initAndShow(recordId){
            this.show = true;
            this.recordId = recordId;
        },
        openForm() {
            getProductInfo().then(({data}) => {
                this.productTypeMap = data.productTypeMap
                this.productCodeArrMap = data.productCodeMap
            });
            getNotifyRecord(this.recordId).then(({data}) => {
                this.form = data;
            });
        },
        closeForm() {
            this.show = false;
            this.loading = false;
            this.recordId = '';
            Object.keys(this.form).forEach(key => this.form[key] = undefined)//清空表单数据
            this.$refs.form.clearValidate()//清除校验规则
        }
    }
};
</script>

<style lang="scss" scoped>
::v-deep .el-dialog {
    width: 450px;

    .el-textarea {
        width: 400px;
    }
    .el-input {
        width: 400px;
    }
}
</style>
