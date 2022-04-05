<template>
    <div class="login-container">
        <el-form ref="loginForm" :model="loginForm" :rules="loginRules" class="login-form" autocomplete="on" label-position="left">

            <div class="title-container">
                <h3 class="title">企业管理后台</h3>
            </div>

            <el-form-item prop="loginName">
                    <span class="svg-container">
                        <svg-icon icon-class="user"/>
                    </span>
                <el-input
                    ref="loginName"
                    v-model="loginForm.loginName"
                    placeholder="请输入登录名"
                    name="loginName"
                    type="text"
                    tabindex="1"
                    autocomplete="on"
                />
            </el-form-item>

            <el-tooltip v-model="capsTooltip" content="大写已开启" placement="right" manual>
                <el-form-item prop="password">
                        <span class="svg-container">
                            <svg-icon icon-class="password"/>
                        </span>
                    <el-input
                        :key="passwordType"
                        ref="password"
                        v-model="loginForm.password"
                        :type="passwordType"
                        placeholder="请输入密码"
                        name="password"
                        tabindex="2"
                        autocomplete="on"
                        @keyup.native="checkCapslock"
                        @blur="capsTooltip = false"
                        @keyup.enter.native="loginSystem"
                    />
                    <span class="show-pwd" @click="showPwd">
                            <svg-icon :icon-class="passwordType === 'password' ? 'eye' : 'eye-open'"/>
                        </span>
                </el-form-item>
            </el-tooltip>

            <el-form-item prop="verifyCode" class="verify-code">
                <span class="svg-container">
                    <svg-icon icon-class="captcha"/>
                </span>
                <el-input
                    v-model.trim="loginForm.verifyCode"
                    maxlength="4"
                    placeholder="请输入验证码"
                    @keyup.enter.native="loginSystem()">
                </el-input>

                <div v-on:click="renderVerifyCode" class="verify-code-img">
                    <img :src="captchaImageSrc" alt="验证码"/>
                </div>
            </el-form-item>

            <el-button :loading="loading" type="primary" style="width:100%;margin-bottom:15px;"
                       @click.native.prevent="loginSystem">登&nbsp;录
            </el-button>

            <div class="login-index-main-actions">
                <el-button type="text" @click="$router.push({ path: '/forgetPwd' })">忘记密码</el-button>
            </div>
        </el-form>
    </div>
</template>

<script>
import {loginSys, getImgVerifyCode} from '@/api/public';
import {encryptParam} from '@/tools/jsencrypt';
import {getToken, setToken} from '@/tools/token';

export default {
    name: 'Login',
    data() {
        return {
            loginLoading: false,
            loginForm: {
                loginName: '',
                password: '',
                codeType: 1,
                codeKey: '',
                verifyCode: ''
            },
            loginRules: {
                loginName: [{required: true, message: '请输入登录名', trigger: 'blur'}],
                password: [{required: true, message: '请输入密码', trigger: 'blur'}],
                verifyCode: [{required: true, message: '请输入验证码', trigger: 'blur'}]
            },
            loading: false,
            passwordType: 'password',
            capsTooltip: false,
            redirect: undefined,
            captchaImageSrc: '',
            hasLogin: false
        }
    },
    mounted() {
        if (this.loginForm.loginName === '') {
            this.$refs.loginName.focus()
        } else if (this.loginForm.password === '') {
            this.$refs.password.focus()
        }

        this.renderVerifyCode();
        this.hasLogin = !!getToken();
    },
    methods: {
        checkCapslock(e) {
            const {key} = e
            this.capsTooltip = key && key.length === 1 && (key >= 'A' && key <= 'Z')
        },
        showPwd() {
            if (this.passwordType === 'password') {
                this.passwordType = ''
            } else {
                this.passwordType = 'password'
            }
            this.$nextTick(() => {
                this.$refs.password.focus()
            })
        },
        renderVerifyCode() {
            const oldCodeKey = this.loginForm.codeKey;
            getImgVerifyCode(oldCodeKey).then(res => {
                this.loginForm.codeKey = res.data.codeKey;
                this.loginForm.verifyCode = '';
                this.captchaImageSrc = `data:image/png;base64,${res.data.imgBase64}`;
            });
        },
        loginSystem() {
            this.$refs.loginForm.validate(valid => {
                if (!valid) {
                    this.$message.warning('请正确填写登录信息');
                    return false;
                }

                this.loading = true
                let params = {...this.loginForm};
                params.password = encryptParam(params.password);
                //step 1. 请求登陆
                loginSys(params)
                    .then((res) => {
                        if (res.code === 200) {
                            return res.data;
                        } else {
                            throw Error(res.msg);
                        }
                    })
                    .then(data => {
                        const {token} = data;
                        //step 2. 存储token
                        setToken(token);
                        this.$message.success('登录成功');
                        this.loading = false;
                        //step 3. 跳转到首页
                        this.$router.push({path: this.redirect || '/'})
                    })
                    .catch(() => {
                        this.loading = false;
                        this.renderVerifyCode()
                    });
            })
        }
    }
}
</script>

