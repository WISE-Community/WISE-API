package org.wise.portal.presentation.web.controllers.student;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.wise.portal.domain.authentication.MutableUserDetails;
import org.wise.portal.domain.authentication.impl.StudentUserDetails;
import org.wise.portal.domain.user.User;
import org.wise.portal.presentation.web.controllers.ControllerUtil;
import org.wise.portal.presentation.web.response.ResponseEntityGenerator;
import org.wise.portal.service.password.PasswordService;
import org.wise.portal.service.user.UserService;

@RestController
@RequestMapping(value = "/api/student/forgot", produces = "application/json;charset=UTF-8")
public class StudentForgotAccountAPIController {

  @Autowired
  private PasswordService passwordService;

  @Autowired
  private UserService userService;

  @Autowired
  private Properties i18nProperties;

  private static final int MAX_FAILED_ANSWER_ATTEMPTS = 5;

  private static final int FAILED_ANSWER_ATTEMPT_WINDOW_MINUTES = 10;

  @GetMapping("/username/search")
  protected String getStudentUsernames(@RequestParam("firstName") String firstName,
      @RequestParam("lastName") String lastName, @RequestParam("birthMonth") Integer birthMonth,
      @RequestParam("birthDay") Integer birthDay) {
    List<User> accountsThatMatch = userService.retrieveStudentsByNameAndBirthday(firstName,
        lastName, birthMonth, birthDay);
    return getUsernamesJSON(accountsThatMatch).toString();
  }

  private JSONArray getUsernamesJSON(List<User> users) {
    JSONArray usernamesJSON = new JSONArray();
    for (User user : users) {
      MutableUserDetails userDetails = user.getUserDetails();
      usernamesJSON.put(userDetails.getUsername());
    }
    return usernamesJSON;
  }

  @GetMapping("/password/security-question")
  protected String getSecurityQuestion(@RequestParam("username") String username)
      throws JSONException {
    User user = userService.retrieveUserByUsername(username);
    JSONObject response;
    if (user != null && user.isStudent()) {
      String accountQuestionKey = getAccountQuestionKey(user);
      String accountQuestionValue = getAccountQuestionValue(accountQuestionKey);
      response = ControllerUtil.createSuccessResponse("usernameFound");
      response.put("question", accountQuestionValue);
      response.put("questionKey", accountQuestionKey);
    } else {
      response = ControllerUtil.createErrorResponse("usernameNotFound");
    }
    return response.toString();
  }

  @PostMapping("/password/security-question")
  protected String checkSecurityAnswer(@RequestParam("username") String username,
      @RequestParam("answer") String answer, @RequestParam("token") String token)
      throws JSONException {
    if (ControllerUtil.isReCaptchaEnabled() && !ControllerUtil.isReCaptchaResponseValid(token)) {
      return ControllerUtil.createErrorResponse("recaptchaResponseInvalid").toString();
    }
    User user = userService.retrieveUserByUsername(username);
    JSONObject response;
    if (user != null) {
      if (isTooManyFailedAnswerAttempts(user)) {
        response = ControllerUtil.createErrorResponse("tooManyFailedAnswerAttempts");
      } else if (isAnswerCorrect(user, answer)) {
        clearFailedAnswerAttempts(user);
        response = ControllerUtil.createSuccessResponse("correctAnswer");
      } else {
        recordFailedAnswerAttempt(user);
        response = ControllerUtil.createErrorResponse("incorrectAnswer");
      }
    } else {
      response = ControllerUtil.createErrorResponse("invalidUsername");
    }
    return response.toString();
  }

  @PostMapping("/password/change")
  protected ResponseEntity<Map<String, Object>> changePassword(
      @RequestParam("username") String username, @RequestParam("answer") String answer,
      @RequestParam("password") String password,
      @RequestParam("confirmPassword") String confirmPassword) throws JSONException {
    User user = userService.retrieveUserByUsername(username);
    if (user == null) {
      return ResponseEntityGenerator.createError("invalidUsername");
    } else if (isTooManyFailedAnswerAttempts(user)) {
      return ResponseEntityGenerator.createError("tooManyFailedAnswerAttempts");
    } else {
      if (isAnswerCorrect(user, answer)) {
        if (!passwordService.isValid(password)) {
          return ResponseEntityGenerator.createError(passwordService.getErrors(password));
        } else if (!isPasswordsMatch(password, confirmPassword)) {
          return ResponseEntityGenerator.createError("passwordsDoNotMatch");
        } else {
          clearFailedAnswerAttempts(user);
          userService.updateUserPassword(user, password);
          return ResponseEntityGenerator.createSuccess("passwordChanged");
        }
      } else {
        recordFailedAnswerAttempt(user);
        return ResponseEntityGenerator.createError("incorrectAnswer");
      }
    }
  }

  private String getAccountQuestionKey(User user) {
    return ((StudentUserDetails) user.getUserDetails()).getAccountQuestion();
  }

  private String getAccountQuestionValue(String accountQuestionKey) {
    return i18nProperties.getProperty("accountquestions." + accountQuestionKey);
  }

  private String getAccountAnswer(User user) {
    return ((StudentUserDetails) user.getUserDetails()).getAccountAnswer();
  }

  private boolean isAnswerCorrect(User user, String answer) {
    String accountSecurityAnswer = getAccountAnswer(user);
    return answer != null && answer.equals(accountSecurityAnswer);
  }

  /**
   * The security answer is compared in plain text and answers are low entropy, so without a
   * limit an attacker could brute force the answer and take over a student account. Allow only
   * a small number of failed attempts within a short window before the reset is temporarily
   * blocked, matching the throttling the teacher password reset flow already uses. The counter
   * reuses the shared failed password reset verification fields on the user details.
   */
  private boolean isTooManyFailedAnswerAttempts(User user) {
    MutableUserDetails userDetails = user.getUserDetails();
    Date recentFailedAttemptTime = userDetails.getRecentFailedVerificationCodeAttemptTime();
    Integer failedAttempts = userDetails.getNumberOfRecentFailedVerificationCodeAttempts();
    return recentFailedAttemptTime != null 
        && failedAttempts != null
        && isWithinAttemptWindow(recentFailedAttemptTime)
        && failedAttempts >= MAX_FAILED_ANSWER_ATTEMPTS;
  }

  private void recordFailedAnswerAttempt(User user) {
    MutableUserDetails userDetails = user.getUserDetails();
    if (!isWithinAttemptWindow(userDetails.getRecentFailedVerificationCodeAttemptTime())) {
      userDetails.clearNumberOfRecentFailedVerificationCodeAttempts();
    }
    userDetails.setRecentFailedVerificationCodeAttemptTime(new Date());
    userDetails.incrementNumberOfRecentFailedVerificationCodeAttempts();
    userService.updateUser(user);
  }

  private void clearFailedAnswerAttempts(User user) {
    MutableUserDetails userDetails = user.getUserDetails();
    userDetails.clearRecentFailedVerificationCodeAttemptTime();
    userDetails.clearNumberOfRecentFailedVerificationCodeAttempts();
    userService.updateUser(user);
  }

  private boolean isWithinAttemptWindow(Date date) {
    if (date == null) {
      return false;
    }
    long difference = new Date().getTime() - date.getTime();
    return difference < ControllerUtil.convertMinutesToMilliseconds(FAILED_ANSWER_ATTEMPT_WINDOW_MINUTES);
  }

  private boolean isPasswordBlank(String password1, String password2) {
    return password1 == null || password2 == null || password1.equals("") || password2.equals("");
  }

  private boolean isPasswordsMatch(String password1, String password2) {
    return password1.equals(password2);
  }
}
