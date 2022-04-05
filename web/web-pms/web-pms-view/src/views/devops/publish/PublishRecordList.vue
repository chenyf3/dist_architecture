<template>
    <div v-loading="loading" class="app-container">
        <el-form :inline="true" size="small" label-width="80px">
            <el-form-item label="发布时间">
                <el-date-picker v-model="searchForm.createTimeBegin" value-format="yyyy-MM-dd HH:mm:ss" type="datetime" :clearable="false"/>
                至
                <el-date-picker v-model="searchForm.createTimeEnd" value-format="yyyy-MM-dd HH:mm:ss" type="datetime"/>
            </el-form-item>
            <el-form-item label="状态">
                <el-select v-model="searchForm.status" class="selectInput" clearable>
                    <el-option v-for="(value,key) in publishStatusObj" :key="key" :label="value" :value="key"/>
                </el-select>
            </el-form-item>
            <el-form-item label="项目描述">
                <el-input v-model="searchForm.buildMsg" class="textInput" placeholder="模糊搜索"/>
            </el-form-item>
            <el-form-item label="发布批次">
                <el-input v-model="searchForm.buildNo" class="textInput" placeholder="精确搜索"/>
            </el-form-item>
            <el-form-item label="创建人">
                <el-input v-model="searchForm.creator" class="textInput" placeholder="精确搜索"/>
            </el-form-item>
            <el-form-item>
                <el-button type="primary" @click="refreshList"><i class="el-icon-search"/>查询</el-button>
            </el-form-item>
        </el-form>

        <div>
            <el-button v-authorize="'devops:publish:manage'" @click="addPublishPage" type="success" plain round>提交上线<i class="el-icon-s-promotion"/></el-button>
            <el-button v-authorize="'devops:publish:flowSwitch'" @click="flowSwitchPage" type="primary" plain round>流量切换<i class="el-icon-s-operation"/></el-button>
        </div>

        <div class="tool-panel" >
            <el-button v-authorize="'devops:publish:syncIdcPublish'" @click="publishSyncPage" size="small" type="primary" plain>同步代码</el-button>
        </div>

        <div class="">
            <el-table :data="pageResult.data" style="width: 100%" row-key="id" border @selection-change="selectionChangeHandler">
                <el-table-column type="selection" width="50px" :selectable="row=>row.status === 4 ? true : false"/>
                <el-table-column>
                    <template slot="header"> 发布时间 </template>
                    <template slot-scope="scope">
                        {{ scope.row.createTime | timeFilter }}
                    </template>
                </el-table-column>
                <el-table-column label="项目描述" prop="buildMsg"/>
                <el-table-column>
                    <template slot="header">发布机房 <br> 发布批次 </template>
                    <template slot-scope="scope">
                        {{ getIdcName(scope.row.idc) }} <br> {{ scope.row.buildNo }}
                    </template>
                </el-table-column>
                <el-table-column>
                    <template slot="header">状态</template>
                    <template slot-scope="scope">
                        {{ publishStatusObj[scope.row.status] }}
                    </template>
                </el-table-column>
                <el-table-column>
                    <template slot="header"> 处理次数 <br> 发布次数</template>
                    <template slot-scope="scope">
                        {{ scope.row.processTimes }}<br>{{ scope.row.publishTimes }}
                    </template>
                </el-table-column>
                <el-table-column>
                    <template slot="header"> 创建人 <br> 修改人</template>
                    <template slot-scope="scope">
                        {{ scope.row.creator }}<br>{{ scope.row.modifier }}
                    </template>
                </el-table-column>
                <el-table-column label="操作">
                    <template slot-scope="scope">
                        <el-button v-authorize="'devops:publish:view'" size="mini"
                                   @click="viewPublishPage(scope.row)" type="text" plain>查看
                        </el-button>
                        <el-button v-authorize="'devops:publish:manage'" v-if="scope.row.status === 1 || scope.row.status === 2 || scope.row.status === 3"
                                   size="mini" @click="cancelPublish(scope.row.id)" type="text" plain>取消
                        </el-button>
                        <el-button v-authorize="'devops:publish:manage'" v-if="scope.row.status === 5 || scope.row.status === 6 || scope.row.status === 7"
                                   size="mini" @click="republishPage(scope.row)" type="text" plain>重新发布
                        </el-button>
                        <el-button v-authorize="'devops:publish:manage'" v-if="scope.row.status === 8"
                                   size="mini" @click="auditPage(scope.row)" type="text" plain>审核
                        </el-button>
                    </template>
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

        <publish-form ref="publishForm" @success="refreshList"/>
        <republish-form ref="republishForm" @success="refreshList"/>
        <audit-form ref="auditForm" @success="refreshList"/>
        <flow-switch-form ref="flowSwitchForm" @success="refreshList"/>
        <publish-sync-form ref="publishSyncForm" @success="refreshList"/>
    </div>
