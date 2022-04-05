<template>
    <el-dialog
        :visible.sync="show"
        :close-on-click-modal="false"
        :title="title"
        append-to-body
        width="600px"
        @open="openForm"
        @close="closeForm"
    >
        <el-form ref="form" :inline="true" :model="form" :rules="rules" size="small" label-width="100px">
            <el-form-item v-if="actionType === 'ADD'" label="商户名称">
                <el-autocomplete
                    v-model="form.mchName"
                    placeholder="请输入商户名称搜索"
                    :clearable="true"
                    :trigger-on-focus="false"
                    :readonly="actionType === 'EDIT'"
                    :fetch-suggestions="searchMerchant"
                    @select="selectMerchant"
                    @clear="clearMerchant"
                />
            </el-form-item>
            <el-form-item label="商户编号" prop="mchNo">
                <el-input v-model="form.mchNo" placeholder="商户编号" :readonly="true" />
            </el-form-item>
            <el-form-item label="业务线" prop="productType" v-if="actionType === 'ADD'">
                <el-select v-model="form.productType" @change="productTypeChange" class="selectInput" clearable>
                    <el-option v-for="(value,key) in productTypeMap" :key="key" :label="value" :value="key"/>
                </el-select>
            </el-form-item>
            <el-form-item label="业务线" v-if="actionType === 'EDIT'">
                <el-input :value="productTypeName" :readonly="true"/>
            </el-form-item>
            <el-form-item label="产品名称" prop="productCode" v-if="actionType === 'ADD'">
                <el-select v-model="form.productCode" class="selectInput" clearable>
                    <el-option v-for="(value,key) in currProductCodeMap" :key="key" :label="value" :value="key"/>
                </el-select>
            </el-form-item>
            <el-form-item label="产品名称" v-if="actionType === 'EDIT'">
                <el-input :value="productCodeName" :readonly="true"/>
            </el-form-item>
            <el-form-item label="状态" prop="status">
                <el-radio-group v-model="form.status">
                    <el-radio :label="1">启用</el-radio>
                    <el-radio :label="2" :disabled="actionType === 'ADD'">禁用</el-radio>
                </el-radio-group>
            </el-form-item>
            <el-form-item label="过期时间" prop="expireDate">
                <el-date-picker v-model="form.expireDate" type="date" value-format="yyyy-MM-dd"/>
            </el-form-item>
            <el-form-item label="备注" prop="remark">
                <el-input v-model="form.remark" placeholder="请输入备注" />
            </el-form-item>
        </el-form>
        <div slot="footer" class="dialog-footer">
            <el-button @click="closeForm">取消</el-button>
            <el-button :loading="loading" type="primary" @click="doSubmit">确认</el-button>
        </div>
    </el-dialog>
</template>

<script>
import {getProductInfo} from '@/api/public';
import {addProductOpen, editProductOpen, searchMerchant} from '@/api/baseConfig/product';

export default {
    name: 'ProductOpenFrom',
    data() {
        return {
            loading: false,
            show: false,
            actionType: '',
            title: '',
            productTypeMap: {},
            productCodeArrMap: {},
            currProductCodeMap: {},
            form: {
                id: '',
                productType: '',
                productCode: '',
                status: 1,
                mchNo: '',
                mchName: '',
                expireDate: '',
                remark: ''
            },
            rules: {
                mchNo: [
                    { required: true, message: '请输入需开通产品的商户', trigger: 'blur' },
                ],
                productType: [
                    { required: true, message: '请选择业务线', trigger: 'blur' },
                ],
                productCode: [
                    { required: true, message: '请选择产品名称', trigger: 'blur' },
                ],
                expireDate: [
                    { required: true, message: '请选择过期时间', trigger: 'blur' },
                ],
                status: [
                    { required: true, message: '请选择状态', trigger: 'blur' },
                ],
                remark: [
                    { required: true, message: '请输入备注描述', trigger: 'blur' },
                    { min: 1, max: 200, message: '备注的长度不能超过200', trigger: 'blur' }
                ]
            }
        };
    },
    computed: {
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
        initAndShow(form, actionType){
            this.actionType = actionType
            if(this.actionType === 'ADD'){
                this.title = '新增商户产品开通'
                this.form.status = 1
            }else if(this.actionType === 'EDIT'){
                this.form = form;
                this.title = '编辑商户产品开通'
            }
            this.show = true;
        },
        openForm(){
            getProductInfo().then(({data}) => {
                this.productTypeMap = data.productTypeMap
                this.productCodeArrMap = data.productCodeMap
            });
        },
        productTypeChange(currType){
            this.form.productCode = ''
            this.currProductCodeMap = {}
            this.currProductCodeMap = this.productCodeArrMap[currType] ? this.productCodeArrMap[currType] : {}
        },
        doSubmit() {
            this.actionType === 'ADD' && this.doAdd()
            this.actionType === 'EDIT' && this.doEdit()
        },
        doAdd(){
            this.$refs.form.validate(valid => {
                if (valid) {
                    this.loading = true;
                    addProductOpen(this.form).then(resp => {
                        if (resp.code == 200) {
                            this.$message.success(resp.data);
                            this.$emit('success')
                            this.closeForm();
                        }
                    }).finally(() => {
                        this.loading = false;
                    });
                }
            });
        },
        doEdit(){
            this.$refs.form.validate(valid => {
                if (valid) {
                    this.loading = true;
                    editProductOpen(this.form).then(resp => {
                        if (resp.code == 200) {
                            this.$message.success(resp.data);
                            this.$emit('success')
                            this.closeForm();
                        }
                    }).finally(() => {
                        this.loading = false;
                    });
                }
            });
        },
        searchMerchant(mchName, callback) {
            if (!mchName) {
                return;
            }
            searchMerchant(mchName).then(({ data }) => {
                if (data) {
                    for (let i = 0; i < data.length; i++) {
                        const item = data[i];
                        item.value = item.fullName
                    }
                    callback(data);
                }
            });
        },
        selectMerchant(item) {
            if (item) {
                this.form.mchNo = item.mchNo;
                this.form.mchName = item.fullName;
            }
        },
        clearMerchant() {
            this.form.mchNo = '';
            this.form.mchName = '';
        },
        closeForm() {
            this.show = false;
            this.loading = false;
            this.form = {}//清空表单数据
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
