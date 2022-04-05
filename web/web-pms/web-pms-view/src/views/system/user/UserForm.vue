<template>
    <el-dialog
        class="dialog-container"
        :visible.sync="show"
        :close-on-click-modal="false"
        :before-close="closeForm"
        :title="(actionType==='ADD'&&'新增操作员') || (actionType==='EDIT'&&'编辑操作员') || ''"
        append-to-body
        width="450px"
    >
        <el-form ref="form" :inline="true" :model="form" :rules="rules" size="small" label-width="100px">
            <el-form-item label="登录名" prop="loginName">
                <el-input v-model="form.loginName" :disabled="actionType !== 'ADD'" placeholder="系统登录账号"/>
            </el-form-item>
            <el-form-item v-if="actionType==='ADD'" label="登录密码" prop="loginPwd">
                <el-input v-model="form.loginPwd" type="password" placeholder="登录密码"/>
            </el-form-item>
            <el-form-item v-if="actionType==='ADD'" label="确认密码" prop="pwdConfirm">
                <el-input v-model="form.pwdConfirm" type="password" placeholder="确认登录密码"/>
            </el-form-item>
            <el-form-item label="姓名" prop="realName">
                <el-input v-model="form.realName" placeholder="姓名"/>
            </el-form-item>
            <el-form-item label="手机号" prop="mobileNo">
                <el-input v-model="form.mobileNo" placeholder="手机号"/>
            </el-form-item>
            <el-form-item label="邮箱" prop="email">
                <el-input v-model="form.email" placeholder="邮箱"/>
            </el-form-item>
            <el-form-item label="描述" prop="remark">
                <el-input v-model="form.remark" placeholder="描述"/>
            </el-form-item>
        </el-form>
        <div slot="footer" class="dialog-footer">
            <el-button :loading="loading" type="primary" @click="doSubmit">确认</el-button>
            <el-button @click="closeForm">取消</el-button>
        </div>
    </el-dialog>
</template>

<script>
import {addUser, editUser, getUserById} from '@/api/system/user'
import {encryptParam} from '@/tools/jsencrypt'

export default {
    data() {
        return {
            loading: false,
            show: false,
            actionType: undefined,
            form: {},
            rules: {
                loginName: [
                    {required: true, message: '请输入登录名', trigger: 'blur'},
                    {min: 1, max: 80, message: '登录名长度不能超过80', trigger: 'blur'}
                ],
                loginPwd: [{required: true, message: '请输入密码', trigger: 'blur'}],
                pwdConfirm: [
                    {required: true, message: '请输入确认密码', trigger: 'blur'},
                    {
                        required: true,
                        trigger: 'blur',
                        validator: (rule, value, callback) => value !== this.form.loginPwd ? callback(new Error('两次输入密码不一致!')) : callback()
                    }
                ],
                realName: [
                    {required: true, message: '请输入姓名', trigger: 'blur'},
                    {min: 1, max: 50, message: '姓名的长度不能超过50', trigger: 'blur'}
                ],
                mobileNo: [{required: true, message: '请输入手机号码', trigger: 'blur', pattern: /^1[0-9]{10}$/}],
                email: [{min: 1, max: 100, message: '邮箱的长度不能超过100', trigger: 'blur'}],
                remark: [{min: 1, max: 200, message: '描述的长度不能超过200', trigger: 'blur'}]
            }
        }
    },
    methods: {
        initAndShow(type, userId){
            this.actionType = type
            if(this.actionType == 'ADD'){
                this.show = true
            }else if(this.actionType == 'EDIT'){
                getUserById(userId).then(({data}) => {
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
            const formData = {}
            Object.assign(formData, this.form)
            formData.loginPwd = encryptParam(formData.loginPwd)
            formData.pwdConfirm = encryptParam(formData.pwdConfirm)

            addUser(formData).then((resp) => {
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
            editUser(this.form).then((resp) => {
                if(resp.code === 200){
                    this.closeForm()
                    this.$message.success(resp.data)
                    this.$emit('success')
                }
            }).finally(() => {
                this.loading = false
            })
        },
        closeForm() {
            this.show = false
            this.loading = false
            this.actionType = undefined
            this.form = {}//清空表单数据
            this.$refs.form.clearValidate()//清除校验规则
        }
    }
}
</script>
