package org.wise.portal.presentation.web.controllers.peergroup;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.wise.portal.domain.peergroup.PeerGroup;
import org.wise.portal.domain.peergroup.impl.PeerGroupImpl;
import org.wise.portal.domain.peergrouping.PeerGrouping;
import org.wise.portal.domain.run.Run;
import org.wise.portal.domain.run.impl.RunImpl;
import org.wise.portal.domain.user.User;
import org.wise.portal.domain.workgroup.Workgroup;
import org.wise.portal.domain.workgroup.impl.WorkgroupImpl;
import org.wise.portal.service.peergroup.PeerGroupCreationException;
import org.wise.portal.service.peergrouping.PeerGroupingNotFoundException;
import org.wise.portal.service.workgroup.WorkgroupService;
import org.wise.vle.domain.annotation.wise5.Annotation;

@RestController
@Secured("ROLE_USER")
@RequestMapping("/api/peer-group")
public class PeerGroupAnnotationsAPIController extends AbstractPeerGroupAPIController {
  @Autowired
  private WorkgroupService workgroupService;

  @GetMapping("/{peerGroupId}/{nodeId}/{componentId}/annotations")
  List<Annotation> getPeerGroupAnnotations(@PathVariable("peerGroupId") PeerGroupImpl peerGroup,
      @PathVariable String nodeId, @PathVariable String componentId, Authentication auth) {
    if (isUserInPeerGroup(peerGroup, auth)) {
      List<Workgroup> teacherWorkgroups = getTeacherWorkgroups(
          peerGroup.getPeerGrouping().getRun());
      return peerGroupService.getStudentAnnotations(peerGroup, nodeId, componentId,
          teacherWorkgroups);
    } else {
      throw new AccessDeniedException("Not permitted");
    }
  }

  @Secured("ROLE_TEACHER")
  @GetMapping("/{runId}/{workgroupId}/{nodeId}/{componentId}/annotations")
  List<Annotation> getPeerGroupAnnotations(@PathVariable("runId") RunImpl run,
      @PathVariable("workgroupId") WorkgroupImpl workgroup, @PathVariable String nodeId,
      @PathVariable String componentId, Authentication auth)
      throws JSONException, PeerGroupingNotFoundException, PeerGroupCreationException {
    User user = userService.retrieveUserByUsername(auth.getName());
    if (runService.isAllowedToViewStudentWork(run, user)) {
      PeerGrouping peerGrouping = peerGroupingService.getByComponent(run, nodeId, componentId);
      PeerGroup peerGroup = peerGroupService.getPeerGroup(workgroup, peerGrouping);
      List<Workgroup> teacherWorkgroups = getTeacherWorkgroups(run);
      return peerGroupService.getStudentAnnotations(peerGroup, nodeId, componentId,
          teacherWorkgroups);
    } else {
      throw new AccessDeniedException("Not permitted");
    }
  }

  List<Workgroup> getTeacherWorkgroups(Run run) {
    List<Workgroup> teacherWorkgroups = new ArrayList<Workgroup>();
    User runOwner = run.getOwner();
    List<Workgroup> workgroupsForRunOwner = workgroupService.getWorkgroupListByRunAndUser(run,
        runOwner);
    teacherWorkgroups.addAll(workgroupsForRunOwner);
    for (User sharedOwner : run.getSharedowners()) {
      List<Workgroup> sharedTeacherWorkgroups = workgroupService.getWorkgroupListByRunAndUser(run,
          sharedOwner);
      teacherWorkgroups.addAll(sharedTeacherWorkgroups);
    }
    return teacherWorkgroups;
  }
}
