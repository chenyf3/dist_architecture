<template>
    <el-dialog
        ref="dialog"
        class="dialog-container"
        :visible.sync="show"
        :close-on-click-modal="false"
        :before-close="closeForm"
        :title="actionType === 'ADD' ? '新增商户管理员' : '编辑商户管理员'"
        append-to-body
        width="500px"
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
                <el-input v-model="form.mchNo" placeholder="商户编号" readonly />
            </el-form-item>
            <el-form-item label="登录名" prop="loginName">
                <el-input v-model="form.loginName" :readonly="actionType === 'EDIT'"/>
            </el-form-item>
            <el-form-item label="姓名" prop="realName">
                <el-input v-model="form.realName" :readonly="false" placeholder="姓名"/>
            </el-form-item>
            <el-form-item label="手机号" prop="mobileNo">
                <el-input v-model="form.mobileNo" :readonly="false" placeholder="手机号"/>
            </el-form-item>
            <el-form-item label="邮箱" prop="email">
                <el-input v-model="form.email" :readonly="false" placeholder="邮箱"/>
            </el-form-item>
            <el-form-item label="类型" prop="type">
                <el-radio-group v-model="form.type">
                    <el-radio :label="1" :disabled="actionType === 'EDIT'" :border="true">管理员</el-radio>
                    <el-radio :label="2" :disabled="true" :border="true">普通用户</el-radio>
                </el-radio-group>
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
import {searchMerchant, addAdminUser, editUser} from '@/api/portal/user';

export default {
    data() {
        return {
            loading: false,
            show: false,
            actionType: undefined,
            form: {
                id: undefined,
                mchNo: '',
                mchType: undefined,
                loginName: '',
                realName: '',
                mobileNo: '',
                email: '',
                type: 1,
                remark: ''
            },
            rules: {
                loginName: [
                    {required: true, message: '请输入登录名', trigger: 'blur'},
                    {min: 1, max: 80, message: '登录名长度不能超过80', trigger: 'blur'}
                ],
                realName: [
                    {required: true, message: '请输入姓名', trigger: 'blur'},
                    {min: 1, max: 50, message: '姓名的长度不能超过50', trigger: 'blur'}
                ],
                email: [
                    {required: false, message: '请输入邮箱', trigger: 'blur'},
                    {type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur'}
                ],
                type: [{required: true, message: '请选择用户类型', trigger: 'blur'}],
                mobileNo: [{required: true, message: '请输入手机号码', trigger: 'blur', pattern: /^1[0-9]{10}$/}],
                mchNo: [
                    {required: true, message: '请输入商户编号', trigger: 'blur'},
                    {min: 1, max: 50, message: '商户编号的长度不能超过50', trigger: 'blur'}
                ],
                remark: [
                    {required: false, message: '请输入描述', trigger: 'blur'},
                    {min: 1, max: 200, message: '描述的长度不能超过200', trigger: 'blur'}
                ]
            }
        }
    },
    methods: {
        initAndShow(actionType, row) {
            if (actionType === 'ADD') {
                this.form.type = 1
            } else if (actionType === 'EDIT') {
                this.form.id = row.id
                this.form.loginName = row.loginName
                this.form.realName = row.realName
                this.form.mobileNo = row.mobileNo
                this.form.email = row.email
                this.form.type = row.type
                this.form.mchNo = row.mchNo
                this.form.mchType = row.mchType
                this.form.remark = row.remark
            }
            this.actionType = actionType
            this.show = true
        },
        searchMerchant(mchName, callback) {
            if (!mchName) {
                return;
            }

            searchMerchant(mchName)
                .then(({ data }) => {
                    if (data) {
                        for (let i = 0; i < data.length; i++) {
                            const item = data[i];
                            const dict = this.$dictItem('MchTypeEnum', item.mchType);
                            item.value = dict ? item.fullName + ` - ${dict.desc}` : item.fullName;
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
            this.form.mchType = '';
            this.form.mchName = '';
        },
        doSubmit() {
            this.$refs.form.validate((valid) => {
                if (valid) {
                    this.loading = true
                    this.actionType === 'ADD' && this.doAdd()
                    this.actionType === 'EDIT' && this.doEdit()
                } else {
                    this.loading = false
                }
            })
        },
        doAdd() {
            const formData = JSON.parse(JSON.stringify(this.form))
            addAdminUser(formData)
                .then((resp) => {
                    if (resp.code == 200) {
                        this.closeForm()
                        this.$message.success(resp.data)
                        this.$emit('success')
                    }
                }).finally(() => {
                    this.loading = false
                })
        },
        doEdit() {
            editUser(this.form)
                .then((resp) => {
                    if (resp.code == 200) {
                        this.loading = true
                        this.closeForm()
                        this.$message.success(resp.data)
                        this.$emit('success')
                    }
                }).finally(() => {
                    this.loading = false
                })
        },
        closeForm() {
            this.loading = false
            this.show = false
            this.actionType = undefined
            Object.keys(this.form).forEach(key => this.form[key] = undefined)//清空表单数据
            this.$refs.form.clearValidate()//清除校验规则
        }
    }
}
</script>
