<template>
    <div v-loading="loading" class="app-container">
        <el-form :inline="true" size="small">
            <el-form-item label="任务类型">
                <el-select v-model="searchForm.jobType" clearable>
                    <el-option v-for="(value,key) in jobTypeObj" :key="key" :label="value" :value="key"/>
                </el-select>
            </el-form-item>
            <el-form-item label="任务分组">
                <el-input v-model="searchForm.jobGroup" placeholder="精确搜索"/>
            </el-form-item>
            <el-form-item label="任务名称">
                <el-input v-model="searchForm.jobName" placeholder="精确搜索"/>
            </el-form-item>
            <el-form-item label="任务状态">
                <el-select v-model="searchForm.jobStatus" clearable>
                    <el-option v-for="(value,key) in jobStatusObj" :key="key" :label="value" :value="key"/>
                </el-select>
            </el-form-item>
            <el-form-item label="任务描述">
                <el-input v-model="searchForm.jobDescription" placeholder="模糊搜索"/>
            </el-form-item>
            <el-form-item label="排序字段">
                <el-select v-model="searchForm.sort" clearable>
                    <el-option v-for="(value,key) in sortObj" :key="key" :label="value" :value="key"/>
                </el-select>
            </el-form-item>
            <el-form-item>
                <el-button type="primary" @click="refreshList"><i class="el-icon-search"/>查询</el-button>
                <el-button v-authorize="'devops:timer:manage'" @click="addScheduleJobPage" type="success" plain><i class="el-icon-plus"/>添加任务</el-button>
                <el-button v-authorize="'devops:timer:instanceManage'" @click="instanceListPage">实例列表</el-button>
                <el-button v-authorize="'devops:timer:opLogList'" @click="opLogPage">操作日志</el-button>
            </el-form-item>
        </el-form>

        <div class="">
            <el-table :data="pageResult.data" style="width: 100%" border>
                <el-table-column label="序号" type="index" width="50px"/>
                <el-table-column>
                    <template slot="header"> 任务分组 <br> 任务名称</template>
                    <template slot-scope="scope">
                        {{ scope.row.jobGroup }} <br> {{ scope.row.jobName }}
                    </template>
                </el-table-column>
                <el-table-column min-width="166">
                    <template slot="header"> 上次执行时间 <br> 下次执行时间</template>
                    <template slot-scope="scope">
                        {{ scope.row.lastExecuteTime | timeFilter }}<br>{{ scope.row.nextExecuteTime | timeFilter }}
                    </template>
                </el-table-column>
                <el-table-column>
                    <template slot="header"> 任务类型 <br> 任务状态</template>
                    <template slot-scope="scope">
                        {{ Number(scope.row.jobType) | MapFilter(jobTypeObj) }}<br>{{ scope.row.jobStatus | MapFilter(jobStatusObj) }}
                    </template>
                </el-table-column>
                <el-table-column>
                    <template slot="header"> 任务间隔 <br> cron表达式</template>
                    <template slot-scope="scope">
                        <span v-if="scope.row.jobType === 1" >
                              {{ (Number(scope.row.intervals)) }}{{ Number(scope.row.intervalUnit) | MapFilter(intervalUnitObj) }}
                        </span>
                        <br>
                        <span v-if="scope.row.jobType === 2" >
                            {{ scope.row.cronExpression }}
                        </span>
                    </template>
                </el-table-column>
                <el-table-column label="通知地址">
                    <template slot-scope="scope">
                        {{ scope.row.destination }}
                    </template>
                </el-table-column>
                <el-table-column label="任务描述">
                    <template slot-scope="scope">
                        {{ scope.row.jobDescription }}
                    </template>
                </el-table-column>
                <el-table-column label="已执行次数">
                    <template slot-scope="scope">
                        {{ scope.row.executedTimes }}
                    </template>
                </el-table-column>
                <el-table-column v-slot="{row}" label="操作">
                    <el-button v-if="row.jobStatus !== 'PAUSED'" v-authorize="'devops:timer:operate'" type="text"
                               size="small" @click="pauseScheduleJob(row)">暂停
                    </el-button>
                    <el-button v-if="row.jobStatus === 'PAUSED'" v-authorize="'devops:timer:operate'" type="text"
                               size="small" @click="resumeScheduleJob(row)">恢复
                    </el-button>
                    <el-button v-if="row.jobStatus !== 'PAUSED'" v-authorize="'devops:timer:operate'" type="text"
                               size="small" @click="triggerScheduleJob(row)">触发
                    </el-button>
                    <el-button v-authorize="'devops:timer:list'" type="text" size="small"
                               @click="viewScheduleJobPage(row)">查看
                    </el-button>
                    <el-button v-authorize="'devops:timer:manage'" type="text" size="small"
                               @click="editScheduleJobPage(row)">编辑
                    </el-button>
                    <el-button v-authorize="'devops:timer:manage'" type="text" size="small"
                               @click="deleteScheduleJob(row)">删除
                    </el-button>
                </el-table-column>
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

        <scheduleJob-form ref="scheduleJobForm" @success="refreshList"/>
        <instance-list ref="instanceList" @success="refreshList"/>
        <opLog-list ref="opLogList" @success="refreshList"/>
    </div>