<style lang="scss">
/* 修复input 背景不协调 和光标变色 */
/* Detail see https://github.com/PanJiaChen/vue-element-admin/pull/927 */

$bg: #283443;
$light_gray: #fff;
$cursor: #fff;

@supports (-webkit-mask: none) and (not (cater-color: $cursor)) {
    .login-container .el-input input {
        color: $cursor;
    }
}

/* reset element-ui css */
.login-container {
    .el-input {
        display: inline-block;
        height: 47px;
        width: 85%;

        input {
            background: transparent;
            border: 0px;
            -webkit-appearance: none;
            border-radius: 0px;
            padding: 12px 5px 12px 15px;
            color: $light_gray;
            height: 47px;
            caret-color: $cursor;

            &:-webkit-autofill {
                box-shadow: 0 0 0px 1000px $bg inset !important;
                -webkit-text-fill-color: $cursor !important;
            }
        }
    }

    .el-form-item {
        border: 1px solid rgba(255, 255, 255, 0.1);
        background: rgba(0, 0, 0, 0.1);
        border-radius: 5px;
        color: #454545;
    }
}
</style>

<style lang="scss" scoped>
$bg: #2d3a4b;
$dark_gray: #889aa4;
$light_gray: #eee;

.login-container {
    min-height: 100%;
    width: 100%;
    background-color: $bg;
    overflow: hidden;

    .login-form {
        position: relative;
        width: 520px;
        max-width: 100%;
        padding: 160px 35px 0;
        margin: 0 auto;
        overflow: hidden;
    }

    .tips {
        font-size: 14px;
        color: #fff;
        margin-bottom: 10px;

        span {
            &:first-of-type {
                margin-right: 16px;
            }
        }
    }

    .svg-container {
        padding: 6px 5px 6px 15px;
        color: $dark_gray;
        vertical-align: middle;
        width: 10%;
        display: inline-block;
    }

    .title-container {
        position: relative;

        .title {
            font-size: 26px;
            color: $light_gray;
            margin: 0px auto 40px auto;
            text-align: center;
            font-weight: bold;
        }
    }

    .show-pwd {
        position: absolute;
        right: 10px;
        top: 7px;
        font-size: 16px;
        color: $dark_gray;
        cursor: pointer;
        user-select: none;
    }

    .thirdparty-button {
        position: absolute;
        right: 0;
        bottom: 6px;
    }

    .verify-code ::v-deep .el-form-item__content {
        display: flex;
        flex-wrap: nowrap;
        line-height: 40px;

        .el-input {
            width: 50%;
        }

        .verify-code-img {
            cursor: pointer;
            width: 40%;
            height: 44px;
            display: inline-block;

            img {
                width: 100%;
                border-radius: 2px 5px 5px 2px;
            }
        }
    }

    @media only screen and (max-width: 470px) {
        .thirdparty-button {
            display: none;
        }
    }
}
</style>
