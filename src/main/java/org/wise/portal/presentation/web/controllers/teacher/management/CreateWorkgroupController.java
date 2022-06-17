package org.wise.portal.presentation.web.controllers.teacher.management;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;

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
import org.wise.portal.domain.user.User;
import org.wise.portal.domain.workgroup.Workgroup;
import org.wise.portal.service.group.GroupService;
import org.wise.portal.service.run.RunService;
import org.wise.portal.service.user.UserService;
import org.wise.portal.service.workgroup.WorkgroupService;

@Secured({ "ROLE_TEACHER" })
@RestController
public class CreateWorkgroupController {

  @Autowired
  private GroupService groupService;

  @Autowired
  private RunService runService;

  @Autowired
  private WorkgroupService workgroupService;

  @Autowired
  private UserService userService;

  @PostMapping("/api/teacher/run/{runId}/workgroup/create/{periodId}")
  long createWorkgroup(Authentication auth, @PathVariable Long runId,
      @PathVariable Long periodId, @RequestBody List<Long> userIds) throws ObjectNotFoundException {
    Run run = runService.retrieveById(runId);
    if (runService.hasWritePermission(auth, run)) {
      HashSet<User> newGroupMembers = new HashSet<User>();
      for (Long userId : userIds) {
        User user = userService.retrieveById(userId);
        List<Workgroup> workgroups = workgroupService.getWorkgroupListByRunAndUser(run, user);
        if (workgroups.size() > 0) {
          Workgroup workgroup = workgroups.get(0); // student can be in only one workgroup per run
          workgroupService.removeMembers(workgroup, Collections.singleton(user));
        }
        newGroupMembers.add(user);
      }
      Group period = groupService.retrieveById(periodId);
      Workgroup newWorkgroup =
          workgroupService.createWorkgroup("Student", newGroupMembers, run, period);
      return newWorkgroup.getId();
    } else {
      throw new AccessDeniedException("User does not have permission to change period");
    }
  }
}
