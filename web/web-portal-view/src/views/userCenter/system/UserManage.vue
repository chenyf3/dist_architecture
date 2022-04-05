<template>
  <div class="user-manage search-table-wrapper">
    <div class="search-table-form">
      <el-form ref="form" :inline="true" size="small" :model="searchForm" label-width="80px">
        <div class="search-form-row">
          <el-form-item label="登录名">
            <el-input v-model.trim="searchForm.loginName" placeholder="请输入登录名"></el-input>
          </el-form-item>
          <el-form-item label="姓名">
            <el-input v-model.trim="searchForm.realName" placeholder="请输入姓名"></el-input>
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model.trim="searchForm.status" placeholder="请选择状态">
              <el-option label="所有" value=""></el-option>
              <el-option
                  v-for="item in DICT['UserStatusEnum']"
                  :key="item.value"
                  :label="item.desc"
                  :value="item.code"
              >
              </el-option>
            </el-select>
          </el-form-item>
        </div>
        <div class="search-form-row">
          <el-form-item>
            <el-button type="primary" @click="executeSearch()">
              <i class="el-icon-search"> 查询</i>
            </el-button>
            <el-button v-authorize="'sys:user:add'" @click="openDialog('add')">
              <i class="el-icon-circle-plus-outline"> 添加用户</i>
            </el-button>
          </el-form-item>
        </div>
      </el-form>
    </div>
    <div class="user-manage-table search-table-data">
      <el-table :data="tableData" border style="width: 100%">
        <el-table-column prop="loginName" label="登录名" width="180"></el-table-column>
        <el-table-column prop="realName" label="姓名" min-width="100"></el-table-column>
        <el-table-column prop="mobileNo" label="手机号" min-width="120"></el-table-column>
        <el-table-column prop="email" label="邮箱" min-width="180"></el-table-column>
        <el-table-column prop="type" label="用户类型">
          <template slot-scope="scope">
            {{ String(scope.row.type) | DictFilter('UserTypeEnum') }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态">
          <template slot-scope="scope">
            {{ String(scope.row.status) | DictFilter('UserStatusEnum') }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180">
          <template slot-scope="scope">
            <el-button type="text" @click="openDialog('show', scope.row.id)">查看</el-button>
            <el-button
                v-authorize="'sys:user:edit'"
                v-if="scope.row.type !== 1"
                type="text"
                @click="openDialog('edit', scope.row.id)"
            >编辑
            </el-button>
            <el-button
                v-authorize="'sys:user:assignRoles'"
                v-if="isCurrentUser !== scope.row.loginName && scope.row.type !== 1"
                type="text"
                @click="openDialog('assign', scope.row.id)"
            >分配角色
            </el-button>
            <el-button
                v-authorize="'sys:user:changeStatus'"
                v-if="isCurrentUser !== scope.row.loginName && scope.row.type !== 1"
                type="text"
                @click="changeUserStatus(scope.row.id, scope.row.status)"
            >{{ scope.row.status === 1 ? '冻结' : '激活' }}
            </el-button>
            <el-button
                v-authorize="'sys:user:delete'"
                v-if="isCurrentUser !== scope.row.loginName && scope.row.type !== 1"
                type="text"
                @click="deleteUser(scope.row.id)"
            >删除
            </el-button>
            <el-button
                v-authorize="'sys:user:resetpwd'"
                v-if="isCurrentUser !== scope.row.loginName && scope.row.type !== 1"
                type="text"
                @click="openDialog('pwd', scope.row.id)"
            >重置密码
            </el-button>
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
        :title="dialogType === 'assign' ? '分配角色' : '用户'"
        width="550px"
        :visible.sync="dialogFormVisible"
        :close-on-click-modal="false"
    >
      <el-form ref="dialogForm" :inline="false" :rules="dialogRules" :model="dialogForm" size="small" label-width="120px">
        <el-form-item label="登录名" prop="loginName">
          <el-input
              v-model.trim="dialogForm.loginName"
              autocomplete="off"
              placeholder="请输入登录名"
              :readonly="dialogType === 'show' || dialogType === 'assign'"
              :disabled="dialogType === 'edit'"
          ></el-input>
        </el-form-item>
        <el-form-item v-if="dialogType === 'add'" label="登录密码" prop="loginPwd">
          <el-input
              v-model.trim="dialogForm.loginPwd"
              :readonly="dialogType === 'show'"
              type="password"
              autocomplete="off"
              placeholder="请输入登录密码"
          ></el-input>
        </el-form-item>
        <el-form-item label="姓名" prop="realName">
          <el-input
              v-model.trim="dialogForm.realName"
              autocomplete="off"
              placeholder="请输入姓名"
              :readonly="dialogType === 'show' || dialogType === 'assign'"
          ></el-input>
        </el-form-item>
        <el-form-item v-if="dialogType === 'add' || dialogType === 'show'" label="手机号" prop="mobileNo">
          <el-input
              v-model.trim="dialogForm.mobileNo"
              :readonly="dialogType === 'show'"
              autocomplete="off"
              placeholder="请输入手机号"
          ></el-input>
        </el-form-item>
        <el-form-item v-if="dialogType !== 'assign'" label="邮 箱" prop="email">
          <el-input
              v-model.trim="dialogForm.email"
              :readonly="dialogType === 'show'"
              autocomplete="off"
              placeholder="请输入邮箱"
          ></el-input>
        </el-form-item>
        <el-form-item v-if="dialogType !== 'assign'" label="备注" prop="remark">
          <el-input
              v-model.trim="dialogForm.remark"
              :readonly="dialogType === 'show'"
              autocomplete="off"
              placeholder="请输入备注"
          ></el-input>
        </el-form-item>
        <el-form-item v-if="dialogType === 'assign'" label="角色分配" prop="roleIdsProp">
          <el-checkbox-group v-model.trim="dialogForm.roleIds" :disabled="dialogType === 'show'">
            <template v-for="(item, index) in allRolesList">
              <el-checkbox :key="index" :label="item.id">{{ item.roleName }}</el-checkbox>
            </template>
          </el-checkbox-group>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="closeDialog()">关 闭</el-button>
        <el-button v-if="dialogType !== 'show'" type="primary" :loading="loading" @click="submitDialogForm()">确 定</el-button>
      </div>
    </el-dialog>


    <el-dialog
        class="search-table-dialog"
        title="重置用户密码"
        width="400px"
        :visible.sync="pwdDialog"
        :close-on-click-modal="false"
    >
      <el-form ref="pwdForm" :rules="pwdRules" :model="pwdForm" label-width="120px">
        <el-form-item label="新密码" prop="newPwd">
          <el-input v-model.trim="pwdForm.newPwd" type="password" placeholder="请输入新密码"></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="pwdDialog = false">取 消</el-button>
        <el-button type="primary" @click="resetUserPwd()">确定修改</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script lang="js">
import {
  listUser,
  addUser,
  editUser,
  getUserById,
  deleteUser,
  changeUserStatus,
  resetUserPwd,
  listAllRoles,
  assignRoles,
} from '@/api/userCenter/system';
import {encryptParam} from '@/tools/jsencrypt';
import {
  formCheckPhone,
  formCheckPwd,
  formCheckEmail,
  formCheckChinese
} from '@/tools/validator';
import dict from '@/mixins/dict';

export default {
  name: 'UserManage',
  mixins: [dict],
  data() {
    return {
      loading: false,
      searchForm: {
        loginName: '',
        realName: '',
        status: '',
      },
      pageData: {
        currentPage: 1,
        pageSize: 20,
        totalRecord: 0,
      },
      tableData: [],
      // 弹窗
      allRolesList: [],
      dialogType: '',
      dialogFormVisible: false,
      editId: '',
      dialogForm: {
        loginName: '',
        loginPwd: '',
        realName: '',
        mobileNo: '',
        email: '',
        remark: '',
        roleIds: [],
        roleIdsProp: '',
      },
      dialogRules: {
        loginName: [
          {required: true, message: '请输入登录名', trigger: 'blur'},
          {max: 30, message: '登录名需要在30字符内', trigger: 'blur'}
        ],
        loginPwd: [
          {required: true, message: '请输入登录密码', trigger: 'blur'},
          {validator: formCheckPwd, trigger: 'blur'}
        ],
        realName: [
          {required: true, message: '请输入姓名', trigger: 'blur'},
          {max: 30, message: '姓名需要在30字符内', trigger: 'blur'},
          {validator: formCheckChinese, trigger: 'blur'},
        ],
        mobileNo: [
          {required: true, message: '请输入手机号', trigger: 'blur'},
          {validator: formCheckPhone, trigger: 'blur'},
        ],
        email: [
          {required: true, message: '请输入邮箱', trigger: 'blur'},
          {validator: formCheckEmail, trigger: 'blur'},
        ],
        remark: [
          {required: true, message: '请输入备注', trigger: 'blur'},
          {max: 50, message: '备注需要在50字符内', trigger: 'blur'},
        ],
      },
      pwdDialog: false,
      pwdForm: {
        newPwd: '',
      },
      pwdRules: {
        newPwd: [
          {required: true, message: '请输入密码', trigger: 'blur'},
          {validator: formCheckPwd, trigger: 'blur'}
        ]
      }
    }
  },
  computed: {
    isCurrentUser() {
      return this.$store.getters.getUserInfoByKey('loginName');
    }
  },
  mounted() {
    this.listAllRoles();
    this.executeSearch();
  },
  methods: {
    executeSearch() {
      let searchData = {...this.searchForm}
      searchData.currentPage = this.pageData.currentPage
      searchData.pageSize = this.pageData.pageSize
      listUser(searchData)
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
    listAllRoles() {
      listAllRoles().then(res => {
        if (res.code === 200) {
          this.allRolesList = res.data;
        }
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
      if (type === 'add') {
        this.dialogType = type;
        this.dialogFormVisible = true;
      } else if (type === 'edit') {
        this.dialogType = type;
        this.dialogFormVisible = true;
        this.editId = id;
        this.showUser(id);
      } else if (type === 'show') {
        this.dialogType = type;
        this.dialogFormVisible = true;
        this.showUser(id);
      } else if (type === 'pwd') {
        this.pwdDialog = true;
        this.pwdForm.userId = id;
      } else if (type === 'assign') {
        this.dialogType = type;
        this.editId = id;
        this.dialogFormVisible = true;
        this.showUser(id);
      }
    },
    closeDialog() {
      this.dialogFormVisible = false
      this.dialogType = '';
      for (let key in this.dialogForm) {
        this.dialogForm[key] = ''
      }
      this.dialogForm.roleIds = [];
      this.$nextTick(() => {
        this.$refs['dialogForm'].clearValidate();
      })
    },
    submitDialogForm() {
      this.$refs['dialogForm'].validate((valid) => {
        if (valid) {
          if (this.dialogType === 'add') {
            this.addUser();
          } else if (this.dialogType === 'edit') {
            this.editUser();
          } else if (this.dialogType === 'assign') {
            this.assignRoles();
          }
        } else {
          this.$message.warning('请正确填写表单');
        }
      })
    },
    addUser() {
      const params = {...this.dialogForm};
      params.loginPwd = encryptParam(params.loginPwd);
      this.loading = true
      addUser(params).then((res) => {
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
    showUser(id) {
      getUserById(id).then((res) => {
        if (res.code === 200) {
          this.dialogForm = Object.assign(this.dialogForm, res.data);
        } else {
          this.$message.error(res.msg);
        }
      })
    },
    editUser() {
      const {realName, email, remark, roleIds} = this.dialogForm;
      const params = {
        id: this.editId,
        realName, email, remark, roleIds
      }
      this.loading = true
      editUser(params).then((res) => {
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
    deleteUser(id) {
      this.$confirm('用户删除后将不可恢复, 是否继续?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        deleteUser(id).then((res) => {
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
    resetUserPwd() {
      resetUserPwd({
        userId: this.pwdForm.userId,
        newPwd: encryptParam(this.pwdForm.newPwd)
      }).then((res) => {
        if (res.code === 200) {
          this.pwdForm.newPwd = '';
          this.pwdDialog = false;
          this.$message.success('重置成功');
          this.executeSearch();
        } else {
          this.$message.error(res.msg);
        }
      })
    },
    changeUserStatus(id, status) {
      const msg = status === 1 ? '冻结' : status === -1 ? '激活' : '操作'
      this.$confirm(`确认${msg}此用户吗?`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() =>{
        changeUserStatus(id).then((res) => {
          if (res.code === 200) {
            this.$message.success('更改用户状态成功');
            this.executeSearch();
          } else {
            this.$message.error(res.msg);
          }
        })
      })
    },
    assignRoles() {
      const params = {
        userId: this.editId,
        roleIds: this.dialogForm.roleIds
      };
      assignRoles(params).then((res) => {
        if (res.code === 200) {
          this.$message.success('分配完成');
          this.closeDialog();
          this.executeSearch();
        } else {
          this.$message.error(res.msg);
        }
      })
    },
  },
}
</script>

<style lang="stylus" scoped></style>
