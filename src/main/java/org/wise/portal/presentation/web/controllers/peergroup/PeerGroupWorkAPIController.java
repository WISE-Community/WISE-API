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
import org.wise.portal.domain.user.User;
import org.wise.portal.service.peergroup.PeerGroupService;
import org.wise.portal.service.user.UserService;
import org.wise.vle.domain.work.StudentWork;

@RestController
@Secured("ROLE_USER")
@RequestMapping("/api/peer-group")
public class PeerGroupWorkAPIController {

  @Autowired
  private UserService userService;

  @Autowired
  private PeerGroupService peerGroupService;

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
}
