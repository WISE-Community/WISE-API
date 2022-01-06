package org.wise.portal.presentation.web.controllers.teacher.run;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.wise.portal.dao.ObjectNotFoundException;
import org.wise.portal.domain.run.Run;
import org.wise.portal.service.run.RunService;
import org.wise.portal.spring.data.redis.MessagePublisher;

@Secured({ "ROLE_TEACHER" })
@Controller
public class SendNodeToPeriodController {

  @Autowired
  private MessagePublisher redisPublisher;

  @Autowired
  private RunService runService;

  @MessageMapping("/api/teacher/run/{runId}/node-to-period/{periodId}")
  public void sendNodeToPeriod(Authentication auth, @DestinationVariable Long runId,
      @DestinationVariable Long periodId, @Payload String node)
      throws ObjectNotFoundException, JSONException {
    Run run = runService.retrieveById(runId);
    if (runService.hasReadPermission(auth, run)) {
      JSONObject msg = new JSONObject();
      msg.put("type", "node");
      msg.put("node", new JSONObject(node));
      msg.put("topic", String.format("/topic/classroom/%s/%s", run.getId(), periodId));
      redisPublisher.publish(msg.toString());
    }
  }
}
