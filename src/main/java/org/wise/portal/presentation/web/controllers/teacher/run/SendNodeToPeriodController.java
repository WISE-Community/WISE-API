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
import org.springframework.transaction.annotation.Transactional;
import org.wise.portal.dao.ObjectNotFoundException;
import org.wise.portal.domain.Tag;
import org.wise.portal.domain.run.Run;
import org.wise.portal.domain.workgroup.Workgroup;
import org.wise.portal.service.run.RunService;
import org.wise.portal.service.tag.TagService;
import org.wise.portal.spring.data.redis.MessagePublisher;

@Secured({ "ROLE_TEACHER" })
@Controller
public class SendNodeToPeriodController {

  @Autowired
  private MessagePublisher redisPublisher;

  @Autowired
  private RunService runService;

  @Autowired
  private TagService tagService;

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

  @MessageMapping("/api/teacher/run/{runId}/workgroup-to-node/{workgroupId}/{nodeId}")
  public void sendWorkgroupToNode(Authentication auth,
      @DestinationVariable Long runId, @DestinationVariable String workgroupId,
      @DestinationVariable String nodeId) throws ObjectNotFoundException, JSONException {
    Run run = runService.retrieveById(runId);
    if (runService.hasReadPermission(auth, run)) {
      this.publishWorkgroupToNodeMessage(workgroupId, nodeId);
    }
  }

  @MessageMapping("/api/teacher/run/{runId}/group-to-node/{groupId}/{nodeId}")
  @Transactional
  public void sendGroupToNode(Authentication auth,
      @DestinationVariable Long runId, @DestinationVariable Integer groupId,
      @DestinationVariable String nodeId) throws ObjectNotFoundException, JSONException {
    Run run = runService.retrieveById(runId);
    if (runService.hasReadPermission(auth, run)) {
      Tag tag = tagService.getTagById(groupId);
      for (Workgroup workgroup : runService.getWorkgroups(runId)) {
        if (workgroup.getTags().contains(tag)) {
          this.publishWorkgroupToNodeMessage(workgroup.getId().toString(), nodeId);
        }
      }
    }
  }

  private void publishWorkgroupToNodeMessage(String workgroupId, String nodeId) throws JSONException {
    JSONObject msg = new JSONObject();
    msg.put("type", "goToNode");
    msg.put("nodeId", nodeId);
    msg.put("topic", String.format("/topic/workgroup/%s", workgroupId));
    redisPublisher.publish(msg.toString());
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

  @MessageMapping("/api/teacher/run/{runId}/period-to-node/{periodId}/{nodeId}")
  public void sendPeriodToNode(Authentication auth,
      @DestinationVariable Long runId, @DestinationVariable Long periodId,
      @DestinationVariable String nodeId) throws ObjectNotFoundException, JSONException {
    Run run = runService.retrieveById(runId);
    if (runService.hasReadPermission(auth, run)) {
      JSONObject msg = new JSONObject();
      msg.put("type", "goToNode");
      msg.put("nodeId", nodeId);
      msg.put("topic", String.format("/topic/classroom/%s/%s", runId, periodId));
      redisPublisher.publish(msg.toString());
    }
  }
}