</template>

<script>
import Pagination from '@/components/Pagination'
import ScheduleJobForm from './ScheduleJobForm';
import InstanceList from './InstanceList';
import OpLogList from './OpLogList';
import {listScheduleJob,pauseJob,resumeJob,triggerJob,deleteJob} from '@/api/devops/timer'

export default {
    name: 'ScheduleJobList',
    components: {
        Pagination,
        ScheduleJobForm,
        InstanceList,
        OpLogList
    },
    data() {
        return {
            loading: false,
            jobTypeObj: {1: '间隔任务', 2: 'cron任务'},
            jobStatusObj: {NONE: '无', NORMAL: '正常', PAUSED: '暂停中', COMPLETE: '已完成', ERROR: '异常', BLOCKED: '阻塞'},
            sortObj: {ID_DESC: 'ID降序', CREATE_TIME_DESC: '创建时间降序', GROUP_ASC: '分组名升序', NAME_ASC: '任务名升序'},
            intervalUnitObj: {1: '毫秒', 2: '秒', 3: '分', 4: '时'},
            searchForm: {
                currentPage: 1,
                pageSize: 20,
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
    mounted() {
        this.refreshList();
    },
    methods: {
        refreshList() {
            this.loading = true;
            listScheduleJob(this.searchForm)
                .then(({data}) => {
                    this.pageResult = data;
                })
                .finally(() => {
                    this.loading = false;
                });
        },
        addScheduleJobPage() {
            this.$refs.scheduleJobForm.initAndShow('ADD', {});
        },
        editScheduleJobPage(row) {
            this.$refs.scheduleJobForm.initAndShow('EDIT', {...row});
        },
        viewScheduleJobPage(row) {
            this.$refs.scheduleJobForm.initAndShow('VIEW', {...row});
        },
        pauseScheduleJob(row) {
            this.$confirm('确定暂停当前任务吗?')
                .then(() => pauseJob(row.jobGroup, row.jobName))
                .then(({data}) => {
                    this.$message.success(data);
                    this.refreshList();
                });
        },
        resumeScheduleJob(row) {
            this.$confirm('确定恢复当前任务吗?')
                .then(() => resumeJob(row.jobGroup, row.jobName))
                .then(({data}) => {
                    this.$message.success(data);
                    this.refreshList();
                });
        },
        triggerScheduleJob(row) {
            this.$confirm('确定触发当前任务吗?')
                .then(() => triggerJob(row.jobGroup, row.jobName))
                .then(({data}) => {
                    this.$message.success(data);
                    this.refreshList();
                });
        },
        deleteScheduleJob(row) {
            this.$confirm('确定删除当前任务吗?')
                .then(() => deleteJob(row.jobGroup, row.jobName))
                .then(({data}) => {
                    this.$message.success(data);
                    this.refreshList();
                });
        },
        instanceListPage() {
            this.$refs.instanceList.show = true;
            this.$refs.instanceList.refreshList();
        },
        opLogPage() {
            this.$refs.opLogList.show = true;
            this.$refs.opLogList.refreshList();
        }
    }
};
</script>
