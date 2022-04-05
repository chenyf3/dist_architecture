<template>
    <el-dialog
        :visible.sync="show"
        :close-on-click-modal="false"
        title="查看消息轨迹记录"
        append-to-body
        width="600px"
        @open="openForm"
        @close="closeForm"
    >
        <el-form ref="form" :inline="true" :model="form" :rules="rules" size="mini" label-width="100px">
            <el-form-item label="消息时间">
                <el-date-picker v-model="form.msgTime" :readonly="true" type="datetime"
                                value-format="yyyy-MM-dd HH:mm:ss"/>
            </el-form-item>
            <el-form-item label="创建时间">
                <el-date-picker v-model="form.createTime" :readonly="true" type="datetime"
                                value-format="yyyy-MM-dd HH:mm:ss"/>
            </el-form-item>
            <el-form-item label="消息ID">
                <el-input v-model="form.traceId" :readonly="true"/>
            </el-form-item>
            <el-form-item label="流水号">
                <el-input v-model="form.trxNo" :readonly="true"/>
            </el-form-item>
            <el-form-item label="投递类型">
                <el-select v-model="form.type" :disabled="true">
                    <el-option v-for="(value,key) in typeObj" :key="key" :label="value" :value="Number(key)"/>
                </el-select>
            </el-form-item>
            <el-form-item label="投递次数">
                <el-input v-model="form.deliveryCount" :readonly="true"/>
            </el-form-item>
            <el-form-item label="消息状态">
                <el-select v-model="form.msgStatus" :disabled="true">
                    <el-option v-for="(value,key) in msgStatusObj" :key="key" :label="value" :value="Number(key)"/>
                </el-select>
            </el-form-item>
            <el-form-item label="客户端标识">
                <el-input v-model="form.clientFlag" :readonly="true"/>
            </el-form-item>
            <el-form-item label="业务线">
                <el-input :value="topicGroupName" name="topicGroup" :readonly="true"/>
            </el-form-item>
            <el-form-item label="生产队列">
                <el-input :value="topicName" name="topic" :readonly="true"/>
            </el-form-item>
            <el-form-item label="消费队列">
                <el-input :value="consumeDestName" name="consumeDest" :readonly="true"/>
            </el-form-item>
            <el-form-item label="补发创建">
                <el-input :value="resendName" name="resend" :readonly="true"/>
            </el-form-item>
            <el-form-item label="错误信息">
                <el-input v-model="form.errMsg" type="textarea" :readonly="true" :autosize="{minRows: 2, maxRows: 9}"/>
            </el-form-item>
            <el-form-item label="源消息体">
                <el-input v-model="form.oriMsg" type="textarea" :readonly="true" :autosize="{minRows: 5, maxRows: 9}"/>
            </el-form-item>
        </el-form>
        <div slot="footer" class="dialog-footer">
            <el-button @click="closeForm">关闭</el-button>
        </div>
    </el-dialog>
</template>

<script>
import {getTopicInfo} from '@/api/devops/mqtrace';

export default {
    name: 'ViewMQTrace',
    data() {
        return {
            show: false,
            typeObj: {1: '生产', 2: '消费'},
            msgStatusObj: {1: '成功', 2: '失败'},
            groupMap: {},
            topicMap: {},
            groupTopicMap: {},
            vtopicConsumeMap: {},
            form: {},
            rules: {}
        };
    },
    computed: {
        topicGroupName() {
            return this.groupMap[this.form.topicGroup] ? this.groupMap[this.form.topicGroup] : this.form.topicGroup;
        },
        topicName() {
            if (this.topicMap[this.form.topic]) {
                return this.topicMap[this.form.topic] + `(${this.form.topic})`;
            } else {
                return this.form.topic;
            }
        },
        consumeDestName() {
            let destName = this.form.consumeDest;
            if (this.vtopicConsumeMap[this.form.topic]) {
                const consumeNameMap = this.vtopicConsumeMap[this.form.topic];
                if (consumeNameMap[this.form.consumeDest]) {
                    destName = consumeNameMap[this.form.consumeDest] + `(${destName})`;
                }
            }
            return destName;
        },
        resendName() {
            return this.form.resend === 1 ? '是' : '否';
        }
    },
    methods: {
        initAndShow(form){
            this.form = form;
            this.show = true;
        },
        openForm() {
            getTopicInfo().then(({data}) => {
                this.groupMap = data.groupMap;
                this.topicMap = data.topicMap;
                this.groupTopicMap = data.groupTopicMap;
                this.vtopicConsumeMap = data.vtopicConsumeMap;
            });
        },
        closeForm() {
            this.show = false;
            this.loading = false;
            this.form = {}//清空表单数据
            this.$refs.form.clearValidate()//清除校验规则
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
