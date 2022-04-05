<template>
    <el-dialog
        class="dialog-container"
        :visible.sync="show"
        :close-on-click-modal="false"
        :before-close="closeForm"
        :title="(actionType==='ADD'&&'新增商户') || (actionType==='EDIT'&&'编辑商户') || (actionType==='VIEW'&&'查看商户') || ''"
        append-to-body
        width="450px"
    >
        <el-form ref="form" :inline="true" :model="form" :rules="rules" size="small" label-width="120px">
            <el-form-item label="商户类型" prop="mchType">
                <el-select v-model="form.mchType">
                    <el-option v-for="{code,desc} in $dictArray('MchTypeEnum')" :key="parseInt(code)" :label="desc"
                               :value="parseInt(code)" :disabled="actionType !== 'ADD'"/>
                </el-select>
            </el-form-item>
            <el-form-item label="商户全称" prop="fullName">
                <el-input v-model="form.fullName" placeholder="商户全称" :readonly="actionType !== 'ADD'"/>
            </el-form-item>
            <el-form-item label="商户简称" prop="shortName">
                <el-input v-model="form.shortName" placeholder="商户简称" :readonly="actionType === 'VIEW'"/>
            </el-form-item>
            <el-form-item label="联系地址" prop="address">
                <el-input v-model="form.address" placeholder="联系地址" :readonly="actionType === 'VIEW'"/>
            </el-form-item>
            <el-form-item label="固定电话" prop="telephone">
                <el-input v-model="form.telephone" placeholder="固定电话" :readonly="actionType === 'VIEW'"/>
            </el-form-item>
            <el-form-item label="业务联系手机" prop="bussMobileNo">
                <el-input v-model="form.bussMobileNo" placeholder="业务联系手机" :readonly="actionType === 'VIEW'"/>
            </el-form-item>
            <el-form-item label="业务联系邮箱" prop="bussContactEmail">
                <el-input v-model="form.bussContactEmail" placeholder="业务联系邮箱" :readonly="actionType === 'VIEW'"/>
            </el-form-item>
            <el-form-item label="网址" prop="url">
                <el-input v-model="form.url" placeholder="网址"/>
            </el-form-item>
        </el-form>
        <div slot="footer" class="dialog-footer">
            <el-button :loading="loading" v-show="actionType !== 'VIEW'" type="primary" @click="doSubmit">确认</el-button>
            <el-button @click="closeForm">取消</el-button>
        </div>
    </el-dialog>
</template>

<script>
import {addMerchant, getMerchantInfo} from '@/api/merchant/baseInfo'

export default {
    data() {
        return {
            loading: false,
            show: false,
            actionType: undefined,
            form: {},
            rules: {
                mchType: [
                    {required: true, message: '请选择商户类型', trigger: 'blur'},
                ],
                fullName: [
                    {required: true, message: '请输入商户全称', trigger: 'blur'},
                    {min: 1, max: 200, message: '商户全称长度不能超过200', trigger: 'blur'}
                ],
                shortName: [
                    {required: true, message: '请输入商户简称', trigger: 'blur'},
                    {min: 1, max: 100, message: '商户简称长度不能超过100', trigger: 'blur'}
                ],
                address: [
                    {required: true, message: '请输入联系地址', trigger: 'blur'},
                    {min: 1, max: 200, message: '联系地址长度不能超过200', trigger: 'blur'}
                ],
                permissionFlag: [
                    {
                        required: true,
                        validator: (rule, value, callback) => {
                            if (this.form.functionType === 2 && !value) {
                                callback(new Error('请输入权限标识'))
                            } else {
                                callback()
                            }
                        },
                        trigger: 'blur'
                    }
                ],
                bussMobileNo: [
                    {required: true, min: 1, max: 20, message: '业务联系手机长度不能超过20', trigger: 'blur'},
                    {
                        required: true,
                        validator: (rule, value, callback) => {
                            if (!value) {
                                callback(new Error('请输入手机号'));
                            } else if (!/^1[3456789]\d{9}$/.test(value)) {
                                callback(new Error('请输入正确的手机号'))
                            } else {
                                callback()
                            }
                        },
                        trigger: 'blur'
                    }
                ],
                bussContactEmail: [
                    {required: true, message: '请输入业务联系邮箱', trigger: 'blur'},
                    {type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur'}
                ],
                telephone: [
                    {required: false, min: 1, max: 20, message: '固定电话长度不能超过20', trigger: 'blur'}
                ],
                url: [
                    {required: false, min: 1, max: 200, message: '网址长度不能超过200', trigger: 'blur'}
                ],
            }
        }
    },
    methods: {
        initAndShow(type, mchNo){
            this.actionType = type
            if(this.actionType === 'ADD'){
                this.show = true
            }else if(this.actionType === 'EDIT' || this.actionType === 'VIEW'){
                getMerchantInfo(mchNo).then(({data}) => {
                    this.form = data
                    this.show = true
                })
            }
        },
        doSubmit() {
            this.$refs.form.validate((valid) => {
                if (valid) {
                    this.loading = true
                    this.actionType === 'ADD' && this.doAdd()
                    this.actionType === 'EDIT' && this.doEdit()
                }
            })
        },
        doAdd() {
            const formData = {...this.form}
            addMerchant(formData).then((resp) => {
                if(resp.code === 200){
                    this.closeForm()
                    this.$message.success(resp.data)
                    this.$emit('success')
                }
            }).finally(() => {
                this.loading = false
            })
        },
        doEdit() {
            this.$message.warning('暂时不支持编辑商户信息！')//TODO
            this.loading = false
        },
        closeForm() {
            this.show = false
            this.loading = false
            this.form = {}//清空表单数据
            this.$refs.form.clearValidate()//清除校验规则
        }
    }
}
</script>
