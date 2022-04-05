<template>
    <el-dialog
        :visible.sync="show"
        :close-on-click-modal="false"
        :title="(actionType==='ADD'&&'新增上线 - 项目发布')||(actionType==='VIEW'&&'查看项目发布记录')||''"
        append-to-body
        width="600px"
        @open="openForm"
        @close="closeForm"
    >
        <el-form ref="form" :inline="true" :model="form" :rules="rules" size="mini" label-width="100px">
            <el-form-item prop="buildMsg" label="项目描述" label-width="100px">
                <el-input v-model="form.buildMsg" :readonly="actionType==='VIEW'" placeholder="本次上线项目的需求简述"/>
            </el-form-item>
            <el-form-item prop="apps" label="发布项目">
                <el-input v-model="form.apps" type="textarea" :readonly="actionType==='VIEW'" :rows="5"
                          placeholder="请自行对项目构建顺序进行排序，多个项目时按回车键换行"/>
            </el-form-item>
            <el-form-item prop="notifyEmail" label="通知邮箱">
                <el-input v-model="form.notifyEmail" type="textarea" :readonly="actionType==='VIEW'" :rows="5"
                          placeholder="多个收件人按回车键换行"/>
            </el-form-item>
            <el-form-item v-if="actionType==='VIEW'" prop="notifyUrl" label="回调地址">
                <el-input v-model="form.notifyUrl" type="textarea" :readonly="actionType==='VIEW'"/>
            </el-form-item>
            <el-form-item v-if="actionType==='ADD'" prop="buildType" label="构建方式">
                <el-radio-group v-model="form.buildType" :disabled="actionType==='VIEW'">
                    <el-radio :label="1">触发任务</el-radio>
                    <el-radio :label="2" disabled>直接构建</el-radio>
                </el-radio-group>
            </el-form-item>
            <el-form-item prop="idc" label="发布机房" v-if="actionType==='ADD' && idcList.length > 0">
                <el-radio-group v-model="form.idc" :change="changeDeployIdc()">
                    <el-radio v-for="idc in idcList" :key="idc.code" :label="idc.code" border>{{ idc.name }}</el-radio>
                </el-radio-group>
                <el-input v-if="actionType==='VIEW'" v-model="idcName" :readonly="true"/>
            </el-form-item>
            <el-form-item label="定时任务挂起" v-if="actionType==='ADD' && idcList.length > 0">
                <el-tooltip effect="light" content="上线之前将会挂起发布机房的定时任务" placement="top-start">
                    <el-radio-group v-model="form.timerIdc" :disabled="true" text-color="#409eff">
                        <el-radio v-for="idc in idcList" :key="idc.code" :label="idc.code" border>{{ idc.name }}</el-radio>
                    </el-radio-group>
                </el-tooltip>
            </el-form-item>
            <el-form-item prop="idc" label="发布机房" v-if="actionType==='VIEW'">
                <el-input v-model="idcName" :readonly="true"/>
            </el-form-item>
            <el-form-item prop="buildNo" label="发布批次" v-if="actionType==='VIEW'">
                <el-input v-model="form.buildNo" :readonly="true"/>
            </el-form-item>
            <el-form-item v-if="actionType==='VIEW'" label="状态">
                <el-select v-model="form.status" :disabled="true">
                    <el-option v-for="(value,key) in publishStatusObj" :key="key" :label="value" :value="Number(key)"/>
                </el-select>
            </el-form-item>
            <el-form-item v-if="actionType==='ADD'" label="当前流量机房">
                <el-input v-model="form.currFlowIdc" :readonly="true" width="50px"/>
            </el-form-item>
            <el-form-item v-if="actionType==='VIEW'" label="发布时间">
                <el-date-picker v-model="form.createTime" :readonly="true" type="datetime"
                                value-format="yyyy-MM-dd HH:mm:ss"/>
            </el-form-item>
            <el-form-item v-if="actionType==='VIEW'" label="处理次数">
                <el-input v-model="form.processTimes" :readonly="true"/>
            </el-form-item>
            <el-form-item v-if="actionType==='VIEW'" label="发布次数">
                <el-input v-model="form.publishTimes" :readonly="true"/>
            </el-form-item>
            <el-form-item v-if="actionType==='VIEW'" label="构建任务">
                <el-input v-model="form.jobName" :readonly="true"/>
            </el-form-item>
            <el-form-item v-if="actionType==='VIEW'" label="队列Id">
                <el-input v-model="form.queueId" :readonly="true"/>
            </el-form-item>
            <el-form-item v-if="actionType==='VIEW'" label="构建Id">
                <el-input v-model="form.buildId" :readonly="true"/>
            </el-form-item>
            <el-form-item v-if="actionType==='VIEW'" label="备注">
                <el-input v-model="form.remark" :readonly="true"/>
            </el-form-item>
            <el-form-item v-if="actionType==='VIEW'" label="创建人">
                <el-input v-model="form.creator" :readonly="true" autocomplete="off"/>
            </el-form-item>
            <el-form-item v-if="actionType==='VIEW'" label="修改人">
                <el-input v-model="form.modifier" :readonly="true"/>
            </el-form-item>
        </el-form>
        <div slot="footer" class="dialog-footer">
            <el-button @click="closeForm">取 消</el-button>
            <el-button v-if="actionType==='ADD'" :loading="loading" type="primary" @click="doSubmit">确定</el-button>
        </div>
    </el-dialog>
