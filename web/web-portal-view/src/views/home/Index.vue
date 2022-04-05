<template>
  <div class="home-container">
    <div class="home-data">
      <el-card class="box-card chartCard">
        <div slot="header" class="box-card-header clearfix">
          <div style="display: inline-block">
            <div class="colorB">
              交易概况
            </div>
          </div>
          <div class="block" style="display: inline-block">
            <el-date-picker
                @change="dateChange"
                v-model="timeArr"
                type="daterange"
                value-format="yyyy-MM-dd"
                align="right"
                size="small"
                unlink-panels
                range-separator="至"
                start-placeholder="开始日期"
                end-placeholder="结束日期"
                :picker-options="disabledDateOption"
                :clearable="false"
                :disabled="true"
            >

            </el-date-picker>
          </div>
          <el-radio-group class="timeBox" v-model="choicedDay" @change="choiceDay" size="small">
            <el-radio-button v-if="false" label="近7日" value="sevenDay"></el-radio-button>
            <el-radio-button label="本月" value="month"></el-radio-button>
            <el-radio-button label="上月" value="lastMonth"></el-radio-button>
            <el-radio-button v-if="false" label="近3个月" value="threeMonth"></el-radio-button>
            <el-radio-button v-if="false" label="月度" value="year" :class="{ colorGreen: true }"></el-radio-button>
          </el-radio-group>
        </div>
        <div class="chartContent" v-show="isShowChart">
          <div class="tabBox">
            <el-radio-group v-model="choicedType" @change="changeHandler" size="small">
              <el-radio-button label="交易金额"></el-radio-button>
              <el-radio-button label="交易笔数"></el-radio-button>
            </el-radio-group>
            <span class="floatR" style="font-size:14px;">
              笔均
              <el-popover
                  style="display: inline-block;"
                  placement="top-start"
                  width="320"
                  trigger="hover"
                  content="搜索时间段内的交易成功金额除以交易成功笔数。"
              >
                <div slot="reference">
                  <!-- <img src="./../../assets/img/crossBorder/u1561.png" alt="" class="quesIcon" /> -->
                </div>
              </el-popover>
              ￥{{ averageAmount | money }}
            </span>
          </div>
          <template v-show="isShowChart">
            <div id="myAreaChart" style="width: 848px;height: 320px" ref="myAreaChart"></div>
          </template>
        </div>
        <div class="chartContentEmpty" v-show="!isShowChart">
          <!-- <img src="../../assets/img/home-empty.svg" alt="" /> -->
          <div style="margin-top: 20px">暂无数据</div>
        </div>
      </el-card>
    </div>
    <div v-if="false" class="home-userinfo-box">
      <template v-for="item in userInfoEnum">
        <span class="home-userinfo-box-item" :key="item.id">
          <span class="label">
            {{ item.label }}
          </span>
          <span class="dashed"></span>
          <span class="info">
            {{ userInfo[item.key] }}
          </span>
        </span>
      </template>
    </div>
  </div>
</template>

<script>
import echarts from '@/tools/echarts';
import dayjs from 'dayjs';
import {getTradeDataStatics} from '@/api/home';

