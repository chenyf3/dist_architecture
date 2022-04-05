<template>
    <el-dialog
        title="分配权限"
        :visible.sync="showDialog"
        :close-on-click-modal="false"
        @close="closeForm"
        append-to-body
        width="800px"
    >
        <el-input placeholder="输入关键字进行过滤" v-model="filterText" />

        <el-tree
            ref="assignTree"
            :data="assignTreeData"
            :props="{ children: 'children', hasChildren: 'hasChildren' }"
            :default-expand-all="true"
            :filter-node-method="filterNode"
            v-slot="{data: menu}"
        >
            <div class="treeNode">
                <el-checkbox
                    class="menuNode"
                    :key="menu.id"
                    :label="menu.id"
                    v-model="assignedInfo[menu.id]"
                    @change="checked => selectChange(menu, checked)"
                >
                    <span class="authName">{{ menu.name }}</span>
                </el-checkbox>

                <div class="actionNode" v-if="menu.actions">
                    <el-checkbox
                        v-for="action in menu.actions"
                        :key="action.id"
                        :label="action.id"
                        v-model="assignedInfo[action.id]"
                        @change="checked => selectChange(action, checked)"
                    >
                        <span class="authName">{{ action.name }}</span>
                    </el-checkbox>
                </div>
            </div>
        </el-tree>

        <div slot="footer" class="dialog-footer">
            <el-button @click="closeForm">取 消</el-button>
            <el-button type="primary" @click="doAssignAuth">确 定</el-button>
        </div>
    </el-dialog>
</template>

<script>
import {listAllAuth} from '@/api/portal/auth';
import {listRoleAuth, assignAdminRoleAuth} from '@/api/portal/role';

const buildAssignTree = function (pAuth, pidGroup, subMenuArr, subActionArr) {
    const pid = (pAuth && pAuth.id) || 0
    if (!pidGroup[pid]) {
        return
    }
    pidGroup[pid].forEach(node => {
        node.children = []
        node.actions = []
        if (node.authType === 1) {
            subMenuArr.push(node)
            if (pidGroup[node.id]) {
                buildAssignTree(node, pidGroup, node.children, node.actions)
            }
        } else {
            subActionArr.push(node)
        }
        node.parent = pAuth
    })
}

export default {
    name: 'AssignAuth',
    data() {
        return {
            filterText: '',
            auths: [],
            assignedInfo: [],
            roleId: undefined,
            showDialog: false
        }
    },

    computed: {
        assignTreeData() {
            const pidGroup = []
            this.auths.forEach(node => {
                if (!pidGroup[node.parentId]) {
                    pidGroup[node.parentId] = [node]
                } else {
                    pidGroup[node.parentId].push(node)
                }
            })
            const subMenuArr = []
            buildAssignTree(null, pidGroup, subMenuArr, [])
            return subMenuArr
        }
    },
    watch: {
        filterText(val) {
            this.$refs.assignTree.filter(val)
        }
    },

    methods: {
        initAndShow(roleId) {
            this.roleId = roleId
            const p1 = listAllAuth()
            const p2 = listRoleAuth(`${this.roleId}`)
            Promise.all([p1, p2])
                .then(([{data: allAuths}, {data: assignedAuths}]) => {
                    this.auths = allAuths
                    // eslint-disable-next-line no-return-assign
                    assignedAuths.forEach(f => this.assignedInfo[f.id] = true)
                    this.showDialog = true
                })
        },

        filterNode(value, menu) {
            if (!value) {
                return true
            }
            return menu.name.indexOf(value) !== -1 || menu.actions.some(p => p.name.indexOf(value) !== -1)
        },
        recursiveUp(currentAuth, checked) {
            if (!currentAuth) {
                return
            }
            if (checked) {
                this.assignedInfo[currentAuth.id] = true
                this.recursiveUp(currentAuth.parent, checked)
            }
        },
        recursiveDown(currentAuth, checked) {
            currentAuth.children.forEach(c => {
                this.assignedInfo[c.id] = checked
                this.recursiveDown(c, checked)
            })
            currentAuth.actions.forEach(c => {
                this.assignedInfo[c.id] = checked
            })
        },
        selectChange(func, checked) {
            this.recursiveUp(func, checked)
            this.recursiveDown(func, checked)
        },
        doAssignAuth() {
            const assignedIds = []
            this.assignedInfo.forEach((v, i) => {
                if (v === true) {
                    assignedIds.push(i)
                }
            })
            assignAdminRoleAuth(`${this.roleId}`, assignedIds)
                .then((resp) => {
                    if(resp.code == 200){
                        this.$message.success(resp.data)
                        this.$emit('success')
                        this.showDialog = false
                    }
                })
        },
        closeForm() {
            this.auths = []
            this.roleId = undefined
            this.assignedInfo = []
            this.showDialog = false
        }
    }
}
</script>

<style lang="scss" scoped>
::v-deep .el-dialog {
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
                        padding: 6px 6px 6px 0;
                        margin-top: 2px;
                    }

                    .el-checkbox {
                        margin-top: 5px;
                        margin-bottom: 5px;
                    }

                    .treeNode {
                        display: flex;

                        .menuNode {
                            display: inline-block;
                            width: 150px;
                            margin-right: 50px;
                            white-space: normal;
                            margin-top: 5px;

                            .el-checkbox__label {
                                font-size: 13px;
                            }
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

                        .authName {
                            font-size: small;
                        }
                    }
                }
            }
        }
    }
}
</style>
