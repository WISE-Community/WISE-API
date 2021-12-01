package org.wise.portal.presentation.web.controllers.student;

import java.io.IOException;
import java.util.List;

import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.wise.portal.dao.ObjectNotFoundException;
import org.wise.portal.domain.authentication.impl.StudentUserDetails;
import org.wise.portal.domain.peergroup.PeerGroup;
import org.wise.portal.domain.project.impl.ProjectComponent;
import org.wise.portal.domain.run.Run;
import org.wise.portal.domain.user.User;
import org.wise.portal.service.peergroup.PeerGroupService;
import org.wise.vle.domain.work.StudentWork;

@RestController
@Secured("ROLE_STUDENT")
@RequestMapping("/api/classmate/peer-chat")
public class ClassmatePeerChatDataController extends ClassmateDataController {

  String PEER_CHAT_TYPE = "PeerChat";

  @Autowired
  protected PeerGroupService peerGroupService;

  @GetMapping("/student-work/{peerGroupId}/{nodeId}/{componentId}/{showWorkNodeId}/{showWorkComponentId}")
  public List<StudentWork> getClassmatePeerChatWork(Authentication auth,
      @PathVariable Long peerGroupId, @PathVariable String nodeId, @PathVariable String componentId,
      @PathVariable String showWorkNodeId, @PathVariable String showWorkComponentId)
      throws IOException, JSONException, ObjectNotFoundException {
    PeerGroup peerGroup = peerGroupService.getById(peerGroupId);
    if (isAllowedToGetData(auth, peerGroup)) {
      Run run = peerGroup.getPeerGroupActivity().getRun();
      if (isValidPeerChatComponent(run, nodeId, componentId, showWorkNodeId, showWorkComponentId)) {
        return peerGroupService.getLatestStudentWork(peerGroup, showWorkNodeId, showWorkComponentId);
      }
    }
    throw new AccessDeniedException(NOT_PERMITTED);
  }

  private boolean isAllowedToGetData(Authentication auth, PeerGroup peerGroup)
      throws ObjectNotFoundException {
    User user = userService.retrieveUser((StudentUserDetails) auth.getPrincipal());
    return peerGroup.isMember(user);
  }

  private boolean isValidPeerChatComponent(Run run, String nodeId, String componentId,
      String showWorkNodeId, String showWorkComponentId)
      throws IOException, JSONException, ObjectNotFoundException {
    ProjectComponent projectComponent = getProjectComponent(run, nodeId, componentId);
    return projectComponent.getString("type").equals(PEER_CHAT_TYPE)
        && projectComponent.getString("showWorkNodeId").equals(showWorkNodeId)
        && projectComponent.getString("showWorkComponentId").equals(showWorkComponentId);
  }
}
