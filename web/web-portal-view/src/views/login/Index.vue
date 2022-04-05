<template>
  <div class="login-index-container">
    <div v-if="!hasLogin" v-loading="loginLoading" class="login-index-main">
      <div class="login-index-main-form">
        <p class="title">欢迎使用商户系统</p>
        <el-form ref="loginForm" :model="loginForm" :rules="loginRules" label-width="80px">
          <el-form-item label="登录名" prop="loginName">
            <el-input v-model.trim="loginForm.loginName" placeholder="请输入登录名"></el-input>
          </el-form-item>
          <el-form-item label="密码" prop="password">
            <el-input
              v-model.trim="loginForm.password"
              type="password"
              placeholder="请输入密码"
              @keyup.enter.native="loginSystem('ruleForm')"
            ></el-input>
          </el-form-item>
          <el-form-item label="验证码" prop="verifyCode" class="verify-code">
            <el-input
              v-model.trim="loginForm.verifyCode"
              maxlength="4"
              placeholder="请输入验证码"
              @keyup.enter.native="loginSystem('ruleForm')"
            ></el-input>
            <span class="verify-code-img" v-debounce="renderVerifyCode">
              <img :src="verifyCodeImgSrc" alt="验证码" />
            </span>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" round @click="loginSystem('ruleForm')">登录</el-button>
          </el-form-item>
        </el-form>
        <div class="login-index-main-actions">
          <el-button type="text" @click="$router.push({ name: 'ForgetPwd' })">忘记密码</el-button>
        </div>
      </div>
    </div>
    <div v-else class="login-index-main">
      <div class="login-index-already">
        <p class="title">您已登录，欢迎使用</p>
        <el-button type="primary" @click="jumpToApp()">进入后台</el-button>
      </div>
    </div>
  </div>
</template>

<script>
import { getImgVerifyCode, loginSys } from '@/api/public/login';
import { encryptParam } from '@/tools/jsencrypt';
import { getToken, setToken } from '@/tools/token';

export default {
  name: 'Login',
  data() {
    return {
      loginLoading: false,
      loginForm: {
        loginName: '',
        password: '',
        codeType: 1, //1=图形验证码 2=短信验证码
        codeKey: '',
        verifyCode: ''
      },
      verifyCodeImgSrc: '',
      loginRules: {
        loginName: [{ required: true, message: '请输入登录名', trigger: 'blur' }],
        password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
        verifyCode: [{ required: true, message: '请输入验证码', trigger: 'blur' }]
      },
      hasLogin: false
    };
  },
  mounted() {
    this.renderVerifyCode();
    this.hasLogin = !!getToken();
  },
  methods: {
    renderVerifyCode() {
      const oldCodeKey = this.loginForm.codeKey
      getImgVerifyCode(oldCodeKey).then(res => {
        if (res.code === 200) {
          this.loginForm.codeKey = res.data.codeKey;
          this.loginForm.verifyCode = '';
          this.verifyCodeImgSrc = `data:image/png;base64,${res.data.imgBase64}`;
        }
      });
    },
    loginSystem() {
      this.$refs['loginForm'].validate(valid => {
        if (valid) {
          this.loginLoading = true;
          let params = { ...this.loginForm };
          params.password = encryptParam(params.password);
          loginSys(params)
            .then(res => {
              let data;
              if (res.code === 200) {
                data = res.data;
              } else {
                this.loginLoading = false;
                throw Error(res.msg);
              }
              return data;
            })
            .then(data => {
              const { token } = data;
              setToken(token);
              this.$message.success('登录成功');
              this.loginLoading = false;
              setTimeout(() => {
                window.location.href = './index.html';
              }, 100);
            })
            .catch(() => {
              this.loginLoading = false;
              this.renderVerifyCode();
            });
        } else {
          this.$message.warning('请正确填写登录信息');
          return false;
        }
      });
    },
    jumpToApp() {
      setTimeout(() => {
        window.location.href = 'index.html#/';
      }, 100);
    }
  }
};
</script>

<style lang="stylus" scoped>
.login-index-container
  flex-grow 1;
  background url('../../assets/images/login/loign-bg.jpg') center no-repeat
  background-size cover;
  display flex
  justify-content center
  align-items  center

  .login-index-main
    border 1px solid white;
    border-radius 6px;
    background #ffffffdd;
    min-width 400px

  .login-index-main-form
    padding 40px
    .title
      margin 10px 20px 50px 20px
      text-align center
      font-size large
      color #00c1e7

  .login-index-already
    display: flex;
    flex-direction: column;
    justify-content: space-around;
    padding: 50px;
    height: 200px;
    .title
      margin 0 auto;

  .verify-code
    /deep/.el-form-item__content
      display flex
      cursor: pointer
      .el-input
        width 120px
      .verify-code-img
        padding-left 10px
        height 40px
        display flex
        align-items center
        img
          width 140px
          border-radius 6px;

  .el-button.is-round
    padding 12px 110px;
</style>
