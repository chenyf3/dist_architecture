package com.xpay.service.accountmch.biz;

import com.xpay.common.statics.query.PageQuery;
import com.xpay.common.statics.result.PageResult;
import com.xpay.common.utils.BeanUtil;
import com.xpay.common.utils.DateUtil;
import com.xpay.common.utils.StringUtil;
import com.xpay.facade.accountmch.dto.AccountBalanceSnapDto;
import com.xpay.facade.accountmch.dto.AccountMchDto;
import com.xpay.service.accountmch.dao.AccountBalanceSnapDao;
import com.xpay.service.accountmch.dao.AccountMchDao;
import com.xpay.service.accountmch.dao.AccountProcessDetailDao;
import com.xpay.service.accountmch.dao.AccountProcessDetailHistoryDao;
import com.xpay.service.accountmch.entity.AccountBalanceSnap;
import com.xpay.service.accountmch.entity.AccountMch;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by chenyf on 2017/9/26.
 */
@Component
public class AccountQueryBiz {
    @Autowired
    private AccountMchDao accountMchDao;
    @Autowired
    AccountBalanceSnapDao accountBalanceSnapDao;
    @Autowired
    AccountProcessDetailDao accountProcessDetailDao;
    @Autowired
    AccountProcessDetailHistoryDao accountProcessDetailHistoryDao;

    public PageResult<List<AccountBalanceSnapDto>> listBalanceSnapPage(Map<String, Object> paramMap, PageQuery pageQuery){
        PageResult<List<AccountBalanceSnap>> result = accountBalanceSnapDao.listPage(paramMap, pageQuery);
        return PageResult.newInstance(BeanUtil.newAndCopy(result.getData(), AccountBalanceSnapDto.class), result);
    }

    /**
     * 多条件分页查询账户编号
     * @param paramMap
     * @param pageCurrent
     * @param pageSize
     * @param sortColumn
     * @return
     */
    public List<String> listAccountNoPage(Map<String, Object> paramMap, Integer pageCurrent, Integer pageSize, String sortColumn){
        return accountMchDao.listAccountNoPage(paramMap, pageCurrent, pageSize, sortColumn);
    }

    /**
     * 分页查询主账户信息
     * @param pageQuery
     * @param paramMap
     * @return
     */
    public PageResult<List<AccountMchDto>> listAccountPage(Map<String, Object> paramMap, PageQuery pageQuery) {
        PageResult<List<AccountMch>> result = accountMchDao.listPage(paramMap, pageQuery);
        return PageResult.newInstance(BeanUtil.newAndCopy(result.getData(), AccountMchDto.class), result);
    }

    /**
     * 根据用户编号获取账户信息
     * @param accountNo 用户编号.
     * @return account 查询到的账户信息.
     */
    public AccountMchDto getAccountByAccountNo(String accountNo) {
        if (StringUtil.isEmpty(accountNo)) {
            return null;
        }
        AccountMch accountMch = accountMchDao.getByAccountNo(accountNo);
        return BeanUtil.newAndCopy(accountMch, AccountMchDto.class);
    }

    /**
     * 根据多个账户编号获取账户信息.
     * @deprecated
     * @param accountNoList 多个账户编号
     * @return List<Account> 查询到的账户信息.
     */
    public List<AccountMchDto> listAccountByAccountNos(List<String> accountNoList){
        if (accountNoList == null || accountNoList.isEmpty()) {
            return null;
        }
        Map<String, Object> param = new HashMap<String, Object>();
        param.put("accountNoList", accountNoList);

        List<AccountMch> accountMchList = accountMchDao.listBy(param);
        return BeanUtil.newAndCopy(accountMchList, AccountMchDto.class);
    }

    /**
     * 根据多个账户编号获取账户信息.
     * @param accountNoList 多个账户编号.
     * @return List<Account> 查询到的账户信息.
     */
    public Map<String, AccountMchDto> mapAccountByAccountNoList(List<String> accountNoList){
        if (accountNoList == null || accountNoList.isEmpty()) {
            return new HashMap<>();
        }
        Map<String, Object> paramMap = new HashMap<String, Object>();
        paramMap.put("accountNoList", accountNoList);
        Map<String, AccountMch> accountMap = accountMchDao.mapBy(paramMap, "accountNo");
        if(accountMap == null){
            return new HashMap<>();
        }

        Map<String, AccountMchDto> accountDtoMap = new HashMap<>();
        accountMap.forEach((k,v) -> {
            accountDtoMap.put(k, BeanUtil.newAndCopy(v, AccountMchDto.class));
        });
        return accountDtoMap;
    }

    public boolean isAccountProcessDetailExist(String accountNo, String requestNo, Integer processType, String processNo) {
        boolean isExist = accountProcessDetailDao.isAccountProcessDetailExist(accountNo, requestNo, processType, processNo);
        if(! isExist){
            Date date = DateUtil.addDay(new Date(), -180);
            isExist = accountProcessDetailHistoryDao.isAccountProcessDetailExist(accountNo, requestNo, processType, processNo, date);
        }
        return isExist;
    }
}
