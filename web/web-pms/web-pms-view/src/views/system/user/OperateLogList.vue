<template>
    <div v-loading="loading" class="app-container">
        <el-form :inline="true" size="small">
            <el-form-item label="创建时间">
                <el-date-picker v-model="searchForm.createTimeBegin" value-format="yyyy-MM-dd HH:mm:ss"
                                type="datetime" :clearable="false"/>
                至
                <el-date-picker v-model="searchForm.createTimeEnd" value-format="yyyy-MM-dd HH:mm:ss" type="datetime"/>
            </el-form-item>
            <el-form-item label="操作类型">
                <el-select placeholder="操作类型" v-model="searchForm.operateType" clearable>
                    <el-option v-for="{code,desc} in $dictArray('OperateLogTypeEnum')"
                               :key="parseInt(code)" :label="desc" :value="parseInt(code)"
                    />
                </el-select>
            </el-form-item>
            <el-form-item label="操作用户">
                <el-input v-model="searchForm.loginName" placeholder="精确查询"/>
            </el-form-item>
            <el-form-item>
                <el-button type="primary" @click="refreshList"><i class="el-icon-search"/>查询</el-button>
            </el-form-item>
        </el-form>

        <div class="">
            <el-table border :data="pageResult.data">
                <el-table-column type="index" label="序号" width="50px"/>
                <el-table-column prop="createTime" label="创建时间" sortable />
                <el-table-column prop="loginName" label="操作用户"/>
                <el-table-column prop="operateType" label="操作类型"
                                 :formatter="row=>$dictName('OperateLogTypeEnum', row.operateType)"/>
                <el-table-column prop="content" label="操作内容"/>
            </el-table>
        </div>

        <div class="pagination-container">
            <pagination
                v-show="pageResult.totalRecord >0 "
                :total="pageResult.totalRecord"
                :page.sync="searchForm.currentPage"
                :limit.sync="searchForm.pageSize"
                @pagination="refreshList"
            />
        </div>
    </div>
</template>
<script>
import Pagination from '@/components/Pagination'
import {listOperateLog} from '@/api/system/user'

export default {
    name: 'OperateLogList',
    components: {
        Pagination
    },
    data() {
        return {
            loading: false,
            searchForm: {
                currentPage: 1,
                pageSize: 10,
                createTimeBegin: this.$dayStart(new Date()),
                createTimeEnd: '',
                loginName: '',
                operateType: ''
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
            listOperateLog(this.searchForm)
                .then(({data}) => {
                    this.pageResult = data
                }).finally(() => {
                    this.loading = false
                })
        },
    }
}
</script>
