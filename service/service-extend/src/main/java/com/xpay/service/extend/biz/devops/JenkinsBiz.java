package com.xpay.service.extend.biz.devops;

import com.cdancy.jenkins.rest.JenkinsClient;
import com.cdancy.jenkins.rest.domain.common.IntegerResponse;
import com.cdancy.jenkins.rest.domain.common.RequestStatus;
import com.cdancy.jenkins.rest.domain.job.BuildInfo;
import com.cdancy.jenkins.rest.domain.queue.QueueItem;
import com.xpay.common.statics.exception.BizException;
import com.xpay.common.utils.JsonUtil;
import com.xpay.common.utils.HttpUtil;
import com.xpay.common.utils.StringUtil;
import com.xpay.facade.extend.enums.BuildResultEnum;
import com.xpay.service.extend.conifg.DevopsProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * jenkins对接层
 * @author chenyf
 */
@Component
class JenkinsBiz {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    DevopsProperties devopsProperties;
    JenkinsClient client;

    public JenkinsBiz(DevopsProperties devopsProperties){
        this.devopsProperties = devopsProperties;
        String credential = devopsProperties.getJenkins().getCredential();
        credential = Base64.getEncoder().encodeToString(credential.getBytes(StandardCharsets.UTF_8));
        client = JenkinsClient.builder()
                .endPoint(devopsProperties.getJenkins().getUrl())
                .credentials(credential)
                .build();
    }

    /**
     * 触发jenkins中项目构建并发布项目
     * @param jobName       构建的任务名
     * @param buildMsg      构建描述
     * @param apps          需要部署的项目，多个项目用英文的逗号分割
     * @param idc           发布机房
     * @param version       镜像版本号
     * @param relayApp      中继项目名
     * @param notifyEmail   构建结果通知收件人
     * @param notifyUrl     构建结果回调通知地址
     * @return 返回jenkins中的队列Id，可通过此id查询到当前任务构建id，再通过构建id查询得到构建结果
     */
    public Integer buildAndPublish(String jobName, String buildMsg, String apps, String idc, String version,
                                   String relayApp, String notifyEmail, String notifyUrl) {
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("buildMsg", buildMsg);
        paramMap.put("apps", apps);
        paramMap.put("idc", idc != null ? idc : "");
        paramMap.put("version", version != null ? version : "");
        paramMap.put("relayApp", relayApp != null ? relayApp : "");
        paramMap.put("notifyEmail", notifyEmail != null ? notifyEmail : "");
        paramMap.put("notifyUrl", notifyUrl != null ? notifyUrl : "");
        IntegerResponse response = triggerJob(jobName, paramMap);
        Integer queueId = response.value();
        return queueId;
    }

    /**
     * 发布项目（通常用以机房代码同步）
     * @param toIdc     发布机房
     * @param publishMsg    发布描述
     * @return
     */
    public boolean publish(String toIdc, String publishMsg, List<String> packageNameList, String notifyUrl){
        String jobName = devopsProperties.getJenkins().getPublishJobName();
        if(StringUtil.isEmpty(jobName)){
            throw new BizException(BizException.BIZ_INVALID, "publishJobName参数未配置项目发布的Jenkins任务，无法执行");
        }

        String packageNames = String.join(",", packageNameList);
        Map<String, String> param = new HashMap<>();
        param.put("idc", toIdc);
        param.put("publishMsg", publishMsg);
        param.put("packageNames", packageNames);
        param.put("notifyUrl", notifyUrl == null ? "" : notifyUrl);

        IntegerResponse response = triggerJob(jobName, param);
        return response.value() != null && response.value() > 0;
    }

    /**
     * 根据queueId查询得到buildId
     * @param queueId   队列id
     * @param jobName   任务名
     * @return  返回内容 -1表示此任务已取消，0表示此任务正在排队中，其他大于0的值表示实际的构建id
     */
    public Integer getBuildIdByQueueId(Integer queueId, String jobName){
        //队列Id查询链接(此链接有失效时间的，jenkins默认5分钟)
        QueueItem queueItem = client.api().queueApi().queueItem(queueId);
        if (queueItem.cancelled()) {
            logger.info("任务队列已取消 queueId={} jobName={}", queueId, jobName);
            return -1;
        }else if(queueItem.executable() == null){
            logger.info("任务正在排队处理中 queueId={} jobName={}", queueId, jobName);
            return 0;
        }

        Integer buildId = queueItem.executable().number();
        logger.info("通过queueId查询到buildId了 queueId={} buildId={}", queueId, buildId);
        return buildId;
    }

