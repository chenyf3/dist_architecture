<template>
    <div v-loading="loading" class="app-container">
        <el-form :inline="true" size="small">
            <el-form-item label="登录名">
                <el-input v-model="searchForm.loginName" placeholder="精确查询"></el-input>
            </el-form-item>
            <el-form-item label="姓名">
                <el-input v-model="searchForm.realName" placeholder="模糊查询"></el-input>
            </el-form-item>
            <el-form-item label="手机号">
                <el-input v-model="searchForm.mobileNo" placeholder="精确查询"></el-input>
            </el-form-item>
            <el-form-item label="状态">
                <el-select placeholder="状态" v-model="searchForm.status" clearable>
                    <el-option v-for="{code,desc} in $dictArray('UserStatusEnum')"
                               :key="parseInt(code)" :label="desc" :value="parseInt(code)"
                    />
                </el-select>
            </el-form-item>
            <el-form-item label="用户类型">
                <el-select placeholder="用户类型" v-model="searchForm.type" clearable>
                    <el-option v-for="{code,desc} in $dictArray('UserTypeEnum')"
                               :key="parseInt(code)" :label="desc" :value="parseInt(code)"
                    />
                </el-select>
            </el-form-item>
            <el-form-item>
                <el-button @click="refreshList" type="primary" ><i class="el-icon-search"/>查询</el-button>
                <el-button v-authorize="'sys:user:add'" @click="addUserPage" type="success" plain><i class="el-icon-plus"/>新增用户</el-button>
            </el-form-item>
        </el-form>

        <div>
            <el-table :data="pageResult.data" border row-key="id">
                <el-table-column label="序号" type="index" width="50px"/>
                <el-table-column label="登录名" prop="loginName"/>
                <el-table-column label="姓名" prop="realName"/>
                <el-table-column label="手机号" prop="mobileNo"/>
                <el-table-column label="用户类型" prop="type" :formatter="row=>$dictName('UserTypeEnum', row.type)"/>
                <el-table-column label="状态" v-slot="{row}">
                     <span :style="row.status === 1 ? 'color:green;' : row.status === 2 ? 'color:red' : ''">
                         {{ $dictName('UserStatusEnum', row.status) }}
                     </span>
                </el-table-column>
                <el-table-column label="操作" v-slot="{row}" min-width="100px">
                    <el-button v-if="row.type !== 1" v-authorize="'sys:user:edit'" type="text" size="mini"
                               @click="editUserPage(row)">编辑
                    </el-button>
                    <el-button v-if="row.type !== 1" v-authorize="'sys:user:assignRoles'" type="text" size="mini"
                               @click="assignRolesPage(row)">关联角色
                    </el-button>
                    <el-button v-if="row.type !== 1" v-authorize="'sys:user:resetPwd'" type="text" size="mini"
                               @click="resetPwdPage(row)">重置密码
                    </el-button>
                    <el-button v-if="row.type !== 1" v-authorize="'sys:user:changeStatus'" type="text" size="mini"
                               @click="changeUserStatus(row)">
                        {{ row.status === 1 ? '冻结' : row.status === 2 ? '激活' : row.status === 3 ? '审核' : '' }}
                    </el-button>
                    <el-button v-if="row.type !== 1" v-authorize="'sys:user:delete'" type="text" size="mini" :style="'color:red;'"
                               @click="deleteUser(row)">删除
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

        <user-form ref="userForm" @success="refreshList"/>
        <assign-roles ref="assignRoles"/>
        <reset-pwd ref="resetPwd"/>
    </div>
</template>

<script>
import Pagination from '@/components/Pagination'
import UserForm from './UserForm'
import AssignRoles from './AssignRoles'
import ResetPwd from './ResetPwd'
import {listUser,changeUserStatus,deleteUser} from '@/api/system/user';

export default {
    name: 'UserList',
    components: {
        Pagination,
        UserForm,
        AssignRoles,
        ResetPwd
    },
    data() {
        return {
            loading: false,
            searchForm: {
                currentPage: 1,
                pageSize: 10,
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
            listUser(this.searchForm).then(res => {
                this.pageResult = res.data
            }).finally(() => {
                this.loading = false
            })
        },
        addUserPage() {
            this.$refs.userForm.initAndShow('ADD')
        },
        editUserPage(row) {
            this.$refs.userForm.initAndShow('EDIT', row.id)
        },
        assignRolesPage(row) {
            this.$refs.assignRoles.initAndShow(row.id)
        },
        resetPwdPage(row) {
            this.$refs.resetPwd.initAndShow({...row})
        },
        changeUserStatus(row) {
            const msg = (row.status === 1 && '冻结') || (row.status === 2 && '激活') || (row.status === 3 && '审核通过')
            this.$confirm('确定' + msg + ' [' + row.realName + '] 这个用户吗？')
                .then(() => {
                    changeUserStatus(`${row.id}`).then(({data}) => {
                        this.$message.success(data)
                        this.refreshList()
                    })
                })
        },
        deleteUser(row) {
            this.$confirm(`确定删除操作员 [${row.realName}] 吗?`)
                .then(() => {
                        deleteUser(`${row.id}`).then(({data}) => {
                            this.$message.success(data)
                            this.refreshList()
                        });
                })
        }
    }
}
</script>
