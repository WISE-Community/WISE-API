package org.wise.portal.presentation.web;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.wise.portal.domain.authentication.impl.TeacherUserDetails;
import org.wise.portal.domain.user.User;
import org.wise.portal.presentation.web.controllers.teacher.TeacherAPIController;

@RestController
@RequestMapping("/api/teacher/verify")
public class TeacherVerificationAPIController extends TeacherAPIController {
  
  @GetMapping()
  @Secured({ "ROLE_ANONYMOUS" })
  public void verifyTeacherAndRedirect(@RequestParam String code, HttpServletResponse response,
      HttpServletRequest request) throws IOException {
    User user = userService.retrieveTeacherByVerificationCode(code);
    boolean verified = verifyTeacher(user, request);
    String link = getLoginLink(request, user, verified);
    sendWelcomeEmail(user, link, request);
    response.sendRedirect(link);
  }

  private boolean verifyTeacher(User user, HttpServletRequest request) {
    if (user != null) {
      TeacherUserDetails tud = (TeacherUserDetails) user.getUserDetails();
      return verifyTeacherAccount(user, tud, request);
    } else {
      return false;
    }
  }

  private String getLoginLink(HttpServletRequest request, User user, boolean verified) {
    String link;
    if (user == null) {
      link = "/login?verified=error";
    } else if (!isTeacher(user)) {
      link = getRedirectLink(user, false);
    } else {
      link = getRedirectLink(user, verified);
    }
    return link;
  }

  private void sendWelcomeEmail(User user, String link, HttpServletRequest request) {
    if (link.contains("verified=true")) {
      TeacherUserDetails tud = (TeacherUserDetails) user.getUserDetails();
      this.mailService.sendWelcomeTeacherEmail(tud.getEmailAddress(), tud.getDisplayname(), tud.getUsername(),
          false, request.getLocale(), request);
    }
  }

  private String getRedirectLink(User user, boolean verified) {
    return String.format("/login?verified=%s&username=%s", 
        verified, user.getUserDetails().getUsername());
  }

  private boolean verifyTeacherAccount(User user, TeacherUserDetails tud, HttpServletRequest request) {
    if (!tud.isVerified()) {
      tud.setVerified(true);
      userService.updateUser(user);
      return true;
    } else {
      return false;
    }
  }
}
