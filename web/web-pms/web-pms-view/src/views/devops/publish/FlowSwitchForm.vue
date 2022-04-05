<template>
    <el-dialog
        :visible.sync="show"
        :close-on-click-modal="false"
        title="流量切换"
        append-to-body
        width="550px"
        @open="openFrom"
        @close="closeForm"
    >
        <el-form ref="form" :inline="true" :model="form" :rules="rules" size="mini" label-width="120px">
            <el-form-item label="当前流量机房">
                <el-input v-model="form.currFlow" :readonly="true" autocomplete="off"/>
            </el-form-item>
            <el-form-item prop="idc" label="切入机房" v-if="idcList.length > 0">
                <el-checkbox-group v-model="form.idc">
                    <el-checkbox v-for="idc in idcList" :key="idc.code" :label="idc.code" border>{{
                            idc.name
                        }}
                    </el-checkbox>
                </el-checkbox-group>
            </el-form-item>
            <el-form-item prop="checkPublishing" label="上线记录检查">
                <el-radio-group v-model="form.checkPublishing">
                    <el-tooltip effect="light" content="如果还有未完成的上线记录则不会执行切量" placement="top-start">
                        <el-radio :label="true">是</el-radio>
                    </el-tooltip>
                    <el-radio :label="false">否</el-radio>
                </el-radio-group>
            </el-form-item>
        </el-form>
        <div slot="footer" class="dialog-footer">
            <el-button @click="closeForm">取 消</el-button>
            <el-button :loading="loading" type="primary" @click="doSubmit">确定</el-button>
        </div>
    </el-dialog>
</template>

<script>
import {getCurrIdcFlow, getPublishInfo, flowSwitch} from '@/api/devops/publish'

export default {
    name: 'SwitchForm',
    data() {
        return {
            show: false,
            loading: false,
            idcList: [],
            form: {
                currFlow: '',
                idc: [],
                checkPublishing: true,
            },
            rules: {
                idc: [{required: true, message: '请选择流量切入的机房', trigger: 'blur'}],
                checkPublishing: [{required: true, message: '请选择是否检查还有正在发布中的项目', trigger: 'blur'}]
            }
        };
    },
    methods: {
        doSubmit() {
            this.$refs.form.validate(valid => {
                if (valid) {
                    this.loading = true;
                    flowSwitch(this.form.idc, this.form.checkPublishing).then(response => {
                        this.$message.success(response.data);
                        this.closeForm();
                    }).finally(() => {
                        this.loading = false;
                    });
                }
            });
        },
        openFrom() {
            getPublishInfo().then(({data: publishInfo}) => {
                this.idcList = publishInfo.idcList;
            });
            getCurrIdcFlow().then(({data: idcVo}) => {
                this.form.currFlow = idcVo ? idcVo.name : '';
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
        width: 300px;
    }
}
</style>
