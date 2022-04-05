<template>
    <div v-loading="loading" class="app-container">
        <el-form :inline="true" size="small">
            <el-form-item label="登录名">
                <el-input v-model="searchForm.loginName" placeholder="精确匹配"></el-input>
            </el-form-item>
            <el-form-item label="姓名">
                <el-input v-model="searchForm.realName" placeholder="模糊匹配"></el-input>
            </el-form-item>
            <el-form-item label="商户号">
                <el-input v-model="searchForm.mchNo" placeholder="精确匹配"></el-input>
            </el-form-item>
            <el-form-item label="手机号">
                <el-input v-model="searchForm.mobileNo" placeholder="精确匹配"></el-input>
            </el-form-item>
            <el-form-item label="用户类型">
                <el-select placeholder="用户类型" v-model="searchForm.type" clearable>
                    <el-option v-for="{code,desc} in $dictArray('UserTypeEnum')"
                               :key="parseInt(code)" :label="desc" :value="parseInt(code)"
                    />
                </el-select>
            </el-form-item>
            <el-form-item label="状态">
                <el-select placeholder="状态" v-model="searchForm.status" clearable>
                    <el-option v-for="{code,desc} in $dictArray('UserStatusEnum')"
                               :key="parseInt(code)" :label="desc" :value="parseInt(code)"
                    />
                </el-select>
            </el-form-item>
            <el-form-item>
                <el-button type="primary" @click="refreshList"><i class="el-icon-search"/>查询</el-button>
                <el-button type="success" plain v-authorize="'portal:user:addAdmin'" @click="addPortalUserPage"><i class="el-icon-plus"/>新增用户</el-button>
            </el-form-item>
        </el-form>

        <div class="">
            <el-table :data="pageResult.data" border>
                <el-table-column label="序号" type="index" width="50px"/>
                <el-table-column label="登录名" prop="loginName"/>
                <el-table-column label="姓名" prop="realName"/>
                <el-table-column label="手机号" prop="mobileNo"/>
                <el-table-column label="邮箱" prop="email"/>
                <el-table-column label="商户编号" prop="mchNo"/>
                <el-table-column label="商户类型" prop="mchType"
                                 :formatter="row=>$dictName('MchTypeEnum', row.mchType)"/>
                <el-table-column label="用户类型" prop="type"
                                 :formatter="row=>$dictName('UserTypeEnum', row.type)"/>
                <el-table-column label="状态" v-slot="{row}">
                     <span :style="row.status === 1 ? 'color:green;' : row.status === 2 ? 'color:red' : ''">
                         {{ $dictName('UserStatusEnum', row.status) }}
                     </span>
                </el-table-column>
                <el-table-column label="操作" v-slot="{row}" min-width="100px">
                    <el-button v-authorize="'portal:user:edit'" type="text" size="small"
                               @click="editPortalUserPage(row)">编辑
                    </el-button>
                    <el-button v-if="row.type === 1" :key="row.loginName"
                               v-authorize="'portal:user:assignAdminRoles'" type="text" size="small"
                               @click="assignAdminRolesPage(row)">关联角色
                    </el-button>
                    <el-button v-authorize="'portal:user:changeStatus'" type="text" size="small" @click="changeStatus(row)"
                               v-text="row.status === 1 ? '冻结' : row.status === 2 ? '激活' : '审核'" :style="row.status === 1 ? 'color:red;' : ''"/>
                </el-table-column>
            </el-table>
        </div>

        <div class="pagination-container">
            <pagination
                v-show="pageResult.totalRecord>0"
                :total="pageResult.totalRecord"
                :page.sync="searchForm.currentPage"
                :limit.sync="searchForm.pageSize"
                @pagination="refreshList"
            />
        </div>

        <user-from ref="userFrom" @success="refreshList"/>
        <assign-role-from ref="assignRoleFrom"/>
    </div>
</template>

<script>
import Pagination from '@/components/Pagination'
import UserFrom from './UserForm'
import AssignRoleFrom from './AssignRole'
import {listUser, changeStatus} from '@/api/portal/user';

export default {
    name: 'PortalUserList',
    components: {
        Pagination,
        UserFrom,
        AssignRoleFrom
    },
    data() {
        return {
            loading: false,
            searchForm: {
                currentPage: 1,
                pageSize: 10,
                type: 1, //默认只查管理员类型的用户
                mchNo: '',
                loginName: '',
                mobileNo: '',
                realName: '',
                status: '',
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
            listUser(this.searchForm)
                .then(({data}) => {
                    this.pageResult = data
                })
                .finally(() => {
                    this.loading = false
                })
        },
        addPortalUserPage() {
            this.$refs.userFrom.initAndShow('ADD')
        },
        editPortalUserPage(row) {
            this.$refs.userFrom.initAndShow('EDIT', {...row})
        },
        changeStatus(row) {
            let msg = row.status === 1 ? '冻结' : row.status === 2 ? '激活' : '审核通过'
            this.$confirm(`确定${msg} [${row.realName}] 这个用户吗?`)
                .then(() => changeStatus(`${row.id}`))
                .then(({data}) => {
                    this.$message.success(data)
                    this.refreshList()
                })
        },
        assignAdminRolesPage(row) {
            this.$refs.assignRoleFrom.initAndShow(row.id)
        },
    }
}
</script>

<style lang="scss" scoped>
.accountList-container {
    height: calc(100vh - 84px);
    padding: 30px 40px;
    background: #f7f7f7;

    .search-container {
        padding: 30px 30px;
        background: #fff;

        .search-item {
            display: inline-block;
            margin-right: 20px;

            .el-input,
            .el-select {
                width: 180px;
            }
        }

        .search-btn {
            float: right;
        }
    }

    .content-container {
        margin-top: 30px;
        background: #fff;
        padding: 10px 20px;
    }

    .pagination {
        margin: 5px;
    }
}
</style>
