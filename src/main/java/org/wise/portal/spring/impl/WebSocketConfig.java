package org.wise.portal.spring.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.messaging.converter.MessageConverter;
import org.springframework.messaging.handler.invocation.HandlerMethodArgumentResolver;
import org.springframework.messaging.handler.invocation.HandlerMethodReturnValueHandler;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketTransportRegistration;

import java.util.List;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

  // Server heartbeats keep idle connections open through proxies. None are required from clients
  // because browsers throttle timers in background tabs, which would get those sockets closed.
  private static final long[] HEARTBEAT_MILLIS = { 20000, 0 };

  private TaskScheduler messageBrokerTaskScheduler;

  @Autowired
  public void setMessageBrokerTaskScheduler(
      @Lazy @Qualifier("messageBrokerTaskScheduler") TaskScheduler messageBrokerTaskScheduler) {
    this.messageBrokerTaskScheduler = messageBrokerTaskScheduler;
  }

  @Override
  public void configureMessageBroker(MessageBrokerRegistry config) {
    config.enableSimpleBroker("/topic")
        .setHeartbeatValue(HEARTBEAT_MILLIS)
        .setTaskScheduler(messageBrokerTaskScheduler);
    config.setApplicationDestinationPrefixes("/app");
  }

  @Override
  public void registerStompEndpoints(StompEndpointRegistry registry) {
    registry.addEndpoint("/websocket").setAllowedOriginPatterns("*");
  }

  @Override
  public void configureWebSocketTransport(WebSocketTransportRegistration webSocketTransportRegistration) {

  }

  @Override
  public void configureClientInboundChannel(ChannelRegistration channelRegistration) {

  }

  @Override
  public void configureClientOutboundChannel(ChannelRegistration channelRegistration) {

  }

  @Override
  public void addArgumentResolvers(List<HandlerMethodArgumentResolver> list) {

  }

  @Override
  public void addReturnValueHandlers(List<HandlerMethodReturnValueHandler> list) {

  }

  @Override
  public boolean configureMessageConverters(List<MessageConverter> list) {
    return false;
  }
}
