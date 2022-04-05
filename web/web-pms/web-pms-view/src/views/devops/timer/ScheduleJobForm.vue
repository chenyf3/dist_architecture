<template>
    <el-dialog
        :visible.sync="show"
        :close-on-click-modal="false"
        :title="(actionType==='ADD'&&'新增任务')||(actionType==='VIEW'&&'查看任务')||(actionType==='EDIT'&&'编辑任务')||''"
        append-to-body
        width="530px"
        @close="closeForm"
    >
        <el-form ref="form" :inline="true" :model="form" :rules="rules" size="mini" label-width="100px">
            <el-form-item prop="jobType" label="任务类型">
                <el-select v-model="form.jobType" :disabled="actionType !== 'ADD'">
                    <template v-for="(value, key) in jobTypeObj">
                        <el-option :key="key" :label="value" :value="Number(key)"/>
                    </template>
                </el-select>
            </el-form-item>
            <el-form-item v-show="form.jobType === 1" label="任务间隔" class="jobInterval">
                <el-col :span="10">
                    <el-form-item prop='intervals'>
                        <el-input v-model.number="form.intervals" :readonly="actionType === 'VIEW'" style="width: 100%;"/>
                    </el-form-item>
                </el-col>
                <el-col :span="10">
                    <el-form-item prop='intervalUnit'>
                        <el-select v-model="form.intervalUnit" :disabled="actionType === 'VIEW'" style="width: 100%;">
                            <el-option v-for="(value,key) in intervalUnitObj" :key="key" :label="value"
                                       :value="Number(key)"/>
                        </el-select>
                    </el-form-item>
                </el-col>
            </el-form-item>
            <el-form-item v-show="form.jobType === 2" prop="cronExpression" label="cron表达式">
                <el-input v-model="form.cronExpression" :readonly="actionType === 'VIEW'"/>
            </el-form-item>
            <el-form-item prop="jobGroup" label="任务分组">
                <el-input v-model="form.jobGroup" :readonly="actionType !== 'ADD'"/>
            </el-form-item>
            <el-form-item prop="jobName" label="任务名称">
                <el-input v-model="form.jobName" :readonly="actionType !== 'ADD'"/>
            </el-form-item>
            <el-form-item prop="startTime" label="开始时间">
                <el-date-picker v-model="form.startTime" :readonly="actionType !== 'ADD'" type="datetime"
                                value-format="yyyy-MM-dd HH:mm:ss"/>
            </el-form-item>
            <el-form-item prop="endTime" label="结束时间">
                <el-date-picker v-model="form.endTime" :readonly="actionType === 'VIEW'" type="datetime"
                                value-format="yyyy-MM-dd HH:mm:ss"/>
            </el-form-item>
            <el-form-item prop="destination" label="通知地址">
                <el-input v-model="form.destination" :readonly="actionType === 'VIEW'"/>
            </el-form-item>
            <el-form-item prop="jobDescription" label="任务描述">
                <el-input v-model="form.jobDescription" :readonly="actionType === 'VIEW'"/>
            </el-form-item>
            <el-form-item prop="paramJson" label="任务参数">
                <el-input v-model="form.paramJson" type="textarea" :readonly="actionType === 'VIEW'"
                          :autosize="{minRows: 5, maxRows: 9}" :placeholder="'JSON格式的参数'"/>
            </el-form-item>
        </el-form>
        <div slot="footer">
            <el-button v-if="actionType==='ADD' || actionType==='EDIT'" :loading="loading" type="primary"
                       @click="doSubmit">确定
            </el-button>
            <el-button @click="closeForm">取 消</el-button>
        </div>
    </el-dialog>
</template>

<script>
import {addJob, editJob} from '@/api/devops/timer';

export default {
    name: 'ScheduleJobForm',
    data() {
        return {
            show: false,
            loading: false,
            actionType: '',
            jobTypeObj: {1: '间隔任务', 2: 'cron任务'},
            jobStatusObj: {NONE: '无', NORMAL: '正常', PAUSED: '暂停中', COMPLETE: '已完成', ERROR: '异常', BLOCKED: '阻塞'},
            sortObj: {ID_DESC: 'ID降序', CREATE_TIME_DESC: '创建时间降序', GROUP_ASC: '分组名升序', NAME_ASC: '任务名升序'},
            intervalUnitObj: {2: '秒', 3: '分', 4: '时'},
            form: {},
            rules: {
                jobType: [
                    {required: true, message: '任务类型不能为空', trigger: 'blur'}
                ],
                jobGroup: [
                    {required: true, message: '任务分组不能为空', trigger: 'blur'}
                ],
                jobName: [
                    {required: true, message: '任务名称不能为空', trigger: 'blur'}
                ],
                startTime: [
                    {required: true, message: '开始时间不能为空', trigger: 'blur'}
                ],
                destination: [
                    {required: true, message: '通知地址不能为空', trigger: 'blur'}
                ],
                intervals: [
                    {required: true, type: 'number', message: '任务间隔须为数字', trigger: 'blur'},
                    {required: true,
                    validator: (rule, value, callback) => {
                        if (this.form.jobType === 1 && !value) {
                            callback(new Error('请输入任务间隔'))
                        } else {
                            callback()
                        }
                    }
                }],
                intervalUnit: [{
                    required: true,
                    validator: (rule, value, callback) => {
                        if (this.form.jobType === 1 && !value) {
                            callback(new Error('请输入间隔单位'))
                        } else {
                            callback()
                        }
                    }
                }],
                cronExpression: [{
                    required: true,
                    validator: (rule, value, callback) => {
                        if (this.form.jobType === 2 && !value) {
                            callback(new Error('请输入cron表达式'))
                        } else {
                            callback()
                        }
                    }
                }],
                jobDescription: [
                    {required: true, message: '任务描述不能为空', trigger: 'blur'}
                ]
            }
        };
    },
    methods: {
        initAndShow(actionType, form) {
            this.actionType = actionType;
            this.form = form;
            this.show = true
        },
        doSubmit() {
            this.actionType === 'ADD' && this.doAdd();
            this.actionType === 'EDIT' && this.doEdit();
        },
        doAdd() {
            this.$refs.form.validate(valid => {
                if (valid) {
                    this.loading = true;
                    addJob(this.form).then(resp => {
                        if (resp.code == 200) {
                            this.$message.success(resp.data);
                            this.$emit('success')
                            this.closeForm();
                        }
                    }).finally(() => {
                        this.loading = false;
                    });
                }
            });
        },
        doEdit() {
            this.$refs.form.validate(valid => {
                if (valid) {
                    this.loading = true;
                    editJob(this.form).then(resp => {
                        if (resp.code == 200) {
                            this.$message.success(resp.data);
                            this.$emit('success')
                            this.closeForm();
                        }
                    }).finally(() => {
                        this.loading = false;
                    });
                }
            });
        },
        closeForm() {
            this.show = false;
            this.loading = false;
            this.actionType = '';
            this.form = {}//清空表单数据
            this.$refs.form.clearValidate()//清除校验规则
        }
    }
};
</script>

<style lang="scss" scoped>
::v-deep .el-dialog {
    .el-textarea {
        width: 300px;
    }

    .jobInterval {
        margin-bottom: 6px;
    }
}

.el-input {
    width: 300px;
}

</style>
