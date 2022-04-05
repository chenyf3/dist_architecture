<template>
    <div v-loading="loading" class="app-container">
        <el-form :inline="true" class="" size="small">
            <el-form-item label="角色类型">
                <el-select v-model="searchForm.roleType" clearable>
                    <el-option v-for="{code,desc} in $dictArray('RoleTypeEnum')"
                               :key="parseInt(code)" :label="desc" :value="parseInt(code)"
                    />
                </el-select>
            </el-form-item>
            <el-form-item label="角色名称">
                <el-input v-model="searchForm.roleName" placeholder="模糊匹配"></el-input>
            </el-form-item>
            <el-form-item label="商户号">
                <el-input v-model="searchForm.mchNo" placeholder="精确匹配"></el-input>
            </el-form-item>
            <el-form-item label="商户类型">
                <el-select placeholder="商户类型" v-model="searchForm.mchType" clearable>
                    <el-option v-for="{code,desc} in $dictArray('MchTypeEnum')"
                               :key="parseInt(code)" :label="desc" :value="parseInt(code)"
                    />
                </el-select>
            </el-form-item>
            <el-form-item>
                <el-button type="primary" @click="refreshList"><i class="el-icon-search"/>查询</el-button>
                <el-button type="success" plain v-authorize="'portal:role:addAdmin'" @click="toCreateRole"><i class="el-icon-plus"/>新增角色</el-button>
            </el-form-item>
        </el-form>

        <div class="">
            <el-table :data="pageResult.data" border>
                <el-table-column label="序号" type="index" width="50px"/>
                <el-table-column label="角色名称" prop="roleName"/>
                <el-table-column label="角色类型" prop="roleType"
                                 :formatter="row=>$dictName('RoleTypeEnum', row.roleType)"/>
                <el-table-column label="商户号" prop="mchNo"/>
                <el-table-column label="商户类型" prop="mchType"
                                 :formatter="row=>$dictName('MchTypeEnum', row.mchType)"/>
                <el-table-column label="可自动分配" prop="autoAssign" :formatter="row=>row.autoAssign===1?'是':'否'"/>
                <el-table-column label="描述" prop="remark"/>
                <el-table-column label="创建时间" prop="createTime"/>
                <el-table-column v-slot="{row}" label="操作">
                    <el-button v-if="row.roleType === 1" v-authorize="'portal:role:assignAdminRoleAuth'" type="text"
                               size="small" @click="toAssignAuth(row.id)">分配权限
                    </el-button>
                    <el-button v-if="row.roleType === 1" v-authorize="'portal:role:editAdmin'" type="text" size="small"
                               @click="toEditRole(row)">编辑
                    </el-button>
                    <el-button v-if="row.roleType === 1" v-authorize="'portal:role:deleteAdmin'" type="text" size="small"
                               @click="toDelete(row.id)">删除
                    </el-button>
                </el-table-column>
            </el-table>
        </div>

        <div class="pagination-container">
            <pagination
                v-show="pageResult.totalRecord > 0"
                :total="pageResult.totalRecord"
                :page.sync="searchForm.currentPage"
                :limit.sync="searchForm.pageSize"
                @pagination="refreshList"
            />
        </div>

        <role-form ref="roleForm" @success="refreshList"/>
        <assign-auth ref="assignAuth"/>
    </div>
</template>

<script>
import Pagination from '@/components/Pagination'
import RoleForm from './RoleForm'
import AssignAuth from './AssignAuth'
import {listRole, deleteAdminRole} from '@/api/portal/role';

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
            searchForm: {
                currentPage: 1,
                pageSize: 10
            },
            pageResult: {
                totalRecord: 0,
            }
        }
    },
    mounted() {
        this.refreshList()
    },
    methods: {
        refreshList() {
            this.loading = true
            listRole(this.searchForm)
                .then(({data}) => {
                    this.pageResult = data
                })
                .finally(() => {
                    this.loading = false
                })
        },
        toAssignAuth(roleId) {
            this.$refs.assignAuth.initAndShow(roleId)
        },
        toCreateRole() {
            this.$refs.roleForm.initForAdd()
        },
        toEditRole(row) {
            this.$refs.roleForm.initForEdit({...row})
        },
        toDelete(id) {
            this.$confirm('删除角色将一并回收商户在此角色下的权限，确定吗?', '提示', {
                confirmButtonText: '确定',
                cancelButtonText: '取消',
                type: 'warning'
            }).then(() => {
                this.loading = true
                deleteAdminRole(id).then(({data}) => {
                    this.$message.success(data)
                    this.refreshList()
                }).finally(() => {
                    this.loading = false
                })
            })
        }
    }
}
</script>
