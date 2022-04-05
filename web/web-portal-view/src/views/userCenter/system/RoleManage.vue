<template>
  <div class="role-manage search-table-wrapper">
    <div class="role-manage-search search-table-form">
      <el-form ref="form" :inline="true" size="small" :model="searchForm" label-width="80px">
        <div class="search-form-row">
          <el-form-item v-if="false" label="角色类型">
            <el-select v-model="searchForm.roleType" placeholder="请选择">
              <el-option label="所有" value=""></el-option>
              <el-option v-for="item in DICT['RoleTypeEnum']" :key="item.value" :label="item.desc" :value="item.code">
              </el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="角色名称">
            <el-input v-model="searchForm.roleName" placeholder="请输入角色名称"></el-input>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="executeSearch()">
              <i class="el-icon-search"></i>
              查询
            </el-button>
            <el-button v-authorize="'sys:role:add'" @click="openDialog('add')">
              <i class="el-icon-circle-plus-outline"></i>
              添加角色
            </el-button>
          </el-form-item>
        </div>
      </el-form>
    </div>

    <div class="role-manage-table search-table-data">
      <el-table :data="tableData" border style="width: 100%">
        <el-table-column prop="roleName" label="角色名称"></el-table-column>
        <el-table-column v-if="false" prop="roleType" label="角色类型">
          <template slot-scope="scope">
            {{ String(scope.row.roleType) | DictFilter('RoleTypeEnum') }}
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="描述"></el-table-column>
        <el-table-column label="操作">
          <template slot-scope="scope">
            <el-button v-authorize="'sys:role:assignAuth'" type="text" @click="openDialog('assign', scope.row.id)"
              >分配权限</el-button
            >
            <el-button v-authorize="'sys:role:edit'" type="text" @click="openDialog('edit', scope.row)">编辑</el-button>
            <el-button v-authorize="'sys:role:delete'" type="text" @click="deleteRole(scope.row.id)">删除</el-button>
          </template>
        </el-table-column>
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

    <el-dialog
      class="search-table-dialog"
      title="角色"
      width="400px"
      :visible.sync="dialogFormVisible"
      :close-on-click-modal="false"
    >
      <el-form ref="dialogForm" :rules="dialogRules" :model="dialogForm" :inline="true" size="medium" label-width="120px">
        <el-form-item label="角色名称" prop="roleName">
          <el-input v-model="dialogForm.roleName" autocomplete="off" :disabled="dialogType === 'show'"></el-input>
        </el-form-item>
        <el-form-item label="角色描述" prop="remark">
          <el-input v-model="dialogForm.remark" :disabled="dialogType === 'show'" autocomplete="off"></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="closeDialog()">取 消</el-button>
        <el-button v-if="dialogType !== 'show'" :loading="loading" type="primary" @click="submitDialogForm()">确 定</el-button>
      </div>
    </el-dialog>
    <PermissionTree ref="treeDialog" :show.sync="showTreeDialog"></PermissionTree>
  </div>
</template>

<script lang="js">
import {
  listRole,
  addRole,
  editRole,
  deleteRole,
} from '@/api/userCenter/system';
import { formCheckChinese } from '@/tools/validator'
import dict from '@/mixins/dict';
import PermissionTree from '@/components/userCenter/PermissionTree';

export default {
  name: 'RoleManage',
  components: {
    PermissionTree
  },
  mixins: [dict],
  data() {
    return {
      loading: false,
      searchForm: {
        roleType: '',
        roleName: '',
      },
      pageData: {
        currentPage: 1,
        pageSize: 20,
        totalRecord: 0,
      },
      tableData: [],
      // Tree 弹窗
      showTreeDialog: false,
      dialogType: '',
      dialogFormVisible: false,
      editId: '',
      dialogForm: {
        roleName: '',
        remark: '',
      },
      dialogRules: {
        roleName: [
          { required: true, message: '请输入角色名称', trigger: 'blur' },
          { max: 30, message: '角色名称需要在30字符内', trigger: 'blur' },
          { validator: formCheckChinese, trigger: 'blur' },
        ],
        remark: [
          { required: true, message: '请输入备注', trigger: 'blur' },
          { max: 50, message: '备注需要在50字符内', trigger: 'blur' },
        ],
      },
    }
  },
  mounted() {
    this.executeSearch();
  },
  methods: {
    executeSearch() {
      const params = {
        ...this.searchForm,
      }
      params.currentPage = this.pageData.currentPage
      params.pageSize = this.pageData.pageSize
      listRole(params).then((res) => {
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
    openDialog(type, id = '') {
      if (type === 'assign') {
        this.showTreeDialog = true;
        this.editId = id;
        this.$refs['treeDialog'].initAndShow(id);
      } else if (type === 'add') {
        this.dialogType = type;
        this.dialogFormVisible = true;
      } else if (type === 'edit') {
        this.dialogType = type;
        this.dialogFormVisible = true;
        this.editId = id.id;
        this.dialogForm.roleName = id.roleName;
        this.dialogForm.remark = id.remark;
      }
    },
    closeDialog() {
      this.dialogFormVisible = false;
      this.dialogForm.roleName = ''
      this.dialogForm.remark = ''
      this.$nextTick(() => {
        this.$refs['dialogForm'].clearValidate()
      })
    },
    submitDialogForm() {
      this.$refs['dialogForm'].validate((valid) => {
        if (valid) {
          if (this.dialogType === 'add') {
            this.addRole();
          } else if (this.dialogType === 'edit') {
            this.editRole();
          }
        } else {
          this.$message.warning('请正确填写表单');
        }
      })
    },
    addRole() {
      const params = { ...this.dialogForm };
      this.loading = true
      addRole(params).then((res) => {
        if (res.code === 200) {
          this.$message.success('添加成功');
          this.closeDialog();
          this.executeSearch();
        } else {
          throw Error(res.msg);
        }
      }).finally(() => {
        this.loading = false
      })
    },
    editRole() {
      const { roleName, remark } = this.dialogForm;
      const params = {
        id: this.editId,
        roleName, remark,
      }
      this.loading = true
      editRole(params).then((res) => {
        if (res.code === 200) {
          this.$message.success('编辑成功');
          this.closeDialog();
          this.executeSearch();
        } else {
          this.$message.error(res.msg);
        }
      }).finally(() => {
        this.loading = false
      });
    },
    deleteRole(id) {
      this.$confirm('此操作将删除该角色, 是否继续?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        deleteRole(id).then((res) => {
          if (res.code === 200) {
            this.$message.success('删除成功');
            this.executeSearch();
          } else {
            this.$message.error(res.msg);
          }
        })
      }).catch(() => {
        console.info('已取消删除');
      });
    },
  },
}
</script>

<style lang="stylus" scoped>
@import '../../../assets/style/seach-table.styl'
</style>
