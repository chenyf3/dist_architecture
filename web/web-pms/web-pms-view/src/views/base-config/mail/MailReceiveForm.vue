<template>
    <el-dialog
        :visible.sync="show"
        :close-on-click-modal="false"
        :title="(actionType==='ADD'&&'添加')||(actionType==='VIEW'&&'查看')||(actionType==='EDIT'&&'编辑')||''"
        append-to-body
        width="600px"
        @close="closeForm"
    >
        <el-form ref="form" :inline="true" :model="form" :rules="rules" size="small" label-width="100px">
            <el-form-item v-if="actionType !== 'ADD'" prop="createTime" label="创建时间">
                <el-date-picker v-model="form.createTime" :readonly="true" type="datetime"
                                value-format="yyyy-MM-dd HH:mm:ss"/>
            </el-form-item>
            <el-form-item prop="groupKey" label="业务分组" >
                <el-select placeholder="业务分组" v-model="form.groupKey" :disabled="actionType !== 'ADD'">
                    <el-option v-for="item in $dictArray('EmailGroupKeyEnum')"
                               :key="item.name" :label="item.desc" :value="item.name"/>
                </el-select>
            </el-form-item>
            <el-form-item prop="sender" label="发件人">
                <el-select v-model="form.sender" :disabled="actionType === 'VIEW'">
                    <el-option v-for="(value, key) in mailSenderObj" :key="key" :label="value" :value="key"/>
                </el-select>
            </el-form-item>
            <el-form-item prop="receivers" label="收件人">
                <el-input v-model="form.receivers" type="textarea" :autosize="{minRows: 6, maxRows: 9}"
                          :readonly="actionType === 'VIEW'" placeholder="请按照json数组格式"/>
            </el-form-item>
            <el-form-item prop="remark" label="备注">
                <el-input v-model="form.remark" :readonly="actionType === 'VIEW'"/>
            </el-form-item>
        </el-form>
        <div slot="footer" class="dialog-footer">
            <el-button v-if="actionType==='ADD' || actionType==='EDIT'" :loading="loading" type="primary"
                       @click="doSubmit">确定</el-button>
            <el-button @click="closeForm">取 消</el-button>
        </div>
    </el-dialog>
</template>

<script>
import {addMailReceiver, editMailReceiver, getMailSender} from '@/api/baseConfig/mailReceiver';

export default {
    name: 'MailReceiveForm',
    data() {
        return {
            show: false,
            loading: false,
            actionType: '',
            mailSenderObj: {},
            form: {},
            rules: {
                groupKey: [
                    {required: true, message: '业务分组不能为空', trigger: 'blur'}
                ],
                sender: [
                    {required: true, message: '请选择发件人', trigger: 'blur'}
                ],
                receivers: [
                    {required: true, message: '请输入收件人', trigger: 'blur'}
                ],
                remark: [
                    {required: false, message: '请输入备注', trigger: 'blur'}
                ],
            }
        };
    },
    methods: {
        initAndShow(actionType, formData){
            this.actionType = actionType;
            if(this.actionType !== 'ADD'){
                this.form = formData;
            }
            this.show = true;

            getMailSender().then((resp) => {
                if (resp.code === 200) {
                    this.mailSenderObj = resp.data;
                }
            });
        },
        doSubmit() {
            this.actionType === 'ADD' && this.doAdd();
            this.actionType === 'EDIT' && this.doEdit();
        },
        doAdd() {
            this.$refs.form.validate(valid => {
                if (valid) {
                    this.loading = true;
                    const formData = {...this.form};
                    addMailReceiver(formData).then(resp => {
                        if(resp.code === 200){
                            this.$message.success(resp.data);
                            this.closeForm();
                            this.$emit('success');
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
                    const formData = {...this.form};
                    editMailReceiver(formData).then(resp => {
                        if(resp.code === 200){
                            this.$message.success(resp.data);
                            this.closeForm();
                            this.$emit('success');
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
    width: 450px;

    .el-textarea {
        width: 400px;
    }
    .el-input {
        width: 400px;
    }
}
</style>
