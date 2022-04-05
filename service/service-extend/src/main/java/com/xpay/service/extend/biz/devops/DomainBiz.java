package com.xpay.service.extend.biz.devops;

import com.aliyun.teaopenapi.models.Config;
import com.aliyun.waf_openapi20190910.Client;
import com.aliyun.waf_openapi20190910.models.*;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.alidns.model.v20150109.*;
import com.aliyuncs.cdn.model.v20180510.DescribeCdnDomainDetailRequest;
import com.aliyuncs.cdn.model.v20180510.DescribeCdnDomainDetailResponse;
import com.aliyuncs.cdn.model.v20180510.ModifyCdnDomainRequest;
import com.aliyuncs.cdn.model.v20180510.ModifyCdnDomainResponse;
import com.aliyuncs.dcdn.model.v20180115.DescribeDcdnDomainDetailRequest;
import com.aliyuncs.dcdn.model.v20180115.DescribeDcdnDomainDetailResponse;
import com.aliyuncs.dcdn.model.v20180115.UpdateDcdnDomainRequest;
import com.aliyuncs.dcdn.model.v20180115.UpdateDcdnDomainResponse;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.http.FormatType;
import com.aliyuncs.profile.DefaultProfile;
import com.aliyuncs.profile.IClientProfile;
import com.xpay.common.utils.JsonUtil;
import com.xpay.common.utils.StringUtil;
import com.xpay.service.extend.conifg.DevopsProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 域名解析处理层
 * @author chenyf
 */
@Component
class DomainBiz {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private IAcsClient acsClient;
    private Client wafClient;

    private String dnsEnableStatus = "Enable";
    private String dnsDisableStatus = "Disable";

    public DomainBiz(DevopsProperties devopsProperties){
        initial(devopsProperties.getNetConfig().getAccessKey(), devopsProperties.getNetConfig().getSecretKey());
    }

    /**
     * 修改DNS的域名和IP绑定(即修改解析记录)
     * @param domainName    域名
     * @param bindIpList    跟域名进行绑定的ip列表
     */
    public void modifyDnsDomainBindingIp(String domainName, List<String> bindIpList){
        //1.先查询出当前主域名下的所有解析记录
        List<DescribeDomainRecordsResponse.Record> list = null;
        try {
            DescribeDomainRecordsRequest request = new DescribeDomainRecordsRequest();
            request.setDomainName(domainName);
            request.setAcceptFormat(FormatType.JSON); //指定api返回格式
            DescribeDomainRecordsResponse response = acsClient.getAcsResponse(request);
            list = response.getDomainRecords();
        } catch (ClientException e){
            logger.error("修改dns域名绑定记录时查询解析记录异常 domainName={} errorCode={} errorMsg={}", domainName, e.getErrCode(), e.getErrMsg(), e);
            throw new RuntimeException("修改dns域名绑定记录时查询解析记录异常，请检查相关配置！");
        } catch (Exception e) {
            throw new RuntimeException("修改dns域名绑定记录时查询解析记录异常 domainName="+domainName, e);
        }

        //2.从所有解析记录中分离出需要启用和禁用的记录
        List<String> needEnableRecordList = new ArrayList<>();
        List<String> needDisableRecordList = new ArrayList<>();
        for (DescribeDomainRecordsResponse.Record record : list) {
            String ip = record.getValue();

            if(dnsEnableStatus.equalsIgnoreCase(record.getStatus()) && ! bindIpList.contains(ip)){
                //2.1 处于Enable状态且不在ipList中的解析记录，需要禁用
                needDisableRecordList.add(record.getRecordId());
            }else if(dnsDisableStatus.equalsIgnoreCase(record.getStatus()) && bindIpList.contains(ip)){
                //2.2 处于Disable状态且在ipList中的解析记录，需要启用
                needEnableRecordList.add(record.getRecordId());
            }
        }

        //3.1 把应该启用的解析记录置为启用状态
        if(needEnableRecordList.size() > 0){
            for (String recordId : needEnableRecordList) {
                SetDomainRecordStatusRequest request = new SetDomainRecordStatusRequest();
                request.setRecordId(recordId);
                request.setStatus(dnsEnableStatus);

                boolean isSuccess;
                try{
                    SetDomainRecordStatusResponse response = acsClient.getAcsResponse(request);
                    isSuccess = dnsEnableStatus.equalsIgnoreCase(response.getStatus());
                } catch (ClientException e){
                    logger.error("启用dns域名绑定记录时出现异常 domainName={} errorCode={} errorMsg={}", domainName, e.getErrCode(), e.getErrMsg(), e);
                    throw new RuntimeException("启用dns域名绑定记录时出现异常，请检查相关配置！");
                } catch(Exception e) {
                    throw new RuntimeException("启用dns域名绑定记录时出现异常 domainName="+domainName+",recordId="+recordId, e);
                }

                if(!isSuccess){
                    throw new RuntimeException("dns域名绑定记录启用失败 domainName="+domainName+",recordId="+recordId);
                }
            }
        }

        //3.2 把应该禁用的解析记录置为禁用状态
        if(needDisableRecordList.size() > 0){
            for (String recordId : needDisableRecordList) {
                SetDomainRecordStatusRequest request = new SetDomainRecordStatusRequest();
                request.setRecordId(recordId);
                request.setStatus(dnsDisableStatus);

                boolean isSuccess;
                try{
                    SetDomainRecordStatusResponse response = acsClient.getAcsResponse(request);
                    isSuccess = dnsDisableStatus.equalsIgnoreCase(response.getStatus());
                } catch (ClientException e){
                    logger.error("禁用dns域名绑定记录时出现异常 domainName={} errorCode={} errorMsg={}", domainName, e.getErrCode(), e.getErrMsg(), e);
                    throw new RuntimeException("禁用dns域名绑定记录时出现异常，请检查相关配置！");
                } catch(Exception e){
                    throw new RuntimeException("禁用dns域名绑定记录时出现异常 domainName="+domainName+",recordId="+recordId, e);
                }

                if(!isSuccess){
                    throw new RuntimeException("dns域名绑定记录禁用失败 domainName="+domainName+",recordId="+recordId);
                }
            }
        }
    }

