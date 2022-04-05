<template>
  <div class="search-table-wrapper">
    <div class="search-table-form">
      <el-upload
          class="file-upload"
          v-authorize="'examples:file:upload'"
          ref="upload"
          :action="host + '/examples/file/uploadImage'"
          :headers="headers"
          :file-list="fileList"
          :on-change="handleChange"
          :on-remove="handleRemove"
          :on-exceed="handleExceed"
          :before-upload="beforeUpload"
          :on-success="onSuccess"
          :on-error="onError"
          :auto-upload="false"
          :limit="limitSize"
          accept="image/png,image/jpeg"
          name="files"
          list-type="picture">
        <el-button slot="trigger" size="small" type="primary" :disabled="disablePick">选取文件</el-button>
        <div slot="tip" class="el-upload__tip">只能上传jpg/png文件，且不超过4M</div> <!-- 本页面只演示图片文件的处理，其他文件的处理其实都差不多的 -->
        <el-button size="small" type="success" @click="submitUpload" :loading="loading">上 传</el-button>
        <el-button size="small" type="primary" @click="executeSearch" :loading="loading"><i class="el-icon-search">刷 新</i></el-button>
      </el-upload>
    </div>

    <div class="search-table-data">
      <el-table :data="tableData" border style="width: 100%">
        <el-table-column label="序号" type="index" width="50"> </el-table-column>
        <el-table-column prop="createTime" label="创建时间" sortable>
          <template slot-scope="scope">
            {{ scope.row.createTime | timeFilter }}
          </template>
        </el-table-column>
        <el-table-column prop="creator" label="创建人"></el-table-column>
        <el-table-column prop="type" label="图片">
          <template slot-scope="scope">
            <img :src="viewUrl + '&picName=' + scope.row.imageName" alt="" style="width: 200px; height: 100px; cursor: pointer"
                 @click="zoomPicView(scope.row.imageName)" >
          </template>
        </el-table-column>
        <el-table-column label="操作">
          <template slot-scope="scope">
            <el-button v-authorize="'examples:file:download'" size="small" type="text" @click="download(scope.row.imageName)">下载</el-button>
            <el-button v-authorize="'examples:file:delete'" size="small" type="text" @click="deleteImageFile(scope.row.imageName)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 图片放大预览 -->
    <el-dialog :visible.sync="zoomDialog" :modal="false" width="70%">
      <img :src="zoomPicUrl" alt="" width="100%"/>
    </el-dialog>
  </div>
</template>

<script lang="js">
import { getTokenHeader,getToken } from '@/tools/token';
import { getImageRecord,deleteImage } from '@/api/examples/file';
import {FILE_DOWNLOAD_URL, PIC_FILE_ENC_GET_URL} from '@/api/public/constants';

export default {
  name: 'FileUpAndDown',
  data() {
    return {
      host: process.env.VUE_APP_BASE_API,
      viewUrl: PIC_FILE_ENC_GET_URL + '?token=' + getToken(),
      downloadUrl: FILE_DOWNLOAD_URL + '?token=' + getToken(),
      headers: {},
      limitSize: 3,
      disablePick: false,
      loading: false,
      fileList: [],
      tableData: [],

      zoomDialog: false,
      zoomPicUrl: '',
    }
  },
  mounted() {
    this.headers = getTokenHeader()
    this.executeSearch()
  },
  methods: {
    executeSearch() {
      getImageRecord().then((res) => {
        if (res.code === 200) {
          this.tableData = res.data
        }
      })
    },
    handleChange(file, fileList){
      if(fileList.length >= this.limitSize) {
        this.disablePick = true;//超出文件个数限制时，'选取文件' 按钮变为不可用
      }
    },
    handleRemove(file, fileList){
      if(fileList.length < this.limitSize) {
        this.disablePick = false;//没达到文件个数限制时，'选取文件' 按钮变为可用
      }
    },
    handleExceed(files, fileList){
      this.$message.info(`上传文件数量不能超过${this.limitSize}个`);
    },
    beforeUpload(file){
      const isGt4M = file.size / 1024 / 1024 > 4;
      if(isGt4M) {
        this.$message.error('上传图片大小不能超过4M');
        return false//返回false，阻止文件上传
      }
    },
    submitUpload(){
      this.$refs.upload.submit()//提交表单
    },
    onSuccess(response, file, fileList){ //文件上传成功时的回调函数
      this.$refs.upload.clearFiles()//清空已上传的文件
      this.disablePick = false
      this.executeSearch()
    },
    onError(err, file, fileList){//文件上传失败时的回调函数
      this.$message.info('文件上传失败，请重新上传')
    },
    zoomPicView(imageName){
      this.zoomPicUrl = this.viewUrl + '&picName=' + imageName
      this.zoomDialog = true
    },
    download(fileName){
      let a = document.createElement('a');
      let event = new MouseEvent('click');
      a.href = this.downloadUrl + '&fileNameEnc=' + fileName;
      a.dispatchEvent(event);
    },
    deleteImageFile(fileName){
      this.$confirm('是否删除此文件?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        deleteImage(fileName).then((resp) => {
          if (resp.code === 200) {
            this.$message.success(resp.data)
            this.executeSearch()
          }else{
            this.$message.error(resp.msg)
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

</style>
