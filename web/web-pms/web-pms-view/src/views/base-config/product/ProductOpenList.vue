<template>
    <div v-loading="loading" class="app-container">
        <el-form :inline="true" size="small">
            <el-form-item label="商户编号">
                <el-input v-model="searchForm.mchNo" placeholder="精确查询"></el-input>
            </el-form-item>
            <el-form-item label="状态">
                <el-select placeholder="请选择" v-model="searchForm.status" clearable>
                    <el-option key="1" label="启用" value="1"/>
                    <el-option key="2" label="禁用" value="2"/>
                </el-select>
            </el-form-item>
            <el-form-item label="业务线">
                <el-select v-model="searchForm.productType" @change="productTypeChange" class="selectInput" clearable>
                    <el-option v-for="(value,key) in productTypeMap" :key="key" :label="value" :value="key"/>
                </el-select>
            </el-form-item>
            <el-form-item label="产品名称">
                <el-select v-model="searchForm.productCode" class="selectInput" clearable>
                    <el-option v-for="(value,key) in currProductCodeMap" :key="key" :label="value" :value="key"/>
                </el-select>
            </el-form-item>
            <el-form-item>
                <el-button type="primary" @click="refreshList"><i class="el-icon-search"/>查询</el-button>
                <el-button type="success" plain v-authorize="'baseConfig:productOpen:add'" @click="addProductOpenPage"><i class="el-icon-plus"/>新增产品开通</el-button>
            </el-form-item>
        </el-form>

        <div>
            <el-table :data="pageResult.data" border row-key="id">
                <el-table-column label="序号" type="index" width="50px"/>
                <el-table-column label="商户编号" prop="mchNo"/>
                <el-table-column label="业务线" prop="productType"
                                 :formatter="row=>$dictName('ProductTypeEnum', row.productType)"/>
                <el-table-column label="产品名称" prop="productCode"
                                 :formatter="row=>$dictName('ProductCodeEnum', row.productCode)"/>
                <el-table-column label="状态" v-slot="{row}">
                     <span :style="row.status === 1 ? 'color:green;' : row.status === 2 ? 'color:red' : ''">
                         {{ row.status === 1 ? '启用' : '禁用' }}
                     </span>
                </el-table-column>
                <el-table-column label="过期时间" prop="expireDate" >
                    <template slot-scope="scope">
                        {{ scope.row.expireDate | dayFilter }}
                    </template>
                </el-table-column>
                <el-table-column label="备注" prop="remark"/>
                <el-table-column v-slot="{row}" label="操作">
                    <el-button v-authorize="'baseConfig:productOpen:edit'" type="text" size="small"
                               @click="editProductOpenPage(row)" style="color:blue;">编辑
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

        <product-open-from ref="productOpenFrom" @success="refreshList"/>
    </div>
</template>

<script>
import Pagination from '@/components/Pagination/index'
import ProductOpenFrom from './ProductOpenFrom';
import {listProductOpenPage} from '@/api/baseConfig/product';
import {getProductInfo} from '@/api/public';

export default {
    name: 'MerchantList',
    components: {
        Pagination,
        ProductOpenFrom,
    },
    data() {
        return {
            loading: false,
            productTypeMap: {},
            productCodeMap: {},
            currProductCodeMap: {},
            searchForm: {
                currentPage: 1,
                pageSize: 10,
                mchNo: '',
                status: '',
                productType: '',
                productCode: '',
            },
            pageResult: {}
        }
    },
    mounted() {
        this.getProductInfo();
        this.refreshList()
    },
    methods: {
        refreshList() {
            this.loading = true
            listProductOpenPage(this.searchForm).then(res => {
                this.pageResult = res.data
            }).finally(() => {
                this.loading = false
            })
        },
        getProductInfo(){
            getProductInfo().then(({data}) => {
                this.productTypeMap = data.productTypeMap
                this.productCodeMap = data.productCodeMap
            });
        },
        productTypeChange(currType){
            this.searchForm.productCode = ''
            this.currProductCodeMap = {}
            this.currProductCodeMap = this.productCodeMap[currType] ? this.productCodeMap[currType] : {}
        },
        addProductOpenPage(){
            this.$refs.productOpenFrom.initAndShow({}, 'ADD')
        },
        editProductOpenPage(row) {
            this.$refs.productOpenFrom.initAndShow({...row}, 'EDIT')
        },
    }
}
</script>
