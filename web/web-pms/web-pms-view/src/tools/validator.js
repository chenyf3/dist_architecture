export const checkPwd = pwd => {
  const pwdReg = /(?=.*[0-9])(?=.*[a-zA-Z])(?=.*[^a-zA-Z0-9]).{8,20}/;
  return pwdReg.test(String(pwd));
};

export const checkPhone = phone => {
  const phoneReg = /^1[3-9]\d{9}$/;
  return phoneReg.test(String(phone));
};

export const checkMoney = money => {
  const moneyReg = /^([1-9]\d{0,8}|0)(\.\d{1,2})?$/;
  return moneyReg.test(String(money));
};

export const checkLiter = liter => {
  const literReg = /^([1-9]\d{0,8}|0)(\.\d{1,2})?$/;
  return literReg.test(String(liter));
};

export const checkEmail = email => {
  // eslint-disable-next-line no-useless-escape
  const emailReg = /^(([^<>()\[\]\\.,;:\s@"]+(\.[^<>()\[\]\\.,;:\s@"]+)*)|(".+"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/;
  return emailReg.test(String(email).toLowerCase());
};

export const checkChineseChar = string => {
  const reg = /[\u4e00-\u9fa5]+/g;
  return reg.test(string);
};

export const checkInteger = integer => {
  const integerReg = /^[0-9]*[1-9][0-9]*$/;
  return integerReg.test(String(integer));
};

// var r1= /^[0-9]*[1-9][0-9]*$/ //正整数
// var r2=/^-[0-9]*[1-9][0-9]*$/ //负整数
// var r3=/^\d+$/ //非负整数（正整数 + 0）
// var r4=/^-?\d+$/ //整数

// TODO: 动态验证长度
export const checkLength = length => input => {
  const inputReg = `/^[ A-Za-z\u3000\u3400-\u4DBF\u4E00-\u9FFF]{0,${length}}$/`;
  console.log(inputReg);

  return inputReg.test(String(input));
};

// export const checkInput50 = input => {
//   const inputReg = /^[ A-Za-z\u3000\u3400-\u4DBF\u4E00-\u9FFF]{0,50}$/;
//   return inputReg.test(String(input));
// };

export const formCheckPhone = (_, phone, callback) => {
  if (!checkPhone(phone)) {
    callback('请输入正确的手机号');
  } else {
    callback();
  }
};

export const formCheckMoney = (_, money, callback) => {
  if (!checkMoney(money)) {
    callback('金额仅限2位小数，9位整数');
  } else if (money <= 0) {
    callback('金额需要大于零');
  } else {
    callback();
  }
};

export const formCheckPwd = (_, pwd, callback) => {
  if (!checkPwd(pwd)) {
    callback('密码应为8-20位数字,字母和特殊字符组合');
  } else if (pwd.length > 20) {
    callback('密码不超过20位');
  } else {
    callback();
  }
};

export const formCheckEmail = (_, email, callback) => {
  if (!checkEmail(email)) {
    callback('邮箱格式不正确');
  } else {
    callback();
  }
};

export const formCheckChinese = (_, string, callback) => {
  if (!checkChineseChar(string)) {
    callback('请输入中文');
  } else {
    callback();
  }
};
