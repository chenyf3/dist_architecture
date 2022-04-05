<template>
  <el-form-item :label="labelText" :class="[selectShow ? 'date-sm-picker' : '', 'date-picker-group', 'ele-form-item']">
    <el-col :span="11">
      <el-form-item prop="beginDate">
        <el-date-picker
          :clearable="isClearable"
          v-model="beginDate"
          :type="selectType"
          :value-format="timeFormat || 'yyyy-MM-dd HH:mm:ss'"
          :format="timeFormat || 'yyyy-MM-dd HH:mm:ss'"
          :picker-options="startTimeOption"
          placeholder="请输入开始时间"
          @change="dateChange"
          :default-time="startTime"
        >
        </el-date-picker>
      </el-form-item>
    </el-col>
    <el-col class="line" :span="2">-</el-col>
    <el-col :span="11">
      <el-form-item prop="endDate">
        <el-date-picker
          :clearable="isClearable"
          v-model="endDate"
          :type="selectType"
          :value-format="timeFormat || 'yyyy-MM-dd HH:mm:ss'"
          :format="timeFormat || 'yyyy-MM-dd HH:mm:ss'"
          :picker-options="endTimeOption"
          :default-time="endTime"
          placeholder="请输入结束时间"
          @change="dateChange"
        >
        </el-date-picker>
      </el-form-item>
    </el-col>
    <span class="btn-text" v-if="selectShow !== 'no'">
      <el-button
        v-for="(item, index) in options"
        type="text"
        @click="choiceDay(item)"
        :key="index"
        :class="{ active: activeItem === item }"
      >
        {{ getName(item) }}
      </el-button>
    </span>
  </el-form-item>
</template>

