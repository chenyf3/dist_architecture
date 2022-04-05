<template>
    <div v-loading="loading" class="app-container">
        <el-form :inline="true" size="small">
            <el-form-item label="角色名称">
                <el-input placeholder="模糊匹配" v-model="searchParam.roleName"/>
            </el-form-item>
            <el-form-item>
                <el-button @click="refreshList" type="primary" ><i class="el-icon-search"/>查询</el-button>
                <el-button v-authorize="'sys:role:add'" @click="addRolePage" type="success" plain><i class="el-icon-plus"/>新增角色</el-button>
            </el-form-item>
        </el-form>

        <div>
            <el-table :data="pageResult.data" border>
                <el-table-column label="序号" type="index" width="50px"/>
                <el-table-column label="角色名称" prop="roleName"/>
                <el-table-column label="描述" prop="remark"/>
                <el-table-column label="创建时间" prop="createTime"/>
                <el-table-column v-slot="{row}" label="操作">
                    <el-button type="text" size="small" v-authorize="'sys:role:assignAuth'" @click="assignAuthPage(row)">分配权限</el-button>
                    <el-button type="text" size="small" v-authorize="'sys:role:edit'" @click="editRolePage(row)">编辑</el-button>
                    <el-button type="text" size="small" v-authorize="'sys:user:delete'" @click="deleteRole(row)">删除</el-button>
                </el-table-column>
            </el-table>
        </div>

        <div class="pagination-container">
            <pagination
                v-show="pageResult.totalRecord > 0"
                :total="pageResult.totalRecord"
                :page.sync="searchParam.currentPage"
                :limit.sync="searchParam.pageSize"
                @pagination="refreshList"
            />
        </div>

        <role-form ref="roleForm" @success="refreshList"/>
        <assign-auth ref="assignAuth"/>
    </div>
</template>

<script>
import Pagination from '@/components/Pagination'
import {listRole, deleteRole} from '@/api/system/role'
import RoleForm from './RoleForm'
import AssignAuth from './AssignAuth'

export default {
    name: 'RoleList',
    components: {
        Pagination,
        RoleForm,
        AssignAuth
    },
    data() {
        return {
            loading: false,
            searchParam: {
                currentPage: 1,
                pageSize: 10,
                roleName: ''
            },
            pageResult: {}
        }
    },
    mounted() {
        this.refreshList()
    },
    methods: {
        refreshList() {
            this.loading = true
            listRole(this.searchParam)
                .then(({data}) => {
                    this.pageResult = data
                }).finally(() => {
                    this.loading = false
                })
        },
        addRolePage() {
            this.$refs.roleForm.initAndShow('ADD', {})
        },
        editRolePage(row) {
            this.$refs.roleForm.initAndShow('EDIT', {...row})
        },
        assignAuthPage(row) {
            this.$refs.assignAuth.initAndShow(row.id)
        },
        deleteRole(row) {
            this.$confirm('删除角色将一并删除此角色与用户、权限的关联关系，确定吗?')
                .then(() => {
                    deleteRole(`${row.id}`).then((resp) => {
                        if(resp.code === 200){
                            this.$message.success(resp.data)
                            this.refreshList()
                        }
                    })
                })
        },
    }
}
</script>
