package org.wise.portal.presentation.web.controllers.peergroup;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.acls.domain.BasePermission;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.wise.portal.domain.peergrouping.impl.PeerGroupingImpl;
import org.wise.portal.domain.project.Project;
import org.wise.portal.domain.run.impl.RunImpl;
import org.wise.portal.domain.user.User;
import org.wise.portal.presentation.web.response.ResponseEntityGenerator;
import org.wise.portal.service.acl.AclService;
import org.wise.portal.service.peergrouping.PeerGroupingService;
import org.wise.portal.service.user.UserService;

@RestController
@Secured("ROLE_TEACHER")
@RequestMapping("/api/run/{runId}/peer-grouping")
public class PeerGroupingAPIController {

  @Autowired
  private AclService<Project> aclService;

  @Autowired
  private PeerGroupingService peerGroupingService;

  @Autowired
  private UserService userService;

  @PostMapping
  Object create(Authentication auth, @PathVariable("runId") RunImpl run,
      @RequestBody PeerGroupingImpl peerGrouping) {
    if (isAuthorized(auth, run)) {
      try {
        return peerGroupingService.createPeerGrouping(run, peerGrouping);
      } catch (Exception e) {
        return ResponseEntityGenerator.createError("genericError");
      }
    } else {
      return ResponseEntityGenerator.createError("notAuthorized");
    }
  }

  @PutMapping("/{tag}")
  Object update(Authentication auth, @PathVariable("runId") RunImpl run,
      @PathVariable("tag") String tag, @RequestBody PeerGroupingImpl peerGrouping) {
    if (isAuthorized(auth, run)) {
      return peerGroupingService.updatePeerGrouping(run, tag, peerGrouping);
    } else {
      return ResponseEntityGenerator.createError("notAuthorized");
    }
  }

  Boolean isAuthorized(Authentication auth, RunImpl run) {
    User user = userService.retrieveUserByUsername(auth.getName());
    return aclService.hasPermission(run.getProject(), BasePermission.WRITE, user) || user.isAdmin();
  }
}