export default {
  name: 'Home',
  data() {
    return {
      userInfoEnum: [
        {label: '登录名', key: 'loginName'},
        {label: '手机号', key: 'mobileNo'},
        {label: '邮箱', key: 'email'},
        {label: '商户号', key: 'mchNo'},
        {label: '商户类型', key: 'mchType'}
      ],
      menuList: [],
      timeArr: [],
      isShowChart: false,
      choicedDay: '近3个月',
      choicedType: '交易金额',
      xArr: [],
      numArr: [],
      amountArr: [],
      averageAmount: 0
    };
  },
  computed: {
    userInfo() {
      return this.$store.state.userInfo;
    },
    disabledDateOption() {
      return {
        disabledDate: time => {
          if (time > dayjs().add(-1, 'day') || time < dayjs().startOf('year')) {
            return true;
          } else {
            return false;
          }
        }
      };
    }
  },
  mounted() {
    // this.initFun();
    this.choiceDay('本月');
  },
  methods: {
    changeHandler(value) {
      this.choicedType = value;
      if (value == '交易金额') {
        this.drawAreaLine(this.xArr, this.amountArr, this.choicedDay === '月度');
      } else {
        this.drawAreaLine(this.xArr, this.numArr, this.choicedDay === '月度');
      }
    },
    choiceDay(val) {
      this.choicedDay = val;
      const timeArr = [];
      switch (val) {
          // case '近7日':
          //   timeArr.push(
          //     dayjs()
          //       .add(-7, 'day')
          //       .format('YYYY-MM-DD'),
          //     dayjs()
          //       .add(-1, 'day')
          //       .format('YYYY-MM-DD')
          //   );
          //   this.timeArr = timeArr;
          //   this.dateChange(timeArr, true);
          //   break;
        case '本月':
          timeArr.push(
              dayjs()
                  .startOf('month')
                  .format('YYYY-MM-DD'),
              dayjs()
                  .endOf('month')
                  .format('YYYY-MM-DD')
          );
          this.timeArr = timeArr;
          this.dateChange(timeArr, true);
          break;
        case '上月':
          timeArr.push(
              dayjs()
                  .add(-1, 'month')
                  .startOf('month')
                  .format('YYYY-MM-DD'),
              dayjs()
                  .add(-1, 'month')
                  .endOf('month')
                  .format('YYYY-MM-DD')
          );
          this.timeArr = timeArr;
          this.dateChange(timeArr, true);
          break;
          // case '近3个月':
          //   this.timeArr = [
          //     dayjs()
          //       .add(-3, 'month')
          //       .format('YYYY-MM-DD'),
          //     dayjs()
          //       .add(-1, 'day')
          //       .format('YYYY-MM-DD')
          //   ];
          //   this.dateChange(timeArr, true);
          //   break;
        default:
          break;
      }
    },
    dateChange(val, isShowDay) {
      /**
       * 修改交易概况时间
       * @param val Array 所选中的时间
       * @param isShowDay Boolean 是否展示右上角快速选择时间的标志
       * @param isYear Boolean 快捷时间选择的是否是月度
       */
      if (!isShowDay) {
        this.choicedDay = '';
      }
      this.xArr = [];
      this.amountArr = [];
      this.numArr = [];
      let tempResult = getTradeDataStatics({
        startDay: val[0],
        endDay: val[1]
      });

      tempResult.then(response => {
        this.topLoading = false;
        const data = response.data;
        if (Array.isArray(data) && data.length > 0) {
          this.isShowChart = true;
          let numTotal = 0; // 总笔数
          let amountTotal = 0; // 总价钱
          data.forEach(item => {
            amountTotal += item.amount;
            numTotal += item.num;
            this.xArr.push(item.tradeDate);
            this.amountArr.push(item.amount);
            this.numArr.push(item.num);
          });
          this.averageAmount = numTotal === 0 ? 0 : Number(amountTotal) / Number(numTotal);
          // this.choicedType = '交易金额',
          this.drawAreaLine(this.xArr, this.choicedType === '交易金额' ? this.amountArr : this.numArr);
        } else {
          // 展示数据为空的图片
          this.isShowChart = false;
        }
      })
          .catch(err => {
            console.error(err);
          });
    },
    drawAreaLine(xArr, yArr, isYear) {
      const week = ['周日', '周一', '周二', '周三', '周四', '周五', '周六'];
      // let myAreaChart = echarts.init(this.$refs.myAreaChart);
      let myAreaChart = echarts.init(document.getElementById('myAreaChart'));
      const option = {
        tooltip: {
          trigger: 'axis',
          axisPointer: {
            type: 'cross',
            crossStyle: {
              type: 'solid'
            },
            label: {
              backgroundColor: '#6a7985'
            }
          },
          // formatter: (params, ticket, callback) => {
          formatter: params => {
            var showHtm = '';
            for (var i = 0; i < params.length; i++) {
              //x轴名称
              var name = params[i].name;
              //值
              var value = params[i].value;
              const decimal = Number(value)
                  .toFixed(2)
                  .split('.')[1];
              value =
                  this.choicedType === '交易金额'
                      ? '￥' + (Math.floor(value).toLocaleString('en-US') + '.' + decimal)
                      : value + '笔';
              showHtm += name + '<br>' + value;
            }
            return showHtm;
          }
        },
        xAxis: {
          type: 'category',
          boundaryGap: false,
          data: xArr,
          axisLabel: {
            formatter: name => {
              const time = new Date(name);
              const date = time.getDay();
              return isYear ? name.substr(5) + '月' : name.substr(5) + '\n' + week[date];
            },
            textStyle: {
              color: '#2A3241'
            }
          },
          axisLine: {
            lineStyle: {
              color: '#CED3D9'
            }
          }
        },
        yAxis: {
          name: this.choicedType === '交易金额' ? '金额：元' : '笔数：笔',
          type: 'value',
          // 控制网格线是否显示
          splitLine: {
            show: true,
            // 改变轴线颜色
            lineStyle: {
              type: 'solid',
              // 使用深浅的间隔色
              color: ['#F5F6F7']
            }
          },
          nameTextStyle: {
            color: '#2A3241',
            padding: [10, 60, 22, 0]
          },
          axisLabel: {
            textStyle: {
              color: '#2A3241'
            }
          },
          axisLine: {
            lineStyle: {
              color: '#fff'
            }
          }
        },
        dataZoom: [
          // 这个dataZoom组件，若未设置xAxisIndex或yAxisIndex，则默认控制x轴。
          {
            type: 'slider', //这个 dataZoom 组件是 slider 型 dataZoom 组件（只能拖动 dataZoom 组件导致窗口变化）
            xAxisIndex: 0, //控制x轴
            start: this.choicedDay === '近3个月' ? 50 : 0, // 左边在 0% 的位置
            end: 100 // 右边在 100% 的位置
          }
        ],
        series: [
          {
            type: 'line',
            data: yArr,
            markPoint: {
              symbol: 'circle', //标注类型
              symbolRotate: [-180],
              // symbol:'image://http://img30.360buyimg.com/poprx/jfs/t20023/329/2344745722/12670/a004c21d/5b3c2794Ndd1983e8.png',
              symbolSize: [10, 10], //标记大小
              symbolOffset: [0, 0],
              itemStyle: {
                normal: {
                  label: {
                    position: 'top',
                    formatter: obj => {
                      let value = obj.value;
                      const decimal = Number(value)
                          .toFixed(2)
                          .split('.')[1];
                      value =
                          this.choicedType === '交易金额'
                              ? '￥' + (Math.floor(value).toLocaleString('en-US') + '.' + decimal)
                              : value + '笔';
                      return value;
                    }
                  }
                },
                color: 'red'
              },
              data: [
                {type: 'max', name: '最大值'},
                {type: 'min', name: '最小值'}
              ]
            },
            // smooth: true,  //这句就是让曲线变平滑的
            // symbol:'none',  //这句就是去掉点的
            lineStyle: {
              color: this.choicedDay === '月度' ? '#0BB59E' : '#FC9942'
            },
            areaStyle: {
              // color: "#EBF4FE"
              color:
                  this.choicedDay === '月度'
                      ? new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                        {
                          // offset: 0, color: '#0BB59E' // 0% 处的颜色
                          offset: 0,
                          color: 'rgba(11,181,158,1)'
                        },
                        {
                          // offset: 1, color: '#fff' // 100% 处的颜色
                          offset: 1,
                          color: 'rgba(255,255,255,0.1)'
                        }
                      ])
                      : new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                        {
                          // offset: 0, color: '#FC9942' // 0% 处的颜色
                          offset: 0,
                          color: 'rgba(83,159,248,1)'
                        },
                        {
                          // offset: 1, color: '#fff' // 100% 处的颜色
                          offset: 1,
                          color: 'rgba(255,255,255,0.1)'
                        }
                      ]) //背景渐变色
            },
            itemStyle: {
              normal: {
                color: this.choicedDay === '月度' ? '#0BB59E' : '#FC9942'
              }
            }
          }
        ],
        grid: {
          y: '50',
          bottom: '80'
        }
      };
      myAreaChart.setOption(option);
    }
  }
};
</script>

<style lang="stylus" scoped>
.home-container
  color #333
  font-size 18px
  display: flex;
  justify-content: center;

  .home-userinfo-box
    display flex
    width 240px
    flex-wrap wrap;
    justify-content space-between
    align-items center
    border 1px solid #EBEEF5
    border-radius 6px
    padding 20px
    background #fff;
    box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);

    &-item
      display inline-flex
      margin 6px 0
      justify-content space-between
      align-items center
      width 100%

      .label::after
        content ':';

      .dashed
        border-bottom 1px dashed #333;
        flex-grow 1;
        height 1px;
        margin 0 6px

.box-card-header
  display flex;
  justify-content: space-between;
  align-items: center;

.home-data {
  width: 850px;
}

.chartContentEmpty {
  box-sizing: border-box;
  padding: 139px 0;
  text-align: center;
  background: #F5F6F7;

  img {
    width: 100px;
  }
}
</style>
