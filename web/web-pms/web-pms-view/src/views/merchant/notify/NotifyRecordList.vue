<template>
    <div v-loading="loading" class="app-container">
        <el-form :inline="true" size="mini">
            <el-form-item label="创建时间">
                <el-date-picker v-model="searchForm.createTimeBegin" value-format="yyyy-MM-dd HH:mm:ss"
                                type="datetime" :clearable="false"/>
                至
                <el-date-picker v-model="searchForm.createTimeEnd" value-format="yyyy-MM-dd HH:mm:ss"
                                type="datetime" :clearable="false"/>
            </el-form-item>
            <el-form-item label="商户编号">
                <el-input v-model="searchForm.mchNo" placeholder="精确查询"></el-input>
            </el-form-item>
            <el-form-item label="状态">
                <el-select placeholder="请选择" v-model="searchForm.status" clearable>
                    <el-option v-for="{code,desc} in $dictArray('NotifyRecordStatusEnum')"
                               :key="code" :label="desc" :value="code"
                    />
                </el-select>
            </el-form-item>
            <el-form-item label="系统流水号">
                <el-input v-model="searchForm.trxNo" placeholder="精确查询"></el-input>
            </el-form-item>
            <el-form-item label="商户流水号">
                <el-input v-model="searchForm.mchTrxNo" placeholder="精确查询"></el-input>
            </el-form-item>
            <el-form-item label="业务线">
                <el-select v-model="searchForm.productType" @change="productTypeChange" class="selectInput" clearable>
                    <el-option v-for="(value,key) in productTypeMap" :key="key" :label="value" :value="key"/>
                </el-select>
            </el-form-item>
            <el-form-item label="产品名称">
                <el-select v-model="searchForm.productCode" class="selectInput" clearable>
                    <el-option v-for="(value,key) in currProductCodeMap" :key="key" :label="value" :value="key"/>
                </el-select>
            </el-form-item>
            <el-form-item>
                <el-button type="primary" @click="refreshList"><i class="el-icon-search"/>查询</el-button>
            </el-form-item>
        </el-form>

        <div class="tool-panel">
            <el-button v-authorize="'merchant:notify:manage'" @click="resendNotifyBatch" size="small" type="primary" plain><i class="el-icon-plus"/>补发</el-button>
        </div>

        <div>
            <el-table :data="pageResult.data" row-key="id" @selection-change="selectionChangeHandler" :header-cell-style="{'text-align':'center'}" border>
                <el-table-column type="selection" width="50px"/>
                <el-table-column>
                    <template slot="header">创建时间</template>
                    <template slot-scope="scope">
                        {{ scope.row.createTime | timeFilter }}
                    </template>
                </el-table-column>
                <el-table-column label="商户编号" prop="mchNo"/>
                <el-table-column label="系统流水号" prop="trxNo"/>
                <el-table-column label="商户流水号" prop="mchTrxNo"/>
                <el-table-column prop="status" label="状态" align="center"
                                 :formatter="row=>$dictName('NotifyRecordStatusEnum', row.status)"/>
                <el-table-column label="通知次数" prop="currTimes" align="center"/>
                <el-table-column v-slot="{row}" label="操作">
                    <el-button v-authorize="'merchant:notify:list'" type="text" size="small"
                               @click="viewNotifyRecord(row.id)">查看
                    </el-button>
                    <el-button v-authorize="'merchant:notify:list'" type="text" size="small"
                               @click="viewNotifyLog(row.id)">日志
                    </el-button>
                    <el-button v-authorize="'merchant:notify:manage'" type="text" size="small"
                               @click="resendNotify(row.id)">补发
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

        <view-notify-record ref="viewNotifyRecord" @success="refreshList"/>
        <view-notify-log ref="viewNotifyLog"/>
    </div>
</template>

<script>
import Pagination from '@/components/Pagination'
import ViewNotifyRecord from './ViewNotifyRecord'
import ViewNotifyLog from './ViewNotifyLog';
import {getProductInfo} from '@/api/public/index';
import {listMchNotifyPage, notifyAgain, notifyAgainBatch} from '@/api/merchant/notify';

export default {
    name: 'mchNotifyList',
    components: {
        Pagination,
        ViewNotifyRecord,
        ViewNotifyLog,
    },
    data() {
        return {
            loading: false,
            selectedIds: [],
            productTypeMap: {},
            productCodeMap: {},
            currProductCodeMap: {},
            searchForm: {
                currentPage: 1,
                pageSize: 10,
                createTimeBegin: this.$dayStart(new Date()),
                createTimeEnd: this.$dayEnd(new Date()),
                mchNo: '',
                trxNo: '',
                mchTrxNo: '',
                status: '',
                productType: '',
                productCode: '',
            },
            pageResult: {}
        }
    },
    mounted() {
        this.getProductInfo();
        this.refreshList()
    },
    methods: {
        getProductInfo(){
            getProductInfo().then(({data}) => {
                this.productTypeMap = data.productTypeMap
                this.productCodeMap = data.productCodeMap
            });
        },
        refreshList() {
            this.loading = true
            listMchNotifyPage(this.searchForm).then(res => {
                this.pageResult = res.data
            }).finally(() => {
                this.loading = false
            })
        },
        resendNotify(recordId) {
            this.$confirm('重新补发通知吗?')
                .then(() => notifyAgain(recordId))
                .then(resp => {
                    if(resp.code === 200){
                        this.$message.success(resp.data);
                        this.refreshList();
                    }else{
                        this.$message.error(resp.data);
                    }
                });
        },
        productTypeChange(currType){
            this.searchForm.productCode = ''
            this.currProductCodeMap = {}
            this.currProductCodeMap = this.productCodeMap[currType] ? this.productCodeMap[currType] : {}
        },
        selectionChangeHandler(val) {
            this.selectedIds = [];
            val.forEach(row => {
                this.selectedIds.push(row.id);
            });
        },
        resendNotifyBatch() {
            if (this.selectedIds.length === 0) {
                this.$message.info('请选择需要补发通知的记录！');
                return;
            }

            let recordIdStr = '';
            for (let i = 0; i < this.selectedIds.length; i++) {
                recordIdStr += this.selectedIds[i] + ',';
            }
            if (recordIdStr.length > 0) {
                recordIdStr = recordIdStr.substr(0, recordIdStr.length - 1);//去掉最后一个逗号
            }
            this.$confirm(`确定补发${this.selectedIds.length}条商户通知吗?`)
                .then(() => notifyAgainBatch(recordIdStr))
                .then(({data}) => {
                    this.$message.success(data);
                    this.refreshList();
                });
        },
        viewNotifyRecord(recordId) {
            this.$refs.viewNotifyRecord.initAndShow(recordId)
        },
        viewNotifyLog(recordId) {
            this.$refs.viewNotifyLog.initAndShow(recordId)
        },
    }
}
</script>
