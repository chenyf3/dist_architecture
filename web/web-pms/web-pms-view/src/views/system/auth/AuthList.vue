<template>
    <div class="app-container">
        <div class="filter-container">
            <div class="filter-item">
                <el-button :loading="loading" class="el-button-small" @click="refreshAuthList" type="primary"><i class="el-icon-search"/>刷新</el-button>
                <el-button v-authorize="'sys:auth:add'" class="el-button-small" @click="addAuthPage({id:0, name:'根节点'})" type="success" plain>
                    <i class="el-icon-plus"/>添加顶级菜单
                </el-button>
            </div>
        </div>

        <div class="">
            <el-table :data="treeData" border row-key="id"
                      :tree-props="{children: 'children', hasChildren: 'hasChildren'}">
                <el-table-column label="权限名称" prop="name"/>
                <el-table-column label="排序编号" prop="number"/>
                <el-table-column label="URL" prop="url"/>
                <el-table-column label="权限标识" prop="permissionFlag"/>
                <el-table-column label="权限类型" prop="authType"
                                 :formatter="row => (row.authType === 1 && '菜单') || (row.authType === 2 && '功能')"/>
                <el-table-column v-slot="{row}" label="操作">
                    <el-button v-authorize="'sys:auth:edit'" type="text" size="mini"
                               @click="editAuthPage(row)">编辑
                    </el-button>
                    <el-button v-authorize="'sys:auth:add'" v-if="row.authType===1" type="text" size="mini"
                               @click="addAuthPage(row)">添加权限
                    </el-button>
                    <el-button v-authorize="'sys:auth:delete'" type="text" size="mini"
                               @click="deleteAuth(row)">删除
                    </el-button>
                </el-table-column>
            </el-table>
        </div>
        <auth-form ref="authForm" @success="refreshAuthList"/>
    </div>
</template>

<script>
import AuthForm from './AuthForm'
import {listAllAuth, deleteAuth} from '@/api/system/auth'

const buildTreeData = function (pid, pidGroup, resultArr) {
    if (!pidGroup[pid]) {
        return
    }
    pidGroup[pid].forEach(f => {
        resultArr.push(f)
        if (pidGroup[f.id]) {
            f.children = []
            buildTreeData(f.id, pidGroup, f.children)
        }
    })
}

export default {
    name: 'AuthList',
    components: {
        AuthForm
    },
    data() {
        return {
            loading: false,
            auths: []
        }
    },
    computed: {
        treeData() {
            const pidGroup = []
            this.auths.forEach(f => {
                if (!pidGroup[f.parentId]) {
                    pidGroup[f.parentId] = [f]
                } else {
                    pidGroup[f.parentId].push(f)
                }
            })
            const resultArr = []
            buildTreeData(0, pidGroup, resultArr)
            return resultArr
        }
    },
    mounted() {
        this.refreshAuthList()
    },
    methods: {
        refreshAuthList() {
            this.loading = true
            listAllAuth().then(({data}) => {
                this.auths = data
            }).finally(() => {
                this.loading = false
            })
        },
        addAuthPage(row) {
            this.$refs.authForm.initAndShow('ADD', {...row})
        },
        editAuthPage(row) {
            this.$refs.authForm.initAndShow('EDIT', {...row})
        },
        deleteAuth(row) {
            this.$confirm('确定删除 [' + row.name + '] 这个权限吗?')
                .then(() => deleteAuth(`${row.id}`))
                .then(({data}) => {
                    this.$message.success(data)
                    this.refreshAuthList()
                })
        }
    }
}
</script>

