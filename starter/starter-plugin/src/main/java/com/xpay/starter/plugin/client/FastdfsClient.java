package com.xpay.starter.plugin.client;

import com.xpay.libs.fdfs.common.MyException;
import com.xpay.libs.fdfs.common.NameValuePair;
import com.xpay.libs.fdfs.fasfdfs.*;
import com.xpay.starter.plugin.properties.FastdfsProperties;
import com.xpay.starter.plugin.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Stream;

/**
 * Description: fastdfs文件客户端，请使用该类对fastdfs进行操作，在StorageClient类中的do_upload_file、download_file等方法中会自动获取Connection，并在使用完毕之后自动释放
 * @author chenyf
 */
public class FastdfsClient {
    private static final String ORIGINAL_FILE_NAME = "ORIGINAL_FILE_NAME";
    private Logger logger = LoggerFactory.getLogger(FastdfsClient.class);
    private String nginxUrl;

    public FastdfsClient(FastdfsProperties fastdfsProperties){
        if(Utils.isNotEmpty(fastdfsProperties.getNginxHost())){
            String host = fastdfsProperties.getNginxHost();
            if(!host.startsWith("http://") && !host.startsWith("https://")){
                host = "http://" + host;
            }
            if(! host.endsWith("/")){
                host = host + "/";
            }
            this.nginxUrl = host;
        }
        initClientGlobal(fastdfsProperties);
    }

    /**
     * 上传文件
     * @param localFile         本地文件全路径
     * @param originalFileName  源文件名
     * @return
     */
    public String uploadFile(String localFile, String originalFileName) {
        StorageClient1 storageClient1 = null;
        try {
            storageClient1 = getStorageClient();
            return storageClient1.upload_file1(localFile, getFileExtension(originalFileName), new NameValuePair[]{new NameValuePair(ORIGINAL_FILE_NAME, originalFileName)});
        } catch (Exception e) {
            logger.info("上传到文件服务器失败, localFileName={}, originalFileName={}", localFile, originalFileName, e);
            throw new RuntimeException("上传到文件服务器失败");
        } finally {
            closeStorageClient(storageClient1);
        }
    }

    /**
     * 上传文件
     * @param fileBytes         文件字节数组
     * @param originalFileName  源文件名
     * @return
     */
    public String uploadFile(byte[] fileBytes, String originalFileName) {
        StorageClient1 storageClient1 = null;
        try {
            storageClient1 = getStorageClient();
            return storageClient1.upload_file1(fileBytes, getFileExtension(originalFileName), new NameValuePair[]{new NameValuePair(ORIGINAL_FILE_NAME, originalFileName)});
        } catch (Exception e) {
            logger.error("上传到文件服务器失败, originalFileName={}", originalFileName, e);
            throw new RuntimeException("上传到文件服务器失败");
        } finally {
            closeStorageClient(storageClient1);
        }
    }

    /**
     * 下载文件
     * @param fastdfsFile    在fastdfs中的文件名
     * @return 返回一个流
     */
    public InputStream downloadAsStream(String fastdfsFile) {
        StorageClient1 storageClient1 = null;
        try {
            storageClient1 = getStorageClient();
            byte[] bytes = storageClient1.download_file1(fastdfsFile);
            return new ByteArrayInputStream(bytes);
        } catch (Exception ex) {
            logger.error("从文件服务器下载失败", ex);
            throw new RuntimeException("从文件服务器下载失败");
        } finally {
            closeStorageClient(storageClient1);
        }
    }

    /**
     * 下载文件
     * @param fastdfsFile    在fastdfs中的文件名
     * @return 返回一个字节数组
     */
    public byte[] downloadAsByte(String fastdfsFile) {
        StorageClient1 storageClient1 = null;
        try {
            storageClient1 = getStorageClient();
            byte[] bytes = storageClient1.download_file1(fastdfsFile);
            return bytes;
        } catch (Exception ex) {
            logger.error("从文件服务器下载失败", ex);
            throw new RuntimeException("从文件服务器下载失败");
        } finally {
            closeStorageClient(storageClient1);
        }
    }

