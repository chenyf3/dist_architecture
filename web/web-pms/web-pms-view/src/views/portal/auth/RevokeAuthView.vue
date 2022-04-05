<template>
    <el-dialog
        class="dialog-container"
        :visible.sync="show"
        :close-on-click-modal="false"
        :before-close="closeForm"
        title="功能回收查看"
        append-to-body
        width="600px"
    >
        <el-form ref="form" :inline="true" :model="form" size="small" label-width="100px">
            <el-form-item label="创建时间">
                <el-input v-model="form.createTime" :readonly="true"/>
            </el-form-item>
            <el-form-item label="创建人">
                <el-input v-model="form.creator" :readonly="true"/>
            </el-form-item>
            <el-form-item label="回收类型">
                <el-input :value="$dictName('RevokeAuthTypeEnum', form.revokeType)" :readonly="true"/>
            </el-form-item>
            <el-form-item label="操作对象">
                <el-input v-model="form.objectKey" :readonly="true"/>
            </el-form-item>
            <el-form-item label="状态">
                <el-input :value="$dictName('RevokeAuthStatusEnum', form.status)" :readonly="true"/>
            </el-form-item>
            <el-form-item label="说明">
                <el-input v-model="form.remark" :readonly="true"/>
            </el-form-item>
            <el-form-item label="当前回收商户">
                <el-input v-model="form.currMchNo" :readonly="true"/>
            </el-form-item>
            <el-form-item label="需回收商户">
                <el-input v-model="form.mchNos" type="textarea" :readonly="true"/>
            </el-form-item>
        </el-form>
        <div slot="footer" class="dialog-footer">
            <el-button @click="closeForm">关闭</el-button>
        </div>
    </el-dialog>
</template>

<script>
export default {
    name: 'RevokeAuthView',
    data() {
        return {
            show: false,
            form: {
                createTime: '',
                creator: '',
                revokeType: undefined,
                objectKey: '',
                status: undefined,
                remark: 0,
                currMchNo: '',
                mchNos: ''
            }
        }
    },
    methods: {
        initAndShow(record){
            this.show = true
            this.form = {...record}
        },
        closeForm() {
            this.show = false
            this.$refs.form.resetFields()
        }
    }
}
</script>
