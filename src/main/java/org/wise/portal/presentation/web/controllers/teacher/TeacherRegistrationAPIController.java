package org.wise.portal.presentation.web.controllers.teacher;

import java.util.Locale;
import java.util.Map;

import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.wise.portal.domain.authentication.Schoollevel;
import org.wise.portal.domain.authentication.impl.TeacherUserDetails;
import org.wise.portal.domain.user.User;
import org.wise.portal.presentation.web.controllers.ControllerUtil;
import org.wise.portal.presentation.web.exception.InvalidNameException;
import org.wise.portal.presentation.web.response.ResponseEntityGenerator;
import org.wise.portal.service.authentication.DuplicateUsernameException;


@RestController
@RequestMapping("/api/teacher/register")
public class TeacherRegistrationAPIController extends TeacherAPIController {

  @PostMapping()
  @Secured({ "ROLE_ANONYMOUS" })
  ResponseEntity<Map<String, Object>> createTeacherAccount(
      @RequestBody Map<String, String> teacherFields, HttpServletRequest request)
      throws DuplicateUsernameException, InvalidNameException {
    if (ControllerUtil.isReCaptchaEnabled()) {
      String token = teacherFields.get("token");
      if (!ControllerUtil.isReCaptchaResponseValid(token)) {
        return ResponseEntityGenerator.createError("recaptchaResponseInvalid");
      }
    }
    TeacherUserDetails tud = new TeacherUserDetails();
    String firstName = teacherFields.get("firstName");
    String lastName = teacherFields.get("lastName");
    if (!isFirstNameAndLastNameValid(firstName, lastName)) {
      String messageCode = this.getInvalidNameMessageCode(firstName, lastName);
      throw new InvalidNameException(messageCode);
    }
    tud.setFirstname(firstName);
    tud.setLastname(lastName);
    String email = teacherFields.get("email");
    tud.setEmailAddress(email);
    tud.setCity(teacherFields.get("city"));
    tud.setState(teacherFields.get("state"));
    tud.setCountry(teacherFields.get("country"));
    String googleUserId = teacherFields.get("googleUserId");
    String microsoftUserId = teacherFields.get("microsoftUserId");
    if (isSet(googleUserId)) {
      tud.setGoogleUserId(googleUserId);
      tud.setPassword(RandomStringUtils.random(10, true, true));
    } else if (isSet(microsoftUserId)) {
      tud.setMicrosoftUserId(microsoftUserId);
      tud.setPassword(RandomStringUtils.random(10, true, true));
    } else {
      String password = teacherFields.get("password");
      if (!passwordService.isValid(password)) {
        return ResponseEntityGenerator.createError(passwordService.getErrors(password));
      } else {
        tud.setPassword(password);
      }
    }
    String displayName = firstName + " " + lastName;
    tud.setDisplayname(displayName);
    tud.setEmailValid(true);
    tud.setSchoollevel(Schoollevel.valueOf(teacherFields.get("schoolLevel")));
    tud.setSchoolname(teacherFields.get("schoolName"));
    tud.setHowDidYouHearAboutUs(teacherFields.get("howDidYouHearAboutUs"));
    Locale locale = request.getLocale();
    tud.setLanguage(locale.getLanguage());
    User createdUser = this.userService.createUser(tud);
    String username = createdUser.getUserDetails().getUsername();
    String sendEmailEnabledStr = appProperties.getProperty("send_email_enabled", "false");
    Boolean iSendEmailEnabled = Boolean.valueOf(sendEmailEnabledStr);
    boolean socialAccount = this.isSet(googleUserId) || this.isSet(microsoftUserId);
    if (iSendEmailEnabled) {
      sendCreateTeacherAccountEmail(email, displayName, username, socialAccount, locale, request);
    }
    return createRegisterSuccessResponse(username);
  }

  private void sendCreateTeacherAccountEmail(String email, String displayName, String username,
      boolean socialAccount, Locale locale, HttpServletRequest request) {
    String fromEmail = appProperties.getProperty("portalemailaddress");
    String[] recipients = { email };
    String defaultSubject = messageSource.getMessage(
        "presentation.web.controllers.teacher.registerTeacherController.welcomeTeacherEmailSubject",
        null, Locale.US);
    String subject = messageSource.getMessage(
        "presentation.web.controllers.teacher.registerTeacherController.welcomeTeacherEmailSubject",
        null, defaultSubject, locale);
    String defaultBody = messageSource.getMessage(
        "presentation.web.controllers.teacher.registerTeacherController.welcomeTeacherEmailBody",
        new Object[] { username }, Locale.US);
    String gettingStartedUrl = getGettingStartedUrl(request);
    String message;
    if (socialAccount) {
      message = messageSource.getMessage(
          "presentation.web.controllers.teacher.registerTeacherController.welcomeTeacherEmailBodyNoUsername",
          new Object[] { displayName, gettingStartedUrl }, defaultBody, locale);
    } else {
      message = messageSource.getMessage(
          "presentation.web.controllers.teacher.registerTeacherController.welcomeTeacherEmailBody",
          new Object[] { displayName, username, gettingStartedUrl }, defaultBody, locale);
    }
    try {
      mailService.postMail(recipients, subject, message, fromEmail);
    } catch (MessagingException e) {
      e.printStackTrace();
    }
  }

  private String getGettingStartedUrl(HttpServletRequest request) {
    return ControllerUtil.getPortalUrlString(request) + "/help/getting-started";
  }

  private boolean isSet(String value) {
    return value != null && !value.isEmpty();
  }
}
