<template>
    <div v-loading="loading" class="app-container">
        <el-form :inline="true" class="" size="small">
            <el-form-item label="业务分组">
                <el-select v-model="searchForm.groupKey" clearable>
                    <el-option v-for="item in $dictArray('EmailGroupKeyEnum')"
                               :key="item.name" :label="item.desc" :value="item.name"/>
                </el-select>
            </el-form-item>
            <el-form-item label="发件人">
                <el-input v-model="searchForm.sender" placeholder="精确搜索"></el-input>
            </el-form-item>
            <el-form-item label="描述">
                <el-input v-model="searchForm.remark" placeholder="模糊搜索"></el-input>
            </el-form-item>
            <el-form-item>
                <el-button @click="refreshList" type="primary"><i class="el-icon-search"/>查询</el-button>
                <el-button v-authorize="'baseConfig:mailGroup:manage'" @click="addMailGroupPage" type="success" plain><i class="el-icon-plus"/>添加
                </el-button>
            </el-form-item>
        </el-form>

        <div class="">
            <el-table :data="pageResult.data" style="width: 100%" :row-style="{'max-height': '100px'}" border>
                <el-table-column label="序号" type="index" width="50px"/>
                <el-table-column label="创建时间" prop="createTime"/>
                <el-table-column label="业务分组">
                    <template slot-scope="scope">
                        {{ groupKeyName(scope.row.groupKey) }}
                    </template>
                </el-table-column>
                <el-table-column label="描述" prop="remark"/>
                <el-table-column label="发件人" prop="sender"/>
                <el-table-column label="收件人" prop="receivers"/>
                <el-table-column label="抄送人" prop="cc"/>
                <el-table-column v-slot="{row}" label="操作">
                    <el-button v-authorize="'baseConfig:mailGroup:list'" type="text" size="mini"
                               @click="viewMailGroupPage(row)">查看
                    </el-button>
                    <el-button v-authorize="'baseConfig:mailGroup:manage'" type="text" size="mini"
                               @click="editMailGroupPage(row)">编辑
                    </el-button>
                    <el-button v-authorize="'baseConfig:mailGroup:manage'" type="danger" size="mini"
                               @click="deleteMailGroup(row.id)">删除
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

        <mailGroup-form ref="mailGroupForm" @success="refreshList"/>
    </div>
</template>

<script>
import Pagination from '@/components/Pagination';
import MailGroupForm from './MailGroupForm';
import {listMailGroup, deleteMailGroup} from '@/api/baseConfig/mailGroup';

export default {
    name: 'MailReceiveList',
    components: {
        Pagination,
        MailGroupForm
    },
    data() {
        return {
            loading: false,
            searchForm: {
                groupKey: '',
                sender: '',
                remark: '',
                currentPage: 1,
                pageSize: 20,
            },
            pageResult: {}
        };
    },
    mounted() {
        this.refreshList();
    },
    methods: {
        refreshList() {
            this.loading = true;
            listMailGroup(this.searchForm)
                .then(({data}) => {
                    this.pageResult = data;
                }).finally(() => {
                    this.loading = false;
                });
        },
        addMailGroupPage() {
            this.$refs.mailGroupForm.initAndShow('ADD');
        },
        viewMailGroupPage(row) {
            this.$refs.mailGroupForm.initAndShow('VIEW', {...row});
        },
        editMailGroupPage(row) {
            this.$refs.mailGroupForm.initAndShow('EDIT', {...row});
        },
        deleteMailGroup(id) {
            this.$confirm('确定删除这条记录吗?')
                .then(() => deleteMailGroup(id))
                .then(({data}) => {
                    this.$message.success(data);
                    this.refreshList();
                });
        },
        groupKeyName(groupKey){
            const dictArr = this.$dictArray('EmailGroupKeyEnum');
            for (const item of dictArr) {
                if (item.name === String(groupKey)) {
                    return item.desc;
                }
            }
            return '';
        },
    }
};
</script>