    /**
     * 查询jenkins的中执行结果
     * @param buildId	查询的Id
     * @param jobName	任务名称
     * @return
     */
    public BuildResultEnum queryBuildResult(String jobName, Integer buildId) {
        //根据构建Id查询链接
        BuildInfo buildInfo = client.api().jobsApi().buildInfo(null, jobName, buildId);
        String result = buildInfo.result();
        if (StringUtil.isEmpty(result)) {
            logger.info("还未有最终执行结果：jobName={} buildId={}", jobName, buildId);
            return BuildResultEnum.PROCESSING;
        }
        logger.info("查询到执行结果：jobName={} buildId={} result={} ", jobName, buildId, result);
        return BuildResultEnum.getEnum(result);
    }

    /**
     * 取消jenkins中正在排队或构建的任务
     * @param jobName   任务名
     * @param queueId   队列id
     * @param buildId   构建id
     * @return
     */
    public boolean cancelJob(String jobName, Integer queueId, Integer buildId){
        if(buildId == null && queueId == null){
            logger.error("queueId和buildId都为null，无法取消任务 jobName={}", jobName);
            return false;
        }

        if(buildId == null && queueId != null){
            RequestStatus requestStatus = client.api().queueApi().cancel(queueId);

            if(requestStatus.value() != null && requestStatus.value()){
                return true;
            }else{
                //通过queueId取消失败，说明队列可能已经不存在，很有可能是已进入到build阶段，此时可以获取buildId，然后再通过buildId来取消
                logger.info("取消队列失败 queueId={} errorMsg={}", queueId, requestStatus.errors());
                buildId = getBuildIdByQueueId(queueId, jobName);
            }
        }

        if(buildId != null && buildId > 0){
            return stopBuildingJob(jobName, buildId);
        }
        return false;
    }

    /**
     * 触发jenkins中的任务
     * @param jobName   任务名
     * @param jobParam  任务参数
     * @return
     */
    private IntegerResponse triggerJob(String jobName, Map<String, String> jobParam) {
        Map<String, List<String>> params = new HashMap<>();
        for(Map.Entry<String, String> entry : jobParam.entrySet()){
            String value = entry.getValue();
            params.put(entry.getKey(), Collections.singletonList(value==null ? "" : value));
        }
        IntegerResponse response = client.api().jobsApi().buildWithParameters(null, jobName, params);
        if(response.value() == null){
            logger.error("请求触发任务的响应结果为：jobName={} error={} jobParam={}",  jobName,  response.errors(), JsonUtil.toJson(jobParam));
        }
        return response;
    }

    /**
     * 停止正在构建的任务
     * @param jobName   任务名
     * @param buildId   构建id
     * @return
     */
    private boolean stopBuildingJob(String jobName, Integer buildId){
        Map<String, String> header = new HashMap<>();
        addJenkinsCredentialHeader(header);

        String address = devopsProperties.getJenkins().getUrl() + "/job/" + jobName + "/" + buildId + "/stop";
        try{
            HttpUtil.Response response = HttpUtil.postFormSync(address, header, null);
            if(response.getHttpStatus() == 200){
                return true;
            }else{
                logger.info("任务取消失败 jobName={} buildId={} httpStatus={} header={} body={}", jobName, buildId,
                        response.getHttpStatus(), JsonUtil.toJson(response.getHeaders()), response.getBodyString());
                return false;
            }
        }catch(Exception e){
            logger.error("任务取消时出现异常 jobName={} buildId={}", jobName, buildId, e);
            throw new BizException(BizException.BIZ_INVALID, "取消任务时出现异常，请排查问题后重试！");
        }
    }

    /**
     * 添加请求jenkins的http header，用以jenkins鉴权
     * @param header    http请求头
     */
    private void addJenkinsCredentialHeader(Map<String, String> header){
        String address = devopsProperties.getJenkins().getUrl() + "/crumbIssuer/api/json";
        String credential = Base64.getEncoder().encodeToString(devopsProperties.getJenkins().getCredential().getBytes(StandardCharsets.UTF_8));

        header.put("Authorization", "Basic " + credential);
        String body;
        try{
            HttpUtil.Response response = HttpUtil.postJsonSync(address, header, JsonUtil.toJson(new HashMap<>()));//获取访问的token
            body = response.getBodyString();
        }catch(Exception e){
            logger.error("请求鉴权token失败 JenkinsErrMsg = {}", e.getMessage());
            throw new BizException(BizException.BIZ_INVALID, e.getMessage());
        }
        HashMap<String, String> respMap = JsonUtil.toBean(body, HashMap.class);
        header.put("Jenkins-Crumb", respMap.get("crumb"));
    }
}
