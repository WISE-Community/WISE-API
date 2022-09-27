package org.wise.portal.presentation.web.controllers.teacher;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.util.Date;
import java.util.Map;

import org.easymock.EasyMockRunner;
import org.easymock.Mock;
import org.easymock.TestSubject;
import org.json.JSONException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.wise.portal.domain.user.User;
import org.wise.portal.presentation.web.controllers.APIControllerTest;
import org.wise.portal.service.password.PasswordService;

@RunWith(EasyMockRunner.class)
public class TeacherForgotAccountAPIControllerTest extends APIControllerTest {

  @TestSubject
  private TeacherForgotAccountAPIController teacherForgotAccountAPIController = new TeacherForgotAccountAPIController();

  @Mock
  private PasswordService passwordService;

  String PASSWORD_INVALID_LENGTH = "1234567";
  String PASSWORD_INVALID_PATTERN = "abcd1234";
  String VERIFICATION_CODE = "123456";

  @Test
  public void changePassword_InvalidPasswordLength_ReturnError() throws JSONException {
    expect(userService.retrieveUserByUsername(TEACHER_USERNAME)).andReturn(teacher1);
    expect(passwordService.isValidLength(PASSWORD_INVALID_LENGTH)).andReturn(false);
    replayServices();
    setVerificationCode(teacher1, VERIFICATION_CODE);
    ResponseEntity<Map<String, Object>> response = teacherForgotAccountAPIController.changePassword(
        TEACHER_USERNAME, VERIFICATION_CODE, PASSWORD_INVALID_LENGTH, PASSWORD_INVALID_LENGTH);
    assertResponseValues(response, HttpStatus.BAD_REQUEST, "invalidPasswordLength");
    verifyServices();
  }

  @Test
  public void changePassword_InvalidPasswordPattern_ReturnError() throws JSONException {
    expect(userService.retrieveUserByUsername(TEACHER_USERNAME)).andReturn(teacher1);
    expect(passwordService.isValidLength(PASSWORD_INVALID_PATTERN)).andReturn(true);
    expect(passwordService.isValidPattern(PASSWORD_INVALID_PATTERN)).andReturn(false);
    replayServices();
    setVerificationCode(teacher1, VERIFICATION_CODE);
    ResponseEntity<Map<String, Object>> response = teacherForgotAccountAPIController.changePassword(
        TEACHER_USERNAME, VERIFICATION_CODE, PASSWORD_INVALID_PATTERN, PASSWORD_INVALID_PATTERN);
    assertResponseValues(response, HttpStatus.BAD_REQUEST, "invalidPasswordPattern");
    verifyServices();
  }

  private void setVerificationCode(User user, String verificationCode) {
    user.getUserDetails().setResetPasswordVerificationCodeRequestTime(new Date());
    user.getUserDetails().setResetPasswordVerificationCode(verificationCode);
  }

  private void assertResponseValues(ResponseEntity<Map<String, Object>> response,
      HttpStatus expectedStatus, String expectedMessageCode) {
    assertEquals(expectedStatus, response.getStatusCode());
    assertEquals(expectedMessageCode, response.getBody().get("messageCode"));
  }

  private void replayServices() {
    replay(passwordService, userService);
  }

  private void verifyServices() {
    verify(passwordService, userService);
  }
}
