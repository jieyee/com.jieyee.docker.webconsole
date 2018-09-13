package com.jieyee.docker.webconsole.service;

import org.apache.http.HttpEntity;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONObject;

/**
 * 访问docker registry的服务.
 * @author jieyee
 *
 */
@Component
public class RegistryService {
    private static final Logger logger = LoggerFactory.getLogger(RegistryService.class);
   
    
    /**
     * 列出所有镜像.
     * @param httpClient
     * @param registry registry host, not docker engine!
     * @return
     * @throws Exception
     */
    public JSONObject listImages(final CloseableHttpClient httpClient, final String registry) throws Exception {
        String retMsg = "error";
        JSONObject retObject = new JSONObject();
        retObject.put("retMsg", retMsg);
        final String url = "http://" + registry + "/v2/_catalog";
        logger.info(url);
        HttpGet httpGet = new HttpGet(url);
        CloseableHttpResponse httpResponse = httpClient.execute(httpGet);   
        StatusLine statusLine = httpResponse.getStatusLine();
        if (statusLine.getStatusCode() == 200) {
            HttpEntity entity = httpResponse.getEntity();
            retObject.put("retMsg", "success");
            JSONObject json = JSONObject.parseObject(EntityUtils.toString(entity));
            retObject.put("result", json);
            
        } else {
            retObject.put("message", statusLine.getReasonPhrase());
            logger.error(statusLine.toString());
        }
        return retObject;
    }
    
    /**
     * 列出所有指定镜像的标签.
     * @param httpClient
     * @param registry
     * @param imageName
     * @return
     * @throws Exception
     */
    public JSONObject listImageTags(final CloseableHttpClient httpClient, final String registry, final String imageName) throws Exception {
        String retMsg = "error";
        JSONObject retObject = new JSONObject();
        retObject.put("retMsg", retMsg);
        final String url = "http://" + registry + "/v2/" + imageName + "/tags/list";
        logger.info(url);
        HttpGet httpGet = new HttpGet(url);
        CloseableHttpResponse httpResponse = httpClient.execute(httpGet);   
        StatusLine statusLine = httpResponse.getStatusLine();
        if (statusLine.getStatusCode() == 200) {
            HttpEntity entity = httpResponse.getEntity();
            retObject.put("retMsg", "success");
            JSONObject json = JSONObject.parseObject(EntityUtils.toString(entity));
            retObject.put("result", json);
        } else {
            retObject.put("message", statusLine.getReasonPhrase());
            logger.error(statusLine.toString());
        }
        return retObject;
    }
    
    /**
     * 获取指定镜像的Manifest.
     * @param httpClient
     * @param registry
     * @param imageName 注意这里name和tag分开，其他接口基本都是合并的.
     * @param tag  tag or digest
     * @return
     * @throws Exception
     */
    public JSONObject getManifest(final CloseableHttpClient httpClient, final String registry, final String imageName, final String tag) throws Exception {
        String retMsg = "error";
        JSONObject retObject = new JSONObject();
        retObject.put("retMsg", retMsg);
        final String url = "http://" + registry + "/v2/" + imageName + "/manifests/" + tag;
        logger.info(url);
        HttpGet httpGet = new HttpGet(url);
        httpGet.addHeader("Accept", "application/vnd.docker.distribution.manifest.v2+json");
        CloseableHttpResponse httpResponse = httpClient.execute(httpGet);   
        StatusLine statusLine = httpResponse.getStatusLine();
        if (statusLine.getStatusCode() == 200) {
            HttpEntity entity = httpResponse.getEntity();
            retObject.put("retMsg", "success");
            retObject.put("digest", httpResponse.getFirstHeader("Docker-Content-Digest").getValue());
        } else {
            retObject.put("message", statusLine.getReasonPhrase());
            logger.error(statusLine.toString());
        }
        return retObject;
    }
    
    /**
     * 删除镜像摘要.
     * @param httpClient
     * @param registry
     * @param imageName
     * @param digest
     * @return
     * @throws Exception
     */
    public JSONObject deleteImageDigest(final CloseableHttpClient httpClient, final String registry, final String imageName, final String digest) throws Exception {
        String retMsg = "error";
        JSONObject retObject = new JSONObject();
        retObject.put("retMsg", retMsg);
        final String url = "http://" + registry + "/v2/" + imageName + "/manifests/" + digest;
        logger.info(url);
        HttpDelete httpDelete = new HttpDelete(url);
        httpDelete.addHeader("Accept", "application/vnd.docker.distribution.manifest.v2+json");
        CloseableHttpResponse httpResponse = httpClient.execute(httpDelete);   
        StatusLine statusLine = httpResponse.getStatusLine();
        if (statusLine.getStatusCode() == 202) {
            retObject.put("retMsg", "success");
        } else {
            retObject.put("message", statusLine.getReasonPhrase());
            logger.error(statusLine.toString());
        }
        return retObject;
    }
    
    /**
     * 删除镜像二进制资源.
     * @param httpClient
     * @param registry
     * @param imageName
     * @param digest
     * @return
     * @throws Exception
     */
    public JSONObject deleteImageBlob(final CloseableHttpClient httpClient, final String registry, final String imageName, final String digest) throws Exception {
        String retMsg = "error";
        JSONObject retObject = new JSONObject();
        retObject.put("retMsg", retMsg);
        final String url = "http://" + registry + "/v2/" + imageName + "/blobs/" + digest;
        logger.info(url);
        HttpDelete httpDelete = new HttpDelete(url);
        CloseableHttpResponse httpResponse = httpClient.execute(httpDelete);   
        StatusLine statusLine = httpResponse.getStatusLine();
        if (statusLine.getStatusCode() == 202) {
            retObject.put("retMsg", "success");
        } else {
            retObject.put("message", statusLine.getReasonPhrase());
            logger.error(statusLine.toString());
        }
        return retObject;
    }
}
