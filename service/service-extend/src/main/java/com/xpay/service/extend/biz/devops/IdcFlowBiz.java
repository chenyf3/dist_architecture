package com.xpay.service.extend.biz.devops;

import com.xpay.common.statics.exception.BizException;
import com.xpay.common.utils.*;
import com.xpay.facade.extend.vo.IdcVo;
import com.xpay.service.extend.conifg.DevopsProperties;
import com.xpay.starter.plugin.client.RedisClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 机房流量处理对接层
 * @author chenyf
 */
@Component
class IdcFlowBiz {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private static final String FLOW_SWITCH_FLAG = "devOps:idc_flow_switch";
    @Autowired
    DevopsProperties devopsProperties;
    @Autowired
    RedisClient redisClient;
    @Autowired
    DomainBiz domainBiz;

    /**
     * 执行流量切换
     * @param toIdcList
     */
    public boolean flowSwitch(List<String> toIdcList, String operator){
        if(! isBindable()){
            throw new BizException(BizException.BIZ_INVALID, "当前未支持网络绑定");
        }

        //1.入参校验
        if(toIdcList == null || toIdcList.isEmpty()){
            throw new BizException(BizException.BIZ_INVALID, "切入的机房编号为空");
        }else if(isFlowSwitching()){
            throw new BizException(BizException.BIZ_INVALID, "流量切换中，请等待当前切换完成后再试");
        }

        //2.配置参数校验
        String domainNames = devopsProperties.getNetConfig().getDomains();
        DevopsProperties.BindType bindType = devopsProperties.getNetConfig().getBindType();
        if(StringUtil.isEmpty(domainNames)){
            throw new BizException(BizException.BIZ_INVALID, "未配置域名");
        }else if(bindType == null){
            throw new BizException(BizException.BIZ_INVALID, "未配置或配置了错误的网络绑定类型");
        }

        //3.取得切入机房的ip地址列表
        List<String> needBindIpList = new ArrayList<>();
        for(String toIdc : toIdcList){
            DevopsProperties.Idc idc = getIdcByCode(toIdc);
            if(idc == null){
                throw new BizException(BizException.BIZ_INVALID, "机房编号为："+toIdc+" 的机房配置不存在");
            }else if(StringUtil.isEmpty(idc.getIncomeIps())){
                throw new BizException(BizException.BIZ_INVALID, "机房编号为："+toIdc+" 没有配置机房IP");
            }
            needBindIpList.addAll(Arrays.asList(idc.getIncomeIps().split(",")));
        }
        if(needBindIpList.isEmpty()){
            throw new BizException(BizException.BIZ_INVALID, "要切入的机房没有配置要绑定的IP！");
        }

        //4.进行流量切换
        boolean isError = false;
        try {
            //4.1 标识为正在进行流量切换
            int switchSecond = devopsProperties.getNetConfig().getNetSwitchSec();
            redisClient.set(FLOW_SWITCH_FLAG, "true", switchSecond);

            //4.2 执行流量切换
            List<String> domainNameList = Arrays.asList(domainNames.split(","));
            for(String domainName : domainNameList){
                if(StringUtil.isEmpty(domainName)){
                    continue;
                }else if(DevopsProperties.BindType.WAF.equals(bindType)){//通过waf进行IP绑定
                    List<String> wafInstances = getWafInstances();
                    domainBiz.modifyWafDomainBindingIp(wafInstances, domainName, needBindIpList);
                }else if(DevopsProperties.BindType.DCDN.equals(bindType)){//通过DCDN全球全站加速进行IP绑定
                    domainBiz.modifyDCDNDomainBindingIp(domainName, needBindIpList);
                }else if(DevopsProperties.BindType.CDN.equals(bindType)){//通过CDN进行IP绑定
                    domainBiz.modifyCDNDomainBindingIp(domainName, needBindIpList);
                }else if(DevopsProperties.BindType.DNS.equals(bindType)){//通过DNS进行IP绑定
                    domainBiz.modifyDnsDomainBindingIp(domainName, needBindIpList);
                }
            }
            logger.info("流量切换成功 toIdcList={} operator={}", JsonUtil.toJson(toIdcList), operator);
            return true;
        } catch (BizException e){
            isError = true;
            throw e;
        } catch(Exception e) {
            isError = true;
            logger.error("机房流量切换异常 domainName={} errorMsg={} ", domainNames, e.getMessage(), e.getCause());
            throw new BizException(BizException.BIZ_INVALID, "机房流量切换异常", e);
        } finally {
            if(isError){
                redisClient.del(FLOW_SWITCH_FLAG);
            }
        }
    }

