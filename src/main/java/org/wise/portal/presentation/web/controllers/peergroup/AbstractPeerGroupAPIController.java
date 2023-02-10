package org.wise.portal.presentation.web.controllers.peergroup;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.wise.portal.domain.peergroup.PeerGroup;
import org.wise.portal.domain.user.User;
import org.wise.portal.service.peergroup.PeerGroupService;
import org.wise.portal.service.peergrouping.PeerGroupingService;
import org.wise.portal.service.run.RunService;
import org.wise.portal.service.user.UserService;

abstract class AbstractPeerGroupAPIController {
  @Autowired
  protected PeerGroupingService peerGroupingService;

  @Autowired
  protected PeerGroupService peerGroupService;

  @Autowired
  protected RunService runService;

  @Autowired
  protected UserService userService;

  protected boolean isUserInPeerGroup(PeerGroup peerGroup, Authentication auth) {
    User user = userService.retrieveUserByUsername(auth.getName());
    return peerGroup.isMember(user);
  }
}
