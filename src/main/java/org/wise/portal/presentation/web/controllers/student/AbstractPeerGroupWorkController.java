package org.wise.portal.presentation.web.controllers.student;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.wise.portal.dao.ObjectNotFoundException;
import org.wise.portal.domain.peergroup.PeerGroup;
import org.wise.portal.domain.run.Run;
import org.wise.portal.domain.user.User;
import org.wise.portal.service.vle.wise5.StudentWorkService;

public abstract class AbstractPeerGroupWorkController extends ClassmateDataController {

  @Autowired
  protected StudentWorkService studentWorkService;

  protected boolean isUserInPeerGroup(Authentication auth, PeerGroup peerGroup)
      throws ObjectNotFoundException {
    User user = userService.retrieveUserByUsername(auth.getName());
    return peerGroup.isMember(user);
  }

  protected boolean isUserTeacherOfPeerGroup(Authentication auth, PeerGroup peerGroup) {
    Run run = peerGroup.getPeerGrouping().getRun();
    return isTeacherOfRun(auth, run);
  }
}
