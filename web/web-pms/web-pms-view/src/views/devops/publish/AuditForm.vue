<template>
    <el-dialog
        :visible.sync="show"
        :close-on-click-modal="false"
        title="审核发布项目"
        append-to-body
        width="600px"
        @open="openForm"
        @close="closeForm"
    >
        <el-form ref="form" :inline="true" :model="form" :rules="rules" size="mini" label-width="100px">
            <el-form-item prop="auditStatus" label="审核状态">
                <el-select v-model="form.auditStatus">
                    <el-option v-for="(value,key) in auditStatusObj" :key="key" :label="value" :value="Number(key)"/>
                </el-select>
            </el-form-item>
            <el-form-item prop="remark" label="备注">
                <el-input v-model="form.remark"/>
            </el-form-item>
            <el-form-item label="发布描述" label-width="100px">
                <el-input v-model="form.buildMsg" :readonly="true"/>
            </el-form-item>
            <el-form-item label="发布项目">
                <el-input v-model="form.apps" type="textarea" :readonly="true" :rows="5"/>
            </el-form-item>
            <el-form-item label="发布机房">
                <el-input v-model="idcName" :readonly="true"/>
            </el-form-item>
            <el-form-item label="当前状态">
                <el-select v-model="form.status" :disabled="true">
                    <el-option v-for="(value,key) in publishStatusObj" :key="key" :label="value" :value="Number(key)"/>
                </el-select>
            </el-form-item>
            <el-form-item label="发布时间">
                <el-input v-model="form.createTime" :readonly="true" autocomplete="off"/>
            </el-form-item>
            <el-form-item label="已发布次数">
                <el-input v-model="form.publishTimes" :readonly="true"/>
            </el-form-item>
        </el-form>
        <div slot="footer" class="dialog-footer">
            <el-button @click="closeForm">取 消</el-button>
            <el-button :loading="loading" type="primary" @click="doSubmit">确定</el-button>
        </div>
    </el-dialog>
</template>

<script>
import {getPublishInfo,audit} from '@/api/devops/publish';

export default {
    name: 'AuditForm',
    data() {
        return {
            show: false,
            loading: false,
            actionType: '',
            idcList: [],
            publishStatusObj: {1: '待处理', 2: '排队中', 3: '处理中', 4: '成功', 5: '失败', 6: '不稳定', 7: '已取消', 8: '已超时'},
            auditStatusObj: {4: '成功', 5: '失败', 7: '已取消'},
            form: {
                auditStatus: '',
            },
            rules: {
                auditStatus: [
                    {required: true, message: '请选择审核状态', trigger: 'blur'}
                ],
                remark: [
                    {required: true, message: '请输入审核备注', trigger: 'blur'}
                ],
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
            this.show = true;
        },
        doSubmit() {
            audit(this.form.id, this.form.auditStatus, this.form.remark).then(response => {
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
