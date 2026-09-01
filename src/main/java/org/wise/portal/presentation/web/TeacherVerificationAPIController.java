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
    boolean verified = verifyTeacher(user);
    String link = getLoginLink(user, verified);
    sendWelcomeEmail(user, link, request);
    response.sendRedirect(link);
  }

  private boolean verifyTeacher(User user) {
    if (user != null) {
      TeacherUserDetails tud = (TeacherUserDetails) user.getUserDetails();
      return verifyTeacherAccount(user, tud);
    } else {
      return false;
    }
  }

  private String getLoginLink(User user, boolean verified) {
    StringBuilder link = new StringBuilder("/login?verified=");
    link.append(user == null ? "error" : verified);
    if (user != null) {
      link.append("&username=").append(user.getUserDetails().getUsername());
    }
    return link.toString();
  }

  private void sendWelcomeEmail(User user, String link, HttpServletRequest request) {
    if (link.contains("verified=true")) {
      TeacherUserDetails tud = (TeacherUserDetails) user.getUserDetails();
      this.teacherMailService.sendWelcomeEmail(tud.getEmailAddress(), tud.getDisplayname(), tud.getUsername(),
          false, request.getLocale(), request);
    }
  }

  private boolean verifyTeacherAccount(User user, TeacherUserDetails tud) {
    if (!tud.isVerified()) {
      tud.setVerified(true);
      userService.updateUser(user);
      return true;
    } else {
      return false;
    }
  }
}
