package com.jieyee.docker.webconsole.Indicator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.jieyee.docker.webconsole.service.DockerService;
@Component("Docker")
public class DockerIndicator implements HealthIndicator {
    private static final Logger logger = LoggerFactory.getLogger(DockerService.class);
    @Autowired
    private RestTemplate restTemplate;
    
    @Override
    public Health health() {
        int errorCode = check();
        if (errorCode != 0) {
          return Health.down().withDetail("Error Code", errorCode).build();
        }
        return Health.up().build();
    }

    private int check() {
        // 对监控对象的检测操作
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON_UTF8); 
            HttpEntity<String> entity = new HttpEntity<String>(headers);
            ResponseEntity<String> resultEntity = restTemplate.exchange("http://localhost:2375/containers/json?all=1", HttpMethod.GET, entity, String.class);
            String result = resultEntity.getBody();
            logger.info(result);
        } catch (Exception e) {
            logger.error("异常", e);
            return -1;
        }
        return 0;
    }
}
