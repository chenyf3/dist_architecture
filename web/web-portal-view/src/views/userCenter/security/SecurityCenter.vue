<template>
  <div class="security-center-container">
    <div class="security-center-main common-box">
      <div class="home-userinfo-box">
        <template v-for="item in userInfoEnum">
          <span class="home-userinfo-box-item" :key="item.id">
            <span class="label">
              {{ item.label }}
            </span>
            <span class="dashed"></span>
            <span class="info">
              <template v-if="item.key !== 'mchType'">
                {{ userInfo[item.key] }}
              </template>
              <template v-else>
                {{ userInfo[item.key] | DictFilter('MchTypeEnum') }}
              </template>
            </span>
          </span>
        </template>
      </div>
      <div class="security-center-main-table">
        <div v-authorize="'merchant:security:changeTradePwd'" class="security-center-row common-box">
          <span class="title">
            <i class="el-icon-lock"></i>支付密码
          </span>
          <span class="text">保护资金安全</span>
          <span>
            <el-button type="default" @click="openEditDialog('change')">修改</el-button>
            <el-button type="default" @click="openEditDialog('reset')">重置</el-button>
          </span>
        </div>
      </div>

      <el-dialog width="500px" :title="editDialog.title" :close-on-click-modal="false" :visible.sync="editDialog.showChangeDialog">
        <el-form ref="changeForm" label-width="100px" :model="changeForm" :rules="changeRules" auto-complete="off">
          <el-form-item label="手机号" prop="mobileNo">
            <el-input class="form-input inline-mobile-no" v-model="changeForm.mobileNo" readonly></el-input>
            <count-down :execute="sendChangeTradePwdSmsCode"></count-down>
          </el-form-item>
          <el-form-item label="验证码" prop="smsCode">
            <el-input
                class="form-input"
                autocomplete="off"
                v-model.number="changeForm.smsCode"
                maxlength="4"
                placeholder="请输入手机验证码"
            ></el-input>
          </el-form-item>
          <el-form-item label="原密码" prop="oldPwd">
            <el-input
              class="form-input"
              type="password"
              autocomplete="off"
              v-model="changeForm.oldPwd"
              placeholder="请输入原密码"
            ></el-input>
          </el-form-item>
          <el-form-item label="新密码" prop="newPwd">
            <el-input
              class="form-input"
              type="password"
              autocomplete="off"
              v-model="changeForm.newPwd"
              placeholder="请输入新密码"
            ></el-input>
          </el-form-item>
          <el-form-item label="确认新密码" prop="confirmPwd">
            <el-input
              class="form-input"
              type="password"
              autocomplete="off"
              v-model="changeForm.confirmPwd"
              placeholder="请输入新密码"
            ></el-input>
          </el-form-item>
          <el-form-item>
            <el-button type="default" @click="closeDialog()">取消</el-button>
            <el-button type="primary" @click="submitForm()" :loading="loading">修改密码</el-button>
          </el-form-item>
        </el-form>
      </el-dialog>

      <el-dialog width="400px" :title="editDialog.title" :close-on-click-modal="false" :visible.sync="editDialog.showResetDialog">
        <el-form ref="resetForm" label-width="100px" :model="resetForm" :rules="resetRules">
          <el-tooltip class="item" effect="dark" content="请联系平台客服发送重置验证码" placement="top">
            <el-form-item label="验证码" prop="verifyCode">
              <el-input
                  class="form-input"
                  autocomplete="off"
                  v-model.number="resetForm.verifyCode"
                  maxlength="4"
                  placeholder="请输入手机验证码"
              ></el-input>
            </el-form-item>
          </el-tooltip>
          <el-form-item label="新密码" prop="newPwd">
            <el-input
                class="form-input"
                type="password"
                autocomplete="off"
                v-model="resetForm.newPwd"
                placeholder="请输入新密码"
            ></el-input>
          </el-form-item>
          <el-form-item label="确认新密码" prop="confirmPwd">
            <el-input
                class="form-input"
                type="password"
                autocomplete="off"
                v-model="resetForm.confirmPwd"
                placeholder="请输入新密码"
            ></el-input>
          </el-form-item>
          <el-form-item>
            <el-button type="default" @click="closeDialog()">取消</el-button>
            <el-button type="primary" @click="submitForm()" :loading="loading">重置密码</el-button>
          </el-form-item>
        </el-form>
      </el-dialog>
    </div>
  </div>
</template>

<script lang="js">
import CountDown from '@/components/CountDown';
import { encryptParam } from '@/tools/jsencrypt';
import {
  sendTradePwdCode,
  changeTradePwd,
  resetTradePwd
} from '@/api/userCenter/security'

