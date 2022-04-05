<template>
    <el-dialog
        :visible.sync="show"
        :close-on-click-modal="false"
        title="机房代码同步"
        append-to-body
        width="880px"
        @open="openForm"
        @close="closeForm"
    >
        <el-form ref="form" :inline="false" :model="form" :rules="rules" size="mini" label-width="120px">
            <el-form-item label="当前流量机房">
                <el-input v-model="form.currFlow" :readonly="true" autocomplete="off"/>
            </el-form-item>
            <el-form-item prop="idc" label="同步到机房" v-if="idcList.length > 0">
                <el-radio-group v-model="form.idc">
                    <el-radio v-for="idc in idcList" :key="idc.code" :label="idc.code" border>{{ idc.name }}</el-radio>
                </el-radio-group>
            </el-form-item>
            <el-form-item prop="timerResumeIdc" label="定时任务恢复" v-if="idcList.length > 0" >
                <el-tooltip effect="light" content="恢复此机房被挂起的定时任务" placement="bottom" open-delay="200">
                    <el-radio-group v-model="form.timerResumeIdc" text-color="#409eff">
                        <el-radio v-for="idc in idcList" :key="idc.code" :label="idc.code" border>{{ idc.name }}</el-radio>
                    </el-radio-group>
                </el-tooltip>
            </el-form-item>
            <el-form-item label="代码同步备注">
                <el-input v-model="form.syncMsg" autocomplete="off"/>
            </el-form-item>
        </el-form>

        <div>
            <el-divider content-position="center">将要同步的上线记录如下</el-divider>
        </div>

        <el-table :data="publishRecords" style="width: 100%" row-key="id" border>
            <el-table-column label="项目描述" prop="buildMsg"/>
            <el-table-column>
                <template slot="header">发布机房</template>
                <template slot-scope="scope">
                    {{ scope.row.idcName }}
                </template>
            </el-table-column>
            <el-table-column>
                <template slot="header">发布批次 </template>
                <template slot-scope="scope">
                    {{ scope.row.buildNo }}
                </template>
            </el-table-column>
            <el-table-column>
                <template slot="header"> 发布时间 </template>
                <template slot-scope="scope">
                    {{ scope.row.createTime | timeFilter }}
                </template>
            </el-table-column>
        </el-table>


        <div slot="footer" class="dialog-footer">
            <el-button @click="closeForm">取 消</el-button>
            <el-button :loading="loading" type="primary" @click="doSubmit">确定</el-button>
        </div>
    </el-dialog>
</template>

<script>
import {getCurrIdcFlow,getPublishInfo,syncIdcPublish} from '@/api/devops/publish'

export default {
    name: 'PublishSyncForm',
    data() {
        return {
            show: false,
            loading: false,
            idcList: [],
            publishRecords: [],
            form: {
                currFlow: '',
                idc: [],
                timerResumeIdc: '',
                syncMsg: '机房代码同步',
            },
            rules: {
                idc: [{required: true, message: '请选择代码同步的机房', trigger: 'blur'}],
                timerResumeIdc: [{required: false, message: '请选择需要恢复定时任务的机房', trigger: 'blur'}],
            }
        };
    },
    methods: {
        openForm() {
            getPublishInfo().then(({data: publishInfo}) => {
                this.idcList = publishInfo.idcList;
            });
            getCurrIdcFlow().then(({data: idcVo}) => {
                this.form.currFlow = idcVo ? idcVo.name : '';
            });
        },
        doSubmit() {
            this.$refs.form.validate(valid => {
                if (valid) {
                    this.loading = true;
                    let idArr = []
                    for(let i=0; i<this.publishRecords.length; i++){
                        idArr.push(this.publishRecords[i].id)
                    }
                    let idStr = idArr.join(',')
                    syncIdcPublish( this.form.idc, idStr, this.form.syncMsg, this.form.timerResumeIdc)
                        .then(response => {
                            this.$message.success(response.data);
                            this.closeForm();
                        }).finally(() => {
                            this.loading = false;
                        });
                }
            });
        },
        closeForm() {
            this.show = false;
            this.loading = false;
            this.publishRecords = []
            this.form = {}//清空表单数据
            this.$refs.form.clearValidate()//清除校验规则
        }
    }
};
</script>

<style lang="scss" scoped>
::v-deep .el-dialog {
    width: 450px;

    __body{
        display: inline-block;
    }
    .el-input {
        width: 300px;
    }
}
</style>
