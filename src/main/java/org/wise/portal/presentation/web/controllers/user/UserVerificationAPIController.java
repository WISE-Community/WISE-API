package org.wise.portal.presentation.web.controllers.user;

import org.springframework.web.bind.annotation.RestController;
import org.wise.portal.domain.authentication.impl.TeacherUserDetails;
import org.wise.portal.domain.user.User;
import org.wise.portal.service.user.UserService;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;


@RestController
@RequestMapping("/api/user")
public class UserVerificationAPIController {

  private final UserService userService;

  public UserVerificationAPIController(UserService userService) {
    this.userService = userService;
  }

  @GetMapping("/verify")
  public void openVerificationLink(@RequestParam String code, @RequestParam String username,
                                   HttpServletResponse response) throws IOException {
    boolean verified = this.verifyTeacherAccount(code);
    response.sendRedirect("/login?verified=" + verified + "&username=" + username);
  }
  
  private boolean verifyTeacherAccount(String verificationCode) {
    User user = userService.retrieveTeacherByVerificationCode(verificationCode);
    if (this.isTeacher(user)) {
      TeacherUserDetails tud = (TeacherUserDetails) user.getUserDetails();
      if (!tud.isVerified()) {
        tud.setVerified(true);
        userService.updateUser(user);
        return true;
      }
    }
    return false;
  }

  private boolean isTeacher(User user) {
    return user != null && !user.getRoles().contains("ROLE_STUDENT");
  }

  @GetMapping("/is-verified")
  public boolean isUserVerified(@RequestParam String username) {
    User user = userService.retrieveTeacherByUsername(username);
    return !this.isTeacher(user) || this.isTeacherVerified(user); // Only teachers need to verify their accounts
  }

  private boolean isTeacherVerified(User user) {
    TeacherUserDetails tud = (TeacherUserDetails) user.getUserDetails();
    return tud.isVerified();
  }
}
