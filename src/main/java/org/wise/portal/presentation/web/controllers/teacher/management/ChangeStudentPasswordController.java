package org.wise.portal.presentation.web.controllers.teacher.management;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.wise.portal.dao.ObjectNotFoundException;
import org.wise.portal.domain.run.Run;
import org.wise.portal.domain.user.User;
import org.wise.portal.service.run.RunService;
import org.wise.portal.service.user.UserService;

@Secured({ "ROLE_TEACHER" })
@RestController
public class ChangeStudentPasswordController {

  @Autowired
  private RunService runService;

  @Autowired
  private UserService userService;

  @PostMapping("/api/teacher/run/{runId}/student/{studentId}/change-password")
  void changeStudentPassword(Authentication auth, @PathVariable Long runId,
      @PathVariable Long studentId, @RequestParam String teacherPassword,
      @RequestParam String newStudentPassword, HttpServletRequest servletRequest)
      throws ObjectNotFoundException {
    Run run = runService.retrieveById(runId);
    User teacherUser = userService.retrieveUserByUsername(auth.getName());
    Boolean isTeacherGoogleUser = teacherUser.getUserDetails().isGoogleUser();
    if (runService.hasWritePermission(auth, run)
        && isTeacherPasswordValid(isTeacherGoogleUser, teacherUser, teacherPassword)) {
      User studentUser = userService.retrieveById(studentId);
      userService.updateUserPassword(studentUser, newStudentPassword);
    } else {
      throw new AccessDeniedException(
          "User does not have permission to change this student's password");
    }
  }

  private Boolean isTeacherPasswordValid(Boolean isGoogleUser, User teacherUser,
      String teacherPasswordSubmitted) {
    return isGoogleUser || userService.isPasswordCorrect(teacherUser, teacherPasswordSubmitted);
  }
}
