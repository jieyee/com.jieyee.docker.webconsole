package com.jieyee.docker.webconsole.security;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.thymeleaf.extras.springsecurity4.dialect.SpringSecurityDialect;

import com.jieyee.docker.webconsole.websocket.User;

/**
 * Created by dimon on 19/07/2017.
 */
@Configuration
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {
    Logger logger = LoggerFactory.getLogger(SecurityConfiguration.class);

    @Value("${platform.oauth.in.memory.clientId:admin}")
    private String clientId;
    @Value("${platform.oauth.in.memory.password:lsh.0904}")
    private String password;
    
    @Bean
    public SpringSecurityDialect springSecurityDialect() {
        SpringSecurityDialect dialect = new SpringSecurityDialect();
        return dialect;
    }
    
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests().antMatchers("/css/**", "/fonts/**", "/images/**", "/js/**","/js/**/**", "/druid/**").permitAll()
                .anyRequest().fullyAuthenticated()
                .and().formLogin().loginPage("/login").failureUrl("/login?error")
                .successHandler(new SuccessHandler("/")).permitAll()
                .and().logout().permitAll();

        http.csrf().disable();
        //session失效后跳转
        http.sessionManagement().invalidSessionUrl("/login");
        http.headers().frameOptions().sameOrigin();
        // loginProcessingUrl("/login")
    }

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        auth.inMemoryAuthentication()
        .withUser(clientId).password(password).roles("USER")
        .and()
        .withUser("aa").password("aa").roles("USER")
        .and()
        .withUser("bb").password("bb").roles("USER");
    }
    
    class SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

        @Override
        public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                Authentication authentication) throws IOException, ServletException {
            String name = request.getParameter("username");
            User user = new User();
            user.setId(name);
            request.getSession(false).setAttribute("user", user);
            System.out.println("user : " + user.getId());
            
            super.onAuthenticationSuccess(request, response, authentication);
        }
        
        SuccessHandler(String defaultTargetUrl) {
            super.setDefaultTargetUrl(defaultTargetUrl);
        }
        
    }
}
