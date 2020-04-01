package org.wise.portal.presentation.web.controllers.teacher.run;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.RestController;
import org.wise.portal.dao.ObjectNotFoundException;
import org.wise.portal.domain.run.Run;
import org.wise.portal.service.run.RunService;
import org.wise.portal.spring.data.redis.MessagePublisher;

@Secured({ "ROLE_TEACHER" })
@RestController
public class TeacherRunAPIController {

  @Autowired
  private RunService runService;

  @Autowired
  private MessagePublisher redisPublisher;

  @MessageMapping("/pause/{runId}/{periodId}")
  public void pausePeriod(@DestinationVariable Integer runId,
      @DestinationVariable Integer periodId) throws Exception {
    JSONObject message = new JSONObject();
    message.put("type", "pause");
    message.put("topic",
        String.format("/topic/classroom/%s/%s", runId, periodId));
    redisPublisher.publish(message.toString());
  }

  @MessageMapping("/unpause/{runId}/{periodId}")
  public void unpausePeriod(@DestinationVariable Integer runId,
      @DestinationVariable Integer periodId) throws Exception {
    JSONObject message = new JSONObject();
    message.put("type", "unpause");
    message.put("topic",
        String.format("/topic/classroom/%s/%s", runId, periodId));
    redisPublisher.publish(message.toString());
  }

  @MessageMapping("/api/teacher/run/{runId}/workgroup-to-node/{workgroupId}")
  public void sendWorkgroupToNode(Authentication auth,
      @DestinationVariable Long runId, @DestinationVariable String workgroupId,
      String nodeId) throws ObjectNotFoundException, JSONException {
    Run run = runService.retrieveById(runId);
    if (runService.hasReadPermission(auth, run)) {
      JSONObject msg = new JSONObject();
      msg.put("type", "goToNode");
      msg.put("nodeId", nodeId);
      msg.put("topic", String.format("/topic/workgroup/%s", workgroupId));
      redisPublisher.publish(msg.toString());
    }
  }

  @MessageMapping("/api/teacher/run/{runId}/workgroup-to-next-node/{workgroupId}")
  public void sendWorkgroupToNextNode(Authentication auth,
      @DestinationVariable Long runId, @DestinationVariable String workgroupId)
      throws ObjectNotFoundException, JSONException {
    Run run = runService.retrieveById(runId);
    if (runService.hasReadPermission(auth, run)) {
      JSONObject msg = new JSONObject();
      msg.put("type", "goToNextNode");
      msg.put("topic", String.format("/topic/workgroup/%s", workgroupId));
      redisPublisher.publish(msg.toString());
    }
  }

  @MessageMapping("/api/teacher/run/{runId}/period-to-node/{periodId}")
  public void sendPeriodToNode(Authentication auth,
      @DestinationVariable Long runId, @DestinationVariable Long periodId,
      String nodeId) throws ObjectNotFoundException, JSONException {
    Run run = runService.retrieveById(runId);
    if (runService.hasReadPermission(auth, run)) {
      JSONObject msg = new JSONObject();
      msg.put("type", "goToNode");
      msg.put("nodeId", nodeId);
      msg.put("topic",
          String.format("/topic/classroom/%s/%s", runId, periodId));
      redisPublisher.publish(msg.toString());
    }
  }
}
