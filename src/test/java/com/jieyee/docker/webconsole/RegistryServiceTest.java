package com.jieyee.docker.webconsole;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

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
import com.jieyee.docker.webconsole.service.DockerService;
import com.jieyee.docker.webconsole.service.RegistryService;

public class RegistryServiceTest {
    private static final Logger logger = LoggerFactory.getLogger(RegistryServiceTest.class);
    private CloseableHttpClient httpClient;
    private RegistryService registryService = new RegistryService();
    private DockerService dockerService = new DockerService();
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
    public void testListImages() throws Exception {
        JSONObject json = registryService.listImages(httpClient, registry);
        JSONArray jsonArray = json.getJSONObject("result").getJSONArray("repositories");
        logger.info(jsonArray.toJSONString());
        assertTrue(jsonArray.size() > 0);
    }
  
    @Test
    public void testListImageTags() throws Exception {
        JSONObject json = registryService.listImageTags(httpClient, registry, "busybox");
        //{"result":{"name":"registry","tags":["latest"]},"retMsg":"success"}
        assertEquals("success", json.getString("retMsg"));
        assertEquals("busybox", json.getJSONObject("result").getString("name"));
        JSONArray jsonArray = json.getJSONObject("result").getJSONArray("tags");
        logger.info(jsonArray.toJSONString());
        assertTrue(jsonArray.size() > 0);
    }
    
    //@Test
    public void testGetManifest0() throws Exception {
        final String imageName = "busybox";
        JSONObject json = registryService.getManifest(httpClient, registry, imageName, "latest");
        //{"result":{"name":"registry","tags":["latest"]},"retMsg":"success"}
        assertEquals("success", json.getString("retMsg"));
        JSONArray jsonArray = json.getJSONObject("result").getJSONArray("layers");
        logger.info(jsonArray.toJSONString());
        logger.info(imageName + " layers:" + jsonArray.size() );
        assertTrue(jsonArray.size() > 0);
        for (Object object : jsonArray) {
            JSONObject layerJSON = (JSONObject)object;
            String digest = layerJSON.getString("digest");
            destroy();
            prepare();
            //删除摘要.
            JSONObject deleteImage = registryService.deleteImageDigest(httpClient, registry, imageName, digest);
            logger.info(deleteImage.getString("retMsg"));
            //assertEquals("success", deleteImage.getString("retMsg"));
            
            destroy();
            prepare();
            //删除资源.
            JSONObject deleteImageBlob = registryService.deleteImageBlob(httpClient, registry, imageName, digest);
            //assertEquals("success", deleteImageBlob.getString("retMsg"));
            logger.info(deleteImageBlob.getString("retMsg"));
            
        }
    }
    
    @Test
    public void testGetManifest() throws Exception {
        final String imageName = "busybox";
        JSONObject json = registryService.getManifest(httpClient, registry, imageName, "latest");
        //{"result":{"name":"registry","tags":["latest"]},"retMsg":"success"}
        assertEquals("success", json.getString("retMsg"));
        String digest = json.getString("digest");
        logger.info("digest " + digest);
        
        //删除摘要.
        JSONObject deleteImage = registryService.deleteImageDigest(httpClient, registry, imageName, digest);
        logger.info(deleteImage.getString("retMsg"));
        assertEquals("success", deleteImage.getString("retMsg"));
        
        dockerService.pushImage(httpClient, host, "busybox", "latest");
    }
    
    public static void main(String[] args) throws Exception{
        Process p = Runtime.getRuntime().exec("/usr/local/bin/docker version");
        InputStream is = p.getInputStream();
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        StreamUtils.copy(is, os);
        System.out.println(os.toString());
        
    }
 }
