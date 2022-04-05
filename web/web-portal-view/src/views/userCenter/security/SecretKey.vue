<template>
  <div class="security-center-container">
    <div class="security-center-main common-box">
      <div class="home-userinfo-box">
        <el-form ref="secretKeyForm" label-width="150px" size="medium" :model="secretKeyForm" :rules="secretKeyRules">
          <el-form-item label="算法类型" prop="signType">
            <el-select v-model="secretKeyForm.signType" placeholder="请选择" @change="signTypeChange">
              <el-option
                  v-for="item in signTypeArr"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value">
              </el-option>
            </el-select>
          </el-form-item>

          <el-form-item label="商户公钥" prop="mchPubKey">
            <el-input
              v-model.trim="secretKeyForm.mchPubKey"
              type="textarea"
              :rows="5"
              :readonly="actionType === 'VIEW'"
              placeholder="请输入商户公钥"
            ></el-input>
          </el-form-item>
          <el-form-item label="平台公钥" v-show="actionType === 'VIEW'">
            <el-input
                v-model.trim="secretKeyForm.platPubKey"
                type="textarea"
                :rows="5"
                :readonly="true"
                placeholder="平台公钥"
            ></el-input>
          </el-form-item>
          <el-form-item label="更新平台密钥对" v-show="actionType === 'EDIT'">
            <el-checkbox v-model="secretKeyForm.updatePlatKey" label="true">是</el-checkbox>
          </el-form-item>

          <el-divider direction="horizontal"></el-divider>

          <el-form-item label="手机号" prop="mobileNo" v-show="actionType === 'EDIT'">
            <el-input class="form-input" v-model="secretKeyForm.mobileNo" readonly style="width: 50%;"></el-input>
            <count-down :execute="sendSmsCode"></count-down>
          </el-form-item>
          <el-form-item label="手机验证码" prop="verifyCode" v-show="actionType === 'EDIT'">
            <el-input
                class="form-input"
                autocomplete="off"
                v-model="secretKeyForm.verifyCode"
                maxlength="6"
                auto-complete="off"
                placeholder="请输入手机验证码"
            ></el-input>
          </el-form-item>

          <el-form-item>
            <el-button v-authorize="'merchant:security:changeSecretKey'" v-show="actionType === 'VIEW'" @click="showEditSecretKey()">修改密钥</el-button>
            <el-button v-authorize="'merchant:security:changeSecretKey'" v-show="actionType === 'EDIT'" @click="cancelEditSecretKey()">取消</el-button>
            <el-button v-authorize="'merchant:security:changeSecretKey'" v-show="actionType === 'EDIT'" type="primary" @click="updateSecretKey()" :loading="loading">提交</el-button>
          </el-form-item>
        </el-form>
      </div>
    </div>
  </div>
</template>

<script lang="js">
import CountDown from '@/components/CountDown'
import {
  getSecretPublicKey,
  sendChangeSecKeyCode,
  changeSecretKey
} from '@/api/userCenter/security'

export default {
  name: 'SecretKey',
  components: {
    CountDown
  },
  data() {
    return {
      actionType: 'VIEW',
      signTypeArr: [{value:2, label:'RSA256'}, {value:3, label:'SM2'}],
      showEditDialog: false,
      loading: false,
      secretKeyForm: {
        mobileNo: '',
        verifyCode: '',
        signType: '',
        mchPubKey: '',
        platPubKey: '',
        updatePlatKey: false,
      },
      secretKeys: [],
      secretKeyRules: {
        verifyCode: [
          { required: true, message: '请输入手机验证码', trigger: 'blur' }
        ],
      }
    }
  },
  computed: {
    userInfo() {
      return this.$store.state.userInfo;
    }
  },
  mounted() {
    const mobileNo = this.$store.getters.getUserInfoByKey('mobileNo');
    this.secretKeyForm.mobileNo = mobileNo;
    this.getPublicKeys()
  },
  methods: {
    closeDialog() {
      this.showEditDialog = false;
    },
    getPublicKeys() {
      getSecretPublicKey().then((res) => {
        if(res.code == 200 && res.data){
          this.secretKeys = res.data
        }
      })
    },
    signTypeChange(val) {
      this.secretKeyForm.mchPubKey = ''
      this.secretKeyForm.platPubKey = ''
      this.secretKeys.forEach(secKey => {
        if(parseInt(secKey.signType) === parseInt(val)){
          this.secretKeyForm.mchPubKey = secKey.mchPublicKey
          this.secretKeyForm.platPubKey = secKey.platPublicKey
        }
      })
    },
    sendSmsCode() {
      sendChangeSecKeyCode().then((res) => {
        if (res.code === 200) {
          this.$message.success('短信验证码已发送');
        }
      })
    },
    updateSecretKey() {
      this.$refs.secretKeyForm.validate((valid) => {
        if (valid) {
          const params = {
            verifyCode: this.secretKeyForm.verifyCode,
            signType: this.secretKeyForm.signType,
            mchPubKey: this.secretKeyForm.mchPubKey,
            updatePlatKey: this.secretKeyForm.updatePlatKey
          };

          this.loading = true
          changeSecretKey(params).then((res) => {
            if (res.code === 200) {
              this.$message.success(res.msg || '密钥提交成功！');
              this.cancelEditSecretKey()
            }
          }).finally(() => {
            this.loading = false
          });
        }
      })
    },
    showEditSecretKey() {
      this.actionType = 'EDIT'
    },
    cancelEditSecretKey() {
      this.actionType = 'VIEW'
    },
  },
}
</script>

<style lang="stylus" scoped>
.security-center-main
  margin: 4vh 4vw;
  padding: 4vh 4vw;

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

  .tip
    color: #FC9942;
    border: 1px solid #FC9942;
    border-radius 6px;
    background-color: #FC994220;
    padding: 1vh 1vw;
    margin: 1vh auto;

  .home-userinfo-box
    display flex
    width 400px
    flex-wrap wrap;
    justify-content space-between;
    align-items center
    // border 1px solid #EBEEF5
    // border-radius 6px
    padding 20px
    // background #fff;
    // box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
    margin 0 auto;
    .el-form-item
      min-width: 450px;
    &-item
      display: flex;
      margin: 1vh 0;
      justify-content: space-between;
      align-items: flex-start;
      width: 100%;
      .label::after
        content ':';
      .label
        width: 20vw;
      .dashed
        border-bottom 1px dashed #333;
        flex-grow 1;
        height 1px;
        margin 0 6px
      .info
        width: 50vw;
</style>
