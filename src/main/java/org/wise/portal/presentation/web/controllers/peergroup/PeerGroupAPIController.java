package org.wise.portal.presentation.web.controllers.peergroup;

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

@RestController
@Secured("ROLE_USER")
@RequestMapping("/api/peer-group")
public class PeerGroupAPIController {

  @Autowired
  private RunService runService;

  @Autowired
  private UserService userService;

  @Autowired
  private WorkgroupService workgroupService;

  @Autowired
  private PeerGroupService peerGroupService;

  @Autowired
  private PeerGroupActivityService peerGroupActivityService;

  @GetMapping("/run/{runId}/workgroup/{workgroupId}/node-id/{nodeId}/component-id/{componentId}")
  PeerGroup getPeerGroup(@PathVariable Long runId, @PathVariable Long workgroupId,
      @PathVariable String nodeId, @PathVariable String componentId, Authentication auth)
      throws ObjectNotFoundException, PeerGroupActivityNotFoundException,
      PeerGroupCreationException {
    Run run = runService.retrieveById(runId);
    Workgroup workgroup = workgroupService.retrieveById(workgroupId);
    User user = userService.retrieveUserByUsername(auth.getName());
    if (workgroupService.isUserInWorkgroupForRun(user, run, workgroup)) {
      return getPeerGroup(run, nodeId, componentId, workgroup);
    } else {
      throw new AccessDeniedException("Not permitted");
    }
  }

  private PeerGroup getPeerGroup(Run run, String nodeId, String componentId, Workgroup workgroup)
      throws PeerGroupActivityNotFoundException, PeerGroupCreationException {
    PeerGroupActivity activity = peerGroupActivityService.getByComponent(run, nodeId, componentId);
    try {
      return peerGroupService.getPeerGroup(workgroup, activity);
    } catch (PeerGroupActivityThresholdNotSatisfiedException e) {
      return null;
    }
  }
}
