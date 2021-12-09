package org.wise.portal.presentation.web.controllers.peergroup;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.wise.portal.domain.group.impl.PersistentGroup;
import org.wise.portal.domain.peergroup.PeerGroup;
import org.wise.portal.domain.peergroupactivity.PeerGroupActivity;
import org.wise.portal.domain.run.impl.RunImpl;
import org.wise.portal.service.peergroup.PeerGroupCreateService;
import org.wise.portal.service.peergroupactivity.PeerGroupActivityNotFoundException;
import org.wise.portal.service.peergroupactivity.PeerGroupActivityService;
import org.wise.portal.service.run.RunService;

@RestController
@Secured("ROLE_TEACHER")
@RequestMapping("/api/peer-group/create")
public class PeerGroupCreateController {

  @Autowired
  private PeerGroupActivityService peerGroupActivityService;

  @Autowired
  private PeerGroupCreateService peerGroupCreateService;

  @Autowired
  private RunService runService;

  @PostMapping("/{runId}/{periodId}/{nodeId}/{componentId}")
  PeerGroup create(@PathVariable("runId") RunImpl run,
      @PathVariable("periodId") PersistentGroup period, @PathVariable String nodeId,
      @PathVariable String componentId, Authentication auth)
      throws PeerGroupActivityNotFoundException {
    if (canCreatePeerGroup(run, period, auth)) {
      PeerGroupActivity activity =
          peerGroupActivityService.getByComponent(run, nodeId, componentId);
      return peerGroupCreateService.create(activity, period);
    }
    throw new AccessDeniedException("Not permitted");
  }

  private boolean canCreatePeerGroup(RunImpl run, PersistentGroup period, Authentication auth) {
    return runService.hasWritePermission(auth, run) && run.getPeriods().contains(period);
  }
}
