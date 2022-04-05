<template>
    <el-dialog
        :visible.sync="show"
        :close-on-click-modal="false"
        title="录入消息"
        append-to-body
        width="600px"
        @open="openForm"
        @close="closeForm"
    >
        <el-form ref="form" :inline="true" :model="form" :rules="rules" size="small" label-width="100px">
            <el-form-item prop="msgJsonStrList" label="消息内容">
                <el-input v-model="form.msgJsonStrList" type="textarea" :autosize="{minRows: 6, maxRows: 9}"
                          placeholder="多条记录请按回车换行"/>
            </el-form-item>
        </el-form>
        <div slot="footer" class="dialog-footer">
            <el-button :loading="loading" type="primary" @click="doSubmit">确定</el-button>
            <el-button @click="closeForm">关闭</el-button>
        </div>
    </el-dialog>
</template>

<script>
import {sendCompensate} from '@/api/devops/mqtrace';

export default {
    name: 'CompensateMsg',
    data() {
        return {
            show: false,
            loading: false,
            form: {
                msgJsonStrList: ''
            },
            rules: {
                msgJsonStrList: [
                    {required: true, message: '请输入要录入的消息', trigger: 'blur'}
                ]
            }
        };
    },
    methods: {
        doSubmit() {
            this.$refs.form.validate(valid => {
                if (valid) {
                    this.loading = true;
                    sendCompensate({msgJsonStrList: this.form.msgJsonStrList})
                        .then(response => {
                            this.$message.success(response.data);
                            this.closeForm();
                        }).finally(() => {
                        this.loading = false;
                    });
                }
            });
        },
        openForm() {

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
}
</style>