    /**
     * 通过nginx访问下载文件，文件一上传完毕，同组内的服务端之间有可能还未完成数据同步，如果此时立马就需要下载文件，会有可能访问不到文件，这时就需要通过nginx来重定向到上传时的那台机器才能访问到
     * @param fastdfsFile
     * @return
     */
    public byte[] downloadAsByteFromNginx(String fastdfsFile){
        if(Utils.isEmpty(nginxUrl)){
            throw new RuntimeException("nginx访问地址为空，无法下载！");
        }
        
        try {
            byte[] fileBytes = getFileFromNetUrl(nginxUrl + fastdfsFile);
            return fileBytes;
        } catch(Exception e) {
            logger.error("http方式从文件服务器下载失败", e);
            throw new RuntimeException("http方式从文件服务器下载失败");
        }
    }

    /**
     * 获取 accessString，附加到url后面，此方法的返回结果为：token=*****&ts=*****
     * @param fastdfsFile fastdfs文件id
     * @return
     */
    public String genAccessString(String fastdfsFile) {
        try {
            long epochSecond = Instant.now().getEpochSecond();
            String token = ProtoCommon.getToken(fastdfsFile.substring(fastdfsFile.indexOf("/") + 1), (int) epochSecond, ClientGlobal.getG_secret_key());
            return "token=" + token + "&ts=" + epochSecond;
        } catch (Exception ex) {
            logger.error("生成token失败, fastdfsfile={}", fastdfsFile, ex);
            throw new RuntimeException("生成token失败");
        }
    }

    /**
     * 获取源文件名
     * @param fastdfsFile fastdfs文件id
     * @return 源文件名
     */
    public String getOriginalFileName(String fastdfsFile) {
        StorageClient1 storageClient1 = null;
        try {
            storageClient1 = getStorageClient();
            NameValuePair[] metadata1 = storageClient1.get_metadata1(fastdfsFile);
            if (metadata1 == null) {
                return null;
            }
            Optional<NameValuePair> first = Stream.of(metadata1).filter(p -> Objects.equals(p.getName(), ORIGINAL_FILE_NAME)).findFirst();
            return first.map(NameValuePair::getValue).orElse(null);
        } catch (Exception e) {
            logger.error("获取源文件名失败, fastdfsFile={}", fastdfsFile, e);
            throw new RuntimeException("获取源文件名失败");
        } finally {
            closeStorageClient(storageClient1);
        }
    }

    /**
     * 删除文件
     * @param fastdfsFile
     * @return
     */
    public boolean deleteFile(String fastdfsFile){
        StorageClient1 storageClient1 = null;
        try {
            storageClient1 = getStorageClient();
            FileInfo fileInfo = storageClient1.query_file_info1(fastdfsFile);
            if(fileInfo == null){//文件不存在，直接认为删除成功
                return true;
            }

            int code = storageClient1.delete_file1(fastdfsFile);
            if(code == 0){ // 0表示删除成功
                return true;
            }else{
                logger.error("文件删除失败, fastdfsFile={} errCode={}", fastdfsFile, code);
                return false;
            }
        } catch (Exception e) {
            logger.error("文件删除失败, fastdfsFile={}", fastdfsFile, e);
            throw new RuntimeException("文件删除失败");
        } finally {
            closeStorageClient(storageClient1);
        }
    }

    /**
     * 查询文件信息
     * @param fastdfsFile
     * @return
     */
    public FileInfo queryFile(String fastdfsFile){
        StorageClient1 storageClient1 = null;
        try {
            storageClient1 = getStorageClient();
            FileInfo fileInfo = storageClient1.query_file_info1(fastdfsFile);
            return fileInfo; //fileInfo == null 时表示文件不存在
        } catch (Exception e) {
            logger.error("文件查询失败, fastdfsFile={}", fastdfsFile, e);
            throw new RuntimeException("文件查询失败");
        } finally {
            closeStorageClient(storageClient1);
        }
    }

    /**
     * 获取StorageServer的客户端，用以上传、下载文件
     * @return
     * @throws IOException
     * @throws MyException
     */
    private StorageClient1 getStorageClient() throws IOException, MyException {
        TrackerClient trackerClient = new TrackerClient();
        TrackerServer trackerServer = trackerClient.getTrackerServer();
        StorageServer storageServer = trackerClient.getStoreStorage(trackerServer);
        return new StorageClient1(trackerServer, storageServer);
        //TrackerServer与服务端的连接、StorageServer与服务端的连接，已放到各个类中的相关方法中去释放资源了，实际上是使用了ConnectionPool来复用Connection连接对象
    }

