package org.wise.portal.presentation.web.controllers.peergroup;

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
import org.wise.portal.domain.peergroup.PeerGroup;
import org.wise.portal.domain.peergroup.impl.PeerGroupImpl;
import org.wise.portal.domain.peergroupactivity.PeerGroupActivity;
import org.wise.portal.domain.run.impl.RunImpl;
import org.wise.portal.domain.user.User;
import org.wise.portal.domain.workgroup.impl.WorkgroupImpl;
import org.wise.portal.service.peergroup.PeerGroupActivityThresholdNotSatisfiedException;
import org.wise.portal.service.peergroup.PeerGroupCreationException;
import org.wise.portal.service.peergroup.PeerGroupService;
import org.wise.portal.service.peergroupactivity.PeerGroupActivityNotFoundException;
import org.wise.portal.service.peergroupactivity.PeerGroupActivityService;
import org.wise.portal.service.run.RunService;
import org.wise.portal.service.user.UserService;
import org.wise.vle.domain.work.StudentWork;

@RestController
@Secured("ROLE_USER")
@RequestMapping("/api/peer-group")
public class PeerGroupWorkAPIController {

  @Autowired
  private PeerGroupActivityService peerGroupActivityService;

  @Autowired
  private PeerGroupService peerGroupService;

  @Autowired
  private RunService runService;

  @Autowired
  private UserService userService;

  @GetMapping("/{peerGroupId}/{nodeId}/{componentId}/student-work")
  List<StudentWork> getPeerGroupWork(@PathVariable("peerGroupId") PeerGroupImpl peerGroup,
      @PathVariable String nodeId, @PathVariable String componentId, Authentication auth) {
    if (isUserInPeerGroup(peerGroup, auth)) {
      return peerGroupService.getStudentWork(peerGroup, nodeId, componentId);
    } else {
      throw new AccessDeniedException("Not permitted");
    }
  }

  private boolean isUserInPeerGroup(PeerGroup peerGroup, Authentication auth) {
    User user = userService.retrieveUserByUsername(auth.getName());
    return peerGroup.isMember(user);
  }

  @Secured("ROLE_TEACHER")
  @GetMapping("/{runId}/{workgroupId}/{nodeId}/{componentId}/student-work")
  List<StudentWork> getPeerGroupWork(@PathVariable("runId") RunImpl run,
      @PathVariable("workgroupId") WorkgroupImpl workgroup,
      @PathVariable String nodeId, @PathVariable String componentId, Authentication auth)
      throws JSONException, PeerGroupActivityNotFoundException,
      PeerGroupActivityThresholdNotSatisfiedException, PeerGroupCreationException {
    User user = userService.retrieveUserByUsername(auth.getName());
    if (runService.isAllowedToViewStudentWork(run, user)) {
      PeerGroupActivity activity = peerGroupActivityService.getByComponent(run, nodeId,
          componentId);
      PeerGroup peerGroup = peerGroupService.getPeerGroup(workgroup, activity);
      return peerGroupService.getStudentWork(peerGroup, nodeId, componentId);
    } else {
      throw new AccessDeniedException("Not permitted");
    }
  }
}
