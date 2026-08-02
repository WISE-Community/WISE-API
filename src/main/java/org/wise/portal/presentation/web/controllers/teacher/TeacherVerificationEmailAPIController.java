package org.wise.portal.presentation.web.controllers.teacher;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.wise.portal.domain.authentication.impl.TeacherUserDetails;
import org.wise.portal.domain.user.User;
import org.wise.portal.presentation.web.response.ResponseEntityGenerator;

@RestController
@RequestMapping("/api/teacher/send-verify-email")
public class TeacherVerificationEmailAPIController extends TeacherAPIController {
  
  @PostMapping()
  @Secured({ "ROLE_ANONYMOUS" })
  ResponseEntity<Map<String, Object>> sendVerificationEmail(@RequestParam String username, 
                                                            HttpServletRequest request) { // TODO: indent
    User user = userService.retrieveTeacherByUsername(username);
    if (isTeacher(user)) {
      TeacherUserDetails tud = (TeacherUserDetails) user.getUserDetails();
      if (tud.isVerified()) {
        return ResponseEntityGenerator.createError("Teacher already verified");
      } else {
        this.mailService.sendVerifyTeacherEmail(tud.getEmailAddress(), tud.getVerificationCode(), 
                                                request.getLocale(), request);
        return createRegisterSuccessResponse(username);
      }
    } else {
      return ResponseEntityGenerator.createError("Not a teacher"); 
    }
  }
}
