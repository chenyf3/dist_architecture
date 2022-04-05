<template>
    <el-dialog
        :visible.sync="show"
        :close-on-click-modal="false"
        title="重新发布项目"
        append-to-body
        width="600px"
        @open="openForm"
        @close="closeForm"
    >
        <el-form ref="form" :inline="true" :model="form" :rules="rules" size="mini" label-width="100px">
            <el-form-item label="发布时间">
                <el-input v-model="form.createTime" :readonly="true" autocomplete="off"/>
            </el-form-item>
            <el-form-item label="发布描述" label-width="100px">
                <el-input v-model="form.buildMsg" :readonly="true"/>
            </el-form-item>
            <el-form-item v-if="actionType==='VIEW'" label="任务名称">
                <el-input v-model="form.jobName" :readonly="true"/>
            </el-form-item>
            <el-form-item prop="apps" label="发布项目">
                <el-input v-model="form.apps" type="textarea" :readonly="true" :rows="5"/>
            </el-form-item>
            <el-form-item prop="notifyEmail" label="通知邮箱">
                <el-input v-model="form.notifyEmail" type="textarea" :readonly="true" :rows="5"/>
            </el-form-item>
            <el-form-item label="发布机房">
                <el-input v-model="idcName" :readonly="true"/>
            </el-form-item>
            <el-form-item label="发布次数">
                <el-input v-model="form.publishTimes" :readonly="true"/>
            </el-form-item>
            <el-form-item label="中继项目">
                <el-input v-model="form.relayApp"/>
            </el-form-item>
            <el-form-item label="备注">
                <el-input v-model="form.remark"/>
            </el-form-item>
        </el-form>
        <div slot="footer" class="dialog-footer">
            <el-button @click="closeForm">取 消</el-button>
            <el-button :loading="loading" type="primary" @click="doSubmit">确定</el-button>
        </div>
    </el-dialog>
</template>

<script>
import {getPublishInfo,republish} from '@/api/devops/publish';

export default {
    name: 'RepublishForm',
    data() {
        return {
            show: false,
            loading: false,
            actionType: '',
            idcList: [],
            form: {},
            rules: {
                buildMsg: [
                    {required: true, message: '发布描述不能为空', trigger: 'blur'},
                    {min: 1, max: 150, message: '发布描述长度不能超过150', trigger: 'blur'}
                ],
                apps: [
                    {required: true, message: '发布项目不能为空', trigger: 'blur'}
                ],
                notifyEmail: [
                    {required: true, message: '通知邮件不能为空', trigger: 'blur'}
                ]
            }
        };
    },
    computed: {
        idcName() {
            if(this.form.idc && this.idcList){
                for(let i=0; i< this.idcList.length; i++){
                    if(this.idcList[i].code === this.form.idc){
                        return this.idcList[i].name;
                    }
                }
            }
            return this.form.idc;
        }
    },
    methods: {
        initAndShow(form){
            this.form = form;
            console.log(this.form)
            this.show = true;
        },
        doSubmit() {
            republish(this.form.id, this.form.relayApp, this.form.remark).then(response => {
                this.$message.success(response.data);
                this.$emit('success')
                this.closeForm();
            }).finally(() => {
                this.loading = false;
            });
        },
        openForm() {
            getPublishInfo().then(({data}) => {
                this.idcList = data.idcList;
            })
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
