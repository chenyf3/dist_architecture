<template>
    <el-dialog
        class="dialog-container"
        :visible.sync="show"
        :close-on-click-modal="false"
        :before-close="closeForm"
        title="重置密码"
        append-to-body
        width="500px"
    >
        <el-form ref="form" :inline="true" :model="form" :rules="rules" size="small" label-width="100px">
            <el-form-item label="登录名" prop="loginName">
                <el-input v-model="form.loginName" disabled/>
            </el-form-item>
            <el-form-item label="姓名" prop="realName">
                <el-input v-model="form.realName" disabled/>
            </el-form-item>
            <el-form-item label="新登录密码" prop="newPwd">
                <el-input v-model="form.newPwd" type="password" placeholder="新登录密码"/>
            </el-form-item>
            <el-form-item label="确认新密码" prop="newPwd2">
                <el-input v-model="form.newPwd2" type="password" placeholder="确认新密码"/>
            </el-form-item>
        </el-form>
        <div slot="footer" class="dialog-footer">
            <el-button :loading="loading" type="primary" @click="doSubmit">确认</el-button>
            <el-button @click="closeForm">取消</el-button>
        </div>
    </el-dialog>
</template>

<script>
import {resetUserPwd} from '@/api/system/user'
import {encryptParam} from '@/tools/jsencrypt'

export default {
    data() {
        return {
            loading: false,
            show: false,
            form: {
                userId: undefined,
                newPwd: ''
            },
            rules: {
                newPwd: [{required: true, message: '请输入密码', trigger: 'blur'}],
                newPwd2: [{
                    required: true,
                    trigger: 'blur',
                    validator: (rule, value, callback) => value !== this.form.newPwd ? callback(new Error('两次输入密码不一致!')) : callback()
                }]
            }
        }
    },
    methods: {
        initAndShow(user){
            this.form = {userId: user.id, loginName: user.loginName, realName: user.realName}
            this.show = true
        },
        doSubmit() {
            this.$refs.form.validate((valid) => {
                if (valid) {
                    this.loading = true
                    const newPwd = encryptParam(this.form.newPwd)
                    resetUserPwd(this.form.userId, newPwd).then((resp) => {
                        if(resp.code === 200){
                            this.closeForm()
                            this.$message.success(resp.data)
                            this.$emit('success')
                        }
                    }).finally(() => {
                        this.loading = false
                    })
                }
            })
        },
        closeForm() {
            this.show = false
            this.loading = false
            this.form = {}
            this.$refs.form.resetFields()
        }
    }
}
</script>

<style lang="scss" scoped>
::v-deep .el-dialog {
    .el-input {
        width: 260px;
    }
}
</style>
