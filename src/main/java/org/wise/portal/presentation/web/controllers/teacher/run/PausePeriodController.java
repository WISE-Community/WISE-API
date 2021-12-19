package org.wise.portal.presentation.web.controllers.teacher.run;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.wise.portal.spring.data.redis.MessagePublisher;

@Secured({ "ROLE_TEACHER" })
@Controller
public class PausePeriodController {

  @Autowired
  private MessagePublisher redisPublisher;

  @MessageMapping("/pause/{runId}/{periodId}")
  public void pausePeriod(@DestinationVariable Integer runId, @DestinationVariable Integer periodId)
      throws Exception {
    JSONObject message = new JSONObject();
    message.put("type", "pause");
    message.put("topic", String.format("/topic/classroom/%s/%s", runId, periodId));
    redisPublisher.publish(message.toString());
  }

  @MessageMapping("/unpause/{runId}/{periodId}")
  public void unpausePeriod(@DestinationVariable Integer runId,
      @DestinationVariable Integer periodId) throws Exception {
    JSONObject message = new JSONObject();
    message.put("type", "unpause");
    message.put("topic", String.format("/topic/classroom/%s/%s", runId, periodId));
    redisPublisher.publish(message.toString());
  }
}
