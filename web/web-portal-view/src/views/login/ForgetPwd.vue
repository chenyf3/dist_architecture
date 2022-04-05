<template>
  <div class="forget-login-pwd">
    <div class="forget-login-pwd-box" v-loading="formLoading">
      <p class="title">找回密码</p>
      <el-form :model="ruleForm" :rules="rules" ref="ruleForm" label-width="120px">
        <el-form-item label="登录名" prop="loginName">
          <el-input v-model="ruleForm.loginName" placeholder="请输入登录名"></el-input>
        </el-form-item>
        <el-form-item v-if="!showFindPwdInfo" label="找回方式" prop="type">
          <el-radio-group v-model="ruleForm.type">
            <el-radio :label="1">手机</el-radio>
            <el-radio :label="2">邮箱</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-if="showFindPwdInfo" label="验证码" prop="code">
          <el-input maxlength="6" v-model="ruleForm.code" placeholder="请输入验证码"></el-input>
        </el-form-item>
        <el-form-item v-if="showFindPwdInfo" label="新登录密码" prop="newPwd">
          <el-input v-model="ruleForm.newPwd" type="password" placeholder="请输入新登录密码"></el-input>
        </el-form-item>
        <el-form-item v-if="showFindPwdInfo" label="确认登录密码" prop="confirmPwd">
          <el-input v-model="ruleForm.confirmPwd" type="password" placeholder="请输入新登录密码"></el-input>
        </el-form-item>
        <el-form-item>
          <el-button v-if="!showFindPwdInfo" type="primary" @click="getVerifyCode()">
            获取验证码
          </el-button>
          <el-button v-if="showFindPwdInfo" type="primary" @click="findPwd()">
            找回密码
          </el-button>
          <el-button type="text" @click="$router.push({ name: 'Login' })">
            返回登录
          </el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script>
import { sendForgetLoginPwd, retrieveLoginPwd } from '@/api/public';
import { encryptParam } from '@/tools/jsencrypt';
import { checkPwd, formCheckPwd } from '@/tools/validator';

export default {
  name: 'ForgetPwd',
  data() {
    const loginPwdValid = (rule, value, cb) => {
      if (!checkPwd(value)) {
        cb('密码应为8-20位数字,字母和特殊字符组合');
      } else if (value !== this.ruleForm.newPwd) {
        cb('两次密码不相同，请确认！');
      } else {
        cb();
      }
    };
    return {
      formLoading: false,
      ruleForm: {
        loginName: '',
        type: 1,
        code: '',
        newPwd: '',
        confirmPwd: ''
      },
      rules: {
        loginName: [{ required: true, message: '请输入登录名', trigger: 'blur' }],
        type: [{ required: true, message: '请选择找回方式', trigger: 'blur' }],
        code: [{ required: true, message: '请输入验证码', trigger: 'blur' }],
        newPwd: [
          { required: true, message: '请输入新登录密码', trigger: 'blur' },
          { validator: formCheckPwd, trigger: 'blur' }
        ],
        confirmPwd: [
          { required: true, message: '请确认登录密码', trigger: 'blur' },
          { validator: loginPwdValid, trigger: 'blur' }
        ]
      },
      showFindPwdInfo: false
    };
  },
  methods: {
    getVerifyCode() {
      this.formLoading = true;
      this.$refs['ruleForm'].validate(valid => {
        if (valid) {
          const { loginName, type } = this.ruleForm;
          sendForgetLoginPwd(`loginName=${loginName}&type=${encodeURI(type)}`)
            .then(res => {
              this.formLoading = false;
              if (res.code === 200) {
                this.$message.success('验证码发送成功，请填写验证码');
                this.showFindPwdInfo = true;
              }
            })
            .catch(() => {
              this.formLoading = false;
            });
        } else {
          this.formLoading = false;
        }
      });
    },
    findPwd() {
      this.formLoading = true;
      this.$refs['ruleForm'].validate(valid => {
        if (valid) {
          const { loginName, code, newPwd, confirmPwd } = this.ruleForm;
          retrieveLoginPwd({
            loginName,
            code,
            newPwd: encryptParam(newPwd),
            confirmPwd: encryptParam(confirmPwd)
          })
            .then(res => {
              this.formLoading = false;
              if (res.code === 200) {
                this.$message.success('找回密码成功，请使用新密码登录');
                this.$router.push({ name: 'Login' });
              } else {
                this.$message.error(res.msg);
              }
            })
            .catch(() => {
              this.formLoading = false;
            });
        } else {
          this.formLoading = false;
          this.$message.error('请正确填写信息');
        }
      });
    }
  }
};
</script>

<style lang="stylus" scoped>
.forget-login-pwd
  background url('../../assets/images/login/loign-bg.jpg') center no-repeat
  background-size cover;
  flex-grow 1;
  display flex
  justify-content center;
  align-items center;
  .forget-login-pwd-box
    background #fffe;
    padding 30px;
    border 1px solid #EBEEF5;
    border-radius 6px;
    box-shadow: 0 1px 4px rgba(0,21,41,0.2);
    .title
      color: #333;
      margin-bottom 2vh;
  .el-input
    width: 260px;
</style>
