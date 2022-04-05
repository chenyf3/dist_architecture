package com.xpay.web.api.common.controller;

import com.xpay.common.statics.annotations.Permission;
import com.xpay.common.statics.enums.user.SystemTypeEnum;
import com.xpay.common.statics.exception.BizException;
import com.xpay.common.statics.query.PageQuery;
import com.xpay.common.statics.result.PageResult;
import com.xpay.common.statics.result.RestResult;
import com.xpay.common.utils.BeanUtil;
import com.xpay.common.utils.StringUtil;
import com.xpay.web.api.common.annotations.CurrentUser;
import com.xpay.web.api.common.ddo.vo.DictionaryVo;
import com.xpay.web.api.common.model.UserModel;
import com.xpay.web.api.common.service.DictionaryService;
import com.xpay.web.api.common.service.OperateLogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 数据字典
 * @author longfenghua
 * @date 2019/11/14
 */
@RestController
@RequestMapping("dictionary")
public class DictionaryController {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    @Autowired(required = false)
    private DictionaryService dictionaryService;
    @Autowired
    private OperateLogService operateLogService;

    @Permission("sys:dictionary:view")
    @RequestMapping("listDictionary")
    public RestResult<PageResult<List<DictionaryVo>>> listDictionary(DictionaryVo dictionaryDto) {
        try {
            PageQuery pageQuery = PageQuery.newInstance(dictionaryDto.getCurrentPage(), dictionaryDto.getPageSize());
            Map<String, Object> paramMap = BeanUtil.toMapNotNull(dictionaryDto);
            PageResult<List<DictionaryVo>> pageResult = dictionaryService.listDictionaryPage(paramMap, pageQuery);
            return RestResult.success(pageResult);
        } catch (BizException e) {
            return RestResult.error("查询失败，" + e.getMsg());
        } catch (Exception e) {
            logger.error("分页查询数据字典异常", e);
            return RestResult.error("获取数据字典失败");
        }
    }

    @Permission("sys:dictionary:view")
    @RequestMapping("getDictionaryById")
    public RestResult<DictionaryVo> getDictionaryById(@RequestParam long id) {
        try {
            DictionaryVo dictionary = dictionaryService.getDictionaryById(id);
            if (dictionary == null) {
                return RestResult.error("数据字典不存在");
            }
            return RestResult.success(dictionary);
        } catch (BizException e) {
            return RestResult.error("查询失败，" + e.getMsg());
        } catch (Exception e) {
            logger.error("查询数据字典异常", e);
            return RestResult.error("获取数据字典失败");
        }
    }

    @Permission("sys:dictionary:delete")
    @RequestMapping("deleteDictionary")
    public RestResult<String> deleteDictionary(@RequestParam Long id, @CurrentUser UserModel userModel) {
        try {
            DictionaryVo dictionary = dictionaryService.getDictionaryById(id);
            if (dictionary == null) {
                return RestResult.error("数据字典不存在");
            }
            boolean isOK =dictionaryService.deleteDictionary(id, userModel.getLoginName());
            if(isOK){
                operateLogService.logDelete("删除数据字典，名称:" + dictionary.getDataName(), userModel);
                return RestResult.success("删除成功");
            }else{
                return RestResult.error("删除失败");
            }
        } catch (BizException e) {
            return RestResult.error("删除失败，" + e.getMsg());
        } catch (Exception e) {
            logger.error("删除数据字典异常", e);
            operateLogService.logDelete("删除异常:" + e.getMessage(), userModel);
            return RestResult.error("删除失败");
        }
    }