</template>

<script>
import PublishForm from './PublishForm';
import RepublishForm from './RepublishForm';
import AuditForm from './AuditForm';
import FlowSwitchForm from './FlowSwitchForm';
import PublishSyncForm from './PublishSyncForm';
import Pagination from '@/components/Pagination'
import {publishRecordList,getPublishInfo,cancelPublish} from '@/api/devops/publish'

export default {
    name: 'PublishRecordList',
    components: {
        Pagination,
        PublishForm,
        RepublishForm,
        AuditForm,
        FlowSwitchForm,
        PublishSyncForm
    },
    data() {
        return {
            loading: false,
            publishStatusObj: {1: '待处理', 2: '排队中', 3: '处理中', 4: '成功', 5: '失败', 6: '不稳定', 7: '已取消', 8: '已超时'},
            idcMap: {},
            searchForm: {
                currentPage: 1,
                pageSize: 10,
                createTimeBegin: this.$dayStart(new Date()),
                createTimeEnd: '',
                creator: '',
                buildMsg: '',
                status: ''
            },
            pageResult: {},
            selectedIds: [],
        };
    },
    mounted() {
        getPublishInfo().then(({data}) => {
            let idcList = data.idcList;
            for (let i = 0; i < idcList.length; i++) {
                this.idcMap[idcList[i].code] = idcList[i].name;
            }
        });
        this.refreshList();
    },
    methods: {
        selectionChangeHandler(val) {
            this.selectedIds = [];
            val.forEach(row => {
                this.selectedIds.push(row.id);
            });
        },
        getIdcName(idcCode){
            return this.idcMap[idcCode] ? this.idcMap[idcCode] : idcCode
        },
        refreshList() {
            this.loading = true;
            publishRecordList(this.searchForm).then(({data}) => {
                this.pageResult = data;
            }).finally(() => {
                this.loading = false;
            });
        },
        addPublishPage() {
            this.$refs.publishForm.initAndShow('ADD', {});
        },
        viewPublishPage(row) {
            let form = {}
            Object.assign(form, row)
            this.$refs.publishForm.initAndShow('VIEW', form);
        },
        republishPage(row) {
            let form = {}
            Object.assign(form, row)
            this.$refs.republishForm.initAndShow(form);
        },
        cancelPublish(id) {
            this.$confirm('确定取消当前的项目发布任务吗?').then(() => {
                cancelPublish(`${id}`).then((resp) => {
                    if(resp.code === 200){
                        this.$message.success(resp.data)
                        this.refreshList()
                    }
                })
            })
        },
        auditPage(row) {
            let form = {}
            Object.assign(form, row)
            this.$refs.auditForm.initAndShow(form);
        },
        flowSwitchPage() {
            this.$refs.flowSwitchForm.show = true;
        },
        publishSyncPage() {
            if(this.selectedIds.length === 0){
                this.$message.info('请选择需要同步的发布记录！')
                return
            }

            let publishRecords = []
            for(let i=0; i<this.pageResult.data.length; i++){
                let row = this.pageResult.data[i]
                if(this.selectedIds.indexOf(row.id) === -1){
                    continue
                }

                let record = {}
                record.id = row.id
                record.buildMsg = row.buildMsg
                record.idcName = this.idcMap[row.idc] ? this.idcMap[row.idc] : row.idc
                record.buildNo = row.buildNo
                record.createTime = row.createTime
                publishRecords.push(record)
            }
            this.$refs.publishSyncForm.publishRecords = publishRecords
            this.$refs.publishSyncForm.show = true;
        },
    }
};
</script>
