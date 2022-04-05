package com.xpay.common.utils;

import java.util.HashSet;
import java.util.Set;

public class LockNameUtil {

    public static String getAccountMchLockName(String userNo) {
        return "accountMchLock-" + userNo;
    }

    public static Set<String> getAccountMchLockName(Set<String> userNos) {
        Set<String> lockNames = new HashSet<>(userNos.size());
        for (String userNo : userNos) {
            lockNames.add("accountMchLock-" + userNo);
        }
        return lockNames;
    }

    public static String getAccountSubLockName(String userNo) {
        return "accountSubLock-" + userNo;
    }

    public static Set<String> getAccountSubLockName(Set<String> userNos) {
        Set<String> lockNames = new HashSet<>(userNos.size());
        for (String userNo : userNos) {
            lockNames.add("accountSubLock-" + userNo);
        }
        return lockNames;
    }

    public static String getAccountTransitLockName(String userNo) {
        return "accountTransitLock-" + userNo;
    }

    public static Set<String> getAccountTransitLockName(Set<String> userNos) {
        Set<String> lockNames = new HashSet<>(userNos.size());
        for (String userNo : userNos) {
            lockNames.add("accountTransitLock-" + userNo);
        }
        return lockNames;
    }
}
