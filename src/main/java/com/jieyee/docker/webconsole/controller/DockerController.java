package com.jieyee.docker.webconsole.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jieyee.docker.webconsole.bean.CreateContainerBean;
import com.jieyee.docker.webconsole.constants.Constants;
import com.jieyee.docker.webconsole.service.DockerService;
import com.jieyee.docker.webconsole.service.RegistryService;

@RestController
@ConfigurationProperties
public class DockerController {
    private static final Logger logger = LoggerFactory.getLogger(DockerController.class);
    
    @Value("${platform.registry:localhost:5000}")
    private String registry;
    
    private List<Map<String, String>> serverList;
    
    /**
     * @return the serverList
     */
    public List<Map<String, String>> getServerList() {
        return serverList;
    }

    /**
     * @param serverList the serverList to set
     */
    public void setServerList(List<Map<String, String>> serverList) {
        this.serverList = serverList;
    }

    @Resource
    private DockerService dockerService;
    
    @Resource
    private RegistryService registryService;
    
    @RequestMapping("/serverList")
    public Object serverList() {
        
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("data", serverList);
        logger.info(jsonObject.toJSONString());
        return jsonObject;
    }
    
    @RequestMapping("/registry/listRegistryImages")
    public Object listRegistryImages(){ 
        CloseableHttpClient httpClient = null;
        JSONObject retObject = new JSONObject();
        JSONObject listImages = null;
        try {
            httpClient = HttpClients.createDefault();
            listImages = registryService.listImages(httpClient, registry);
        } catch (Exception e) {
            if (httpClient != null) {
                try {
                    httpClient.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
        if (listImages != null && Constants.RESTFUL_SUCCESS.equals(listImages.getString(Constants.RETMSG))) {
            JSONArray oldArray = listImages.getJSONObject(Constants.RESULT).getJSONArray(Constants.REPOSITORIES);
            JSONArray newArray = new JSONArray();
            for (int i = 0; i < oldArray.size(); i++) {
                JSONArray subArray = new JSONArray();
                subArray.add(oldArray.get(i));
                subArray.add("-");         
                subArray.add(oldArray.get(i));
                newArray.add(subArray);
            }
            retObject.put("data", newArray);
        }
        return retObject;
    }
    
    @RequestMapping(value= {"/registry/listRegistryImageTags/{imageName}"},method={RequestMethod.POST,RequestMethod.GET},produces = "application/json; charset=UTF-8") 
    public Object listRegistryImageTags(@PathVariable("imageName") String imageName){
        CloseableHttpClient httpClient = null;
        JSONObject retObject = new JSONObject();
        JSONObject listImages = null;
        try {
            httpClient = HttpClients.createDefault();
            listImages = registryService.listImageTags(httpClient, registry, imageName);
        } catch (Exception e) {
            if (httpClient != null) {
                try {
                    httpClient.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
        if (listImages != null && Constants.RESTFUL_SUCCESS.equals(listImages.getString(Constants.RETMSG))) {
            JSONArray tagArray = listImages.getJSONObject(Constants.RESULT).getJSONArray("tags");
            JSONArray newArray = new JSONArray();
            for (int i = 0; i < tagArray.size(); i++) {
                JSONObject sObject = new JSONObject();
                sObject.put("name", imageName);
                sObject.put("tag", tagArray.get(i));
                newArray.add(sObject);
            }
            retObject.put("data", newArray);
        }
        return retObject;
    }
    
    @RequestMapping(value= {"/docker/pullImage"},method={RequestMethod.POST,RequestMethod.GET},produces = "application/json; charset=UTF-8")
    public Object pullImage(@RequestParam final String host, @RequestParam final String image) {
        String retMsg = Constants.RESTFUL_ERROR;
        JSONObject pullImage = new JSONObject();
        pullImage.put("retMsg", retMsg);
        CloseableHttpClient httpClient = null;
        try {
            httpClient = HttpClients.createDefault();
            pullImage = dockerService.pullImage(httpClient, host, registry, image);
        } catch (Exception e) {
            retMsg = e.getMessage();
            pullImage.put("retMsg", retMsg);
            if (httpClient != null) {
                try {
                    httpClient.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
        return pullImage;
    }
    
    @RequestMapping(value= {"/docker/inspectImage"},method={RequestMethod.POST,RequestMethod.GET},produces = "application/json; charset=UTF-8")
    public Object inspectImage(@RequestParam final String host, @RequestParam final String image) {
        CloseableHttpClient httpClient = null;
        JSONObject inspectImage = new JSONObject();
        try {
            httpClient = HttpClients.createDefault();
            inspectImage = dockerService.inspectImage(httpClient, host, image);
        } catch (Exception e) {
            inspectImage.put("retMsg", e.getMessage());
            if (httpClient != null) {
                try {
                    httpClient.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
        return  inspectImage;
    }
    
    @RequestMapping(value= {"/docker/createContainer"},method={RequestMethod.POST,RequestMethod.GET},produces = "application/json; charset=UTF-8")
    @ResponseBody
    public Object createContainer(@RequestBody CreateContainerBean ccb) {
        CloseableHttpClient httpClient = null;
        JSONObject createContainer = new JSONObject();
        try {
            httpClient = HttpClients.createDefault();
            Map parameter = getParameter(ccb);
            createContainer = dockerService.createContainer(httpClient, ccb.getServerSelect(), parameter, ccb.getContainerName());
        } catch (Exception e) {
            createContainer.put("retMsg", e.getMessage());
            if (httpClient != null) {
                try {
                    httpClient.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
        return createContainer;
    }
    
    /**
     * 设法生成容器设置的map.
     * @param ccb
     * @return
     */
    private Map getParameter(final CreateContainerBean ccb) {
        Map contaiterCmd = new HashMap();
        contaiterCmd.put("Cmd", ccb.getCommand());
        contaiterCmd.put("Image", registry + "/" + ccb.getImageName());
        contaiterCmd.put("Memory", 0);
        contaiterCmd.put("MemorySwap", 0);
        contaiterCmd.put("AttachStdin", false);
        contaiterCmd.put("AttachStdout", true);
        contaiterCmd.put("AttachStderr", true);
        contaiterCmd.put("PortSpecs", null);
        contaiterCmd.put("Tty", true);
        contaiterCmd.put("OpenStdin", false);
        contaiterCmd.put("StdinOnce", false);
//      contaiterCmd.put("Env", null);
        contaiterCmd.put("WorkingDir", "");
        contaiterCmd.put("DisableNetwork", false);
        contaiterCmd.put("Entrypoint", ccb.getEntrypoint());
        
        Map contarierHostConfig = new HashMap();
        Map contarierPortBindings = new HashMap();
        contarierHostConfig.put("NetworkMode","bridge");
        contaiterCmd.put("HostConfig",contarierHostConfig);
        contarierHostConfig.put("PortBindings", contarierPortBindings);
        Map<String, String> portMappings = ccb.getPortMappings();
        Iterator<String> it = portMappings.keySet().iterator();
        while (it.hasNext()) {
            final String key = it.next();
            List hostPorts = new ArrayList();
            Map hostPost = new HashMap();
            hostPost.put("HostPort", portMappings.get(key));
            hostPorts.add(hostPost);
            //key 一般是 port6379 这种形式，所以要清理一下
            contarierPortBindings.put(key.substring(4), hostPorts);
        }
        return contaiterCmd;
    }
    
    @RequestMapping(value= {"/docker/startContainer"},method={RequestMethod.POST,RequestMethod.GET},produces = "application/json; charset=UTF-8")
    public String startContainer(@RequestParam final String host, @RequestParam final String containerId) {
        String retMsg = Constants.RESTFUL_ERROR;
        CloseableHttpClient httpClient = null;
        try {
            httpClient = HttpClients.createDefault();
            retMsg = dockerService.startContainer(httpClient, host, containerId);
        } catch (Exception e) {
            retMsg = e.getMessage();
            if (httpClient != null) {
                try {
                    httpClient.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
        return retMsg;
    }
    
    @RequestMapping(value= {"/docker/restartContainer"},method={RequestMethod.POST,RequestMethod.GET},produces = "application/json; charset=UTF-8")
    public String restartContainer(@RequestParam final String host, @RequestParam final String containerId) {
        String retMsg = Constants.RESTFUL_ERROR;
        CloseableHttpClient httpClient = null;
        try {
            httpClient = HttpClients.createDefault();
            retMsg = dockerService.restartContainer(httpClient, host, containerId);
        } catch (Exception e) {
            retMsg = e.getMessage();
            if (httpClient != null) {
                try {
                    httpClient.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
        return retMsg;
    }
    
    @RequestMapping(value= {"/docker/stopContainer"},method={RequestMethod.POST,RequestMethod.GET},produces = "application/json; charset=UTF-8")
    public String stopContainer(@RequestParam final String host, @RequestParam final String containerId) {
        String retMsg = Constants.RESTFUL_ERROR;
        CloseableHttpClient httpClient = null;
        try {
            httpClient = HttpClients.createDefault();
            retMsg = dockerService.stopContainer(httpClient, host, containerId);
        } catch (Exception e) {
            retMsg = e.getMessage();
            if (httpClient != null) {
                try {
                    httpClient.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
        return retMsg;
    }
    
    @RequestMapping(value= {"/docker/deleteContainer"},method={RequestMethod.POST,RequestMethod.GET},produces = "application/json; charset=UTF-8")
    public String deleteContainer(@RequestParam final String host, @RequestParam final String containerId) {
        String retMsg = Constants.RESTFUL_ERROR;
        CloseableHttpClient httpClient = null;
        try {
            httpClient = HttpClients.createDefault();
            retMsg = dockerService.deleteContainer(httpClient, host, containerId);
        } catch (Exception e) {
            retMsg = e.getMessage();
            if (httpClient != null) {
                try {
                    httpClient.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
        return retMsg;
    }
    
    @RequestMapping(value= {"/docker/listContainers"},method={RequestMethod.POST,RequestMethod.GET},produces = "application/json; charset=UTF-8")
    public Object listContainers(@RequestParam final String host) {
        String retMsg = Constants.RESTFUL_ERROR;
        JSONObject listContainers = new JSONObject();
        listContainers.put(Constants.RETMSG, retMsg);
        CloseableHttpClient httpClient = null;
        try {
            httpClient = HttpClients.createDefault();
            listContainers = dockerService.listContainers(httpClient, host);
        } catch (Exception e) {
            retMsg = e.getMessage();
            if (httpClient != null) {
                try {
                    httpClient.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
        return listContainers;
    }
    
    @RequestMapping(value= {"/docker/listImages"},method={RequestMethod.POST,RequestMethod.GET},produces = "application/json; charset=UTF-8")
    public Object listImages(@RequestParam final String host) {
        String retMsg = Constants.RESTFUL_ERROR;
        JSONObject listContainers = new JSONObject();
        listContainers.put(Constants.RETMSG, retMsg);
        CloseableHttpClient httpClient = null;
        try {
            httpClient = HttpClients.createDefault();
            listContainers = dockerService.listImages(httpClient, host);
        } catch (Exception e) {
            retMsg = e.getMessage();
            if (httpClient != null) {
                try {
                    httpClient.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
        return listContainers;
    }
    
    @RequestMapping(value= {"/docker/inspectContainer"},method={RequestMethod.POST,RequestMethod.GET},produces = "application/json; charset=UTF-8")
    public Object inspectContainer(@RequestParam final String host, @RequestParam final String container) {
        CloseableHttpClient httpClient = null;
        JSONObject inspectImage = new JSONObject();
        try {
            httpClient = HttpClients.createDefault();
            inspectImage = dockerService.inspectContainer(httpClient, host, container);
        } catch (Exception e) {
            inspectImage.put("retMsg", e.getMessage());
            if (httpClient != null) {
                try {
                    httpClient.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
        return  inspectImage;
    }
    
    @RequestMapping(value= {"/docker/deleteImage"},method={RequestMethod.POST,RequestMethod.GET},produces = "application/json; charset=UTF-8")
    public String deleteImage(@RequestParam final String host, @RequestParam final String imageId) {
        String retMsg = Constants.RESTFUL_ERROR;
        CloseableHttpClient httpClient = null;
        try {
            httpClient = HttpClients.createDefault();
            retMsg = dockerService.deleteImage(httpClient, host, imageId);
        } catch (Exception e) {
            retMsg = e.getMessage();
            if (httpClient != null) {
                try {
                    httpClient.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
        return retMsg;
    }
    
    @RequestMapping(value= {"/docker/tagImage"},method={RequestMethod.POST,RequestMethod.GET},produces = "application/json; charset=UTF-8")
    public String tagImage(@RequestParam final String host, @RequestParam final String name, final String repo, final String newTag) {
        String retMsg = Constants.RESTFUL_ERROR;
        CloseableHttpClient httpClient = null;
        try {
            httpClient = HttpClients.createDefault();
            retMsg = dockerService.tagImage(httpClient, name, repo, newTag, host);
        } catch (Exception e) {
            retMsg = e.getMessage();
            if (httpClient != null) {
                try {
                    httpClient.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
        return retMsg;
    }
    
    @RequestMapping(value= {"/docker/viewFile"},method={RequestMethod.POST,RequestMethod.GET},produces = "application/json; charset=UTF-8")
    public String viewFile(@RequestParam final String host, @RequestParam final String containerName, final String fileName) {
        String copyFileFromContainer = dockerService.copyFileFromContainer(host, containerName, fileName);
        return copyFileFromContainer;
    }
    
    @RequestMapping(value= {"/docker/update"},method={RequestMethod.POST,RequestMethod.GET},produces = "application/json; charset=UTF-8")
    public String update(@RequestParam final String host, @RequestParam final String containerName, final String projetName) {
        String msg = dockerService.execute(host, containerName, projetName);
        return msg;
    }
}
