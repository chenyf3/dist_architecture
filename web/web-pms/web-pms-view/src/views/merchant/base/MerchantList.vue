<template>
    <div v-loading="loading" class="app-container">
        <el-form :inline="true" size="small">
            <el-form-item label="商户编号">
                <el-input v-model="searchForm.mchNo" placeholder="精确查询"></el-input>
            </el-form-item>
            <el-form-item label="商户名称">
                <el-input v-model="searchForm.fullName" placeholder="模糊查询"></el-input>
            </el-form-item>
            <el-form-item label="商户类型">
                <el-select placeholder="商户类型" v-model="searchForm.mchType" clearable>
                    <el-option v-for="{code,desc} in $dictArray('MchTypeEnum')"
                               :key="parseInt(code)" :label="desc" :value="parseInt(code)"
                    />
                </el-select>
            </el-form-item>
            <el-form-item label="状态">
                <el-select placeholder="状态" v-model="searchForm.status" clearable>
                    <el-option v-for="{code,desc} in $dictArray('MerchantStatusEnum')"
                               :key="parseInt(code)" :label="desc" :value="parseInt(code)"
                    />
                </el-select>
            </el-form-item>

            <el-form-item>
                <el-button type="primary" @click="refreshList"><i class="el-icon-search"/>查询</el-button>
                <el-button type="success" plain v-authorize="'merchant:merchant:add'" @click="addMerchantPage"><i class="el-icon-plus"/>新增商户</el-button>
            </el-form-item>
        </el-form>

        <div>
            <el-table :data="pageResult.data" border row-key="id">
                <el-table-column label="序号" type="index" width="50px"/>
                <el-table-column label="商户编号" prop="mchNo"/>
                <el-table-column label="商户全称" prop="fullName"/>
                <el-table-column label="商户简称" prop="shortName"/>
                <el-table-column label="商户类型" prop="type"
                                 :formatter="row=>$dictName('MchTypeEnum', row.mchType)"/>
                <el-table-column label="商户状态" prop="status"
                                 :formatter="row=>$dictName('MerchantStatusEnum', row.status)"/>
                <el-table-column v-slot="{row}" label="操作" min-width="100px">
                    <el-button v-authorize="'merchant:merchant:view'" type="text" size="small"
                               @click="viewMerchantPage(row.mchNo)">查看
                    </el-button>
                    <el-button v-authorize="'merchant:merchant:edit'" type="text" size="small"
                               @click="editMerchantPage(row.mchNo)">编辑
                    </el-button>
                    <el-button v-authorize="'merchant:pwd:resetTradePwd'" type="text"
                               size="small" @click="resetTradePwd(row.mchNo)">重置支付密码
                    </el-button>
                </el-table-column>
            </el-table>
        </div>

        <div class="pagination-container">
            <pagination
                v-show="pageResult.totalRecord > 0"
                :total="pageResult.totalRecord"
                :page.sync="searchForm.currentPage"
                :limit.sync="searchForm.pageSize"
                @pagination="refreshList"
            />
        </div>

        <merchant-form ref="merchantForm" @success="refreshList"/>
        <reset-trade-pwd ref="resetTradePwdForm"/>
    </div>
</template>

<script>
import Pagination from '@/components/Pagination'
import MerchantForm from './MerchantForm'
import ResetTradePwd from './ResetTradePwd';
import {listMerchant} from '@/api/merchant/baseInfo';

export default {
    name: 'MerchantList',
    components: {
        Pagination,
        MerchantForm,
        ResetTradePwd,
    },
    data() {
        return {
            loading: false,
            searchForm: {
                currentPage: 1,
                pageSize: 10,
                mchNo: '',
                fullName: '',
                mchType: '',
                status: '',
            },
            pageResult: {}
        }
    },
    mounted() {
        this.refreshList()
    },
    methods: {
        refreshList() {
            this.loading = true
            listMerchant(this.searchForm).then(res => {
                this.pageResult = res.data
            }).finally(() => {
                this.loading = false
            })
        },
        addMerchantPage() {
            this.$refs.merchantForm.initAndShow('ADD')
        },
        editMerchantPage(mchNo) {
            this.$refs.merchantForm.initAndShow('EDIT', mchNo)
        },
        viewMerchantPage(mchNo) {
            this.$refs.merchantForm.initAndShow('VIEW', mchNo)
        },
        resetTradePwd(mchNo) {
            this.$refs.resetTradePwdForm.initAndShow(mchNo)
        }
    }
}
</script>
