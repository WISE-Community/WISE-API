package org.wise.portal.presentation.web.controllers.peergroup;

import java.util.List;

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
import org.wise.portal.service.peergroup.PeerGroupService;
import org.wise.portal.service.peergroupactivity.PeerGroupActivityNotFoundException;
import org.wise.portal.service.peergroupactivity.PeerGroupActivityService;
import org.wise.portal.service.run.RunService;

/**
 * @author Hiroki Terashima
 */
@RestController
@Secured("ROLE_TEACHER")
@RequestMapping("/api/teacher/peer-group")
public class TeacherPeerGroupAPIController {

  @Autowired
  private PeerGroupActivityService peerGroupActivityService;

  @Autowired
  private PeerGroupService peerGroupService;

  @Autowired
  private RunService runService;

  @GetMapping("/{runId}/{nodeId}/{componentId}")
  List<PeerGroup> getPeerGroups(@PathVariable Long runId, @PathVariable String nodeId,
      @PathVariable String componentId, Authentication auth)
      throws ObjectNotFoundException, PeerGroupActivityNotFoundException {
    Run run = runService.retrieveById(runId);
    if (runService.hasReadPermission(auth, run)) {
      PeerGroupActivity activity = peerGroupActivityService.getByComponent(run, nodeId,
          componentId);
      return peerGroupService.getPeerGroups(activity);
    } else {
      throw new AccessDeniedException("Not permitted");
    }
  }
}
