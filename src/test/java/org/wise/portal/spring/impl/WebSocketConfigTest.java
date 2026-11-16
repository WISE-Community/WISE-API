package org.wise.portal.spring.impl;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.autoconfigure.websocket.servlet.WebSocketMessagingAutoConfiguration;
import org.springframework.messaging.simp.broker.SimpleBrokerMessageHandler;
import org.springframework.test.context.junit.jupiter.web.SpringJUnitWebConfig;

@SpringJUnitWebConfig(WebSocketConfig.class)
@ImportAutoConfiguration({ JacksonAutoConfiguration.class, WebSocketMessagingAutoConfiguration.class })
public class WebSocketConfigTest {

  @Autowired
  private SimpleBrokerMessageHandler simpleBroker;

  @Test
  public void simpleBroker_shouldSendHeartbeatsWithoutRequiringThemFromClients() {
    assertArrayEquals(new long[] { 20000, 0 }, simpleBroker.getHeartbeatValue());
  }

  @Test
  public void simpleBroker_shouldHaveATaskSchedulerForHeartbeats() {
    assertNotNull(simpleBroker.getTaskScheduler());
  }
}
