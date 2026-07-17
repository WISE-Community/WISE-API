package org.wise.portal.presentation.web.controllers.teacher;

import java.io.IOException;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.wise.portal.domain.authentication.Schoollevel;
import org.wise.portal.domain.authentication.impl.TeacherUserDetails;
import org.wise.portal.domain.user.User;
import org.wise.portal.presentation.web.controllers.ControllerUtil;
import org.wise.portal.presentation.web.exception.InvalidNameException;
import org.wise.portal.presentation.web.exception.InvalidPasswordException;
import org.wise.portal.presentation.web.exception.RecaptchaVerificationException;
import org.wise.portal.presentation.web.response.ResponseEntityGenerator;
import org.wise.portal.service.authentication.DuplicateUsernameException;

@RestController
@RequestMapping("/api/teacher/register")
public class TeacherRegistrationAPIController extends TeacherAPIController {

  private final String emailCodePrefix = 
    "presentation.web.controllers.teacher.registerTeacherController.";
  private final String verifyBodyCode = this.emailCodePrefix + "verifyTeacherEmailBody";
  private final String verifySubjectCode = this.emailCodePrefix + "verifyTeacherEmailSubject";
  private final String welcomeBodyCode = this.emailCodePrefix + "welcomeTeacherEmailBody";
  private final String welcomeSocialAccountBodyCode = this.emailCodePrefix + "welcomeTeacherEmailBodyNoUsername";
  private final String welcomeSubjectCode = this.emailCodePrefix + "welcomeTeacherEmailSubject";
 
  @PostMapping()
  @Secured({ "ROLE_ANONYMOUS" })
  ResponseEntity<Map<String, Object>> createTeacherAccount(
      @RequestBody Map<String, String> teacherFields, HttpServletRequest request)
      throws DuplicateUsernameException, InvalidNameException {
    try {
      validateTeacherFields(teacherFields);
    } catch (RecaptchaVerificationException e) {
      return ResponseEntityGenerator.createError("recaptchaResponseInvalid");
    } catch (InvalidPasswordException e) {
      return ResponseEntityGenerator.createError(
        passwordService.getErrors(teacherFields.get("password")));
    }
    Locale locale = request.getLocale();
    boolean isSocialAccount = isSocialAccount(teacherFields);
    TeacherUserDetails tud = createTeacherUserDetails(teacherFields, isSocialAccount, locale);
    User createdUser = this.userService.createUser(tud);
    String username = createdUser.getUserDetails().getUsername();
    if (isSendEmailEnabled()) {
      if (isSocialAccount) {
        sendWelcomeTeacherEmail(tud.getEmailAddress(), tud.getDisplayname(), username, 
                                true, locale, request);
      } else {
        sendVerifyTeacherEmail(tud.getEmailAddress(), tud.getVerificationCode(), locale, request);
      }
    }
    return createRegisterSuccessResponse(username);
  }

  private boolean isSendEmailEnabled() {
    String sendEmailEnabledStr = appProperties.getProperty("send_email_enabled", "false");
    return Boolean.valueOf(sendEmailEnabledStr);
  }

  private boolean isSocialAccount(Map<String, String> teacherFields) {
      return isSet(teacherFields.get("googleUserId")) || isSet(tud.getMicrosoftUserId());
  }

  private void sendWelcomeTeacherEmail(String email, String displayName, String username,
                                       boolean socialAccount, Locale locale, 
                                       HttpServletRequest request) {
    String subject = getEmailMessage(this.welcomeSubjectCode, this.welcomeSubjectCode, null, locale);
    String body = getWelcomeTeacherBody(displayName, username, socialAccount, locale, request);
    this.sendEmail(email, subject, body);
  }

  private String getEmailMessage(String defaultCode, String code, Object[] args, Locale locale) {
    String defaultMessage = messageSource.getMessage(defaultCode, args, Locale.US);
    return messageSource.getMessage(code, args, defaultMessage, locale);
  }

  private String getWelcomeTeacherBody(String displayName, String username, boolean socialAccount,
                                       Locale locale, HttpServletRequest request) {
    String gettingStartedUrl = getGettingStartedUrl(request);
    String code = socialAccount ? this.welcomeSocialAccountBodyCode : this.welcomeBodyCode;
    Object[] args = socialAccount 
      ? new Object[] { displayName, gettingStartedUrl } 
      : new Object[] { displayName, username, gettingStartedUrl };
    return getEmailMessage(this.welcomeBodyCode, code, args, locale);
  }

  private String getGettingStartedUrl(HttpServletRequest request) {
    return ControllerUtil.getPortalUrlString(request) + "/help/getting-started";
  }

  private void sendVerifyTeacherEmail(String email, String verificationCode, 
                                      Locale locale, HttpServletRequest request) {
    String subject = getEmailMessage(this.verifySubjectCode, this.verifySubjectCode, null, locale);
    String verificationUrl = getVerificationUrl(verificationCode, request);
    Object[] args = new Object[] { verificationUrl };
    String body = getEmailMessage(this.verifyBodyCode, this.verifyBodyCode, args, locale);
    this.sendEmail(email, subject, body);
  }

  private String getVerificationUrl(String verificationCode, HttpServletRequest request) {
    return String.format("%s/api/teacher/register/verify?code=%s", 
                         ControllerUtil.getPortalUrlString(request), verificationCode);
  }

