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
import org.wise.portal.presentation.web.exception.InvalidPasswordException;
import org.wise.portal.presentation.web.exception.RecaptchaVerificationException;
import org.wise.portal.presentation.web.response.ResponseEntityGenerator;
import org.wise.portal.service.authentication.DuplicateUsernameException;

@RestController
@RequestMapping("/api/teacher/register")
public class TeacherRegistrationAPIController extends TeacherAPIController {

  private final String emailCodePrefix = 
    "presentation.web.controllers.teacher.registerTeacherController.welcomeTeacherEmail";
  private final String welcomeBodyCode = this.emailCodePrefix + "Body";
  private final String welcomeSocialAccountBodyCode = this.emailCodePrefix + "BodyNoUsername";
  private final String welcomeSubjectCode = this.emailCodePrefix + "Subject";
 
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
    TeacherUserDetails tud = createTeacherUserDetails(teacherFields, locale);
    User createdUser = this.userService.createUser(tud);
    String username = createdUser.getUserDetails().getUsername();
    if (isSendEmailEnabled()) {
      boolean socialAccount = isSet(tud.getGoogleUserId()) || isSet(tud.getMicrosoftUserId());
      sendWelcomeTeacherEmail(tud.getEmailAddress(), tud.getDisplayname(), username, 
                              socialAccount, locale, request);
    }
    return createRegisterSuccessResponse(username);
  }

  private Boolean isSendEmailEnabled() {
    String sendEmailEnabledStr = appProperties.getProperty("send_email_enabled", "false");
    return Boolean.valueOf(sendEmailEnabledStr);
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
                                                      Locale locale) {
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

  private boolean isSet(String value) {
    return value != null && !value.isEmpty();
  }

  private void setRandomPassword(TeacherUserDetails tud) {
    tud.setPassword(RandomStringUtils.random(10, true, true));
  }
}
