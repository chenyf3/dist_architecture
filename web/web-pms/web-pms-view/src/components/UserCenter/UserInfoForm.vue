<template>
    <el-dialog
        class="dialog-container"
        :visible.sync="show"
        :close-on-click-modal="false"
        :before-close="closeForm"
        title="用户信息"
        append-to-body
        width="460px"
    >
        <el-form ref="form" :inline="true" :model="form" size="medium" label-width="100px">
            <el-form-item label="登录名">
                <el-input v-model="form.loginName" type="text" :readonly="true"/>
            </el-form-item>
            <el-form-item label="姓名">
                <el-input v-model="form.realName" type="text" :readonly="true"/>
            </el-form-item>
            <el-form-item label="手机">
                <el-input v-model="form.mobileNo" type="text" :readonly="true"/>
            </el-form-item>
            <el-form-item label="邮箱">
                <el-input v-model="form.email" type="text" size="medium" :readonly="true"/>
            </el-form-item>
            <el-form-item label="状态">
                <el-input :value="$dictName('UserStatusEnum', form.status)" type="text" :readonly="true"/>
            </el-form-item>
            <el-form-item label="注册时间">
                <el-input v-model="form.createTime" type="datetime" :readonly="true" value-format="yyyy-MM-dd HH:mm:ss"/>
            </el-form-item>
        </el-form>
        <div slot="footer" class="dialog-footer">
            <el-button @click="closeForm">关闭</el-button>
        </div>
    </el-dialog>
</template>

<script>
import {getUserInfo} from '@/api/system/user';

export default {
    name: 'UserInfoForm',
    data() {
        return {
            loading: false,
            show: false,
            form: {
                createTime: '',
                loginName: '',
                realName: '',
                mobileNo: '',
                status: ''
            }
        }
    },
    methods: {
        showUserInfo(){
            this.show = true
            this.loading = true
            getUserInfo().then(({data}) => {
                this.form = data
            }).finally(() => {
                this.loading = false
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