    /**
     * 查询当前流量在哪个机房
     * @return
     */
    public IdcVo getCurrIdcFlow(){
        if(! isBindable()){
            return new IdcVo("", "无");
        }

        //1.配置参数校验
        String domainNames = devopsProperties.getNetConfig().getDomains();
        DevopsProperties.BindType bindType = devopsProperties.getNetConfig().getBindType();
        if(StringUtil.isEmpty(domainNames)){
            throw new BizException(BizException.BIZ_INVALID, "未配置域名");
        }else if(bindType == null){
            throw new BizException(BizException.BIZ_INVALID, "未配置或配置了错误的网络绑定类型");
        }

        //2.取得绑定中且生效的ip列表
        List<String> bindingIpList = new ArrayList<>();
        List<String> domainNameList = Arrays.asList(domainNames.split(","));
        for (String domainName : domainNameList) {
            try {
                List<String> bindingIps = null;
                if(StringUtil.isEmpty(domainName)){
                    continue;
                }else if(DevopsProperties.BindType.WAF.equals(bindType)){ //通过waf进行IP绑定
                    List<String> wafInstances = getWafInstances();
                    bindingIps = domainBiz.queryWafDomainBindingIp(wafInstances, domainName);
                }else if(DevopsProperties.BindType.DCDN.equals(bindType)){ //通过DCDN全球全站加速进行IP绑定
                    bindingIps = domainBiz.queryDCDNDomainBindingIp(domainName);
                }else if(DevopsProperties.BindType.CDN.equals(bindType)){ //通过CDN进行IP绑定
                    bindingIps = domainBiz.queryCDNDomainBindingIp(domainName);
                }else if(DevopsProperties.BindType.DNS.equals(bindType)){ //通过DNS进行IP绑定
                    bindingIps = domainBiz.queryDnsDomainBindingIp(domainName);
                }
                if(bindingIps != null && bindingIps.size() > 0){
                    bindingIpList.addAll(bindingIps);
                }
            } catch(Exception e) {
                logger.error("机房流量查询异常 domainNames={} errorMsg={} ", domainNames, e.getMessage(), e.getCause());
                throw new BizException(BizException.BIZ_INVALID, "机房流量查询异常", e);
            }
        }

        if(bindingIpList.isEmpty()){
            throw new BizException(BizException.BIZ_INVALID, "当前配置的所有域名下都没有绑定任何ip");
        }

        //遍历每个机房的ip，判断其ip是否在bindingIpList中，如果在，说明当前机房有流量
        List<String> idcCodeList = new ArrayList<>();
        List<String> idcNameList = new ArrayList<>();
        List<DevopsProperties.Idc> idcList = getAllIdc();
        for(int i=0; i<idcList.size(); i++){
            DevopsProperties.Idc idc = idcList.get(i);
            String incomeIps = idc.getIncomeIps();
            if(StringUtil.isEmpty(incomeIps)){
                continue;
            }

            String[] incomeIpArr = idc.getIncomeIps().split(",");
            for(String ip : incomeIpArr){
                if(! bindingIpList.contains(ip)){
                    continue;
                }

                idcCodeList.add(idc.getCode());
                idcNameList.add(idc.getName());
                break;
            }
        }

        return new IdcVo(String.join(",", idcCodeList), String.join(",", idcNameList));
    }

    /**
     * 判断是否正在进行流量切换
     * @return
     */
    public boolean isFlowSwitching(){
        return redisClient.exists(FLOW_SWITCH_FLAG);
    }

    /**
     * 判断这个机房是否有流量
     * @param idcCode
     * @return
     */
    public boolean isIdcHasFlow(String idcCode){
        if(StringUtil.isEmpty(idcCode) || !isBindable()){
            return false;
        }
        IdcVo idcVo = getCurrIdcFlow();
        return idcVo.getCode().contains(idcCode);
    }

    /**
     * 判断idcCode和address这两个参数代表的值是否在同一个机房
     * @param idcCode   机房编码
     * @param address   地址,目前仅支持域名
     * @return
     */
    public boolean isInSameIdc(String idcCode, String address){
        if(StringUtil.isEmpty(idcCode) || StringUtil.isEmpty(address)){
            return false;
        }
        DevopsProperties.Idc idc = getIdcByCode(idcCode);
        if(idc == null || StringUtil.isEmpty(idc.getRegex())){
            return false;
        }
        Pattern pattern = Pattern.compile(idc.getRegex());
        Matcher matcher = pattern.matcher(address);
        return matcher.matches();
    }

    /**
     * 列出所有机房编号和名称
     * @return
     */
    public List<IdcVo> listAllIdc(){
        if(! isBindable()){
            return new ArrayList<>();
        }
        List<DevopsProperties.Idc> idcList = getAllIdc();
        List<IdcVo> idcVos = new ArrayList<>();
        for(DevopsProperties.Idc idc : idcList){
            IdcVo vo = BeanUtil.newAndCopy(idc, IdcVo.class);
            idcVos.add(vo);
        }
        return idcVos;
    }

    public boolean isBindable(){
        return devopsProperties.getNetConfig().getBindable();
    }

    private DevopsProperties.Idc getIdcByCode(String idcCode){
        if(StringUtil.isEmpty(idcCode)){
            return null;
        }
        List<DevopsProperties.Idc> idcList = getAllIdc();
        for(DevopsProperties.Idc idc : idcList){
            if(idcCode.equals(idc.getCode())){
                return idc;
            }
        }
        return null;
    }
    private List<DevopsProperties.Idc> getAllIdc(){
        List<DevopsProperties.Idc> idcList = devopsProperties.getNetConfig().getIdcList();
        List<DevopsProperties.Idc> idcs = new ArrayList<>();
        for(DevopsProperties.Idc idc : idcList){
            DevopsProperties.Idc idcTemp = new DevopsProperties.Idc();
            BeanUtil.copy(idc, idcTemp);
            idcs.add(idcTemp);
        }
        return idcs;
    }
    private List<String> getWafInstances(){
        String wafInstances = devopsProperties.getNetConfig().getWafInstances();
        if(StringUtil.isEmpty(wafInstances)){
            throw new RuntimeException("未配置waf实例id");
        }
        String[] wafInstanceArr = wafInstances.split(",");
        return Arrays.asList(wafInstanceArr);
    }
}
