<template>
    <el-dialog
        class="app-container"
        :visible.sync="show"
        :close-on-click-modal="false"
        top="1vh"
        title="实例列表"
        append-to-body
        width="1200px"
        @close="closeForm"
    >
        <div class="">
            <el-form :inline="true" size="small">
                <el-form-item>
                    <el-button :loading="loading" plain class="el-button-small" @click="refreshList"><i
                        class="el-icon-search"/>刷新
                    </el-button>
                </el-form-item>
            </el-form>

            <div class="">
                <el-table :data="pageResult.data" style="width: 100%" height="300" border>
                    <el-table-column label="序号" type="index" width="50px" align="center"/>
                    <el-table-column align="center">
                        <template slot="header">创建时间 <br> 实例ID</template>
                        <template slot-scope="scope">
                            {{ scope.row.createTime | timeFilter }} <br> {{ scope.row.instanceId }}
                        </template>
                    </el-table-column>
                    <el-table-column align="center">
                        <template slot="header">HOST <br> IP</template>
                        <template slot-scope="scope">
                            {{ scope.row.host }} <br> {{ scope.row.ip }}
                        </template>
                    </el-table-column>
                    <el-table-column align="center">
                        <template slot="header">调度状态 <br> 实例状态 </template>
                        <template slot-scope="scope">
                            <span :style="scope.row.scheduleStatus === 2 ? 'color:orange' : 'color:green'">
                                {{ scope.row.scheduleStatus | MapFilter(scheduleStatusObj) }}
                            </span>
                            <br>
                            <span :style="scope.row.status === 2 ? '' : 'color:green'">
                                {{ scope.row.status | MapFilter(instanceStatusObj) }}
                            </span>
                        </template>
                    </el-table-column>
                    <el-table-column align="center">
                        <template slot="header">心跳时间 <br> RPC地址 </template>
                        <template slot-scope="scope">
                            {{ scope.row.updateTime | timeFilter }}
                            <span :style="scope.row.isHealth ? 'color:green' : ''">
                                {{ scope.row.isHealth ? "(健康)" : "(离线)" }}
                            </span>
                            <br>
                            <span>
                                {{ scope.row.url }}
                            </span>
                        </template>
                    </el-table-column>
                    <el-table-column v-slot="{row}" label="操作" align="center">
                        <el-button v-if="row.isHealth && row.scheduleStatus == 1" size="small"
                                   @click="adminInstance(row.instanceId, row.status)" type="danger" plain>挂起实例
                        </el-button>
                        <el-button v-if="row.isHealth && row.scheduleStatus == 2" size="small"
                                   @click="adminInstance(row.instanceId, row.status)" type="success" plain>恢复实例
                        </el-button>
                    </el-table-column>
                </el-table>
            </div>
        </div>
    </el-dialog>
</template>

<script>
import {listInstance, adminInstance} from '@/api/devops/timer';

export default {
    name: 'InstanceList',
    data() {
        return {
            show: false,
            loading: false,
            instanceStatusObj: {1: '已启动', 2: '已关闭'},
            scheduleStatusObj: {1: '运行中', 2: '挂起中'},
            searchParam: {
                jobType: null,
                jobGroup: null,
                jobName: null,
                jobStatus: null,
                jobDescription: null,
                sort: null
            },
            pageResult: {}
        };
    },
    methods: {
        refreshList() {
            this.loading = true;
            listInstance().then(({data}) => {
                this.pageResult = data;
            }).finally(() => {
                this.loading = false;
            });
        },
        adminInstance(instanceId, scheduleStatus) {
            const msg = scheduleStatus === 1 ? '挂起' : status === 2 ? '恢复' : '';
            this.$confirm(`确定${msg}当前实例吗?`)
                .then(() => adminInstance(instanceId))
                .then(({data}) => {
                    this.$message.success(data);
                    this.refreshList();
                });
        },
        closeForm() {
            this.pageResult = {};
        }
    }
};
</script>
