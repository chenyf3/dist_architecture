const RSA_PUBLIC_KEY = process.env.VUE_APP_RSA_PUBLIC_KEY;

import { JSEncrypt } from 'jsencrypt';

export const encryptParam = param => {
  const jsencrypt = new JSEncrypt();
  jsencrypt.setPublicKey(RSA_PUBLIC_KEY);
  const RSA_encrypt = jsencrypt.encrypt(param);
  return RSA_encrypt;
};
