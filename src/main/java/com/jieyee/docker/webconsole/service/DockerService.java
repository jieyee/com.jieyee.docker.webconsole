package com.jieyee.docker.webconsole.service;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jieyee.docker.webconsole.constants.Constants;

/**
 * 访问docker engine的服务.
 * 
 * @author jieyee
 *
 */
@Component
@ConfigurationProperties
public class DockerService {
    private static final Logger logger = LoggerFactory.getLogger(DockerService.class);
    
    @Value("${platform.dockerpath:/usr/local/bin/docker -H }")
    private String dockerpath;
    
    private Map<String, String> projectMap;

    /**
     * @return the projectMap
     */
    public Map<String, String> getProjectMap() {
        return projectMap;
    }

    /**
     * @param projectMap the projectMap to set
     */
    public void setProjectMap(Map<String, String> projectMap) {
        this.projectMap = projectMap;
    }

    /**
     * 获取镜像json.
     * @param httpClient
     * @param host
     * @param image
     * @return
     * @throws Exception
     */
    public JSONObject inspectImage(final CloseableHttpClient httpClient, final String host, final String image) throws Exception {
        JSONObject retObject = new JSONObject();
        String retMsg = "error";
        retObject.put("retMsg", retMsg);
        final String url = "http://" + host + "/images/" + image + "/json";
        logger.info(url);
        HttpGet httpGet = new HttpGet(url);
        CloseableHttpResponse response = httpClient.execute(httpGet);
        StatusLine statusLine = response.getStatusLine();
        if (statusLine.getStatusCode() == 200) {
            // nothing to do.
            String msg = EntityUtils.toString(response.getEntity());
            JSONObject parseObject = JSONObject.parseObject(msg);
            retObject.put("retMsg", Constants.RESTFUL_SUCCESS);
            retObject.put("result", parseObject);
        } else {
            retMsg = "inspect 镜像出错.";
        }
        return retObject;
    }

    public JSONObject buildImage(final CloseableHttpClient httpClient, final String host, final String nameWithTag,
            final String targzFileName) throws Exception {
        String retMsg = "error";
        JSONObject retObject = new JSONObject();
        retObject.put("retMsg", retMsg);
        
        HttpPost httpPost = new HttpPost("http://" + host + "/build?t=" + nameWithTag);  
        
        InputStreamEntity reqEntity = new InputStreamEntity(new FileInputStream(targzFileName), -1, ContentType.APPLICATION_OCTET_STREAM);
        reqEntity.setChunked(true);
        httpPost.setEntity(reqEntity);
        
        System.out.println("Executing request: " + httpPost.getRequestLine());
        CloseableHttpResponse response = httpClient.execute(httpPost);
        System.out.println("----------------------------------------");
        String msg = EntityUtils.toString(response.getEntity());
        logger.info(msg);
        int errIndex = msg.indexOf("{\"errorDetail\"");
        if (errIndex == -1) {
            retObject.put("retMsg", "success");
        } else {
            retMsg = msg.substring(errIndex);
            logger.info(retMsg);
            retObject.put("retMsg", retMsg);
        }
        return retObject;
    }

    /**
     * 拉取镜像.
     * 
     * @param httpclient
     * @param host
     *            localhost:2375
     * @param registry
     *            localhost:5000
     * @param image
     *            imagename
     * @return
     */
    public JSONObject pullImage(final CloseableHttpClient httpclient, final String host, final String registry,
            final String image) throws Exception {
        String retMsg = "error";
        JSONObject retObject = new JSONObject();
        retObject.put("retMsg", retMsg);
        // pull image.
        final String url = "http://" + host + "/images/create?fromImage=" + registry + "/" + image;
        logger.info(url);
        HttpPost httpPost = new HttpPost(url);
        httpPost.addHeader("X-Registry-Auth", "null");
        CloseableHttpResponse response = httpclient.execute(httpPost);
        StatusLine statusLine = response.getStatusLine();
        if (statusLine.getStatusCode() == 200) {
            retObject.put("retMsg", Constants.RESTFUL_SUCCESS);
            retObject.put("image", registry + "/" + image);
        } else {
            retMsg = "下载镜像出错:" + statusLine.getReasonPhrase();
        }
        return retObject;
    }

