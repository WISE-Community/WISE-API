package org.wise.portal.presentation.web.controllers.peergroup;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.wise.portal.domain.peergroup.PeerGroup;
import org.wise.portal.domain.peergroup.impl.PeerGroupImpl;
import org.wise.portal.domain.workgroup.impl.WorkgroupImpl;
import org.wise.portal.service.peergroup.PeerGroupMembershipService;
import org.wise.portal.service.run.RunService;

@Secured("ROLE_TEACHER")
@RestController
@RequestMapping("/api/peer-group/membership")
public class PeerGroupMembershipController {

  @Autowired
  private PeerGroupMembershipService peerGroupMembershipService;

  @Autowired
  private RunService runService;

  @PostMapping("/add/{peerGroupId}/{workgroupId}")
  PeerGroup addMember(@PathVariable("peerGroupId") PeerGroupImpl peerGroup,
      @PathVariable("workgroupId") WorkgroupImpl workgroup, Authentication auth) {
    if (runService.hasWritePermission(auth, peerGroup.getPeerGroupActivity().getRun())) {
      return peerGroupMembershipService.addMember(peerGroup, workgroup);
    }
    throw new AccessDeniedException("Not permitted");
  }
}
