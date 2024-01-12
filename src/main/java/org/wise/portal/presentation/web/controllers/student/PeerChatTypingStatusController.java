package org.wise.portal.presentation.web.controllers.student;

import javax.transaction.Transactional;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.wise.portal.dao.ObjectNotFoundException;
import org.wise.portal.domain.peergroup.PeerGroup;
import org.wise.portal.service.peergroup.PeerGroupService;
import org.wise.portal.spring.data.redis.MessagePublisher;

@Secured({ "ROLE_TEACHER", "ROLE_STUDENT" })
@Controller
public class PeerChatTypingStatusController extends AbstractPeerGroupWorkController {

  @Autowired
  private MessagePublisher redisPublisher;

  @Autowired
  private PeerGroupService peerGroupService;

  @Transactional()
  @MessageMapping("/api/peer-chat/{nodeId}/{componentId}/{peerGroupId}/{workgroupId}/is-typing")
  public void sendTypingStatusToPeerGroup(Authentication auth,
      @DestinationVariable Long peerGroupId, @DestinationVariable String nodeId,
      @DestinationVariable String componentId, @DestinationVariable int workgroupId)
      throws JSONException, ObjectNotFoundException {
    PeerGroup peerGroup = peerGroupService.getById(peerGroupId);
    if (isUserInPeerGroup(auth, peerGroup) || isUserTeacherOfPeerGroup(auth, peerGroup)) {
      JSONObject message = new JSONObject();
      message.put("topic", String.format("/topic/peer-group/%s/is-typing", peerGroupId));
      message.put("type", "isTyping");
      JSONObject body = new JSONObject();
      body.put("nodeId", nodeId);
      body.put("componentId", componentId);
      body.put("workgroupId", workgroupId);
      message.put("body", body);
      redisPublisher.publish(message.toString());
    }
  }
}
