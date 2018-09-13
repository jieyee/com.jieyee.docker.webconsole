package com.jieyee.docker.webconsole;

import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;

@SpringBootApplication
@Configuration
@EnableAutoConfiguration
@EnableConfigurationProperties
@ComponentScan
public class Application extends WebMvcConfigurerAdapter {
    private static final Logger logger = LoggerFactory.getLogger(Application.class);
    
//    @Override
//    public void addViewControllers(ViewControllerRegistry registry) {
//        registry.addViewController("/login").setViewName("login");
//    }
    
    @Bean
    public LocaleResolver localeResolver() {
        SessionLocaleResolver slr = new SessionLocaleResolver();
        //设置默认区域
        slr.setDefaultLocale(Locale.CHINA);
        return slr;
    }
    
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
   
    public static void main(String[] args) throws Exception {
        SpringApplication.run(Application.class, args);
        logger.info("Docker web控制台服务启动...");
    }
}