    /**
     * 获取文件后缀名
     * @param fileName
     * @return
     */
    private String getFileExtension(String fileName) {
        if (fileName == null || fileName.trim().equals("")) {
            return "";
        }
        int i = fileName.lastIndexOf(".");
        if (i > 0) {
            return fileName.substring(i + 1);
        } else {
            return "";
        }
    }

    private byte[] getFileFromNetUrl(String fileUrl) throws Exception {
        if (fileUrl == null || fileUrl.trim().length() <= 0){
            return null;
        }

        InputStream input = null;
        ByteArrayOutputStream output = null;
        HttpURLConnection conn = null;
        try {
            URL url = new URL(fileUrl);
            conn = (HttpURLConnection) url.openConnection();
            conn.connect();
            input = conn.getInputStream();
            output = new ByteArrayOutputStream();
            byte[] buf = new byte[1024];
            int numBytesRead ;
            while ((numBytesRead = input.read(buf)) != -1) {
                output.write(buf, 0, numBytesRead);
            }
            return output.toByteArray();
        } finally {
            if (output != null) {
                try{
                    output.close();
                } catch(Exception e) {
                }
            }
            if (input != null) {
                try {
                    input.close();
                } catch (Exception e) {
                }
            }
            if (conn != null) {
                try{
                    conn.disconnect();
                } catch(Exception e) {
                }
            }
        }
    }



    private void closeStorageClient(StorageClient1 client){
        if (client != null) {
            try {
                client.close();
            } catch (Exception ignored) {

            }
        }
    }

    private void initClientGlobal(FastdfsProperties fastdfsProperties) {
        if(Utils.isEmpty(fastdfsProperties.getTrackerServers())){
            throw new RuntimeException("fastdfs.trackerServers不能为空");
        }

        try{
            Properties properties = new Properties();
            properties.setProperty(ClientGlobal.PROP_KEY_CONNECT_TIMEOUT_IN_SECONDS, fastdfsProperties.getConnectTimeoutInSeconds() + "");
            properties.setProperty(ClientGlobal.PROP_KEY_TRACKER_SERVERS, fastdfsProperties.getTrackerServers());
            properties.setProperty(ClientGlobal.PROP_KEY_NETWORK_TIMEOUT_IN_SECONDS, fastdfsProperties.getNetworkTimeoutInSeconds() + "");
            properties.setProperty(ClientGlobal.PROP_KEY_CHARSET, fastdfsProperties.getCharset());
            properties.setProperty(ClientGlobal.PROP_KEY_HTTP_ANTI_STEAL_TOKEN, fastdfsProperties.isHttpAntiStealToken() + "");
            properties.setProperty(ClientGlobal.PROP_KEY_HTTP_SECRET_KEY, fastdfsProperties.getHttpSecretKey());
            properties.setProperty(ClientGlobal.PROP_KEY_HTTP_TRACKER_HTTP_PORT, fastdfsProperties.getHttpTrackerHttpPort() + "");

            properties.setProperty(ClientGlobal.PROP_KEY_CONNECTION_POOL_ENABLED, fastdfsProperties.getPool().getEnabled() + "");
            properties.setProperty(ClientGlobal.PROP_KEY_CONNECTION_POOL_MAX_COUNT_PER_ENTRY, fastdfsProperties.getPool().getMaxCountPerEntry() + "");
            properties.setProperty(ClientGlobal.PROP_KEY_CONNECTION_POOL_MAX_IDLE_TIME, fastdfsProperties.getPool().getMaxIdleTime() + "");
            properties.setProperty(ClientGlobal.PROP_KEY_CONNECTION_POOL_MAX_WAIT_TIME_IN_MS, fastdfsProperties.getPool().getMaxWaitTimeInMs() + "");

            ClientGlobal.initByProperties(properties);
        }catch(Exception e){
            throw new RuntimeException("初始化fastdfs异常 ", e);
        }
    }
}
