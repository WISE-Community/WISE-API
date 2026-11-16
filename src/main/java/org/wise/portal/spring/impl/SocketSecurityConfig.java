package org.wise.portal.spring.impl;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.config.annotation.web.socket.EnableWebSocketSecurity;
import org.springframework.security.messaging.access.intercept.MessageMatcherDelegatingAuthorizationManager;

@Configuration
@EnableWebSocketSecurity
public class SocketSecurityConfig {

  @Bean
  AuthorizationManager<Message<?>> authorizationManager(
      MessageMatcherDelegatingAuthorizationManager.Builder messages) {
    messages.simpDestMatchers("/app/**", "/app/**/**").authenticated().anyMessage().authenticated();
    return messages.build();
  }

  @Bean
  public ChannelInterceptor csrfChannelInterceptor() {
    return new ChannelInterceptor() {
    };
  }
}
