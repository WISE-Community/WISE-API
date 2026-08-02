package org.wise.portal.presentation.web.controllers.teacher;

import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.dao.DataIntegrityViolationException;
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
    sendNewTeacherEmail(request, locale, isSocialAccount, tud, username);
    return createRegisterSuccessResponse(username);
  }

  private void sendNewTeacherEmail(HttpServletRequest request, Locale locale, boolean isSocialAccount,
      TeacherUserDetails tud, String username) {
    if (isSocialAccount) {
      this.teacherMailService.sendWelcomeTeacherEmail(tud.getEmailAddress(), tud.getDisplayname(), username, 
          true, locale, request);
    } else {
      this.teacherMailService.sendVerifyTeacherEmail(tud.getEmailAddress(), tud.getVerificationCode(), locale, request);
    }
  }

  private boolean isSocialAccount(Map<String, String> teacherFields) {
    return isSet(teacherFields.get("googleUserId")) || isSet(teacherFields.get("microsoftUserId"));
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
    tud.setVerified(isSocialAccount || !teacherMailService.isSendEmailEnabled());
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
}
