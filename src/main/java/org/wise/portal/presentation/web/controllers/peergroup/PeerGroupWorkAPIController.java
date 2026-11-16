package org.wise.portal.presentation.web.controllers.peergroup;

import java.util.List;

import org.json.JSONException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.wise.portal.domain.peergroup.PeerGroup;
import org.wise.portal.domain.peergrouping.PeerGrouping;
import org.wise.portal.domain.run.Run;
import org.wise.portal.domain.user.User;
import org.wise.portal.domain.workgroup.Workgroup;
import org.wise.portal.service.peergroup.PeerGroupCreationException;
import org.wise.portal.service.peergrouping.PeerGroupingNotFoundException;
import org.wise.vle.domain.work.StudentWork;

@RestController
@Secured("ROLE_USER")
@RequestMapping("/api/peer-group")
public class PeerGroupWorkAPIController extends AbstractPeerGroupAPIController {

  @GetMapping("/{peerGroupId}/{nodeId}/{componentId}/student-work")
  List<StudentWork> getPeerGroupWork(@PathVariable("peerGroupId") PeerGroup peerGroup,
      @PathVariable String nodeId, @PathVariable String componentId, Authentication auth) {
    if (isUserInPeerGroup(peerGroup, auth)) {
      return peerGroupService.getStudentWork(peerGroup, nodeId, componentId);
    } else {
      throw new AccessDeniedException("Not permitted");
    }
  }

  @Secured("ROLE_TEACHER")
  @GetMapping("/{runId}/{workgroupId}/{nodeId}/{componentId}/student-work")
  List<StudentWork> getPeerGroupWork(@PathVariable("runId") Run run,
      @PathVariable("workgroupId") Workgroup workgroup, @PathVariable String nodeId,
      @PathVariable String componentId, Authentication auth)
      throws JSONException, PeerGroupingNotFoundException, PeerGroupCreationException {
    User user = userService.retrieveUserByUsername(auth.getName());
    if (runService.isAllowedToViewStudentWork(run, user)) {
      PeerGrouping peerGrouping = peerGroupingService.getByComponent(run, nodeId, componentId);
      PeerGroup peerGroup = peerGroupService.getPeerGroup(workgroup, peerGrouping);
      return peerGroupService.getStudentWork(peerGroup, nodeId, componentId);
    } else {
      throw new AccessDeniedException("Not permitted");
    }
  }
}
