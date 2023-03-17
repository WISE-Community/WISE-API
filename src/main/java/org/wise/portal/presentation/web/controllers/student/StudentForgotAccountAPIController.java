package org.wise.portal.presentation.web.controllers.student;

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
    JSONObject response;
    if (ControllerUtil.isReCaptchaEnabled() && !ControllerUtil.isReCaptchaResponseValid(token)) {
      return ControllerUtil.createErrorResponse("recaptchaResponseInvalid").toString();
    }
    User user = userService.retrieveUserByUsername(username);
    if (user != null) {
      if (isAnswerCorrect(user, answer)) {
        response = ControllerUtil.createSuccessResponse("correctAnswer");
      } else {
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
    } else {
      if (isAnswerCorrect(user, answer)) {
        if (!passwordService.isValidLength(password)) {
          return ResponseEntityGenerator.createError("invalidPasswordLength");
        } else if (!passwordService.isValidPattern(password)) {
          return ResponseEntityGenerator.createError("invalidPasswordPattern");
        } else if (!isPasswordsMatch(password, confirmPassword)) {
          return ResponseEntityGenerator.createError("passwordsDoNotMatch");
        } else {
          userService.updateUserPassword(user, password);
          return ResponseEntityGenerator.createSuccess("passwordChanged");
        }
      } else {
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

  private boolean isPasswordBlank(String password1, String password2) {
    return password1 == null || password2 == null || password1.equals("") || password2.equals("");
  }

  private boolean isPasswordsMatch(String password1, String password2) {
    return password1.equals(password2);
  }
}
