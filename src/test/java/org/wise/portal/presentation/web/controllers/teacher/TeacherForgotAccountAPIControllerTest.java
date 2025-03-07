package org.wise.portal.presentation.web.controllers.teacher;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.util.Date;
import java.util.Map;

import org.easymock.EasyMockExtension;
import org.easymock.TestSubject;
import org.json.JSONException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.wise.portal.domain.user.User;
import org.wise.portal.presentation.web.controllers.APIControllerTest;
import org.wise.portal.service.password.impl.PasswordServiceImpl;

@ExtendWith(EasyMockExtension.class)
public class TeacherForgotAccountAPIControllerTest extends APIControllerTest {

  @TestSubject
  private TeacherForgotAccountAPIController teacherForgotAccountAPIController = new TeacherForgotAccountAPIController();

  @BeforeEach
  public void setUp() {
    super.setUp();
    ReflectionTestUtils.setField(teacherForgotAccountAPIController, "passwordService",
        new PasswordServiceImpl());
  }

  @Test
  public void changePassword_InvalidPassword_ReturnError() throws JSONException {
    expect(userService.retrieveUserByUsername(TEACHER_USERNAME)).andReturn(teacher1);
    replay(userService);
    String verificationCode = "123456";
    setVerificationCode(teacher1, verificationCode);
    String invalidPassword = PasswordServiceImpl.INVALID_PASSWORD_TOO_SHORT;
    ResponseEntity<Map<String, Object>> response = teacherForgotAccountAPIController
        .changePassword(TEACHER_USERNAME, verificationCode, invalidPassword, invalidPassword);
    assertResponseValues(response, HttpStatus.BAD_REQUEST, "invalidPassword");
    verify(userService);
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
}
