package com.jieyee.docker.webconsole.websocket;

import java.security.Principal;
import java.util.Map;

import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptorAdapter;
import org.springframework.session.ExpiringSession;
import org.springframework.session.MapSessionRepository;
import org.springframework.session.SessionRepository;
import org.springframework.session.web.socket.config.annotation.AbstractSessionWebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

@Configuration
@EnableWebSocketMessageBroker
@ConditionalOnWebApplication
public class WebsocketSTOMPConfig extends AbstractSessionWebSocketMessageBrokerConfigurer<ExpiringSession> {
    /**
     * AbstractSessionWebSocketMessageBrokerConfigurer实现了在handshake时获取httpsession，并且每次websocket消息发生时也刷新了httpsession的时间。同时在websocket
     * session中加入了SPRING.SESSION.ID字段。
     */

    @Override
    protected void configureStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/wsendpoint")
                // 在握手时就获得user，判断是否登录。
                .addInterceptors(new SessionAuthHandshakeInterceptor())
                .setHandshakeHandler(new DefaultHandshakeHandler() {
                    @Override
                    protected Principal determineUser(ServerHttpRequest request, WebSocketHandler wsHandler,
                            Map<String, Object> attributes) {
                        User user = (User) attributes.get("user");
                        return new MyPrincipal(user);
                    }
                }).setAllowedOrigins("*").withSockJS();
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.setInterceptors(new ChannelInterceptorAdapter() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                System.out.println("recv : " + message);
                StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
                User user = (User) accessor.getSessionAttributes().get("user");
                return super.preSend(message, channel);
            }
        });
    }

    @Override
    public void configureClientOutboundChannel(ChannelRegistration registration) {
        registration.setInterceptors(new ChannelInterceptorAdapter() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                System.out.println("send: " + message);
                return super.preSend(message, channel);
            }
        });
    }
    
    @Bean
    SessionRepository sessionRepository() {
        return new MapSessionRepository();
    }
    
    @Bean
    public SocketSessionRegistry SocketSessionRegistry(){
        return new SocketSessionRegistry();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // 这是配置到 @MessageMapping Controller
        config.setApplicationDestinationPrefixes("/ws");
        // 直接到broker message handler
        config.enableSimpleBroker("/topic", "/queue");
    }

    class MyPrincipal implements Principal {
        private User user;

        public MyPrincipal(User user) {
            this.user = user;
        }

        @Override
        public String getName() {
            return String.valueOf(user.getId());
        }
    }
}