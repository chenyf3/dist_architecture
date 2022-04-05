<template>
    <div v-loading="loading" class="app-container">
        <el-form :inline="true" size="small" label-position="left" label-width="80px">
            <el-form-item label="消息时间">
                <el-date-picker v-model="searchForm.msgTimeBegin" value-format="yyyy-MM-dd HH:mm:ss" type="datetime"
                                :clearable="false" class="datetimeInput"/>
                <span style="margin-left: 38px; margin-right: 37px">至</span>
                <el-date-picker v-model="searchForm.msgTimeEnd" value-format="yyyy-MM-dd HH:mm:ss" type="datetime"
                                :clearable="false" class="datetimeInput"/>
            </el-form-item>
            <el-form-item label="消息ID">
                <el-input v-model="searchForm.traceId" class="textInput" placeholder="精确搜索"/>
            </el-form-item>
            <el-form-item label="业务线">
                <el-select v-model="searchForm.topicGroup" clearable @change="groupChange" class="selectInput">
                    <el-option v-for="(value,key) in groupMap" :key="key" :label="value" :value="key"/>
                </el-select>
            </el-form-item>
            <el-form-item label="发送队列">
                <el-select v-model="searchForm.topic" clearable @change="topicChange" class="selectInput">
                    <el-option v-for="(value,key) in currTopicMap" :key="key" :label="value" :value="key"/>
                </el-select>
            </el-form-item>
            <el-form-item label="消费队列">
                <el-select v-model="searchForm.consumeDest" class="selectInput" clearable>
                    <el-option v-for="(value,key) in currConsumeMap" :key="key" :label="value" :value="key"/>
                </el-select>
            </el-form-item>
            <el-form-item label="消息状态">
                <el-select v-model="searchForm.msgStatus" class="selectInput" clearable>
                    <el-option v-for="(value,key) in msgStatusObj" :key="key" :label="value" :value="key"/>
                </el-select>
            </el-form-item>
            <el-form-item label="投递类型">
                <el-select v-model="searchForm.type" class="selectInput" clearable>
                    <el-option v-for="(value,key) in typeObj" :key="key" :label="value" :value="key"/>
                </el-select>
            </el-form-item>
            <el-form-item label="补发创建">
                <el-select v-model="searchForm.resend" class="selectInput" clearable>
                    <el-option v-for="(value,key) in resendObj" :key="key" :label="value" :value="key"/>
                </el-select>
            </el-form-item>
            <el-form-item label="商户编号">
                <el-input v-model="searchForm.mchNo" class="textInput" placeholder="精确搜索" />
            </el-form-item>
            <el-form-item label="流水号">
                <el-input v-model="searchForm.trxNo" class="textInput" placeholder="精确搜索" />
            </el-form-item>
            <el-form-item>
                <el-button type="primary" @click="refreshList"><i class="el-icon-search"/>查询</el-button>
                <el-button v-authorize="'devops:mqTrace:manage'" @click="sendCompensatePage" type="success" plain><i class="el-icon-plus"/>录入消息</el-button>
            </el-form-item>
        </el-form>

        <div class="tool-panel">
            <el-button v-authorize="'devops:mqTrace:manage'" @click="resendMsgBatch" size="small" type="primary" plain><i class="el-icon-plus"/>补发</el-button>
        </div>

        <div class="">
            <el-table :data="pageResult.data" style="width: 100%" row-key="id" border
                      @selection-change="selectionChangeHandler">
                <el-table-column type="selection" width="50px" :selectable="row=>row.resend === 2 ? true : false"/>
                <el-table-column min-width="166">
                    <template slot="header"> 消息时间 <br> 创建时间</template>
                    <template slot-scope="scope">
                        {{ scope.row.msgTime | timeFilter }}<br>{{ scope.row.createTime | timeFilter }}
                    </template>
                </el-table-column>
                <el-table-column>
                    <template slot="header"> 消息ID <br> 补发创建 </template>
                    <template slot-scope="scope">
                        {{ scope.row.traceId }} <br> {{ scope.row.resend === 1 ? '是' : '否' }}
                    </template>
                </el-table-column>
                <el-table-column>
                    <template slot="header"> 商户编号 <br> 流水号</template>
                    <template slot-scope="scope">
                        {{ scope.row.mchNo }}<br>{{ scope.row.trxNo }}
                    </template>
                </el-table-column>
                <el-table-column>
                    <template slot="header"> 投递类型 <br> 投递次数</template>
                    <template slot-scope="scope">
                        {{ scope.row.type | MapFilter(typeObj) }}<br>{{ scope.row.deliveryCount }}
                    </template>
                </el-table-column>
                <el-table-column>
                    <template slot="header"> 客户端标识 <br> 消息状态</template>
                    <template slot-scope="scope">
                        {{ scope.row.clientFlag }}<br>{{ scope.row.msgStatus | MapFilter(msgStatusObj) }}
                    </template>
                </el-table-column>
                <el-table-column>
                    <template slot="header"> 业务线 <br> 发送队列</template>
                    <template slot-scope="scope">
                        {{ getTopicGroupName(scope.row.topicGroup) }} <br>{{ getTopicName(scope.row.topic) }}
                    </template>
                </el-table-column>
                <el-table-column label="操作">
                    <template slot-scope="scope">
                        <el-button v-authorize="'devops:mqTrace:list'" size="mini"
                                   @click="viewMQTracePage(scope.row)" type="text" plain>查看</el-button>
                        <el-button v-authorize="'devops:mqTrace:manage'" size="mini" v-if="scope.row.resend === 2"
                                   @click="resendOriMsg(scope.row.id)" type="primary" plain>补发</el-button>
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

        <viewMQTrace ref="viewMQTrace" @success="refreshList"/>
        <compensateMsg ref="compensateMsg" @success="refreshList"/>
    </div>
