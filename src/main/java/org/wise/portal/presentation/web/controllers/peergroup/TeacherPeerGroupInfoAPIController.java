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
import org.wise.portal.domain.run.Run;
import org.wise.portal.service.peergroup.PeerGroupInfoService;
import org.wise.portal.service.peergroupactivity.PeerGroupActivityNotFoundException;
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

  @GetMapping("/{runId}/{nodeId}/{componentId}")
  public Map<String, Object> getPeerGroupsInfo(@PathVariable Long runId,
      @PathVariable String nodeId, @PathVariable String componentId, Authentication auth)
      throws ObjectNotFoundException, PeerGroupActivityNotFoundException {
    Run run = runService.retrieveById(runId);
    if (runService.hasReadPermission(auth, run)) {
      return peerGroupInfoService.getPeerGroupInfo(peerGroupActivityService.getByComponent(run,
          nodeId, componentId));
    } else {
      throw new AccessDeniedException("Not permitted");
    }
  }
}
