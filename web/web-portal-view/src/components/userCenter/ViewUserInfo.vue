<template>
  <el-dialog
      :visible.sync="showDialog"
      :close-on-click-modal="false"
      :before-close="closeForm"
      title="个人信息"
      append-to-body
      width="450px"
  >
    <el-form ref="form" :inline="true" :model="form" size="medium" label-width="100px">
      <el-form-item label="登录名">
        <el-input v-model="form.loginName" type="text" :readonly="true"/>
      </el-form-item>
      <el-form-item label="姓名">
        <el-input v-model="form.realName" type="text" :readonly="true"/>
      </el-form-item>
      <el-form-item label="手机">
        <el-input v-model="form.mobileNo" type="text" :readonly="true"/>
      </el-form-item>
      <el-form-item label="邮箱">
        <el-input v-model="form.email" type="text" size="medium" :readonly="true"/>
      </el-form-item>
      <el-form-item label="状态">
        <el-input :value="form.status | DictFilter('UserStatusEnum')" type="text" :readonly="true"/>
      </el-form-item>
      <el-form-item label="注册时间">
        <el-input v-model="form.createTime" type="datetime" :readonly="true" value-format="yyyy-MM-dd HH:mm:ss"/>
      </el-form-item>
    </el-form>
    <div slot="footer" class="dialog-footer">
      <el-button @click="closeForm">关闭</el-button>
    </div>
  </el-dialog>
</template>

<script>
import {getUserById} from "@/api/userCenter/system";

export default {
  name: 'ViewUserInfo',
  props: {
    show: {
      type: Boolean,
      default: () => false
    }
  },
  data() {
    return {
      loading: false,
      showDialog: this.show,
      form: {
        createTime: '',
        loginName: '',
        realName: '',
        mobileNo: '',
        status: ''
      }
    }
  },
  watch: {
    show(newValue) {
      this.showDialog = newValue;
    }
  },
  methods: {
    initAndShow(userId) {
      this.loading = true
      getUserById(userId).then(({data}) => {
        this.form = data
      }).finally(() => {
        this.loading = false
      })
    },
    closeForm() {
      this.loading = false
      this.$refs.form.resetFields()
      this.$emit('update:show', false);
    }
  }
}
</script>
