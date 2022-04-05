package com.xpay.service.sequence.test;

import com.xpay.service.sequence.ServiceSequenceApp;
import org.jasypt.encryption.StringEncryptor;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = ServiceSequenceApp.class)
public class BaseTestCase {

    @Autowired
    StringEncryptor stringEncryptor;

    @Ignore
    @Test
    public void encryptPwd() {
        String username = stringEncryptor.encrypt("eqp");
        String password = stringEncryptor.encrypt("Eqipay@8");
        System.out.println("加密后的用户为：" + username);
        System.out.println("加密后的密码为：" + password);
    }
}