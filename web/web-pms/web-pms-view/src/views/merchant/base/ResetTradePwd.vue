<template>
    <el-dialog
        ref="dialog"
        class="dialog-container"
        :visible.sync="dialog"
        :close-on-click-modal="false"
        :before-close="closeForm"
        title="重置支付密码"
        append-to-body
        width="550px"
    >
        <el-form ref="form" :inline="true" :model="form" :rules="rules" size="small" label-width="150px">
            <el-form-item label="商户编号" prop="mchNo">
                <el-input v-model="form.mchNo" :readonly="true"/>
            </el-form-item>
            <el-form-item label="商户全称">
                <el-input v-model="form.mchName" :readonly="true"/>
            </el-form-item>
            <el-form-item label="商户后台管理员账号">
                <el-input v-model="form.adminLoginName" :readonly="true"/>
            </el-form-item>
            <el-form-item label="商户后台管理员姓名">
                <el-input v-model="form.adminRealName" :readonly="true"/>
            </el-form-item>
            <el-form-item label="商户后台管理员手机">
                <el-input v-model="form.adminMobileNo" :readonly="true"/>
            </el-form-item>
        </el-form>
        <div slot="footer" class="dialog-footer">
            <el-button :loading="loading" type="primary" @click="doSubmit">发送验证码</el-button>
            <el-button @click="closeForm">取消</el-button>
        </div>
    </el-dialog>
</template>

<script>
import {getTradePwdResetInfo, sendTradePwdResetCode} from '@/api/merchant/baseInfo'

export default {
    data() {
        return {
            loading: false,
            dialog: false,
            form: {
                mchNo: '',
                mchName: '',
                adminLoginName: '',
                adminRealName: '',
                adminMobileNo: '',
            },
            rules: {
                mchNo: [{required: true, message: '请输入商户编号', trigger: 'blur'}]
            }
        }
    },
    methods: {
        initAndShow(mchNo) {
            this.dialog = true
            getTradePwdResetInfo(mchNo).then((resp) => {
                if (resp.code === 200) {
                    this.form = resp.data
                } else {
                    this.$message.error(resp.msg)
                }
            })
        },
        doSubmit() {
            this.$refs.form.validate((valid) => {
                if (valid) {
                    this.loading = true
                    sendTradePwdResetCode(this.form.mchNo)
                        .then(({data}) => {
                            this.$message.success(data)
                            this.$emit('success')
                            this.closeForm()
                        }).catch(() => {
                            this.loading = false
                        })
                }
            })
        },
        closeForm() {
            this.dialog = false
            this.loading = false
            Object.keys(this.form).forEach(key => this.form[key] = undefined)//清空表单数据
            this.$refs.form.clearValidate()//清除校验规则
        },
    }
}
</script>
