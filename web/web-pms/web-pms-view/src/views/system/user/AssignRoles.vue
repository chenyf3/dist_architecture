<template>
    <el-dialog
        class="dialog-container"
        :visible.sync="show"
        :close-on-click-modal="false"
        :before-close="closeForm"
        title="为用户分配角色"
        append-to-body
        width="600px"
    >
        <el-form ref="form" :inline="false" :model="form" :rules="rules" size="medium" label-width="100px">
            <el-form-item label="登录名：">
                <el-input v-model="form.loginName" :readonly="true"/>
            </el-form-item>
            <el-form-item label="姓名：">
                <el-input v-model="form.realName" :readonly="true"/>
            </el-form-item>
            <el-form-item v-if="form.type===2" label="角色：" prop="roleIds">
                <el-checkbox-group v-model="form.roleIds" style="width: 450px;">
                    <el-checkbox v-for="role in allRoles" :key="role.id" :label="role.id">{{
                            role.roleName
                        }}
                    </el-checkbox>
                </el-checkbox-group>
            </el-form-item>
        </el-form>

        <div slot="footer" class="dialog-footer">
            <el-button :loading="loading" type="primary" @click="doSubmit">确认</el-button>
            <el-button @click="closeForm">取消</el-button>
        </div>
    </el-dialog>
</template>

<script>
import {assignRoles, getUserById} from '@/api/system/user'
import {listAllRoles} from '@/api/system/role';

export default {
    data() {
        return {
            loading: false,
            show: false,
            actionType: undefined,
            allRoles: [],
            form: {
                id: undefined,
                roleIds: []
            },
            rules: {}
        }
    },
    methods: {
        initAndShow(userId){
            const p1 = listAllRoles()
            const p2 = getUserById(userId)
            Promise.all([p1, p2])
                .then(([{data: allRoles}, {data: pmsUser}]) => {
                    this.allRoles = allRoles
                    this.form = pmsUser
                    this.show = true
                })
        },
        doSubmit() {
            assignRoles(this.form.id, this.form.roleIds)
                .then((resp) => {
                    if (resp.code === 200) {
                        this.closeForm()
                        this.$message.success(resp.data)
                        this.$emit('success')
                    }
                })
                .finally(() => {
                    this.loading = false
                })
        },
        closeForm() {
            this.show = false
            this.loading = false
            this.actionType = undefined
            this.allRoles = []
            this.$refs.form.resetFields()
        }
    }
}
</script>
