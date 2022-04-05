<template>
    <div class="navbar">
        <hamburger id="hamburger-container" :is-active="sidebar.opened" class="hamburger-container"
                   @toggleClick="toggleSideBar"/>

        <breadcrumb id="breadcrumb-container" class="breadcrumb-container"/>

        <div class="right-menu">
            <template v-if="device !== 'mobile'">
                <screenfull id="screenfull" class="right-menu-item hover-effect"/>

                <el-tooltip content="字体大小" effect="dark" placement="bottom">
                    <size-select id="size-select" class="right-menu-item hover-effect"/>
                </el-tooltip>
            </template>

            <el-dropdown class="avatar-container right-menu-item hover-effect" trigger="click">
                <!--                <div class="avatar-wrapper">-->
                <!--                    <img :src="signedInInfo.userInfo.avatar+'?imageView2/1/w/80/h/80'" class="user-avatar">-->
                <!--                    <i class="el-icon-caret-bottom"/>-->
                <!--                </div>-->
                <div class="el-dropdown-link" style="cursor: pointer; font-size: small">
                    {{ signedInInfo ? signedInInfo.userInfo.realName : "" }}
                    <i class="el-icon-arrow-down el-icon--right"/>
                </div>

                <el-dropdown-menu slot="dropdown">
                    <a target="#" @click="userInfoPage">
                        <el-dropdown-item>个人信息</el-dropdown-item>
                    </a>
                    <a target="#" @click="changePwdPage">
                        <el-dropdown-item>修改密码</el-dropdown-item>
                    </a>
                    <el-dropdown-item divided @click.native="logoutUser">
                        <span style="display:block;">退出</span>
                    </el-dropdown-item>
                </el-dropdown-menu>
            </el-dropdown>
        </div>

        <change-pwd ref="changePwd"/>
        <user-info-form ref="userInfoForm"/>
    </div>
</template>

<script>
import {mapGetters} from 'vuex'
import Breadcrumb from '@/components/Breadcrumb'
import Hamburger from '@/components/Hamburger'
import Screenfull from '@/components/Screenfull'
import SizeSelect from '@/components/SizeSelect'
import {logoutSys} from '@/api/public';
import {logout} from '@/tools/logout';

export default {
    components: {
        Breadcrumb,
        Hamburger,
        Screenfull,
        SizeSelect,
        ChangePwd: () => import('@/components/UserCenter/ChangePwd'),
        UserInfoForm: () => import('@/components/UserCenter/UserInfoForm'),
    },
    computed: {
        ...mapGetters([
            'sidebar',
            'signedInInfo',
            'device'
        ])
    },
    methods: {
        toggleSideBar() {
            this.$store.dispatch('app/toggleSideBar')
        },
        userInfoPage() {
            this.$refs.userInfoForm.showUserInfo()
        },
        changePwdPage() {
            this.$refs.changePwd.show = true
        },
        logoutUser() {
          logoutSys().finally(() => logout())
        }
    }
}
</script>

<style lang="scss" scoped>
.navbar {
    height: 50px;
    overflow: hidden;
    position: relative;
    background: #fff;
    box-shadow: 0 1px 4px rgba(0, 21, 41, .08);

    .hamburger-container {
        line-height: 46px;
        height: 100%;
        float: left;
        cursor: pointer;
        transition: background .3s;
        -webkit-tap-highlight-color: transparent;

        &:hover {
            background: rgba(0, 0, 0, .025)
        }
    }

    .breadcrumb-container {
        float: left;
    }

    .errLog-container {
        display: inline-block;
        vertical-align: top;
    }

    .right-menu {
        float: right;
        height: 100%;
        line-height: 50px;

        &:focus {
            outline: none;
        }

        .right-menu-item {
            display: inline-block;
            padding: 0 8px;
            height: 100%;
            font-size: 18px;
            color: #5a5e66;
            vertical-align: text-bottom;

            &.hover-effect {
                cursor: pointer;
                transition: background .3s;

                &:hover {
                    background: rgba(0, 0, 0, .025)
                }
            }
        }

        .avatar-container {
            margin-right: 30px;

            .avatar-wrapper {
                margin-top: 5px;
                position: relative;

                .user-avatar {
                    cursor: pointer;
                    width: 40px;
                    height: 40px;
                    border-radius: 10px;
                }

                .el-icon-caret-bottom {
                    cursor: pointer;
                    position: absolute;
                    right: -20px;
                    top: 25px;
                    font-size: 12px;
                }
            }
        }
    }
}
</style>
