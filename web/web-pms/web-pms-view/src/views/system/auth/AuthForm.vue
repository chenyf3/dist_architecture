<template>
    <el-dialog
        class="dialog-container"
        :visible.sync="show"
        :close-on-click-modal="false"
        :before-close="closeForm"
        :title="(actionType === 'ADD'&&'新增权限') || (actionType === 'EDIT'&&'编辑权限') || ''"
        append-to-body
        width="500px"
        height="550px"
    >
        <el-form ref="form" :inline="true" :model="form" :rules="rules" size="small" label-width="100px">
            <el-form-item v-if="actionType === 'ADD'" label="父节点" prop="parentName">
                <el-input v-model="form.parentName" :disabled="true"/>
            </el-form-item>
            <el-form-item label="权限类型" prop="authType">
                <el-radio-group v-model="form.authType" :disabled="actionType !== 'ADD' || form.parentId === 0"
                                @change="authTypeChange">
                    <el-radio v-model="form.authType" :label="1">菜单</el-radio>
                    <el-radio v-model="form.authType" :label="2">功能</el-radio>
                </el-radio-group>
            </el-form-item>
            <el-form-item label="权限名称" prop="name">
                <el-input v-model="form.name" placeholder="权限/菜单名称"/>
            </el-form-item>
            <el-form-item label="权限标识" prop="permissionFlag">
                <el-input v-model="form.permissionFlag" placeholder="前后端共用的权限标识"/>
            </el-form-item>
            <el-form-item label="URL地址" prop="url">
                <el-input v-model="form.url" placeholder="前端菜单router路径" :disabled="form.authType === 2"/>
            </el-form-item>
            <el-form-item label="菜单图标" prop="icon">
                <el-popover placement="bottom-start" trigger="click" width="420" @show="$refs['iconSelect'].reset()" :disabled="form.authType === 2">
                    <IconSelect ref="iconSelect" @selected="selectedIcon"/>
                    <el-input slot="reference" v-model="form.icon" placeholder="点击选择图标" :disabled="form.authType === 2">
                        <svg-icon v-if="form.icon" slot="prefix" :icon-class="form.icon" class="el-input__icon"
                                  style="height: 32px;width: 16px;"/>
                        <i v-else slot="prefix" class="el-icon-search el-input__icon"/>
                    </el-input>
                </el-popover>
            </el-form-item>
            <el-form-item label="排序编号" prop="number">
                <el-input v-model="form.number" placeholder="菜单排序编号"/>
            </el-form-item>
        </el-form>
        <div slot="footer" class="dialog-footer">
            <el-button :loading="loading" type="primary" @click="doSubmit">确认</el-button>
            <el-button @click="closeForm">取消</el-button>
        </div>
    </el-dialog>
</template>

<script>
import IconSelect from '@/components/IconSelect'
import {addAuth, editAuth} from '@/api/system/auth';

export default {
    name: 'AuthForm',
    components: {
        IconSelect
    },
    data() {
        return {
            loading: false,
            show: false,
            actionType: undefined,
            form: {},
            rules: {
                authType: [
                    {
                        required: true,
                        validator: (rule, value, callback) => {
                            if (this.form.parentId > 0 && !value) {
                                callback(new Error('请选择权限类型'))
                            } else {
                                callback()
                            }
                        },
                        trigger: 'blur'
                    }
                ],
                name: [
                    {required: true, message: '请输入权限名称', trigger: 'blur'}
                ],
                permissionFlag: [
                    {required: true, message: '请输入权限标识', trigger: 'blur'}
                ],
                url: [{
                    required: true,
                    validator: (rule, value, callback) => {
                        if (this.form.authType === 1 && !value) {
                            callback(new Error('请输入URL地址'))
                        } else {
                            callback()
                        }
                    },
                    trigger: 'blur'
                }],
                icon: [{required: false, message: '请输入icon', trigger: 'blur'}]
            }
        }
    },
    methods: {
        initAndShow(actionType, row) {
            if (actionType === 'ADD') {
                this.form.parentId = row && row.id
                this.form.parentName = row && row.name
                if (this.form.parentId === 0) {
                    this.form.authType = 1;
                }
            } else if (actionType === 'EDIT') {
                this.form = {...row}
            }
            this.actionType = actionType
            this.show = true
        },
        doSubmit() {
            this.loading = true
            this.$refs.form.validate((valid) => {
                if (valid) {
                    this.actionType === 'ADD' && this.doAdd()
                    this.actionType === 'EDIT' && this.doEdit()
                } else {
                    this.loading = false
                }
            })
        },
        doAdd() {
            addAuth(this.form)
                .then((resp) => {
                    if(resp.code === 200){
                        this.$message.success(resp.data)
                        this.$emit('success')
                        this.closeForm()
                    }
                }).finally(() => {
                    this.loading = false
                })
        },
        doEdit() {
            editAuth(this.form)
                .then((resp) => {
                    if(resp.code === 200){
                        this.$message.success(resp.data)
                        this.$emit('success')
                        this.closeForm()
                    }
                }).finally(() => {
                    this.loading = false
                })
        },
        authTypeChange(type) {
            if(type === 2) {
                this.form.url = undefined
                this.form.icon = undefined
            }
        },
        selectedIcon(name) {
            this.form.icon = name
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
