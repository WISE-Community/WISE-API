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
import org.springframework.transaction.annotation.Transactional;
import org.wise.portal.dao.ObjectNotFoundException;
import org.wise.portal.domain.authentication.impl.StudentUserDetails;
import org.wise.portal.domain.run.Run;
import org.wise.portal.domain.user.User;
import org.wise.portal.domain.workgroup.Workgroup;
import org.wise.portal.service.user.UserService;
import org.wise.portal.service.workgroup.WorkgroupService;
import org.wise.portal.spring.data.redis.MessagePublisher;

@Secured({ "ROLE_STUDENT" })
@Controller
public class SendStudentWorkToClassmateController {
  
  @Autowired
  private MessagePublisher redisPublisher;

  @Autowired
  private UserService userService;

  @Autowired
  private WorkgroupService workgroupService;

  @Transactional
  @MessageMapping("/api/workgroup/{workgroupId}/student-work")
  public void sendStudentWorkToClassmate(Authentication auth,
      @DestinationVariable Long workgroupId, @Payload String studentWork)
      throws JSONException, ObjectNotFoundException {
    if (isInSameRun(auth, workgroupId)) {
      JSONObject message = new JSONObject();
      message.put("type", "classmateStudentWork");
      message.put("topic", String.format("/topic/workgroup/%s", workgroupId));
      message.put("studentWork", new JSONObject(studentWork));
      redisPublisher.publish(message.toString());
    }
  }

  private boolean isInSameRun(Authentication auth, Long toWorkgroupId)
      throws ObjectNotFoundException {
    Workgroup toWorkgroup = workgroupService.retrieveById(toWorkgroupId);
    Run run = toWorkgroup.getRun();
    User fromUser = userService.retrieveUser((StudentUserDetails) auth.getPrincipal());
    return run.isStudentAssociatedToThisRun(fromUser);
  }
}
