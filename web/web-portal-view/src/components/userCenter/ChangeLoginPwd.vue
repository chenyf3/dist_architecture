<template>
  <el-dialog width="400px" title="修改登录密码" :visible.sync="showDialog" :close-on-click-modal="false" :before-close="closeForm">
    <el-form ref="form" :inline="true" :model="form" :rules="rules" size="medium" label-width="100px">
      <el-form-item label="原密码" prop="oldPwd">
        <el-input class="form-input" type="password" v-model="form.oldPwd" placeholder="输入原密码"/>
      </el-form-item>
      <el-form-item label="新密码" prop="newPwd">
        <el-input class="form-input" type="password" v-model="form.newPwd" placeholder="输入新密码"/>
      </el-form-item>
      <el-form-item label="确认新密码" prop="confirmPwd">
        <el-input class="form-input" type="password" v-model="form.confirmPwd" placeholder="确认新密码"/>
      </el-form-item>
    </el-form>
    <div slot="footer" class="dialog-footer">
      <el-button @click="closeForm">取消</el-button>
      <el-button :loading="loading" type="primary" @click="changeLoginPwd">确认</el-button>
    </div>
  </el-dialog>
</template>

<script>
import {encryptParam} from '@/tools/jsencrypt'
import {changeLoginPwd} from '@/api/userCenter/security';
import {removeToken} from "@/tools/token";

export default {
  name: 'ChangeLoginPwd',
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
        oldPwd: '',
        newPwd: '',
        confirmPwd: ''
      },
      rules: {
        oldPwd: [{required: true, message: '请输入原密码', trigger: 'blur'}],
        newPwd: [{required: true, message: '请输入新密码', trigger: 'blur'}],
        confirmPwd: [{
          required: true,
          trigger: 'blur',
          validator: (rule, value, callback) => value !== this.form.newPwd ? callback(new Error('两次输入密码不一致!')) : callback()
        }]
      }
    }
  },
  watch: {
    show(newValue) {
      this.showDialog = newValue;
    }
  },
  methods: {
    changeLoginPwd() {
      this.$refs.form.validate(valid => {
        if (valid) {
          const {oldPwd, newPwd, confirmPwd} = this.form;
          const params = {
            oldPwd: encryptParam(oldPwd),
            newPwd: encryptParam(newPwd),
            confirmPwd: encryptParam(confirmPwd),
          }
          changeLoginPwd(params).then((res) => {
            if (res.code === 200) {
              this.$message.success('登录密码修改成功,请重新登录！');
              this.closeForm();
              setTimeout(() => {
                removeToken();
                const loginPath =
                    window.location.origin +
                    (window.location.pathname.includes('index.html')
                        ? window.location.pathname.replace('index', 'login')
                        : window.location.pathname.includes('login.html')
                            ? window.location.pathname
                            : window.location.pathname + 'login.html') +
                    '#/';
                window.location.href = loginPath;
              }, 500);
            } else {
              this.$message.error(res.msg);
            }
          })
        }
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
