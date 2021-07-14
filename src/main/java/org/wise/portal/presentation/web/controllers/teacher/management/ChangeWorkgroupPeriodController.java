package org.wise.portal.presentation.web.controllers.teacher.management;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.wise.portal.dao.ObjectNotFoundException;
import org.wise.portal.domain.group.Group;
import org.wise.portal.domain.run.Run;
import org.wise.portal.domain.workgroup.Workgroup;
import org.wise.portal.service.group.GroupService;
import org.wise.portal.service.run.RunService;
import org.wise.portal.service.workgroup.WorkgroupService;

@Secured({ "ROLE_TEACHER" })
@RestController
public class ChangeWorkgroupPeriodController {

  @Autowired
  private GroupService groupService;

  @Autowired
  private RunService runService;

  @Autowired
  private WorkgroupService workgroupService;

  @PostMapping("/api/teacher/run/{runId}/team/{workgroupId}/change-period")
  void changeWorkgroupPeriod(Authentication auth, @PathVariable Long runId,
      @PathVariable Long workgroupId, @RequestBody Long newPeriodId)
      throws ObjectNotFoundException {
    Run run = runService.retrieveById(runId);
    if (runService.hasWritePermission(auth, run)) {
      Workgroup workgroup = workgroupService.retrieveById(workgroupId);
      Group newPeriod = groupService.retrieveById(newPeriodId);
      workgroupService.changePeriod(workgroup, newPeriod);
    } else {
      throw new AccessDeniedException("User does not have permission to change period");
    }
  }
}
