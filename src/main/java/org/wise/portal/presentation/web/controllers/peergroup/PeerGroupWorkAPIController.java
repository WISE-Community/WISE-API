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
import org.wise.portal.dao.ObjectNotFoundException;
import org.wise.portal.domain.peergroup.PeerGroup;
import org.wise.portal.domain.peergroupactivity.PeerGroupActivity;
import org.wise.portal.domain.run.Run;
import org.wise.portal.domain.user.User;
import org.wise.portal.domain.workgroup.Workgroup;
import org.wise.portal.service.peergroup.PeerGroupActivityThresholdNotSatisfiedException;
import org.wise.portal.service.peergroup.PeerGroupCreationException;
import org.wise.portal.service.peergroup.PeerGroupService;
import org.wise.portal.service.peergroupactivity.PeerGroupActivityNotFoundException;
import org.wise.portal.service.peergroupactivity.PeerGroupActivityService;
import org.wise.portal.service.run.RunService;
import org.wise.portal.service.user.UserService;
import org.wise.portal.service.workgroup.WorkgroupService;
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

  @Autowired
  private WorkgroupService workgroupService;

  @GetMapping("/{peerGroupId}/student-work")
  List<StudentWork> getPeerGroupWork(@PathVariable Long peerGroupId, Authentication auth)
      throws ObjectNotFoundException {
    PeerGroup peerGroup = peerGroupService.getById(peerGroupId);
    if (isUserInPeerGroup(peerGroup, auth)) {
      return peerGroupService.getStudentWork(peerGroup);
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
  List<StudentWork> getPeerGroupWork(@PathVariable Long runId, @PathVariable Long workgroupId,
      @PathVariable String nodeId, @PathVariable String componentId, Authentication auth)
      throws JSONException, ObjectNotFoundException, PeerGroupActivityNotFoundException,
      PeerGroupActivityThresholdNotSatisfiedException, PeerGroupCreationException {
    Run run = runService.retrieveById(runId);
    User user = userService.retrieveUserByUsername(auth.getName());
    if (runService.isAllowedToViewStudentWork(run, user)) {
      PeerGroupActivity activity = peerGroupActivityService.getByComponent(run, nodeId,
          componentId);
      Workgroup workgroup = workgroupService.retrieveById(workgroupId);
      PeerGroup peerGroup = peerGroupService.getPeerGroup(workgroup, activity);
      return peerGroupService.getStudentWork(peerGroup);
    } else {
      throw new AccessDeniedException("Not permitted");
    }
  }
}
