<template>
  <el-date-picker
    v-model="selectedDate"
    type="date"
    placeholder="选择日期"
    value-format="yyyy-MM-dd"
    :picker-options="pickerOptions"
    :clearable="false"
    @change="dateChange"
  >
  </el-date-picker>
</template>

<script lang="js">
import dayjs from 'dayjs';

export default {
  name: 'date-picker-single',
  props: {
    canChooseFurtrue: {
      type: Boolean,
      default: () => false
    },
  },
  data() {
    return {
      selectedDate: dayjs().format('YYYY-MM-DD'),
    }
  },
  computed: {
    pickerOptions() {
      return {
        disabledDate: (time) => {
          if (this.canChooseFurtrue) {
            return time.getTime() < Date.now();
          } else {
            return time.getTime() > Date.now();
          }
        }
      }
    }
  },
  mounted() {
    if (this.canChooseFurtrue) {
      this.selectedDate = dayjs().add(1, 'day').format('YYYY-MM-DD');
    }
    this.dateChange();
  },
  methods: {
    dateChange() {
      this.$emit('update:selectedDate', this.selectedDate)
    },
    resetTime() {
      this.selectedDate = dayjs().format('YYYY-MM-DD');
      this.dateChange();
    },
  },
}
</script>

<style lang="stylus" scoped>
.el-date-editor.el-input, .el-date-editor.el-input__inner
  width: 184px;
</style>
