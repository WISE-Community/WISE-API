package org.wise.portal.presentation.web.controllers.student;

import java.io.IOException;
import java.util.List;

import org.json.JSONException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.wise.portal.dao.ObjectNotFoundException;
import org.wise.portal.domain.peergroup.impl.PeerGroupImpl;
import org.wise.portal.domain.project.impl.ProjectComponent;
import org.wise.portal.domain.run.Run;
import org.wise.vle.domain.work.StudentWork;

@RestController
@Secured({ "ROLE_STUDENT", "ROLE_TEACHER" })
@RequestMapping("/api/classmate/peer-group-work")
public class PeerGroupWorkController extends AbstractPeerGroupWorkController {

  String SHOW_PEER_GROUP_WORK_TYPE = "ShowGroupWork";

  @GetMapping("{peerGroupId}/{showPeerGroupWorkNodeId}/{showPeerGroupWorkComponentId}/{showWorkNodeId}/{showWorkComponentId}")
  public List<StudentWork> getPeerGroupWork(Authentication auth,
      @PathVariable("peerGroupId") PeerGroupImpl peerGroup,
      @PathVariable String showPeerGroupWorkNodeId,
      @PathVariable String showPeerGroupWorkComponentId, @PathVariable String showWorkNodeId,
      @PathVariable String showWorkComponentId)
      throws ObjectNotFoundException, JSONException, IOException {
    if (isUserInPeerGroup(auth, peerGroup) || isUserTeacherOfPeerGroup(auth, peerGroup)) {
      Run run = peerGroup.getPeerGrouping().getRun();
      if (isValidShowPeerGroupWorkComponent(run, showPeerGroupWorkNodeId,
          showPeerGroupWorkComponentId, showWorkNodeId, showWorkComponentId)) {
        return studentWorkService.getLatestStudentWork(peerGroup.getMembers(), showWorkNodeId,
            showWorkComponentId);
      }
    }
    throw new AccessDeniedException(NOT_PERMITTED);
  }

  private boolean isValidShowPeerGroupWorkComponent(Run run, String nodeId, String componentId,
      String showWorkNodeId, String showWorkComponentId)
      throws JSONException, IOException, ObjectNotFoundException {
    ProjectComponent projectComponent = getProjectComponent(run, nodeId, componentId);
    return projectComponent.getString("type").equals(SHOW_PEER_GROUP_WORK_TYPE)
        && projectComponent.getString("showWorkNodeId").equals(showWorkNodeId)
        && projectComponent.getString("showWorkComponentId").equals(showWorkComponentId);
  }
}
