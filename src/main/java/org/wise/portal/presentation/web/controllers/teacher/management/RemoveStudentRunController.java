package org.wise.portal.presentation.web.controllers.teacher.management;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.wise.portal.dao.ObjectNotFoundException;
import org.wise.portal.domain.run.Run;
import org.wise.portal.domain.user.User;
import org.wise.portal.service.run.RunService;
import org.wise.portal.service.student.StudentService;
import org.wise.portal.service.user.UserService;

@Secured({ "ROLE_TEACHER" })
@RestController
public class RemoveStudentRunController {

  @Autowired
  private RunService runService;

  @Autowired
  private StudentService studentService;

  @Autowired
  private UserService userService;

  @DeleteMapping("/api/teacher/run/{runId}/student/{studentId}/remove")
  public void removeStudent(Authentication auth, @PathVariable Long runId,
      @PathVariable Long studentId) throws ObjectNotFoundException {
    Run run = runService.retrieveById(runId);
    if (runService.hasWritePermission(auth, run)) {
      User studentUser = userService.retrieveById(studentId);
      studentService.removeStudentFromRun(studentUser, run);
    } else {
      throw new AccessDeniedException("User does not have permission to remove student from run");
    }
  }
}