    /**
     * 查询处于启用状态下的DNS的域名和IP绑定(即查询处于启用状态的解析记录)
     * @param domainName
     * @return
     */
    public List<String> queryDnsDomainBindingIp(String domainName){
        DescribeDomainRecordsRequest request = new DescribeDomainRecordsRequest();
        request.setDomainName(domainName);
        request.setAcceptFormat(FormatType.JSON); //指定api返回格式

        List<String> bindingIpList = new ArrayList<>();
        try {
            DescribeDomainRecordsResponse response = acsClient.getAcsResponse(request);
            List<DescribeDomainRecordsResponse.Record> list = response.getDomainRecords();
            for (DescribeDomainRecordsResponse.Record record : list) {
                if(dnsEnableStatus.equalsIgnoreCase(record.getStatus())){
                    bindingIpList.add(record.getValue());
                }
            }
        } catch (ClientException e){
            logger.error("查询dns域名绑定记录时出现异常 domainName={} errorCode={} errorMsg={}", domainName, e.getErrCode(), e.getErrMsg(), e);
            throw new RuntimeException("查询dns域名绑定记录时出现异常，请检查相关配置！");
        } catch (Exception e) {
            throw new RuntimeException("查询dns域名绑定记录时出现异常 domainName="+domainName, e);
        }
        return bindingIpList;
    }

    /**
     * 修改CDN中域名和IP绑定关系
     * @param domainName
     * @param bindIpList
     */
    public void modifyCDNDomainBindingIp(String domainName, List<String> bindIpList){
        List<Map<String, String>> sourceList = new ArrayList<>();
        for(String bindIp : bindIpList){
            Map<String, String> source = new HashMap<>();
            source.put("type", "ipaddr");
            source.put("content", bindIp);
            sourceList.add(source);
        }

        String source = JsonUtil.toJson(sourceList);
        ModifyCdnDomainRequest request = new ModifyCdnDomainRequest();
        request.setDomainName(domainName);
        request.setSources(source);

        try {
            ModifyCdnDomainResponse response = acsClient.getAcsResponse(request);
        } catch (ClientException e) {
            logger.error("修改CDN的域名和ip绑定时异常 domainName={} bindIpList={} errorCode={} errorMsg={} ", domainName, JsonUtil.toJson(bindIpList), e.getErrCode(), e.getErrMsg(), e);
            throw new RuntimeException("修改CDN的域名和ip绑定时异常 domainName="+domainName, e);
        } catch (Exception e) {
            throw new RuntimeException("修改CDN的域名和ip绑定时异常 domainName="+domainName, e);
        }
    }

