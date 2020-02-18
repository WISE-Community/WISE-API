package org.wise.portal.spring.impl;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.messaging.MessageSecurityMetadataSourceRegistry;
import org.springframework.security.config.annotation.web.socket.AbstractSecurityWebSocketMessageBrokerConfigurer;

@Configuration
public class WebSocketSecurityConfig
    extends AbstractSecurityWebSocketMessageBrokerConfigurer {

  protected void configureInbound(
      MessageSecurityMetadataSourceRegistry messages) {
    messages.simpDestMatchers("/api/*").authenticated();
  }

  @Override
  protected boolean sameOriginDisabled() {
    return true;
  }
}
