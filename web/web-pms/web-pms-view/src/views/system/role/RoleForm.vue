<template>
    <el-dialog
        class="dialog-container"
        :visible.sync="show"
        :close-on-click-modal="false"
        :title="(actionType === 'ADD' && '新增角色') || (actionType==='EDIT' && '编辑角色') || ''"
        @close="closeForm"
        width="500px"
    >
        <el-form ref="form" :inline="true" :model="form" :rules="rules" size="small" label-width="100px">
            <el-form-item prop="roleName" label="角色名称" label-width="100px">
                <el-input v-model="form.roleName" autocomplete="off"/>
            </el-form-item>
            <el-form-item prop="remark" label="描述" label-width="100px">
                <el-input v-model="form.remark" autocomplete="off"/>
            </el-form-item>
        </el-form>
        <div slot="footer" class="dialog-footer">
            <el-button :loading="loading" type="primary" @click="doSubmit">确定</el-button>
            <el-button @click="closeForm">取 消</el-button>
        </div>
    </el-dialog>
</template>

<script>
import {addRole, editRole} from '@/api/system/role'

export default {
    name: 'RoleForm',
    data() {
        return {
            show: false,
            loading: false,
            actionType: undefined,
            form: {},
            rules: {
                roleName: [
                    {required: true, message: '角色名称不能为空', trigger: 'blur'},
                    {min: 1, max: 80, message: '长度不能超过80', trigger: 'blur'}
                ],
                remark: [
                    {min: 1, max: 200, message: '长度不能超过200', trigger: 'blur'}
                ]
            }
        }
    },
    methods: {
        initAndShow(actionType, row){
            if(actionType === 'ADD') {
                this.form = {}
            }else if(actionType === 'EDIT'){
                this.form = {...row}
            }
            this.actionType = actionType
            this.show = true
        },
        doSubmit() {
            this.actionType === 'ADD' && this.doAdd()
            this.actionType === 'EDIT' && this.doEdit()
        },
        doAdd() {
            this.$refs.form.validate(valid => {
                if (valid) {
                    this.loading = true
                    addRole(this.form).then((resp) => {
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
        doEdit() {
            this.$refs.form.validate(valid => {
                if (valid) {
                    this.loading = true
                    editRole(this.form).then((resp) => {
                        if(resp.code == 200){
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
            this.actionType = undefined
            this.form = {}//清空表单数据
            this.$refs.form.clearValidate()//清除校验规则
        }
    }
}
</script>