    /**
     * 查询CDN域名和IP绑定关系
     * @param domainName
     * @return
     */
    public List<String> queryCDNDomainBindingIp(String domainName){
        DescribeCdnDomainDetailRequest request = new DescribeCdnDomainDetailRequest();
        request.setDomainName(domainName);

        List<String> bindingIpList = new ArrayList<>();
        try {
            DescribeCdnDomainDetailResponse response = acsClient.getAcsResponse(request);
            String domainStatus = response.getGetDomainDetailModel().getDomainStatus();
            if(!"online".equals(domainStatus) && !"configuring".equals(domainStatus)){//启用、配置中 状态的才有效
                return bindingIpList;
            }

            List<DescribeCdnDomainDetailResponse.GetDomainDetailModel.SourceModel> sources = response.getGetDomainDetailModel().getSourceModels();
            if(sources == null || sources.isEmpty()){
                return bindingIpList;
            }
            for(DescribeCdnDomainDetailResponse.GetDomainDetailModel.SourceModel source : sources){
                if("ipaddr".equals(source.getType())){
                    bindingIpList.add(source.getContent());
                }
            }
        } catch (ClientException e) {
            logger.error("查询CDN的域名和ip绑定关系时出现异常 domainName={} errorCode={} errorMsg={} ", domainName, e.getErrCode(), e.getErrMsg(), e);
            throw new RuntimeException("查询CDN的域名和ip绑定关系时出现异常 domainName="+domainName, e);
        } catch (Exception e) {
            throw new RuntimeException("查询CDN的域名和ip绑定关系时出现异常 domainName="+domainName, e);
        }
        return bindingIpList;
    }

    /**
     * 修改全球全站加速的域名和IP绑定关系
     * @param domainName
     * @param bindIpList
     */
    public void modifyDCDNDomainBindingIp(String domainName, List<String> bindIpList){
        List<Map<String, String>> sourceList = new ArrayList<>();
        for(String bindIp : bindIpList){
            Map<String, String> source = new HashMap<>();
            source.put("type", "ipaddr");
            source.put("content", bindIp);
            sourceList.add(source);
        }

        String source = JsonUtil.toJson(sourceList);
        UpdateDcdnDomainRequest request = new UpdateDcdnDomainRequest();
        request.setDomainName(domainName);
        request.setSources(source);

        try {
            UpdateDcdnDomainResponse response = acsClient.getAcsResponse(request);
        }catch (ClientException e) {
            logger.error("修改DCDN的域名和ip绑定时异常 domainName={} errorCode={} errorMsg={} ", domainName, e.getErrCode(), e.getErrMsg(), e);
            throw new RuntimeException("修改DCDN的域名和ip绑定时异常 domainName="+domainName, e);
        } catch (Exception e) {
            throw new RuntimeException("修改DCDN的域名和ip绑定时异常 domainName="+domainName, e);
        }
    }

    /**
     * 查询全球全站加速的域名和IP绑定关系
     * @param domainName
     * @return
     */
    public List<String> queryDCDNDomainBindingIp(String domainName){
        DescribeDcdnDomainDetailRequest request = new DescribeDcdnDomainDetailRequest();
        request.setDomainName(domainName);

        List<String> bindingIpList = new ArrayList<>();
        try {
            DescribeDcdnDomainDetailResponse response = acsClient.getAcsResponse(request);
            String domainStatus = response.getDomainDetail().getDomainStatus();
            if(!"online".equals(domainStatus) && !"configuring".equals(domainStatus)){//启用、配置中 状态的才有效
                return bindingIpList;
            }

            List<DescribeDcdnDomainDetailResponse.DomainDetail.Source> sources = response.getDomainDetail().getSources();
            if(sources == null || sources.isEmpty()){
                return bindingIpList;
            }
            for(DescribeDcdnDomainDetailResponse.DomainDetail.Source source : sources){
                if("ipaddr".equals(source.getType())){
                    bindingIpList.add(source.getContent());
                }
            }
        } catch (ClientException e) {
            logger.error("查询DCDN的域名和ip绑定关系时出现异常 domainName={} errorCode={} errorMsg={} ", domainName, e.getErrCode(), e.getErrMsg(), e);
            throw new RuntimeException("查询DCDN的域名和ip绑定关系时出现异常 domainName="+domainName, e);
        } catch (Exception e) {
            throw new RuntimeException("查询DCDN的域名和ip绑定关系时出现异常 domainName="+domainName, e);
        }
        return bindingIpList;
    }