    /**
     * 推镜像.
     * 
     * @param httpClient
     * @param host
     * @param image
     *            image name without tag
     * @param tag
     * @return
     * @throws Exception
     */
    public String pushImage(final CloseableHttpClient httpClient, final String host, final String image,
            final String tag) throws Exception {
        String retMsg = "success";
        // pull image.
        final String url = "http://" + host + "/images/" + image + "/push";
        logger.info(url);
        HttpPost httpPost = new HttpPost(url);
        List<NameValuePair> formparams = new ArrayList<NameValuePair>();
        formparams.add(new BasicNameValuePair("tag", tag));
        UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams, Consts.UTF_8);
        httpPost.setEntity(entity);
        httpPost.addHeader("X-Registry-Auth", "null");
        CloseableHttpResponse httpResponse = httpClient.execute(httpPost);
        StatusLine statusLine = httpResponse.getStatusLine();
        if (statusLine.getStatusCode() == 200) {
            // nothing to do.
        } else {
            retMsg = "推镜像出错:" + statusLine.getReasonPhrase();
        }
        return retMsg;
    }

    /**
     * 显示镜像.
     * 
     * @param httpClient
     * @param host
     * @return
     * @throws Exception
     */
    public JSONObject listImages(final CloseableHttpClient httpClient, final String host) throws Exception {
        String url = "http://" + host + "/images/json?all=1";
        logger.info(url);
        HttpGet httpGet = new HttpGet(url);
        CloseableHttpResponse httpResponse = httpClient.execute(httpGet);
        HttpEntity entity = httpResponse.getEntity();
        JSONArray oldArray = JSONArray.parseArray(EntityUtils.toString(entity));
        JSONArray newArray = new JSONArray();
        for (int i = 0; i < oldArray.size(); i++) {
            JSONArray names = oldArray.getJSONObject(i).getJSONArray("RepoTags");
            for (int i_name = 0; i_name < names.size(); i_name++) {
                JSONObject subJsonObject = new JSONObject();
                String name = names.getString(i_name);
                // 隐藏2个容器.
                if (name.indexOf("cadvisor") != -1 || name.indexOf("registry") != -1) {
                    continue;
                }
                subJsonObject.put("Names", name.substring(0, name.lastIndexOf(":")));
                subJsonObject.put("Tag", name.substring(name.lastIndexOf(":") + 1, name.length()));
                subJsonObject.put("Id", oldArray.getJSONObject(i).get("Id"));
                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                subJsonObject.put("Created",
                        df.format(new Date(oldArray.getJSONObject(i).getLongValue("Created") * 1000)));
                subJsonObject.put("VirtualSize",
                        ByteConversionGBMBKB(oldArray.getJSONObject(i).getFloat("VirtualSize")));
                newArray.add(subJsonObject);
            }
        }
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("data", newArray);
        return jsonObject;
    }

    /**
     * 删除镜像.
     * 
     * @param httpClient
     * @param host
     * @param name
     * @return
     * @throws Exception
     */
    public String deleteImage(final CloseableHttpClient httpClient, final String host, final String name)
            throws Exception {
        String url = "http://" + host + "/images/" + name + "?force=true";
        // 强制删除.
        logger.info(url);
        HttpDelete httpDelete = new HttpDelete(url);
        CloseableHttpResponse httpResponse = httpClient.execute(httpDelete);
        HttpEntity entity = httpResponse.getEntity();
        if ("HTTP/1.1 200 OK".equals(httpResponse.getStatusLine().toString().trim())) {
            return "success";
        } else {
            String message = EntityUtils.toString(entity);
            return message;
        }
    }

    /**
     * 标记镜像.
     * 
     * @param httpClient
     * @param name
     *            Image name or ID to tag.
     * @param repo
     *            The repository to tag in. For example, someuser/someimage.
     * @param newTag
     *            new tag
     * @param host
     * @return
     * @throws Exception
     */
    public String tagImage(final CloseableHttpClient httpClient, final String name, final String repo, final String newTag, final String host)
            throws Exception {
        String url = "http://" + host + "/images/" + name + "/tag?repo=" + repo + "&tag=" + newTag;
        logger.info(url);
        HttpPost httpPost = new HttpPost(url);
        CloseableHttpResponse httpResponse = httpClient.execute(httpPost);
        HttpEntity entity = httpResponse.getEntity();
        if ("HTTP/1.1 201 Created".equals(httpResponse.getStatusLine().toString().trim())) {
            return "success";
        } else {
            String message = EntityUtils.toString(entity);
            return message;
        }
    }

    private String ByteConversionGBMBKB(final float KSize) {
        int GB = 1024 * 1024 * 1024;// 定义GB的计算常量
        int MB = 1024 * 1024;// 定义MB的计算常量
        int KB = 1024;// 定义KB的计算常量
        java.text.NumberFormat formater = java.text.DecimalFormat.getInstance();
        formater.setMaximumFractionDigits(3);
        formater.setMinimumFractionDigits(3);
        if (KSize / GB >= 1)// 如果当前Byte的值大于等于1GB
            return String.valueOf(formater.format(KSize / (float) GB)) + "GB";// 将其转换成GB
        else if (KSize / MB >= 1)// 如果当前Byte的值大于等于1MB
            return String.valueOf(formater.format(KSize / (float) MB)) + "MB";// 将其转换成MB
        else if (KSize / KB >= 1)// 如果当前Byte的值大于等于1KB
            return String.valueOf(formater.format(KSize / (float) KB)) + "KB";// 将其转换成KGB
        else
            return String.valueOf(KSize) + "Byte";// 显示Byte值
    }
    
    /**
     * 获取容器json.
     * @param httpClient
     * @param host
     * @param container
     *          ID or name of the container
     * @return
     * @throws Exception
     */
    public JSONObject inspectContainer(final CloseableHttpClient httpClient, final String host, final String container) throws Exception {
        JSONObject retObject = new JSONObject();
        String retMsg = "error";
        retObject.put("retMsg", retMsg);
        final String url = "http://" + host + "/containers/" + container + "/json";
        logger.info(url);
        HttpGet httpGet = new HttpGet(url);
        CloseableHttpResponse response = httpClient.execute(httpGet);
        StatusLine statusLine = response.getStatusLine();
        if (statusLine.getStatusCode() == 200) {
            // nothing to do.
            String msg = EntityUtils.toString(response.getEntity());
            JSONObject parseObject = JSONObject.parseObject(msg);
            retObject.put("retMsg", Constants.RESTFUL_SUCCESS);
            retObject.put("result", parseObject);
        } else {
            retMsg = "inspect 镜像出错.";
        }
        return retObject;
    }

    /**
     * 列出容器.
     * 
     * @param httpClient
     * @param host
     * @return
     * @throws Exception
     */
    public JSONObject listContainers(final CloseableHttpClient httpClient, final String host) throws Exception {
        JSONObject retObject = new JSONObject();
        String retMsg = Constants.RESTFUL_ERROR;
        retObject.put(Constants.RESTFUL_RTETMSG, retMsg);
        
        String url = "http://" + host + "/containers/json?all=1";
        logger.info(url);
        HttpGet httpGet = new HttpGet(url);
        CloseableHttpResponse httpResponse = httpClient.execute(httpGet);
        HttpEntity entity = httpResponse.getEntity();
        StatusLine statusLine = httpResponse.getStatusLine();
        String result = EntityUtils.toString(entity);
        if (200 == statusLine.getStatusCode()) {
            JSONArray oldArray = JSONArray.parseArray(result);
            JSONArray newArray = new JSONArray();
            for (int i = 0; i < oldArray.size(); i++) {
                JSONObject subJsonObject = new JSONObject();
                JSONArray names = oldArray.getJSONObject(i).getJSONArray("Names");
                StringBuffer sb = new StringBuffer();
                for (int j = 0; j < names.size(); j++) {
                    //sb.append(names.getString(j).substring(3, names.getString(j).length() - 2));
                    sb.append(names.getString(j).substring(1, names.getString(j).length()));
                    sb.append("</br>");
                }
                subJsonObject.put("Names", sb.toString());
                subJsonObject.put("Id", oldArray.getJSONObject(i).get("Id"));
                subJsonObject.put("Image", oldArray.getJSONObject(i).get("Image"));
                subJsonObject.put("Ports", oldArray.getJSONObject(i).get("Ports"));
                subJsonObject.put("Command", oldArray.getJSONObject(i).get("Command"));
                subJsonObject.put("Status", oldArray.getJSONObject(i).get("Status"));
                newArray.add(subJsonObject);
            }
            retObject.put(Constants.RESTFUL_RTETMSG, Constants.RESTFUL_SUCCESS);
            retObject.put("data", newArray);
        } else {
            retObject.put(Constants.RESULT, result);
        }
        return retObject;
    }

    /**
     * 创建容器.
     * 
     * @param httpClient
     * @param host
     * @param parameter
     * @param containerName
     * @return
     * @throws ClientProtocolException
     * @throws IOException
     */
    public JSONObject createContainer(final CloseableHttpClient httpClient, final String host,
            final Map parameter, final String containerName)
            throws ClientProtocolException, IOException {
        String retMsg = "error";
        String containerId = null;
        JSONObject retObject = new JSONObject();
        retObject.put("retMsg", retMsg);

        final String url = "http://" + host + "/containers/create?name=" + containerName;
        HttpPost httpPost = new HttpPost(url);
        // List <NameValuePair> nvps = new ArrayList <NameValuePair>();
        // nvps.add(new BasicNameValuePair("username", "vip"));
        // nvps.add(new BasicNameValuePair("password", "secret"));
        // httpPost.setEntity(new UrlEncodedFormEntity(nvps));
        HttpEntity entity = new StringEntity(JSONObject.toJSONString(parameter), ContentType.APPLICATION_JSON);
        httpPost.setEntity(entity);
        CloseableHttpResponse httpResponse = httpClient.execute(httpPost);
        StatusLine statusLine = httpResponse.getStatusLine();

        logger.info(statusLine.getStatusCode() + "");
        HttpEntity entity2 = httpResponse.getEntity();
        String result = EntityUtils.toString(entity2);
        EntityUtils.consume(entity2);
        logger.info(result);
        if (statusLine.getStatusCode() == 201) {
            JSONObject parseObject = JSONObject.parseObject(result);
            containerId = (String) parseObject.get("Id");
            retMsg = "success";
            retObject.put("id", containerId);
        } else {
            retMsg = "创建容器出错:\r\n" + result;
        }
        retObject.put("retMsg", retMsg);
        return retObject;
    }

    /**
     * 启动容器.
     * 
     * @param httpClient
     * @param host
     * @param containerId
     *            容器id或名称
     * @return
     * @throws ClientProtocolException
     * @throws IOException
     */
    public String startContainer(final CloseableHttpClient httpClient, final String host,
            final String containerId) throws ClientProtocolException, IOException {
        String retMsg = "error";
        JSONObject retObject = new JSONObject();
        retObject.put("retMsg", retMsg);

        final String url = "http://" + host + "/containers/" + containerId + "/start";
        HttpPost httpPost = new HttpPost(url);
        CloseableHttpResponse httpResponse = httpClient.execute(httpPost);
        StatusLine statusLine = httpResponse.getStatusLine();

        logger.info(statusLine.getStatusCode() + "");
        if (statusLine.getStatusCode() == 204) {
            retMsg = "success";
        } else {
            retMsg = "创建容器出错:\r\n" + statusLine.getReasonPhrase();
        }
        return retMsg;
    }

    /**
     * 停容器.
     * 
     * @param httpClient
     * @param host
     * @param containerId
     * @return
     * @throws ClientProtocolException
     * @throws IOException
     */
    public String stopContainer(final CloseableHttpClient httpClient, final String host,
            final String containerId) throws ClientProtocolException, IOException {
        String retMsg = "error";
        JSONObject retObject = new JSONObject();
        retObject.put("retMsg", retMsg);

        final String url = "http://" + host + "/containers/" + containerId + "/stop?t=5";
        HttpPost httpPost = new HttpPost(url);
        CloseableHttpResponse httpResponse = httpClient.execute(httpPost);
        StatusLine statusLine = httpResponse.getStatusLine();

        logger.info(statusLine.getStatusCode() + "");
        if (statusLine.getStatusCode() == 204) {
            retMsg = "success";
        } else {
            retMsg = "停止容器出错:\r\n" + statusLine.getReasonPhrase();
        }
        return retMsg;
    }
    
    /**
     * 重启容器.
     * 
     * @param httpClient
     * @param host
     * @param containerId
     * @return
     * @throws ClientProtocolException
     * @throws IOException
     */
    public String restartContainer(final CloseableHttpClient httpClient, final String host,
            final String containerId) throws ClientProtocolException, IOException {
        String retMsg = "error";
        JSONObject retObject = new JSONObject();
        retObject.put("retMsg", retMsg);

        final String url = "http://" + host + "/containers/" + containerId + "/restart?t=5";
        HttpPost httpPost = new HttpPost(url);
        CloseableHttpResponse httpResponse = httpClient.execute(httpPost);
        StatusLine statusLine = httpResponse.getStatusLine();

        logger.info(statusLine.getStatusCode() + "");
        if (statusLine.getStatusCode() == 204) {
            retMsg = "success";
        } else {
            retMsg = "重启容器出错:\r\n" + statusLine.getReasonPhrase();
        }
        return retMsg;
    }

    /**
     * 删除容器.
     * 
     * @param httpClient
     * @param host
     * @param containerId
     * @return
     * @throws Exception
     */
    public String deleteContainer(final CloseableHttpClient httpClient, final String host, final String containerId)
            throws Exception {
        String retMsg = "error";
        String url = "http://" + host + "/containers/" + containerId + "?force=true";
        logger.info(url);
        // 强制删除.
        HttpDelete httpDelete = new HttpDelete(url);
        CloseableHttpResponse httpResponse = httpClient.execute(httpDelete);
        StatusLine statusLine = httpResponse.getStatusLine();

        logger.info(statusLine.getStatusCode() + "");
        if (statusLine.getStatusCode() == 204) {
            retMsg = "success";
        } else {
            retMsg = "删除容器出错:\r\n" + statusLine.getReasonPhrase();
        }
        return retMsg;
    }

    /**
     * 改容器名.
     * 
     * @param httpClient
     * @param host
     * @param containerId
     *            容器id或者名称
     * @param name
     *            新名称
     * @return
     * @throws ClientProtocolException
     * @throws IOException
     */
    public static String renameContainer(final CloseableHttpClient httpClient, final String host,
            final String containerId, final String name) throws ClientProtocolException, IOException {
        String retMsg = "error";
        JSONObject retObject = new JSONObject();
        retObject.put("retMsg", retMsg);

        final String url = "http://" + host + "/containers/" + containerId + "/rename?name=" + name;
        HttpPost httpPost = new HttpPost(url);
        CloseableHttpResponse httpResponse = httpClient.execute(httpPost);
        StatusLine statusLine = httpResponse.getStatusLine();

        logger.info(statusLine.getStatusCode() + "");
        if (statusLine.getStatusCode() == 204) {
            retMsg = "success";
        } else {
            retMsg = "容器改名出错:\r\n" + statusLine.getReasonPhrase();
        }
        return retMsg;
    }

    /**
     * 获得容器日志.
     * 
     * @param httpClient
     * @param host
     * @param id
     * @return
     * @throws Exception
     */
    public JSONObject getContainerLogs(final CloseableHttpClient httpClient, final String host, final String id)
            throws Exception {
        String retMsg = "error";
        JSONObject retObject = new JSONObject();
        retObject.put("retMsg", retMsg);

        String url = "http://" + host + "/containers/" + id + "/logs?stdout=true&stdout=true";
        logger.info(url);
        HttpGet httpGet = new HttpGet(url);
        CloseableHttpResponse httpResponse = httpClient.execute(httpGet);
        HttpEntity entity = httpResponse.getEntity();
        StatusLine statusLine = httpResponse.getStatusLine();
        if (statusLine.getStatusCode() == 200) {
            logger.info("获取日志成功");
            retObject.put("retMsg", "success");
            InputStream is = entity.getContent();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            StreamUtils.copy(is, baos);
            is.close();
            String logs = baos.toString();
            retObject.put("message", logs);
        } else {
            logger.info("获取日志失败");
            retObject.put("message", statusLine.toString());
        }
        return retObject;
    }
    
    /**
     * 
     * @param host
     * @param containerName
     * @param fileName
     * @return
     */
    public String copyFileFromContainer(final String host, final String containerName, final String fileName) {
        if (dockerpath == null) {
            dockerpath = "/usr/local/bin/docker -H";
        }
        String fileContent = null;
        Runtime r = Runtime.getRuntime();
        String tmpFile = "tmpFile";
        //Process p = r.exec("/usr/local/bin/docker cp bbb:/root/start.sh /Users/jieyee/docker/dockerfile/sms/temp/."); 
        String command = dockerpath + " " + host + " cp " + containerName + ":" + fileName + " " + tmpFile;
        try {
            logger.info("命令 {}", command);
            Process p = r.exec(command);
            InputStream inputStream = p.getInputStream();
            p.waitFor();
            int exitValue = p.exitValue();
            logger.info("返回值:" + exitValue);
            Charset charset = Charset.forName("UTF8");
            if (exitValue == 0) {
                fileContent = StreamUtils.copyToString(new FileInputStream(tmpFile), charset);
            } else {
                fileContent = StreamUtils.copyToString(inputStream, charset);
            }
            inputStream.close();
            logger.info(fileContent);
        } catch (Exception e) {
            e.printStackTrace();
        } 
        
        return fileContent;
    }
    
    /**
     * 通过容器名、项目名，执行shell来更新容器.
     * @param host
     * @param containerName
     * @param projetName
     * @return
     */
    public String execute(final String host, final String containerName, final String projetName) {
        String msg = Constants.RESTFUL_ERROR;
        Runtime r = Runtime.getRuntime();
        String command = projectMap.get(projetName);
        command = command + " " + containerName; 
        try {
            logger.info("命令 {}", command);
            Process p = r.exec(command);
            InputStream inputStream = p.getInputStream();
            p.waitFor();
            int exitValue = p.exitValue();
            logger.info("返回值:" + exitValue);
            Charset charset = Charset.forName("UTF8");
            if (exitValue == 0) {
                msg = Constants.RESTFUL_SUCCESS;
            } else {
                msg = StreamUtils.copyToString(inputStream, charset);
            }
            inputStream.close();
            logger.info(msg);
        } catch (Exception e) {
            e.printStackTrace();
        } 
        return msg;
    }
}
