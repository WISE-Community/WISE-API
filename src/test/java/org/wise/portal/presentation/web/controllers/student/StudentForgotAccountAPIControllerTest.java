package org.wise.portal.presentation.web.controllers.student;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.util.Map;

import org.easymock.EasyMockRunner;
import org.easymock.TestSubject;
import org.json.JSONException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.wise.portal.presentation.web.controllers.APIControllerTest;
import org.wise.portal.service.password.impl.PasswordServiceImpl;

@RunWith(EasyMockRunner.class)
public class StudentForgotAccountAPIControllerTest extends APIControllerTest {

  @TestSubject
  private StudentForgotAccountAPIController studentForgotAccountAPIController = new StudentForgotAccountAPIController();

  @Before
  public void setUp() {
    super.setUp();
    ReflectionTestUtils.setField(studentForgotAccountAPIController, "passwordService",
        new PasswordServiceImpl());
  }

  @Test
  public void changePassword_InvalidPassword_ReturnError() throws JSONException {
    expect(userService.retrieveUserByUsername(STUDENT_USERNAME)).andReturn(student1);
    replay(userService);
    String invalidPassword = PasswordServiceImpl.INVALID_PASSWORD_TOO_SHORT;
    ResponseEntity<Map<String, Object>> response = studentForgotAccountAPIController.changePassword(
        STUDENT_USERNAME, STUDENT1_ACCOUNT_ANSWER, invalidPassword, invalidPassword);
    assertResponseValues(response, HttpStatus.BAD_REQUEST, "invalidPassword");
    verify(userService);
  }

  private void assertResponseValues(ResponseEntity<Map<String, Object>> response,
      HttpStatus expectedStatus, String expectedMessageCode) {
    assertEquals(expectedStatus, response.getStatusCode());
    assertEquals(expectedMessageCode, response.getBody().get("messageCode"));
  }
}