    /**
     * 修改waf的域名和ip绑定
     * 注意：阿里云的waf防火墙服务，在比较普通的版本上，有IP绑定数量的限制，一个域名只允许绑定一个IP
     *
     * @param domainName    域名
     * @param bindIpList    跟域名进行绑定的ip列表
     */
    public void modifyWafDomainBindingIp(List<String> wafInstances, String domainName, List<String> bindIpList) {
        for(String wafInstanceId : wafInstances){
            String bindIps = JsonUtil.toJson(bindIpList);

            ModifyDomainRequest request = new ModifyDomainRequest();
            request.setDomain(domainName);
            request.setIsAccessProduct(Integer.valueOf(0));//客户端访问流量到WAF前是否有经过其他七层代理转发(例如高防、CDN等)：0=否 1=是
            request.setInstanceId(wafInstanceId);
            request.setSourceIps(bindIps);
            request.setAccessType("waf-cloud-dns");
            request.setHttpPort("[80]");//http协议的端口
            request.setHttpsPort("[443]");//https协议的端口
            try {
                ModifyDomainResponse response = wafClient.modifyDomain(request);
                logger.info("header={}", JsonUtil.toJson(response.getHeaders()));
            } catch (Exception e) {
                throw new RuntimeException("修改waf中的域名和ip绑定时异常 domainName="+domainName+", wafInstanceId="+wafInstanceId, e);
            }
        }
    }

    /**
     * 查询waf的域名和ip绑定
     * @param domainName
     * @return
     */
    public List<String> queryWafDomainBindingIp(List<String> wafInstances, String domainName) {
        List<String> bindingIpList = new ArrayList<>();
        for (String wafInstanceId : wafInstances) {
            DescribeDomainRequest request = new DescribeDomainRequest();
            request.setDomain(domainName);
            request.setInstanceId(wafInstanceId);

            try{
                DescribeDomainResponse response = wafClient.describeDomain(request);
                if(response.getBody().getDomain() != null){
                    List<String> sourceIps = response.getBody().getDomain().getSourceIps();
                    if(sourceIps != null && sourceIps.size() > 0){
                        bindingIpList.addAll(sourceIps);
                    }
                }
            }catch (Exception e) {
                throw new RuntimeException("查询waf的ip绑定关系时出现异常 domainName="+domainName+",wafInstanceId="+wafInstanceId, e);
            }
        }
        return bindingIpList;
    }

    private void initial(String accessKeyId, String accessKeySecret){
        if(StringUtil.isEmpty(accessKeyId) || StringUtil.isEmpty(accessKeySecret)){
            logger.warn("---------- accessKeyId或accessKeySecret为空，将不进行客户端初始化！");
            return;
        }
        //1.初始化dns客户端
        String regionId = "cn-hangzhou"; //必填固定值，必须为“cn-hanghou”
        IClientProfile profile = DefaultProfile.getProfile(regionId, accessKeyId, accessKeySecret);
        // 若报Can not find endpoint to access异常，请添加以下此行代码
        // DefaultProfile.addEndpoint("cn-hangzhou", "cn-hangzhou", "Alidns", "alidns.aliyuncs.com");
        acsClient = new DefaultAcsClient(profile);

        //2.初始化waf客户端
        Config config = new Config()
                .setAccessKeyId(accessKeyId)
                .setAccessKeySecret(accessKeySecret)
                // 访问的域名
                .setEndpoint("wafopenapi.cn-hangzhou.aliyuncs.com");
        try{
            wafClient = new Client(config);
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }
}
