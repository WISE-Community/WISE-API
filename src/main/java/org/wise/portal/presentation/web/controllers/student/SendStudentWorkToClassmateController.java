package org.wise.portal.presentation.web.controllers.student;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.wise.portal.spring.data.redis.MessagePublisher;

@Secured({ "ROLE_STUDENT" })
@Controller
public class SendStudentWorkToClassmateController {
  
  @Autowired
  private MessagePublisher redisPublisher;

  @MessageMapping("/api/workgroup/{workgroupId}/student-work")
  public void sendStudentWorkToClassmate(Authentication auth,
      @DestinationVariable Long workgroupId, @Payload String studentWork) throws JSONException {
    JSONObject message = new JSONObject();
    message.put("type", "classmateStudentWork");
    message.put("topic", String.format("/topic/workgroup/%s", workgroupId));
    message.put("studentWork", new JSONObject(studentWork));
    redisPublisher.publish(message.toString());
  }
}
