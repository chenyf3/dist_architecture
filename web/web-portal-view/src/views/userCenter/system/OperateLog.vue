<template>
  <div class="operate-log search-table-wrapper">
    <div class="search-table-form">
      <el-form ref="form" :inline="true" size="medium" :model="searchForm" label-width="100px">
        <div class="search-form-row">
          <el-form-item label="操作类型">
            <el-select v-model="searchForm.operateType" placeholder="请选择" clearable>
              <el-option
                v-for="item in DICT['OperateLogTypeEnum']"
                :key="item.value"
                :label="item.desc"
                :value="item.code"
              >
              </el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="登录名">
            <el-input v-model="searchForm.loginName" placeholder="请输入登录名"></el-input>
          </el-form-item>
        </div>
        <div class="search-form-date-row">
          <date-picker-group labelText="创建时间" ref="date-picker" @pick-success="pickSuccess" />
          <el-form-item>
            <el-button type="primary" @click="executeSearch()">
              <i class="el-icon-search"></i>
              查询
            </el-button>
          </el-form-item>
        </div>
      </el-form>
    </div>

    <div class="search-table-data">
      <el-table :data="tableData" border style="width: 100%">
        <el-table-column label="序号" type="index" width="50"> </el-table-column>
        <el-table-column prop="createTime" label="创建时间" sortable>
          <template slot-scope="scope">
            {{ scope.row.createTime | timeFilter }}
          </template>
        </el-table-column>
        <el-table-column prop="loginName" label="操作人"></el-table-column>
        <el-table-column prop="type" label="操作类型">
          <template slot-scope="scope">
            {{ String(scope.row.operateType) | DictFilter('OperateLogTypeEnum') }}
          </template>
        </el-table-column>
        <el-table-column prop="content" label="操作内容"></el-table-column>
      </el-table>
      <div class="search-table-pagination">
        <el-pagination
          background
          layout="total, sizes, prev, pager, next, jumper"
          :total="pageData.totalRecord"
          :page-sizes="[20, 50, 100]"
          :current-page="pageData.currentPage"
          :page-size="pageData.pageSize"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        >
        </el-pagination>
      </div>
    </div>
  </div>
</template>

<script lang="js">
import DatePickerGroup from '@/components/public/DatePicker';
import {
  listOperateLog,
} from '@/api/userCenter/system';
import dict from '@/mixins/dict';

export default {
  name: 'OperateLog',
  mixins: [dict],
  components: {
    'date-picker-group': DatePickerGroup,
  },
  data() {
    return {
      searchForm: {
        loginName: '',
        realName: '',
        createTimeBegin: '',
        createTimeEnd: '',
        status: '',
      },
      pageData: {
        currentPage: 1,
        pageSize: 20,
        totalRecord: 0,
      },
      tableData: [],
    }
  },
  mounted() {
    // this.listAllRoles();
    this.executeSearch();
  },
  methods: {
    pickSuccess(pickedTime) {
      this.searchForm.createTimeBegin = pickedTime.beginDate;
      this.searchForm.createTimeEnd = pickedTime.endDate;
    },
    executeSearch() {
      let params = {...this.searchForm}
      params.currentPage = this.pageData.currentPage
      params.pageSize = this.pageData.pageSize

      listOperateLog(params)
        .then((res) => {
          if (res.code === 200) {
            return res.data;
          }
        }).then((data) => {
          this.tableData = data.data;
          this.pageData.currentPage = data.currentPage;
          this.pageData.pageSize = data.pageSize;
          this.pageData.totalRecord = data.totalRecord;
        })
    },
    handleSizeChange(val) {
      this.pageData.pageSize = val;
      this.pageData.currentPage = 1;
      this.executeSearch();
    },
    handleCurrentChange(val) {
      this.pageData.currentPage = val;
      this.executeSearch();
    },
  },
}
</script>

<style lang="stylus" scoped></style>
