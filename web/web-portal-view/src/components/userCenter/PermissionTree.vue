<template>
  <el-dialog title="分配权限" :visible.sync="showDialog" :close-on-click-modal="false" @close="closeForm">
    <el-input placeholder="输入关键字进行过滤" v-model="filterText" />
    <el-tree
      ref="assignTree"
      :data="assignTreeData"
      border
      row-key="id"
      :tree-props="{ children: 'children', hasChildren: 'hasChildren' }"
      :default-expand-all="true"
      :filter-node-method="filterNode"
      v-slot="{ data: menu }"
    >
      <div class="treeNode">
        <el-checkbox
          class="menuNode"
          @change="checked => selectChange(menu, checked)"
          v-model="assignedInfo[menu.id]"
          :key="menu.id"
          :label="menu.id"
        >
          {{ menu.name }}
        </el-checkbox>

        <div class="actionNode" v-if="menu.actions">
          <el-checkbox
            @change="checked => selectChange(action, checked)"
            v-model="assignedInfo[action.id]"
            v-for="action in menu.actions"
            :key="action.id"
            :label="action.id"
            >{{ action.name }}
          </el-checkbox>
        </div>
      </div>
    </el-tree>

    <div slot="footer" class="dialog-footer">
      <el-button @click="closeForm">取 消</el-button>
      <el-button type="primary" :loading="loading" @click="doAssignAuth">确 定</el-button>
    </div>
  </el-dialog>
</template>

<script>
import { listAllAuth, listRoleAuth, assignRoleAuth } from '@/api/userCenter/system';

const buildAssignTree = function(pAuth, pidGroup, subMenuArr, subActionArr) {
  let pid = (pAuth && pAuth.id) || 0;
  if (!pidGroup[pid]) {
    return;
  }
  pidGroup[pid].forEach(f => {
    f.children = [];
    f.actions = [];
    if (f.authType === 1) {
      subMenuArr.push(f);
      if (pidGroup[f.id]) {
        buildAssignTree(f, pidGroup, f.children, f.actions);
      }
    } else {
      subActionArr.push(f);
    }
    f.parent = pAuth;
  });
};

export default {
  name: 'PermissionTree',
  props: {
    show: {
      type: Boolean,
      default: () => false
    }
  },
  data() {
    return {
      loading: false,
      filterText: '',
      auths: [],
      assignedInfo: [],
      roleId: void 0,
      showDialog: this.show
    };
  },
  computed: {
    assignTreeData() {
      let pidGroup = [];
      this.auths.forEach(f => {
        if (!pidGroup[f.parentId]) {
          pidGroup[f.parentId] = [f];
        } else {
          pidGroup[f.parentId].push(f);
        }
      });
      let subMenuArr = [];
      buildAssignTree(null, pidGroup, subMenuArr, []);
      return subMenuArr;
    }
  },
  watch: {
    filterText(val) {
      this.$refs.assignTree.filter(val);
    },
    show(newValue) {
      this.showDialog = newValue;
    }
  },
  methods: {
    initAndShow(roleId) {
      this.roleId = roleId;
      Promise.all([listAllAuth(), listRoleAuth(roleId)]).then(
        ([{ data: allPortalAuth }, { data: assignedAuths }]) => {
          this.auths = allPortalAuth;
          assignedAuths.forEach(f => (this.assignedInfo[f.id] = true));
        }
      );
    },

    filterNode(value, menu) {
      if (!value) {
        return true;
      }
      return menu.name.indexOf(value) !== -1 || menu.actions.some(p => p.name.indexOf(value) !== -1);
    },

    recursiveUp(currentAuth, checked) {
      if (!currentAuth) {
        return;
      }
      if (checked) {
        this.assignedInfo[currentAuth.id] = true;
        this.recursiveUp(currentAuth.parent, checked);
      }
    },
    recursiveDown(currentAuth, checked) {
      currentAuth.children.forEach(c => {
        this.assignedInfo[c.id] = checked;
        this.recursiveDown(c, checked);
      });
      currentAuth.actions.forEach(c => {
        this.assignedInfo[c.id] = checked;
      });
    },

    selectChange(func, checked) {
      this.recursiveUp(func, checked);
      this.recursiveDown(func, checked);
    },

    doAssignAuth() {
      let assignedIds = [];
      this.assignedInfo.forEach((v, i) => {
        if (v === true) {
          assignedIds.push(i);
        }
      });
      this.loading = true
      assignRoleAuth(this.roleId, assignedIds).then(({ data }) => {
        this.$message.success(data);
        // this.show = false;
        this.$emit('update:show', false);
      }).finally(() => {
        this.loading = false
      });
    },

    closeForm() {
      this.auths = [];
      this.roleId = void 0;
      this.assignedInfo = [];
      this.$emit('update:show', false);
    }
  }
};
</script>

<style lang="stylus" scoped>
/deep/ .el-dialog {
  width: 800px;

  .el-input {
    margin-bottom: 10px;
  }

  .el-dialog__body {
    max-height: 600px;
    overflow-y: auto;

    .el-tree {
      border-bottom: 1px solid #EBEEF5;

      .el-tree-node {
        .el-tree-node__content {
          height: auto;
          align-items: flex-start;
          border: 1px solid #EBEEF5;
          border-bottom: 0px;

          .el-tree-node__expand-icon {
            padding: 6px;
            padding-left: 0;
            margin-top: 2px;
          }

          .el-checkbox {
            margin-bottom: 5px;
            margin-top: 5px;
          }

          .treeNode {
            display: flex;

            .menuNode {
              display: inline-block;
              width: 150px;
              margin-right: 50px;
              white-space: normal;
              margin-top: 5px;
            }

            .actionNode {
              width: 500px;
              white-space: normal;

              .el-checkbox {
                margin-right: 15px;
                margin-bottom: 5px;
                margin-top: 5px;
              }
            }

            .el-checkbox__label {
              font-size: small;
            }
          }
        }
      }
    }
  }
}
</style>
