<template>
    <div class="dashboard-container" style="text-align:center">
        <div style="margin:200px auto;">
            <h2 style="color: #4AB7BD">{{ welcomeMessage }}</h2>
        </div>
    </div>
</template>

<script>
export default {
    name: 'dashboard',
    data() {
        return {
            welcomeMessage: '欢迎使用XPAY后台管理系统'
        }
    }
}
</script>