export default {
  name: 'SecurityCenter',
  components: {
    CountDown
  },
  data() {
    return {
      userInfoEnum: [
        { label: '登录名', key: 'loginName' },
        { label: '手机号', key: 'mobileNo' },
        { label: '邮箱', key: 'email' },
        { label: '商户号', key: 'mchNo' },
        { label: '商户类型', key: 'mchType' }
      ],
      loading: false,
      editDialog: {
        type: '',
        title: '',
        showChangeDialog: false,
        showResetDialog: false,
      },
      changeForm: {
        mobileNo: '',
        oldPwd: '',
        newPwd: '',
        confirmPwd: '',
        smsCode: ''
      },
      resetForm: {
        verifyCode: '',
        newPwd: '',
        confirmPwd: '',
      },
      changeRules: {
        smsCode: [
          { required: true, message: '请输入手机验证码', trigger: 'blur' },
          { type: 'number', message: '验证码应为纯数字', trigger: 'blur'}
        ],
        oldPwd: [
          { required: true, message: '请输入旧密码', trigger: 'blur' }
        ],
        newPwd: [
          { required: true, message: '请输入新密码', trigger: 'blur' }
        ],
        confirmPwd: [
          { required: true, message: '请输入新密码', trigger: 'blur' }
        ],
      },
      resetRules: {
        verifyCode: [
          { required: true, message: '请输入重置验证码', trigger: 'blur' },
          { type: 'number', message: '验证码应为纯数字', trigger: 'blur'}
        ],
        newPwd: [
          { required: true, message: '请输入新密码', trigger: 'blur' }
        ],
        confirmPwd: [
          { required: true, message: '请输入新密码', trigger: 'blur' }
        ],
      },
    }
  },
  computed: {
    userInfo() {
      return this.$store.state.userInfo;
    }
  },
  mounted() {
    const mobileNo = this.$store.getters.getUserInfoByKey('mobileNo');
    this.changeForm.mobileNo = mobileNo;
  },
  methods: {
    openEditDialog(type){
      this.editDialog.type = type;
      if (type === 'change') {
        this.editDialog.showChangeDialog = true
        this.editDialog.title = '修改支付密码'
      } else if (type === 'reset') {
        this.editDialog.showResetDialog = true
        this.editDialog.title = '重置支付密码'
      }
    },
    closeDialog() {
      if (this.editDialog.type === 'change') {
        this.editDialog.showChangeDialog = false
        this.$refs.changeForm.resetFields();
      } else if (this.editDialog.type === 'reset') {
        this.editDialog.showResetDialog = false
        this.$refs.resetForm.resetFields();
      }
    },
    sendChangeTradePwdSmsCode() {
      sendTradePwdCode().then(resp => {
        if(resp.code === 200){
          this.$message.success(resp.data);
        }
      })
    },
    submitForm() {
      if (this.editDialog.type === 'change') {
        this.$refs.changeForm.validate((valid) => {
          if (valid) {
            this.changeTradePwd();
          }
        })
      } else if (this.editDialog.type === 'reset') {
        this.$refs.resetForm.validate((valid) => {
          if (valid) {
            this.resetTradePwd();
          }
        })
      }
    },
    changeTradePwd() {
      const { oldPwd, newPwd, confirmPwd, smsCode } = this.changeForm;
      const params = {
        oldPwd: encryptParam(oldPwd),
        newPwd: encryptParam(newPwd),
        confirmPwd: encryptParam(confirmPwd),
        smsCode: smsCode
      }
      this.loading = true
      changeTradePwd(params).then((res) => {
        if (res.code === 200) {
          this.$message.success('修改支付密码成功');
          this.closeDialog();
        } else {
          this.$message.error(res.msg);
        }
      }).finally(() => {
        this.loading = false
      });
    },
    resetTradePwd() {
      const { newPwd, confirmPwd, verifyCode } = this.resetForm;
      const params = {
        newPwd: encryptParam(newPwd),
        confirmPwd: encryptParam(confirmPwd),
        verifyCode: verifyCode,
      }
      this.loading = true
      resetTradePwd(params).then((res) => {
        if (res.code === 200) {
          this.$message.success('重置支付密码成功');
          this.closeDialog();
        } else {
          this.$message.error(res.msg);
        }
      }).finally(() => {
        this.loading = false
      });
    }
  },
}
</script>

<style lang="stylus" scoped>
.security-center-container
  font-size 16px;
.security-center-main-table
  margin 20px 10px
  display flex
  justify-content: space-around;

.security-center-row
  margin 10px
  display flex
  justify-content space-evenly
  align-items center
  min-width 400px
  .title
    min-width 90px;
    color: #FC9942
  .text
    margin 0 20px
    border-bottom 1px dashed #EBEEF5;

.home-userinfo-box
  display flex
  width 400px
  flex-wrap wrap;
  justify-content space-between
  align-items center
  border 1px solid #EBEEF5
  border-radius 6px
  padding 20px
  background #fff;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  margin 0 auto;
  &-item
    display inline-flex
    margin 6px 0
    justify-content space-between
    align-items center
    width 100%
    .label::after
      content ':';
    .dashed
      border-bottom 1px dashed #333;
      flex-grow 1;
      height 1px;
      margin 0 6px

.el-form
  .inline-mobile-no
    width 58%
</style>
