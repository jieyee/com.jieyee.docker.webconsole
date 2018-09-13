package com.jieyee.docker.webconsole.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.metrics.CounterService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class WebController {
    private static final Logger logger = LoggerFactory.getLogger(WebController.class);
    
    @Autowired
    private CounterService counterService;
    
    @RequestMapping("/")
    public String index(){
        counterService.increment("didispace.index.count");
        return "index";
    }
    
    @RequestMapping("/login")
    public String login(){ 
        counterService.increment("didispace.login.count");
        return "login";
    }
    
    @RequestMapping("/registryImageTags")
    public String registryImageTags(){ 
        return "registryImageTags";
    }
    
    @RequestMapping("/start")
    public String start(){ 
        return "start";
    }
    
    @RequestMapping("/containerslist")
    public String containerslist(){ 
        return "containerslist";
    }
    
    @RequestMapping("/hostimagelist")
    public String hostimagelist(){ 
        return "hostimagelist";
    }
    
    
    @RequestMapping("/serverlist")
    public String serverlist(){ 
        return "serverlist";
    }
    
    @RequestMapping("/nav")
    public String nav(){ 
        return "nav";
    }
    
    @RequestMapping("/upload")
    public String upload(){ 
        return "upload";
    }
}

