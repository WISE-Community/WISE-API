package org.wise.portal.presentation.web.controllers.student;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.util.Map;

import org.easymock.EasyMockRunner;
import org.easymock.Mock;
import org.easymock.TestSubject;
import org.json.JSONException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.wise.portal.presentation.web.controllers.APIControllerTest;
import org.wise.portal.service.password.PasswordService;

@RunWith(EasyMockRunner.class)
public class StudentForgotAccountAPIControllerTest extends APIControllerTest {

  @TestSubject
  private StudentForgotAccountAPIController studentForgotAccountAPIController = new StudentForgotAccountAPIController();

  @Mock
  private PasswordService passwordService;

  String PASSWORD_INVALID_LENGTH = "1234567";
  String PASSWORD_INVALID_PATTERN = "abcd1234";

  @Test
  public void changePassword_InvalidPasswordLength_ReturnError() throws JSONException {
    expect(userService.retrieveUserByUsername(STUDENT_USERNAME)).andReturn(student1);
    expect(passwordService.isValidLength(PASSWORD_INVALID_LENGTH)).andReturn(false);
    replayServices();
    ResponseEntity<Map<String, Object>> response = studentForgotAccountAPIController.changePassword(
        STUDENT_USERNAME, STUDENT1_ACCOUNT_ANSWER, PASSWORD_INVALID_LENGTH,
        PASSWORD_INVALID_LENGTH);
    assertResponseValues(response, HttpStatus.BAD_REQUEST, "invalidPasswordLength");
    verifyServices();
  }

  @Test
  public void changePassword_InvalidPasswordPattern_ReturnError() throws JSONException {
    expect(userService.retrieveUserByUsername(STUDENT_USERNAME)).andReturn(student1);
    expect(passwordService.isValidLength(PASSWORD_INVALID_PATTERN)).andReturn(true);
    expect(passwordService.isValidPattern(PASSWORD_INVALID_PATTERN)).andReturn(false);
    replayServices();
    ResponseEntity<Map<String, Object>> response = studentForgotAccountAPIController.changePassword(
        STUDENT_USERNAME, STUDENT1_ACCOUNT_ANSWER, PASSWORD_INVALID_PATTERN,
        PASSWORD_INVALID_PATTERN);
    assertResponseValues(response, HttpStatus.BAD_REQUEST, "invalidPasswordPattern");
    verifyServices();
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
