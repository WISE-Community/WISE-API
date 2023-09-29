package org.wise.portal.presentation.web.controllers.teacher.management;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
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
  protected Long createWorkgroup(Authentication auth, @PathVariable Long runId,
      @PathVariable Long periodId, @RequestBody List<Long> userIds) throws ObjectNotFoundException {
    Run run = runService.retrieveById(runId);
    Group period = groupService.retrieveById(periodId);
    if (isAllowed(auth, run, period, userIds)) {
      removeUsersFromAllWorkgroupsInRun(run, userIds);
      HashSet<User> newGroupMembers = createNewGroupMembers(userIds);
      Workgroup newWorkgroup = workgroupService.createWorkgroup("Student", newGroupMembers, run,
          period);
      return newWorkgroup.getId();
    } else {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          "Error: Could not create workgroup");
    }
  }

  private boolean isAllowed(Authentication auth, Run run, Group period, List<Long> userIds)
      throws ObjectNotFoundException {
    return runService.hasWritePermission(auth, run) && isPeriodInRun(run, period)
        && areAllStudentsInPeriod(run, period, userIds);
  }

  private boolean isPeriodInRun(Run run, Group period) {
    return run.getPeriods().contains(period);
  }

  private boolean areAllStudentsInPeriod(Run run, Group period, List<Long> userIds)
      throws ObjectNotFoundException {
    Set<User> studentsInPeriod = period.getMembers();
    for (Long userId : userIds) {
      User user = userService.retrieveById(userId);
      if (!studentsInPeriod.contains(user)) {
        return false;
      }
    }
    return true;
  }

  private void removeUsersFromAllWorkgroupsInRun(Run run, List<Long> userIds)
      throws ObjectNotFoundException {
    for (Long userId : userIds) {
      User user = userService.retrieveById(userId);
      workgroupService.removeUserFromAllWorkgroupsInRun(run, user);
    }
  }

  private HashSet<User> createNewGroupMembers(List<Long> userIds) throws ObjectNotFoundException {
    HashSet<User> newGroupMembers = new HashSet<User>();
    for (Long userId : userIds) {
      User user = userService.retrieveById(userId);
      newGroupMembers.add(user);
    }
    return newGroupMembers;
  }
}
