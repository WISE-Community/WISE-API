package org.wise.portal.presentation.web.controllers.teacher.management;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
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

  @PutMapping("/api/teacher/run/{runId}/student/{studentId}/change-password")
  void changeStudentPassword(Authentication auth, @PathVariable Long runId,
      @PathVariable Long studentId, HttpServletRequest servletRequest)
      throws ObjectNotFoundException {
    Run run = runService.retrieveById(runId);
    User teacherUser = userService.retrieveUserByUsername(auth.getName());
    Boolean isTeacherGoogleUser = teacherUser.getUserDetails().isGoogleUser();
    String teacherPassword = teacherUser.getUserDetails().getPassword();
    String teacherPasswordSubmitted = servletRequest.getParameter("teacherPassword");
    if (runService.hasWritePermission(auth, run)
        && isTeacherPasswordValid(isTeacherGoogleUser, teacherPassword, teacherPasswordSubmitted)) {
      User studentUser = userService.retrieveById(studentId);
      String newPassword = servletRequest.getParameter("newStudentPassword");
      userService.updateUserPassword(studentUser, newPassword);
    } else {
      throw new AccessDeniedException(
          "User does not have permission to change this student's password");
    }
  }

  private Boolean isTeacherPasswordValid(Boolean isGoogleUser, String teacherPassword,
      String teacherPasswordSubmitted) {
    return isGoogleUser || teacherPassword.equals(teacherPasswordSubmitted);
  }
}
