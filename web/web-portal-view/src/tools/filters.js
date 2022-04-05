import dayjs from 'dayjs';

const filters = {
  // 通用数据字典过滤器
  DictFilter: (input, objKey) => {
    let commonDict = JSON.parse(localStorage.getItem('dictionary'));
    if (objKey && commonDict && commonDict[objKey]) {
      let index = commonDict[objKey].findIndex(item => item.code === String(input));
      return index > -1 ? commonDict[objKey][index].desc : '';
    }
    return '';
  },

  PublicFilter: (input, type) => {
    const typeEnum = {
      actions: { '1': '有效', '-1': '失效' }
    };
    return typeEnum[type][input];
  },

  money: num => {
    let integer = 0; // 整数部分
    let decimals = 0; // 小数部分
    let result = ''; // 返回值
    let minLength = 3;
    let stringNum = typeof num === 'number' ? num : Number(num);
    // 判断是否是数字
    if (isNaN(stringNum)) {
      return 0;
    }
    // 判断正负数
    if (stringNum >= 0) {
      minLength = 3;
    } else {
      minLength = 4;
    }
    // 处理小数位数
    stringNum = stringNum.toFixed(2);
    // 需要分隔符则循环添加
    const numArr = stringNum.split('.');
    integer = numArr[0];
    decimals = numArr[1] || 0;
    while (integer.length > minLength) {
      result = ',' + integer.slice(-3) + result;
      integer = integer.slice(0, integer.length - 3);
    }
    if (integer) {
      result = integer + result;
    }
    if (decimals) {
      result = result + '.' + decimals;
    }
    return result;
  },

  integer: num => {
    !num && (num = '0');
    let integer = 0; // 整数部分
    let result = ''; // 返回值
    let minLength = 3;
    integer = num;
    while (integer.length > minLength) {
      result = ',' + integer.slice(-3) + result;
      integer = integer.slice(0, integer.length - 3);
    }
    if (integer) {
      result = integer + result;
    }
    return result;
  },

  timeFilter: dayTime => {
    if (dayTime) {
      dayTime = typeof(dayTime) === 'number' ? new Date(dayTime) : new Date(Date.parse(dayTime));
      return dayTime ? dayjs(dayTime).format('YYYY-MM-DD HH:mm:ss') : '-';
    } else {
      return dayTime;
    }
  },

  dayFilter: dayTime => {
    if(dayTime){
      dayTime = typeof(dayTime) === 'number' ? new Date(dayTime) : new Date(Date.parse(dayTime));
      return dayTime ? dayjs(dayTime).format('YYYY-MM-DD') : '-';
    }else{
      return dayTime;
    }
  }
};

export default filters;