  private void sendEmail(String email, String subject, String body) {
    String fromEmail = appProperties.getProperty("portalemailaddress");
    String[] recipients = { email };
    try {
      mailService.postMail(recipients, subject, body, fromEmail);
    } catch (MessagingException e) {
      e.printStackTrace();
    }
  }

  private void validateTeacherFields(Map<String, String> teacherFields) 
    throws RecaptchaVerificationException, InvalidNameException, InvalidPasswordException {
    validateReCaptcha(teacherFields.get("token"));
    validateFirstAndLastName(teacherFields.get("firstName"), teacherFields.get("lastName"));
    validatePassword(teacherFields.get("password"));
  }

  private void validateReCaptcha(String token) throws RecaptchaVerificationException {
    if (ControllerUtil.isReCaptchaEnabled()) {
      if (!ControllerUtil.isReCaptchaResponseValid(token)) {
        throw new RecaptchaVerificationException("Invalid ReCaptcha Response");
      }
    }
  }

  private void validateFirstAndLastName(String firstName, String lastName) 
    throws InvalidNameException {
    if (!isFirstNameAndLastNameValid(firstName, lastName)) {
      String messageCode = this.getInvalidNameMessageCode(firstName, lastName);
      throw new InvalidNameException(messageCode);
    }
  }

  private void validatePassword(String password) throws InvalidPasswordException {
    if (password != null && !passwordService.isValid(password)) {
      throw new InvalidPasswordException();
    }
  }

  private TeacherUserDetails createTeacherUserDetails(Map<String, String> teacherFields, 
                                                      boolean isSocialAccount, Locale locale) {
    TeacherUserDetails tud = new TeacherUserDetails();
    tud.setFirstname(teacherFields.get("firstName"));
    tud.setLastname(teacherFields.get("lastName"));
    tud.setEmailAddress(teacherFields.get("email"));
    tud.setCity(teacherFields.get("city"));
    tud.setState(teacherFields.get("state"));
    tud.setCountry(teacherFields.get("country"));
    tud.setDisplayname(tud.getFirstname() + " " + tud.getLastname());
    tud.setSchoollevel(Schoollevel.valueOf(teacherFields.get("schoolLevel")));
    tud.setSchoolname(teacherFields.get("schoolName"));
    tud.setHowDidYouHearAboutUs(teacherFields.get("howDidYouHearAboutUs"));
    tud.setLanguage(locale.getLanguage());
    setPassword(teacherFields, tud);
    tud.setEmailValid(true);
    tud.setVerified(isSocialAccount);
    setVerificationCode(tud);
    return tud;
  }

  private void setPassword(Map<String, String> teacherFields, TeacherUserDetails tud) {
    String googleUserId = teacherFields.get("googleUserId");
    String microsoftUserId = teacherFields.get("microsoftUserId");
    if (isSet(googleUserId)) {
      tud.setGoogleUserId(googleUserId);
      setRandomPassword(tud);
    } else if (isSet(microsoftUserId)) {
      tud.setMicrosoftUserId(microsoftUserId);
      setRandomPassword(tud);
    } else {
      tud.setPassword(teacherFields.get("password"));
    }
  }
  
  private void setVerificationCode(TeacherUserDetails tud) {
    boolean isCodeSet = false;
    while (!isCodeSet) {
      try {
        tud.setVerificationCode(UUID.randomUUID().toString());
        isCodeSet = true;
      } catch (DataIntegrityViolationException e) {
        continue;
      }
    }
  }

  private boolean isSet(String value) {
    return value != null && !value.isEmpty();
  }

  private void setRandomPassword(TeacherUserDetails tud) {
    tud.setPassword(RandomStringUtils.random(10, true, true));
  }

  @GetMapping("/verify")
  @Secured({ "ROLE_ANONYMOUS" })
  public void openVerificationLink(@RequestParam String code, HttpServletResponse response, 
                                   HttpServletRequest request) throws IOException {
    boolean verified = this.verifyTeacherAccount(code, request);
    User user = userService.retrieveTeacherByVerificationCode(code);
    response.sendRedirect("/login?verified=" + verified + "&username=" + user.getUserDetails().getUsername());
  }

  private boolean verifyTeacherAccount(String verificationCode, HttpServletRequest request) {
    User user = userService.retrieveTeacherByVerificationCode(verificationCode);
    if (this.isTeacher(user)) {
      TeacherUserDetails tud = (TeacherUserDetails) user.getUserDetails();
      if (!tud.isVerified()) {
        tud.setVerified(true);
        userService.updateUser(user);
        sendWelcomeTeacherEmail(tud.getEmailAddress(), tud.getDisplayname(), tud.getUsername(), 
                                false, request.getLocale(), request);
        return true;
      }
    }
    return false;
  }

  @PostMapping("send-verify-email")
  @Secured({ "ROLE_ANONYMOUS" })
  ResponseEntity<Map<String, Object>> sendVerificationEmail(@RequestParam String username, 
                                                            HttpServletRequest request) {
    User user = userService.retrieveTeacherByUsername(username);
    if (this.isTeacher(user)) {
      TeacherUserDetails tud = (TeacherUserDetails) user.getUserDetails();
      if (tud.isVerified()) {
        return ResponseEntityGenerator.createError("Teacher already verified"); //TODO: error message codes point where?
      } else {
        sendVerifyTeacherEmail(tud.getEmailAddress(), tud.getVerificationCode(), 
                               request.getLocale(), request);
        return createRegisterSuccessResponse(username);
      }
    } else {
      return ResponseEntityGenerator.createError("Not a teacher"); //TODO: error message codes point where?
    }
  }
}
