package org.wise.portal.presentation.web.controllers.peergroup;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.wise.portal.dao.ObjectNotFoundException;
import org.wise.portal.domain.run.impl.RunImpl;
import org.wise.portal.service.peergroup.PeerGroupInfoService;
import org.wise.portal.service.peergroupactivity.PeerGroupActivityService;
import org.wise.portal.service.run.RunService;

/**
 * @author Hiroki Terashima
 */
@RestController
@Secured("ROLE_TEACHER")
@RequestMapping("/api/teacher/peer-group-info")
public class TeacherPeerGroupInfoAPIController {

  @Autowired
  private PeerGroupActivityService peerGroupActivityService;

  @Autowired
  private PeerGroupInfoService peerGroupInfoService;

  @Autowired
  private RunService runService;

  @GetMapping("/{runId}/{peerGroupActivityTag}")
  public Map<String, Object> getPeerGroupsInfo(@PathVariable("runId") RunImpl run,
      @PathVariable String peerGroupActivityTag, Authentication auth)
      throws ObjectNotFoundException {
    if (runService.hasReadPermission(auth, run)) {
      return peerGroupInfoService.getPeerGroupInfo(peerGroupActivityService.getByTag(run,
          peerGroupActivityTag));
    } else {
      throw new AccessDeniedException("Not permitted");
    }
  }
}
