<template>
  <div class="search-table-wrapper">
    <div class="search-table-form">
      <el-form ref="form" :inline="false" size="small" :model="form" :rules="rules" label-width="120px">
        <div>
          <el-form-item label="语音合成平台" prop="platform">
            <el-select v-model.trim="form.platform" placeholder="请选择语音合成平台">
              <el-option :key="1" :label="'阿里云'" :value="1"></el-option>
              <el-option :key="2" :label="'腾讯云'" :value="2"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="转换文本" prop="text">
            <el-input v-model="form.text" placeholder="请输入要转换的文本内容"></el-input>
          </el-form-item>
          <el-form-item label="播放音量" prop="volume">
            <el-input v-model="form.volume" placeholder="音量调节"></el-input>
          </el-form-item>
          <el-form-item label="播放语速" prop="speechRate">
            <el-input v-model="form.speechRate" placeholder="语速调节"></el-input>
          </el-form-item>
          <el-form-item label="播放语调" prop="pitchRate" v-if="form.platform === 1">
            <el-input v-model="form.pitchRate" placeholder="语调调节"></el-input>
          </el-form-item>
        </div>
        <div class="search-form-date-row">
          <el-form-item>
            <el-button type="primary" :loading="loading" @click="play()">
              <i class="el-icon-video-play">播放</i>
            </el-button>
            <el-button type="primary" :loading="loading" @click="play5Times()">
              <i class="el-icon-video-play">播放5次</i> <!-- 用以模拟从api接口拉取多条记录进行播报 -->
            </el-button>
          </el-form-item>
        </div>
      </el-form>
    </div>
  </div>
</template>

<script lang="js">
import {Howl} from 'howler';
import {transferAudio} from '@/api/examples/audio'

export default {
  name: 'AudioPlay',
  data() {
    return {
      loading: false,
      form: {
        text: '',
        volume: 80,
        pitchRate: 0,
        speechRate: 0,
      },
      rules: {
        platform: [
          { required: true, message: '请选择进行语音合成的平台', trigger: 'blur' },
        ],
        text: [
          { required: true, message: '请输入播放内容', trigger: 'blur' },
          { max: 300, message: '播放内容长度需要在30字符内', trigger: 'blur' },
        ],
      },
    }
  },
  methods: {
    play() {
      this.$refs['form'].validate((valid) => {
        if (valid) {
          this.loading = true
          transferAudio({...this.form}).then((res) => {
            const contentType = 'audio/mp3';
            if (res.code === 200) {
              let soundList = []
              soundList.push(`data:${contentType};base64,${res.data}`)
              playAudio(0, soundList)
            }
          }).finally(() => {
            this.loading = false
          })
        } else {
          this.$message.warning('请正确填写表单');
        }
      })
    },
    play5Times(){ //用以模拟从后端api接口拉取多条记录进行播报
      this.$refs['form'].validate((valid) => {
        if (valid) {
          this.loading = true
          transferAudio({...this.form}).then((res) => {
            const contentType = 'audio/mp3';
            if (res.code === 200) {
              let soundList = []
              for (let i=1; i<=5; i++) {
                soundList.push(`data:${contentType};base64,${res.data}`)
              }
              playAudio(0, soundList)
            }
          }).finally(() => {
            this.loading = false
          })
        } else {
          this.$message.warning('请正确填写表单');
        }
      })
    },
  },
}

function playAudio(i, list){
  let sound = new Howl({
    src: [list[i]],
    preload: true,
    onend: function () {
      let next = i + 1
      if (next < list.length) {
        playAudio(next, list)
      }
    }
  })
  sound.play();
  sound = null
}
</script>

<style lang="stylus" scoped>

</style>