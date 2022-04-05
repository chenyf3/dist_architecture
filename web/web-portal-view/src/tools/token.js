import Cookies from 'js-cookie';

const TokenKey = 'X-Token';

export function getToken() {
  return Cookies.get(TokenKey);
}

export function setToken(token) {
  return Cookies.set(TokenKey, token);
}

export function removeToken() {
  return Cookies.remove(TokenKey);
}

export function getTokenHeader() {
  let obj = {};
  obj[TokenKey] = getToken();
  return obj;
}
