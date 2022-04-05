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
            <el-form-item label="状态" v-if="actionType === 'EDIT'">
                <el-input :value="statusName" :readonly="true"/>
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
import {addProduct, editProduct} from '@/api/baseConfig/product';

export default {
    name: 'ProductFrom',
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
                productType: undefined,
                productCode: undefined,
                status: '',
                remark: ''
            },
            rules: {
                productType: [
                    { required: true, message: '请选择业务线', trigger: 'blur' },
                ],
                productCode: [
                    { required: true, message: '请选择产品名称', trigger: 'blur' },
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
        statusName(){
            return this.form.status === 1 ? '启用' : this.form.status === 2 ? '禁用' : ''
        },
    },
    methods: {
        initAndShow(form, actionType){
            this.actionType = actionType
            if(this.actionType === 'ADD'){
                this.title = '新增产品记录'
            }else if(this.actionType === 'EDIT'){
                this.form = form;
                this.title = this.form.status === 1 ? '禁用产品' : '启用产品'
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
                    addProduct(this.form).then(resp => {
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
                    editProduct(this.form.id, this.form.remark).then(resp => {
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
        closeForm() {
            this.show = false;
            this.loading = false;
            this.form.productType = '';
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
