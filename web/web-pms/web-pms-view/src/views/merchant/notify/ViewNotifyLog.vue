<template>
    <el-dialog
        :visible.sync="show"
        :close-on-click-modal="false"
        title="商户通知日志"
        append-to-body
        width="820px"
        @open="openForm"
        @close="closeForm"
    >
        <div>
            <el-table :data="notifyLogs" max-height="500" :row-style="{height: '10px'}" :header-cell-style="{'text-align':'center'}" border>
                <el-table-column label="通知次数" prop="currTimes" align="center"/>
                <el-table-column align="center">
                    <template slot="header">通知时间</template>
                    <template slot-scope="scope">
                        {{ scope.row.notifyTime | timeFilter }}
                    </template>
                </el-table-column>
                <el-table-column label="http状态码" prop="httpStatus" align="center"/>
                <el-table-column label="http信息" prop="httpErrMsg"/>
                <el-table-column label="响应内容" prop="respContent"/>
            </el-table>
        </div>
        <div slot="footer" class="dialog-footer">
            <el-button @click="closeForm">关闭</el-button>
        </div>
    </el-dialog>
</template>

<script>
import {notifyLog} from '@/api/merchant/notify';

export default {
    name: 'ViewNotifyLog',
    data() {
        return {
            show: false,
            recordId: '',
            notifyRecord: {},
            notifyLogs: [],
        };
    },
    methods: {
        initAndShow(recordId){
            this.recordId = recordId;
            this.notifyRecord = {};
            this.notifyLogs = [];
            this.show = true;
        },
        openForm() {
            notifyLog(this.recordId).then(({data}) => {
                this.notifyLogs = data;
            });
        },
        closeForm() {
            this.show = false;
            this.loading = false;
            this.recordId = '';
            this.notifyRecord = {};
            this.notifyLogs = [];
        }
    }
};
</script>

<style lang="scss" scoped>
::v-deep .el-dialog {
    width: 450px;

    .el-textarea {
        width: 400px;
    }
    .el-input {
        width: 400px;
    }
}
</style>
