package org.wise.portal.presentation.web.controllers.teacher;

import java.io.IOException;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

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
      this.mailService.sendWelcomeTeacherEmail(tud.getEmailAddress(), tud.getDisplayname(), username, 
                                true, locale, request);
    } else {
      this.mailService.sendVerifyTeacherEmail(tud.getEmailAddress(), tud.getVerificationCode(), locale, request);
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
  public void verifyTeacherAndRedirect(@RequestParam String code, HttpServletResponse response,
                                       HttpServletRequest request) throws IOException {
    User user = userService.retrieveTeacherByVerificationCode(code);
    String link = verifyTeacherIfNecessaryAndGetLoginLink(request, user);
    sendWelcomeEmailIfNecessary(user, link, request);
    response.sendRedirect(link);
  }

  private String verifyTeacherIfNecessaryAndGetLoginLink(HttpServletRequest request, User user) {
    String link;
    if (user == null) {
      link = "/login?verified=error";
    } else if (!isTeacher(user)) {
      link = getRedirectLink(user, false);
    } else {
      TeacherUserDetails tud = (TeacherUserDetails) user.getUserDetails();
      boolean verified = verifyTeacherAccount(user, tud, request);
      link = getRedirectLink(user, verified);
    }
    return link;
  }

  private void sendWelcomeEmailIfNecessary(User user, String link, HttpServletRequest request) {
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

  @PostMapping("send-verify-email")
  @Secured({ "ROLE_ANONYMOUS" })
  ResponseEntity<Map<String, Object>> sendVerificationEmail(@RequestParam String username, 
                                                            HttpServletRequest request) {
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