    @Permission("sys:dictionary:add")
    @RequestMapping("addDictionary")
    public RestResult<String> addDictionary(@CurrentUser UserModel userModel, @RequestBody DictionaryVo dictionaryDto) {
        try {
            if (StringUtil.isEmpty(dictionaryDto.getDataName())) {
                return RestResult.error("dataName不能为空");
            } else if (dictionaryService.getDictionaryByName(dictionaryDto.getDataName().trim()) != null) {
                return RestResult.error("dataName已存在");
            } else if (dictionaryDto.getDataInfo().stream().anyMatch(p -> StringUtil.isEmpty(p.getName()) || StringUtil.isEmpty(p.getCode()) || StringUtil.isEmpty(p.getDesc()))) {
                return RestResult.error("数据标识、数据编码和数据描述都不能为空");
            } else if (dictionaryDto.getDataInfo().stream().collect(Collectors.groupingBy(DictionaryVo.Item::getName)).size() != dictionaryDto.getDataInfo().size()) {
                return RestResult.error("数据标识存在重复");
            } else if (dictionaryDto.getDataInfo().stream().collect(Collectors.groupingBy(DictionaryVo.Item::getCode)).size() != dictionaryDto.getDataInfo().size()) {
                return RestResult.error("数据编码存在重复");
            }

            DictionaryVo dictionary = new DictionaryVo();
            dictionary.setCreator(userModel.getLoginName());
            dictionary.setModifyUser(userModel.getLoginName());
            dictionary.setDataName(dictionaryDto.getDataName().trim());
            dictionary.setDataInfo(dictionaryDto.getDataInfo());
            dictionary.setRemark(dictionaryDto.getRemark());
            dictionary.setModifyTime(new Date());
            dictionary.setSystemType(dictionaryDto.getSystemType() == null ? SystemTypeEnum.COMMON_MANAGEMENT.getValue() : dictionaryDto.getSystemType());
            boolean isOk = dictionaryService.addDictionary(dictionary);
            if(isOk){
                operateLogService.logEdit("新增数据字典" + dictionaryDto.getDataName().trim(), userModel);
                return RestResult.success("添加成功");
            }else{
                return RestResult.error("添加失败");
            }
        } catch (BizException e) {
            return RestResult.error("添加失败，" + e.getMsg());
        } catch (Exception e) {
            logger.error("添加数据字典异常", e);
            return RestResult.error("新增失败，系统异常");
        }
    }

    @Permission("sys:dictionary:edit")
    @RequestMapping("editDictionary")
    public RestResult<String> editDictionary(@CurrentUser UserModel userModel, @RequestBody DictionaryVo dictionaryDto) {
        try {
            if (dictionaryDto.getDataInfo() == null) {
                dictionaryDto.setDataInfo(Collections.emptyList());
            }

            DictionaryVo dictionary = dictionaryService.getDictionaryById(dictionaryDto.getId());
            if (dictionary == null) {
                return RestResult.error("数据字典不存在");
            } else if (dictionaryDto.getDataInfo().stream().anyMatch(p -> StringUtil.isEmpty(p.getName()) || StringUtil.isEmpty(p.getCode()) || StringUtil.isEmpty(p.getDesc()))) {
                return RestResult.error("数据标识、数据编码和数据描述都不能为空");
            } else if (dictionaryDto.getDataInfo().stream().collect(Collectors.groupingBy(DictionaryVo.Item::getName)).size() != dictionaryDto.getDataInfo().size()) {
                return RestResult.error("数据标识存在重复");
            } else if (dictionaryDto.getDataInfo().stream().collect(Collectors.groupingBy(DictionaryVo.Item::getCode)).size() != dictionaryDto.getDataInfo().size()) {
                return RestResult.error("数据编码存在重复");
            }
            dictionary.setDataInfo(dictionaryDto.getDataInfo());
            dictionary.setRemark(dictionaryDto.getRemark());
            dictionary.setSystemType(dictionaryDto.getSystemType());
            dictionary.setModifyUser(userModel.getLoginName());
            dictionary.setModifyTime(new Date());
            boolean isOk = dictionaryService.editDictionary(dictionary);
            if(isOk){
                operateLogService.logEdit("修改数据字典" + dictionaryDto.getDataName(), userModel);
                return RestResult.success("修改成功");
            }else{
                return RestResult.error("修改失败");
            }
        } catch (BizException e) {
            return RestResult.error("修改失败，" + e.getMsg());
        } catch (Exception e) {
            logger.error("修改数据字典异常", e);
            return RestResult.error("修改失败，系统异常");
        }
    }

    /**
     * 该接口用于用户运营后台后获取，不添加权限判断
     * @return .
     */
    @RequestMapping("listAllDictionary")
    public RestResult<Map<String, List<DictionaryVo.Item>>> listAllDictionary() {
        Map<String, List<DictionaryVo.Item>> dictionaryMap = dictionaryService.listAllDictionary();
        return RestResult.success(dictionaryMap);
    }

    @RequestMapping("getDictionaryByName")
    public RestResult<List<DictionaryVo.Item>> getDictionaryByName(@RequestParam String name) {
        try {
            DictionaryVo dictionary = dictionaryService.getDictionaryByName(name);
            if(dictionary != null){
                return RestResult.success(dictionary.getDataInfo());
            }else{
                return RestResult.error("数据字典不存在");
            }
        } catch (BizException e) {
            return RestResult.error("查询失败，" + e.getMsg());
        } catch (Exception e) {
            logger.error("获取数据字典异常", e);
            return RestResult.error("获取数据字典失败");
        }
    }
}
