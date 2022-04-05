<template>
    <el-dialog
        class="dialog-container"
        :visible.sync="show"
        :close-on-click-modal="false"
        :before-close="closeForm"
        title="修改密码"
        append-to-body
        width="460px"
    >
        <el-form ref="form" :inline="true" :model="form" :rules="rules" size="medium" label-width="100px">
            <el-form-item label="原密码" prop="oldPwd">
                <el-input v-model="form.oldPwd" type="password" class="form-input" placeholder="输入原密码"/>
            </el-form-item>
            <el-form-item label="新密码" prop="newPwd">
                <el-input v-model="form.newPwd" type="password" class="form-input" placeholder="输入新密码"/>
            </el-form-item>
            <el-form-item label="确认新密码" prop="confirmPwd">
                <el-input v-model="form.confirmPwd" type="password" class="form-input" placeholder="确认新密码"/>
            </el-form-item>
        </el-form>
        <div slot="footer" class="dialog-footer">
            <el-button @click="closeForm">取消</el-button>
            <el-button :loading="loading" type="primary" @click="doSubmit">确认</el-button>
        </div>
    </el-dialog>
</template>

<script>
import {encryptParam} from '@/tools/jsencrypt'
import {changePwd} from '@/api/system/user';
import {logout} from '@/tools/logout';

export default {
    name: 'ChangePwd',

    data() {
        return {
            loading: false, show: false,
            form: {oldPwd: '', newPwd: '', confirmPwd: ''},
            rules: {
                oldPwd: [{required: true, message: '请输入旧密码', trigger: 'blur'}],
                newPwd: [{required: true, message: '请输入新密码', trigger: 'blur'}],
                confirmPwd: [{
                    required: true,
                    trigger: 'blur',
                    validator: (rule, value, callback) => value !== this.form.newPwd ? callback(new Error('两次输入密码不一致!')) : callback()
                }]
            }
        }
    },
    methods: {
        doSubmit() {
            this.$refs.form.validate((valid) => {
                if (valid) {
                    this.loading = true
                    const oldPwd = encryptParam(this.form.oldPwd)
                    const newPwd = encryptParam(this.form.newPwd)
                    const confirmPwd = encryptParam(this.form.confirmPwd)
                    changePwd(oldPwd, newPwd, confirmPwd)
                        .then((resp) => {
                            if (resp.code === 200) {
                                this.$message.success('密码修改成功，请重新登录！')
                                setTimeout(() => {
                                    this.closeForm()
                                    logout()//此处直接清除前端的登录信息即可，后端的登录信息在密码修改成功的时候就已经清除了
                                }, 1000);
                            } else {
                                this.$message.error(resp.msg)
                                this.loading = false
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
            this.$refs.form.resetFields()
        }
    }
}
</script>
