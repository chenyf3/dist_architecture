<template>
    <el-dialog
        ref="dialog"
        class="dialog-container"
        :title="actionType=='ADD'? '新增角色' : '编辑角色'"
        :visible.sync="show"
        @close="closeForm"
        @open="form.roleType=(actionType === 'ADD'?1:form.roleType)"
        width="480px"
    >
        <el-form ref="form" :inline="true" :model="form" :rules="rules" size="small" label-width="100px">
            <el-form-item prop="mchType" label="商户类型" :label-width="labelWidth">
                <el-select v-model="form.mchType">
                    <el-option v-for="{code,desc} in $dictArray('MchTypeEnum')"
                               :key="parseInt(code)" :disabled="actionType === 'EDIT'" :label="desc" :value="parseInt(code)"
                    />
                </el-select>
            </el-form-item>
            <el-form-item prop="roleName" label="角色名称" :label-width="labelWidth">
                <el-input v-model="form.roleName" autocomplete="off"/>
            </el-form-item>
            <el-form-item prop="roleType" label="角色类型" :label-width="labelWidth">
                <el-radio v-model="form.roleType" :label="1" :disabled="actionType === 'EDIT'">管理员角色</el-radio>
                <el-radio v-model="form.roleType" :label="2" :disabled="true">普通角色</el-radio>
            </el-form-item>
            <el-form-item prop="remark" label="描述" :label-width="labelWidth">
                <el-input v-model="form.remark" autocomplete="off"/>
            </el-form-item>
            <el-form-item prop="autoAssign" label="自动分配" :label-width="labelWidth">
                <el-tooltip class="item" effect="light" content="添加商户管理员时自动关联当前角色(相同商户类型时)" placement="right-end">
                    <el-radio v-model="form.autoAssign" :label="1">是</el-radio>
                </el-tooltip>
                <el-radio v-model="form.autoAssign" :label="2">否</el-radio>
            </el-form-item>
        </el-form>
        <div slot="footer" class="dialog-footer">
            <el-button @click="closeForm">取 消</el-button>
            <el-button :loading="loading" type="primary" @click="doSubmit">确 定</el-button>
        </div>
    </el-dialog>
</template>

<script>
import {addAdminRole, editAdminRole} from '@/api/portal/role';

export default {
    name: 'RoleForm',
    data() {
        return {
            form: {
                mchType: '',
                roleName: '',
                roleType: null,
                remark: '',
                autoAssign: 2
            },
            rules: {
                roleName: [
                    {required: true, message: '角色名称不能为空', trigger: 'blur'},
                    {min: 1, max: 80, message: '长度不能超过80', trigger: 'blur'}
                ],
                mchType: [{required: true, message: '请选择商户类型', trigger: 'blur'}],
                autoAssign: [{required: true, message: '请选择是否允许自动分配', trigger: 'blur'}],
                remark: [
                    {required: true, message: '描述不能为空', trigger: 'blur'},
                    {min: 1, max: 200, message: '长度不能超过200', trigger: 'blur'}
                ]
            },
            loading: false,
            show: false,
            actionType: undefined,
            labelWidth: '100px'
        }
    },
    methods: {
        initForAdd(){
            this.actionType = 'ADD'
            this.form.roleType = 1
            this.show = true
        },
        initForEdit(form){
            this.actionType = 'EDIT'
            this.form = form
            this.show = true
        },
        doSubmit() {
            this.loading = true
            this.$refs.form.validate((pass) => {
                if (pass) {
                    this.actionType === 'ADD' && this.doAddRole();
                    this.actionType === 'EDIT' && this.doEditRole();
                } else {
                    this.loading = false
                }
            })
        },
        doAddRole(){
            addAdminRole(this.form).then((resp) => {
                if(resp.code === 200){
                    this.$message.success(resp.data)
                    this.closeForm()
                    this.$emit('success')//向父组件传值，触发 on-success 事件
                }
            }).finally(() => {
                this.loading = false
            })
        },
        doEditRole(){
            editAdminRole(this.form).then((resp) => {
                if(resp.code === 200){
                    this.$message.success(resp.data)
                    this.closeForm()
                    this.$emit('success')//向父组件传值，触发 on-success 事件
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
