package org.wise.portal.presentation.web.controllers.teacher;

import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.wise.portal.domain.authentication.impl.TeacherUserDetails;
import org.wise.portal.domain.user.User;

@RestController
@RequestMapping("/api/teacher/is-verified")
public class TeacherIsVerifiedAPIController extends TeacherAPIController {

  @GetMapping()
  @Secured({ "ROLE_ANONYMOUS" })
  public boolean isVerifiedTeacherOrNonTeacher(@RequestParam String username) {
    User user = userService.retrieveTeacherByUsername(username);
    return !this.isTeacher(user) || this.isTeacherVerified(user); // Only teachers need to verify their accounts
  }

  private boolean isTeacherVerified(User user) {
    TeacherUserDetails tud = (TeacherUserDetails) user.getUserDetails();
    return tud.isVerified();
  }
}
