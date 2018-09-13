package com.jieyee.docker.webconsole;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StreamUtils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jieyee.docker.webconsole.constants.Constants;
import com.jieyee.docker.webconsole.service.DockerService;

public class DockerServiceTest {
    private static final Logger logger = LoggerFactory.getLogger(DockerServiceTest.class);
    private CloseableHttpClient httpClient;
    private DockerService dockerService = new DockerService();;
    private String host = "localhost:2375";
    private String registry = "localhost:5000";
    
    @Before
    public void prepare() {
        httpClient = HttpClients.createDefault();
    }
    
    @After
    public void destroy() {
        try {
            if (httpClient != null) {
                httpClient.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testPullImage() throws Exception {
        String image = "busybox";
        JSONObject pullImage = dockerService.pullImage(httpClient, host, registry, image);
        assertEquals(Constants.RESTFUL_SUCCESS, pullImage.get("retMsg"));
    }
    
    @Test
    public void testPullImage2() throws Exception {
        String image = "not-exist-image";
        JSONObject pullImage = dockerService.pullImage(httpClient, host, registry, image);
        assertNotEquals("Constants.RESTFUL_SUCCESS", pullImage.get("retMsg"));
    }
    
    @Test
    public void testListContainers() throws Exception {
        JSONObject list = dockerService.listContainers(httpClient, host);
        JSONArray array =  (JSONArray)list.get("data");
        assertTrue(array.size() > 0);
    }

    @Test
    public void testListImages() throws Exception {
        JSONObject list = dockerService.listImages(httpClient, host);
        JSONArray array =  (JSONArray)list.get("data");
        logger.info("镜像数:" + array.size());
        assertTrue(array.size() > 0);
    }
    
    @Test
    public void testDeleteImage() throws Exception {
        String name = "busybox";
        dockerService.pullImage(httpClient, host, registry, name);
        Object retMsg = dockerService.deleteImage(httpClient, host, registry + "/" + name);
        assertEquals(retMsg, "success");
    }
    
    @Test
    public void testTagImage() throws Exception {
        //这里只有name，那就代表默认的bugybox:latest，方便后面处理
        String name = "busybox";
        String repo = "127.0.0.1:5000/busybox"; 
        String tag = "thetag";
        String retMsg = dockerService.tagImage(httpClient, name, repo, tag, host);
        assertEquals(retMsg, "success");
        //私有库
        //retMsg = dockerService.deleteImage(httpClient, registry + "/" + name + ":" + tag, host);
        //官方
        retMsg = dockerService.deleteImage(httpClient, host, repo + ":" + tag);
        assertEquals(retMsg, "success");
    }
    
    @Test
    public void testCreateContainer() throws Exception {
        Map contaiterCmd = new HashMap();
        //contaiterCmd.put("Cmd", "ls");
        //contaiterCmd.put("Image", registry + "/busybox");
        contaiterCmd.put("Image", "redis");
        contaiterCmd.put("Memory", 0);
        contaiterCmd.put("MemorySwap", 0);
        contaiterCmd.put("AttachStdin", false);
        contaiterCmd.put("AttachStdout", true);
        contaiterCmd.put("AttachStderr", true);
        contaiterCmd.put("PortSpecs", null);
        contaiterCmd.put("Tty", true);
        contaiterCmd.put("OpenStdin", false);
        contaiterCmd.put("StdinOnce", false);
        contaiterCmd.put("Env", null);
        contaiterCmd.put("WorkingDir", "");
        contaiterCmd.put("DisableNetwork", false);
        Map contarierHostConfig = new HashMap();
        contarierHostConfig.put("NetworkMode","bridge");
        contaiterCmd.put("HostConfig",contarierHostConfig);
        contaiterCmd.put("PublishAllPorts", true);
        //创建
        JSONObject retMsg = dockerService.createContainer(httpClient, host, contaiterCmd, "testcontainername");
        assertEquals(retMsg.get("retMsg"), "success");
        String containerId = (String)retMsg.get("id");
        //启动
        String startContainer = dockerService.startContainer(httpClient, host, containerId);
        assertEquals(startContainer, "success");
        //改名
        String renameContainer = dockerService.renameContainer(httpClient, host, containerId, "newcontainer");
        assertEquals(renameContainer, "success");
        //停止
        String stopContainer = dockerService.stopContainer(httpClient, host, containerId);
        assertEquals(stopContainer, "success");
        //删除容器, API里强制删除，因此不停也能删，前面的停止API可以补调用
        Object deleteContainer = dockerService.deleteContainer(httpClient, host, containerId);
        assertEquals(deleteContainer, "success");
    }
    
    @Test
    public void testGetContainerLogs() throws Exception {
        JSONObject json = dockerService.getContainerLogs(httpClient, host, "registry");
        logger.info(json.getString("message"));
    }
    
    @Test
    public void testPushImage() throws Exception {
        String pushImage = dockerService.pushImage(httpClient, host, "busybox", "latest");
        assertEquals("success", pushImage);
    }
    
    @Test
    public void testBuildImage() throws Exception {
        JSONObject buildImage = dockerService.buildImage(httpClient, host, "aaa", "/Users/jieyee/docker/dockerfile/sms/aaa.tar.gz");
        assertEquals("success", buildImage.get("retMsg"));
    }
    
    @Test
    public void testInspectImage() throws Exception {
        JSONObject inspectImage = dockerService.inspectImage(httpClient, host, "centos68:redis");
        assertEquals("success", inspectImage.get("retMsg"));
    }
    
    @Test
    public void testCopyFile() throws Exception {
        String issue = dockerService.copyFileFromContainer(host, "registry", "/etc/issue");
        logger.info(issue);
        assertTrue(issue.indexOf("Welcome to Alpine Linux") != -1);
    }
    
    
    public static void main(String[] args) throws Exception{
        Runtime r = Runtime.getRuntime(); 
        Process p = r.exec("/Users/jieyee/亮生活/tmp/deploy.sh bbb"); 
        InputStream inputStream = p.getInputStream();
        p.waitFor();
        logger.info("返回值:" + p.exitValue());
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        StreamUtils.copy(inputStream, baos);
        inputStream.close();
        String result = baos.toString();
        logger.info(result);
    }
 }
