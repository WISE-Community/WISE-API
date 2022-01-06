package org.wise.portal.presentation.web.controllers.teacher.run;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.wise.portal.domain.run.impl.RunImpl;
import org.wise.portal.domain.user.User;
import org.wise.portal.service.user.UserService;
import org.wise.portal.service.vle.wise5.VLEService;
import org.wise.vle.domain.status.StudentStatus;

@Secured({ "ROLE_TEACHER" })
@RestController
public class TeacherRunAPIController {

  @Autowired
  private UserService userService;

  @Autowired
  private VLEService vleService;

  /**
   * Returns all student statuses for a given run
   */
  @GetMapping("/api/teacher/run/{runId}/student-status")
  public List<StudentStatus> getStudentStatus(Authentication auth,
      @PathVariable("runId") RunImpl run) {
    User user = userService.retrieveUserByUsername(auth.getName());
    if (run.isTeacherAssociatedToThisRun(user) || user.isAdmin()) {
      return vleService.getStudentStatusesByRunId(run.getId());
    }
    return new ArrayList<StudentStatus>();
  }
}
