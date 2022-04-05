<template>
    <el-dialog
        class="app-container"
        :visible.sync="show"
        :close-on-click-modal="false"
        top="1vh"
        title="操作日志"
        append-to-body
        width="1100px"
        @close="closeForm"
    >
        <div class="">
            <el-form :inline="true" size="mini">
                <el-form-item label="操作时间">
                    <el-date-picker v-model="searchForm.createTimeBegin" value-format="yyyy-MM-dd HH:mm:ss"
                                    type="datetime" :clearable="false"/>
                    至
                    <el-date-picker v-model="searchForm.createTimeEnd" value-format="yyyy-MM-dd HH:mm:ss"
                                    type="datetime"/>
                </el-form-item>
                <el-form-item label="操作主体">
                    <el-input v-model="searchForm.objKey" placeholder="精确搜索"/>
                </el-form-item>
                <el-form-item label="操作备注">
                    <el-input v-model="searchForm.remark" placeholder="模糊搜索"/>
                </el-form-item>
                <el-form-item>
                    <el-button type="primary" @click="refreshList"><i class="el-icon-search"/>查询</el-button>
                </el-form-item>
            </el-form>

            <div class="">
                <el-table :data="pageResult.data" style="width: 100%" height="300" border>
                    <el-table-column label="序号" type="index" width="50px"/>
                    <el-table-column>
                        <template slot="header">操作时间</template>
                        <template slot-scope="scope">
                            {{ scope.row.createTime | timeFilter }}
                        </template>
                    </el-table-column>
                    <el-table-column label="操作主体" prop="objKey" />
                    <el-table-column label="备注" prop="remark" />
                    <el-table-column label="内容描述" prop="content" max-width="500" />
                </el-table>
            </div>

            <div class="pagination-container">
                <pagination
                    v-show="pageResult.totalRecord > 0"
                    :page-sizes="[10,20,30,50,100,500]"
                    :total="pageResult.totalRecord"
                    :page.sync="searchForm.currentPage"
                    :limit.sync="searchForm.pageSize"
                    @pagination="refreshList"
                />
            </div>
        </div>
    </el-dialog>
</template>

<script>
import Pagination from '@/components/Pagination';
import {listTimerOpLog} from '@/api/devops/timer';

export default {
    name: 'OpLogList',
    components: {
        Pagination
    },
    data() {
        return {
            show: false,
            searchForm: {
                currentPage: 1,
                pageSize: 20,
                createTimeBegin: this.$dayStart(new Date()),
                createTimeEnd: null,
                objKey: null,
                operator: null
            },
            pageResult: {}
        };
    },
    methods: {
        refreshList() {
            this.loading = true;
            listTimerOpLog(this.searchForm)
                .then(({data}) => {
                    this.pageResult = data;
                }).finally(() => {
                    this.loading = false;
                });
        },
        closeForm() {
            this.pageResult = {totalRecord: 0};
        }
    }
};
</script>