</template>

<script>
import {getPublishInfo, getCurrIdcFlow, publish} from '@/api/devops/publish';

export default {
    name: 'PublishForm',
    data() {
        return {
            show: false,
            loading: false,
            actionType: '',
            idcList: [],
            publishStatusObj: {1: '待处理', 2: '排队中', 3: '处理中', 4: '成功', 5: '失败', 6: '不稳定', 7: '已取消', 8: '已超时'},
            form: {},
            rules: {
                buildMsg: [
                    {required: true, message: '项目描述不能为空', trigger: 'blur'},
                    {min: 1, max: 150, message: '项目描述长度不能超过150', trigger: 'blur'}
                ],
                apps: [
                    {required: true, message: '发布项目不能为空', trigger: 'blur'}
                ],
                buildType: [
                    {required: true, message: '请选择构建方式', trigger: 'blur'}
                ],
                notifyEmail: [
                    {required: true, message: '通知邮件不能为空', trigger: 'blur'}
                ]
            }
        };
    },
    computed: {
        idcName() {
            if (this.form.idc && this.idcList) {
                for (let i = 0; i < this.idcList.length; i++) {
                    if (this.idcList[i].code === this.form.idc) {
                        return this.idcList[i].name;
                    }
                }
            }
            return this.form.idc;
        }
    },
    methods: {
        initAndShow(actionType, form) {
            this.actionType = actionType;
            this.form = form;
            this.show = true;
        },
        doSubmit() {
            this.actionType === 'ADD' && this.doAdd();
        },
        doAdd() {
            this.$refs.form.validate(valid => {
                if (valid) {
                    this.loading = true;
                    publish(this.form).then(response => {
                        this.$message.success(response.data);
                        this.$emit('success')
                        this.closeForm();
                    }).finally(() => {
                        this.loading = false;
                    });
                }
            });
        },
        openForm() {
            if (this.actionType === 'ADD') {
                this.form = {
                    buildType: 1,
                    idc: '',
                    notifyEmail: '',
                    currFlowIdc: ''
                };
                getPublishInfo().then(({data: publishInfo}) => {
                    this.idcList = publishInfo.idcList;
                    this.form.notifyEmail = publishInfo.emailReceiver;
                });
                getCurrIdcFlow().then(({data: idcFlow}) => {
                    this.form.currFlowIdc = idcFlow ? idcFlow.name : '';
                });
            }
        },
        closeForm() {
            this.show = false;
            this.loading = false;
            this.form = {}//清空表单数据
            this.$refs.form.clearValidate()//清除校验规则
        },
        changeDeployIdc() {
            this.form.timerIdc = this.form.idc;
        },
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