<script>
const dateMap = {
  today: 1,
  yesterday: 2,
  week: 7,
  fifteen: 15,
  month: 30,
  beforeFifteen: 15,
  beforeMonth: 30
};
const dateNameMap = {
  today: '今天',
  yesterday: '昨天',
  week: '近7天',
  fifteen: '近15天内',
  month: '近30天',
  beforeFifteen: '15天之前',
  beforeMonth: '30天前'
};
export default {
  name: 'date-picker',
  /*
    dateOptions（Array） : 后面展示的快捷时间
    selectShow（String）: 是否展示后面的快捷时间        不展示-no
    beginTime （String）: 是否展示默认时间              不展示-none
    labelText (String) : 文字展示内容
    defaultActiveItem（String）: 默认高亮按钮
    timeFormat （String）： 时间格式
    timeSection (String) : 默认展示时间区间
    canChoiceUsed (Boolean) : 是否可以选择以前的时间
    canChooseFurtrue (Boolean)： 展示未来时间
  */
  props: {
    dateOptions: {
      type: Array
    },
    selectShow: {
      type: String
    },
    beginTime: {
      type: String
    },
    labelText: {
      type: String
    },
    defaultActiveItem: {
      type: String
    },
    timeFormat: {
      type: String
    },
    timeSection: {
      type: String
    },
    couldClear: {
      type: Boolean
    },
    canChoiceUsed: {
      type: Boolean,
      defalut: () => false
    },
    canChooseFurtrue: {
      type: Boolean,
      default: () => false
    },
    selectType: {
      type: String,
      default: 'datetime'
    }
  },
  data() {
    return {
      beginDate: '',
      endDate: '',
      activeItem: 'today',
      within15Days: '',
      defaultOptions: ['today', 'yesterday', 'week', 'month'],
      defaultDate: '2017-12-14 00:00:00'
    };
  },
  computed: {
    options() {
      return this.dateOptions && this.dateOptions.length !== 0 ? this.dateOptions : this.defaultOptions;
    },
    isClearable() {
      return this.couldClear || false;
    },
    startTime() {
      return '00:00:00';
    },
    endTime() {
      return '23:59:59';
    },
    startTimeOption() {
      if (this.selectType !== 'month') {
        return {
          disabledDate: time => {
            let dateTimestamp = time.getTime(); // picker中的时间戳
            let nowTimestamp = Date.now(); // 当前时间戳

            if (this.activeItem.indexOf('before') !== -1) {
              if (this.endDate != '') {
                return (
                  nowTimestamp - dateTimestamp < 3600 * 1000 * 24 * dateMap[this.activeItem] ||
                  dateTimestamp > new Date(this.endDate).getTime() ||
                  dateTimestamp < new Date(this.defaultDate).getTime()
                );
              } else {
                return nowTimestamp - dateTimestamp < 3600 * 1000 * 24 * dateMap[this.activeItem];
              }
            } else {
              if (this.canChoiceUsed) {
                return (
                  dateTimestamp > nowTimestamp ||
                  dateTimestamp > new Date(this.endDate).getTime() ||
                  // FIXME: 默认时间之外
                  // || dateTimestamp < new Date(this.defaultDate).getTime()
                  dateTimestamp > nowTimestamp - 3600 * 1000 * 24 * dateMap[this.activeItem] + 86400000
                );
              }
              if (this.canChooseFurtrue) {
                return (
                  dateTimestamp <= nowTimestamp - 3600 * 1000 * 24 || dateTimestamp >= new Date(this.endDate).getTime()
                );
              }
              return dateTimestamp > nowTimestamp || dateTimestamp > new Date(this.endDate).getTime();
            }
          }
        };
      } else {
        // 选择缘分
        return {
          disabledDate: time => {
            let dateTimestamp = time.getTime(); // picker中的时间戳
            let nowTimestamp = Date.now(); // 当前时间戳
            if (this.endDate !== '') {
              return (
                dateTimestamp > new Date(this.endDate).getTime() || dateTimestamp < new Date(this.defaultDate).getTime()
              );
            } else {
              return nowTimestamp > dateTimestamp;
            }
          }
        };
      }
    },
    endTimeOption() {
      if (this.selectType !== 'month') {
        return {
          disabledDate: time => {
            let dateTimestamp = time.getTime(); // picker中的时间戳
            let nowTimestamp = Date.now(); // 当前时间戳
            if (this.activeItem.indexOf('before') !== -1) {
              if (this.beginDate !== '') {
                return (
                  nowTimestamp - dateTimestamp < 3600 * 1000 * 24 * dateMap[this.activeItem] ||
                  dateTimestamp < new Date(this.beginDate).getTime() ||
                  dateTimestamp > nowTimestamp
                );
              } else {
                return nowTimestamp - dateTimestamp > 3600 * 1000 * 24 * dateMap[this.activeItem];
              }
            } else {
              if (this.canChooseFurtrue) {
                return (
                  dateTimestamp < new Date(this.beginDate).getTime() || dateTimestamp <= nowTimestamp - 3600 * 1000 * 24
                );
              }
              return dateTimestamp < new Date(this.beginDate).getTime() || dateTimestamp > nowTimestamp;
            }
          }
        };
      } else {
        return {
          disabledDate: time => {
            let dateTimestamp = time.getTime(); // picker中的时间戳
            let nowTimestamp = Date.now(); // 当前时间戳
            if (this.beginDate) {
              return dateTimestamp < new Date(this.beginDate).getTime() || dateTimestamp > nowTimestamp;
            } else {
              return dateTimestamp > new Date(this.beginDate).getTime();
            }
          }
        };
      }
    }
  },
  methods: {
    clearActive() {
      this.activeItem = 'today';
    },
    chooseDefaultActive() {
      if (this.defaultActiveItem && this.defaultActiveItem !== 'today') {
        this.choiceDay(this.defaultActiveItem);
      } else {
        this.choiceDay();
      }
    },
    clearTime() {
      this.beginDate = '';
      this.endDate = '';
      this.activeItem = 'today';
      this.$emit('pick-success', {
        activeItem: this.activeItem,
        beginDate: this.beginDate,
        endDate: this.endDate
      });
    },
    getName(dateName) {
      return dateName ? dateNameMap[dateName] : null;
    },
    choiceDay(dateName) {
      let start = new Date();
      let end = new Date();
      let Y1 = '',
        M1 = '',
        D1 = '';
      let Y2 = '',
        M2 = '',
        D2 = '';
      let defaultMonth = '01';
      if (!dateMap[dateName] || !dateName) {
        this.activeItem = 'today';
      } else {
        this.activeItem = dateName;
        if (dateName.indexOf('week') !== -1 || dateName.indexOf('month') !== -1 || dateName.indexOf('fifteen') !== -1) {
          start.setTime(start.getTime() - 3600 * 1000 * 24 * (dateMap[dateName] - 1));
        } else if (dateName.indexOf('today') !== -1 || dateName.indexOf('yesterday') !== -1) {
          start.setTime(start.getTime() - 3600 * 1000 * 24 * (dateMap[dateName] - 1));
          end.setTime(end.getTime() - 3600 * 1000 * 24 * (dateMap[dateName] - 1));
        } else {
          start.setTime(start.getTime() - 3600 * 1000 * 24 * dateMap[dateName]);
          end.setTime(end.getTime() - 3600 * 1000 * 24 * dateMap[dateName]);
        }
      }
      if (this.timeSection && this.timeSection == 'beforeYesterday-yesterday') {
        end.setTime(end.getTime() - 3600 * 24 * 1000); // 昨天
        start.setTime(end.getTime() - 3600 * 24 * 1000); // 前天
      }
      Y1 = start.getFullYear();
      M1 = start.getMonth() + 1 > 9 ? start.getMonth() + 1 : '0' + (start.getMonth() + 1);
      D1 = start.getDate() > 9 ? start.getDate() : '0' + start.getDate();
      Y2 = end.getFullYear();
      M2 = end.getMonth() + 1 > 9 ? end.getMonth() + 1 : '0' + (end.getMonth() + 1);
      D2 = end.getDate() > 9 ? end.getDate() : '0' + end.getDate();
      if (this.selectType === 'month') {
        this.beginDate = Y1 + '-' + defaultMonth;
        this.endDate = Y2 + '-' + M2;
      } else {
        this.beginDate = Y1 + '-' + M1 + '-' + D1 + ' 00:00:00';
        this.endDate = Y2 + '-' + M2 + '-' + D2 + ' 23:59:59';
      }
      if (dateName == 'beforeFifteen') {
        this.within15Days = 100;
      } else {
        this.within15Days = '';
      }
      this.$emit('pick-success', {
        activeItem: this.activeItem,
        beginDate: this.beginDate,
        endDate: this.endDate,
        within15Days: this.within15Days
      });
    },
    //默认今天时间
    // getToday(){
    //   const DateTime = new Date();
    //   DateTime.setTime(DateTime.getTime() - 3600 * 1000 * 24 * 0);
    //   const start =DateTime.getFullYear()+"-" + (DateTime.getMonth()+1) + "-" + DateTime.getDate()+ " 00:00:00";
    //   const end =DateTime.getFullYear()+"-" + (DateTime.getMonth()+1) + "-" + DateTime.getDate()+ " 23:59:59";
    //   this.ruleForm.beginDate=start;
    //   this.ruleForm.endDate=end;
    //   this.isActive0=true;
    //   this.isActive1=false;
    //   this.isActive2=false;
    //   this.isActive3=false;
    // },
    // //昨天 近七天 近30天时间选择
    // choiceDay(d){
    //   this.isActive0=false;
    //   if (d==1){
    //     this.isActive1=true;
    //     this.isActive2=false;
    //     this.isActive3=false;
    //     const DateTime = new Date();
    //     DateTime.setTime(DateTime.getTime() - 3600 * 1000 * 24 * 1);
    //     var start = DateTime.getFullYear()+"-" + (DateTime.getMonth()+1) + "-" + DateTime.getDate()+ " 00:00:00";
    //     var end = DateTime.getFullYear()+"-" + (DateTime.getMonth()+1) + "-" + DateTime.getDate()+ " 23:59:59";
    //     this.ruleForm.beginDate=start;
    //     this.ruleForm.endDate=end;
    //   } else {
    //     if (d==7){
    //         this.isActive1=false;
    //         this.isActive2=true;
    //         this.isActive3=false;
    //     } else {
    //         this.isActive1=false;
    //         this.isActive2=false;
    //         this.isActive3=true;
    //     }
    //     let time1 = new Date()
    //     time1.setTime(time1.getTime() - (24 * 60 * 60 * 0))
    //     let Y1 = time1.getFullYear()
    //     let M1 = ((time1.getMonth() + 1) > 9 ? (time1.getMonth() + 1) : '0' + (time1.getMonth() + 1))
    //     let D1 = (time1.getDate() > 9 ? time1.getDate() : '0' + time1.getDate())
    //     let timer1 = Y1 + '-' + M1 + '-' + D1 +" 23:59:59";// 当前时间
    //     let time2 = new Date()
    //     time2.setTime(time2.getTime() - (24 * 60 * 60 * 1000 * d))
    //     let Y2 = time2.getFullYear()
    //     let M2 = ((time2.getMonth() + 1) > 9 ? (time2.getMonth() + 1) : '0' + (time2.getMonth() + 1))
    //     let D2 = (time2.getDate() > 9 ? time2.getDate() : '0' + time2.getDate())
    //     let timer2 = Y2 + '-' + M2 + '-' + D2 +" 00:00:00";// 之前的7天或者30天
    //     this.ruleForm.beginDate=timer2;
    //     this.ruleForm.endDate=timer1;
    //   }
    // },
    dateChange() {
      this.$emit('pick-success', {
        activeItem: this.activeItem,
        beginDate: this.beginDate,
        endDate: this.endDate,
        within15Days: this.within15Days
      });
    },
    resetTime() {
      if (this.beginTime === 'none') {
        this.beginDate = '';
        this.endDate = '';
      } else {
        this.choiceDay();
      }
      this.$emit('pick-success', {
        activeItem: this.activeItem,
        beginDate: this.beginDate,
        endDate: this.endDate
      });
    }
  },
  mounted() {
    if (this.beginTime === 'none') {
      this.beginDate = '';
      this.endDate = '';
      this.activeItem = '';
    } else {
      this.chooseDefaultActive();
    }
  }
};
</script>

<style scoped></style>
