<template>
    <div class="app-container">
        <el-form :inline="true" size="small">
            <el-form-item label="创建时间">
                <el-date-picker v-model="searchForm.createTimeBegin" value-format="yyyy-MM-dd HH:mm:ss"
                                type="datetime" :clearable="false"/>
                至
                <el-date-picker v-model="searchForm.createTimeEnd" value-format="yyyy-MM-dd HH:mm:ss" type="datetime"/>
            </el-form-item>
            <el-form-item label="状态">
                <el-select placeholder="状态" v-model="searchForm.status" clearable>
                    <el-option v-for="{code,desc} in $dictArray('RevokeAuthStatusEnum')"
                               :key="parseInt(code)" :label="desc" :value="parseInt(code)"
                    />
                </el-select>
            </el-form-item>
            <el-form-item>
                <el-button type="primary" @click="refreshList"><i class="el-icon-search"/>查询</el-button>
            </el-form-item>
        </el-form>

        <div class="">
            <el-table border :data="pageResult.data">
                <el-table-column type="index" label="序号" width="50px"/>
                <el-table-column prop="createTime" label="创建时间" sortable/>
                <el-table-column prop="creator" label="创建人"/>
                <el-table-column prop="status" label="状态"
                                 :formatter="row=>$dictName('RevokeAuthStatusEnum', row.status)"/>
                <el-table-column prop="revokeType" label="回收类型"
                                 :formatter="row=>$dictName('RevokeAuthTypeEnum', row.revokeType)"/>
                <el-table-column prop="objectKey" label="回收对象"/>
                <el-table-column prop="remark" label="说明"/>
                <el-table-column v-slot="{row}" label="操作">
                    <el-button v-authorize="'portal:auth:revoke'" type="text" size="small"
                               @click="revokeAuthView(row)">查看
                    </el-button>
                    <el-button v-authorize="'portal:auth:doRevoke'" v-if="row.status !== 3" size="small"
                               @click="doRevokeAuth(row)" type="primary" plain>提交任务
                    </el-button>
                </el-table-column>
            </el-table>
        </div>

        <div class="pagination-container">
            <pagination
                v-show="pageResult.totalRecord>0"
                :page-sizes="[10,20,30,50,100,500]"
                :total="pageResult.totalRecord"
                :page.sync="searchForm.currentPage"
                :limit.sync="searchForm.pageSize"
                @pagination="refreshList"
            />
        </div>

        <revoke-auth-view ref="revokeAuthView"/>
    </div>
</template>

<script>
import Pagination from '@/components/Pagination'
import RevokeAuthView from './RevokeAuthView'
import {listRevokeAuth, doRevokeAuth} from '@/api/portal/auth';

export default {
    name: 'RevokeAuthList',
    components: {
        Pagination,
        RevokeAuthView
    },
    data() {
        return {
            show: false,
            searchForm: {
                currentPage: 1,
                pageSize: 15,
                createTimeBegin: this.$dayStart(new Date()),
                createTimeEnd: '',
                status: undefined
            },
            pageResult: {
                totalRecord: 0
            }
        }
    },
    mounted() {
        this.refreshList()
    },
    methods: {
        refreshList() {
            this.show = true
            listRevokeAuth(this.searchForm)
                .then(({data}) => {
                    this.pageResult = data
                })
                .finally(() => {
                    this.loading = false
                })
        },
        revokeAuthView(row) {
            this.$refs.revokeAuthView.initAndShow({...row})
        },
        doRevokeAuth(row) {
            this.$confirm('确定提交此任务吗?')
                .then(() => doRevokeAuth(`${row.id}`))
                .then(({data}) => {
                    this.$message.success(data)
                    this.$emit('success')
                    this.refreshList()
                })
        },
        closeForm() {
            this.show = false
            this.searchParam = {}
            this.pageResult = {}
        },
    }
}
</script>
