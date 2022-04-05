package com.xpay.web.api.common.service;

import com.xpay.common.statics.query.PageQuery;
import com.xpay.common.statics.result.PageResult;
import com.xpay.web.api.common.ddo.vo.DictionaryVo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface DictionaryService {
    default PageResult<List<DictionaryVo>> listDictionaryPage(Map<String, Object> paramMap, PageQuery pageQuery){
        return PageResult.newInstance(null, pageQuery);
    }

    default DictionaryVo getDictionaryByName(String dictionaryName){
        return null;
    }

    default DictionaryVo getDictionaryById(Long dictionaryId){
        return null;
    }

    default Map<String, List<DictionaryVo.Item>> listAllDictionary(){
        return new HashMap<>();
    }

    default boolean addDictionary(DictionaryVo dto){
        return false;
    }

    default boolean editDictionary(DictionaryVo dto){
        return false;
    }

    default boolean deleteDictionary(Long dictionaryId, String operator){
        return false;
    }
}