</template>

<script>
import Pagination from '@/components/Pagination'
import ViewMQTrace from './ViewMQTrace';
import CompensateMsg from './CompensateMsg';
import {getTopicInfo, listMQTrace, resendOriMsg, resendOriMsgBatch} from '@/api/devops/mqtrace';

export default {
    name: 'MQTraceList',
    components: {
        Pagination,
        ViewMQTrace,
        CompensateMsg
    },
    data() {
        return {
            loading: false,
            typeObj: {1: '生产', 2: '消费'},
            msgStatusObj: {1: '成功', 2: '失败'},
            resendObj: {1: '是', 2: '否'},
            groupMap: {},
            topicMap: {},
            groupTopicMap: {},
            vtopicConsumeMap: {},
            currTopicMap: {},
            currConsumeMap: {},
            selectedIds: [],
            multipleSelection: [],
            searchForm: {
                currentPage: 1,
                pageSize: 20,
                msgTimeBegin: this.$dayStart(new Date()),
                msgTimeEnd: this.$dayEnd(new Date())
            },
            pageResult: {}
        };
    },
    mounted() {
        this.getTopicInfo();
        this.refreshList();
    },
    methods: {
        getTopicInfo() {
            getTopicInfo().then(({data}) => {
                this.groupMap = data.groupMap;
                this.topicMap = data.topicMap;
                this.groupTopicMap = data.groupTopicMap;
                this.vtopicConsumeMap = data.vtopicConsumeMap;
            });
        },
        refreshList() {
            this.loading = true;
            listMQTrace(this.searchForm)
                .then(({data}) => {
                    this.pageResult = data;
                }).finally(() => {
                    this.loading = false;
                });
        },
        groupChange(currGroup) {
            this.currConsumeMap = {};
            this.currTopicMap = this.groupTopicMap[currGroup] ? this.groupTopicMap[currGroup] : {};
        },
        topicChange(currTopic) {
            this.currConsumeMap = {};
            this.currConsumeMap = this.vtopicConsumeMap[currTopic] ? this.vtopicConsumeMap[currTopic] : {};
        },
        getTopicGroupName(topicGroup) {
            return this.groupMap[topicGroup] ? this.groupMap[topicGroup] : topicGroup;
        },
        getTopicName(topic) {
            return this.topicMap[topic] ? this.topicMap[topic] : topic;
        },
        selectionChangeHandler(val) {
            this.selectedIds = [];
            val.forEach(row => {
                this.selectedIds.push(row.id);
            });
        },
        viewMQTracePage(row) {
            this.$refs.viewMQTrace.initAndShow({...row});
        },
        resendOriMsg(id) {
            this.$confirm('重新发送消息吗?')
                .then(() => resendOriMsg(id))
                .then(({data}) => {
                    this.$message.success(data);
                    this.refreshList();
                });
        },
        resendMsgBatch() {
            if (this.selectedIds.length === 0) {
                this.$message.info('请选择需要补发的记录！');
                return;
            }

            let recordIdStr = '';
            for (let i = 0; i < this.selectedIds.length; i++) {
                recordIdStr += this.selectedIds[i] + ',';
            }
            if (recordIdStr.length > 0) {
                recordIdStr = recordIdStr.substr(0, recordIdStr.length - 1);//去掉最后一个逗号
            }
            this.$confirm(`确定补发所选${this.selectedIds.length}条记录的源消息吗?`)
                .then(() => resendOriMsgBatch(recordIdStr))
                .then(({data}) => {
                    this.$message.success(data);
                    this.refreshList();
                });
        },
        sendCompensatePage() {
            this.$refs.compensateMsg.show = true;
        }
    }
};
</script>

<style lang="scss" scoped>
.datetimeInput {
    width: 190px;
}
.textInput {
    width: 190px;
}
.selectInput {
    width: 190px;
}
</style>
