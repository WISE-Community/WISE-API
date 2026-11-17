package org.wise.portal.presentation.web.controllers.student;

import static org.easymock.EasyMock.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.wise.portal.presentation.web.controllers.APIControllerTest;
import org.wise.portal.service.password.impl.PasswordServiceImpl;

@ExtendWith(EasyMockExtension.class)
public class StudentForgotAccountAPIControllerTest extends APIControllerTest {

  @TestSubject
  private StudentForgotAccountAPIController studentForgotAccountAPIController = new StudentForgotAccountAPIController();

  @BeforeEach
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

  @Test
  public void searchUsername_WithNamesOnly_ReturnsStoredUsername() throws Exception {
    expect(userService.retrieveStudentsByName(STUDENT_FIRSTNAME, STUDENT_LASTNAME))
        .andReturn(List.of(student1));
    replay(userService);
    MockMvc mockMvc = MockMvcBuilders.standaloneSetup(studentForgotAccountAPIController).build();

    mockMvc
        .perform(get("/api/student/forgot/username/search")
            .param("firstName", STUDENT_FIRSTNAME)
            .param("lastName", STUDENT_LASTNAME))
        .andExpect(status().isOk())
        .andExpect(content().json("[\"SpongeBobS0101\"]"));

    verify(userService);
  }

  private void assertResponseValues(ResponseEntity<Map<String, Object>> response,
      HttpStatus expectedStatus, String expectedMessageCode) {
    assertEquals(expectedStatus, response.getStatusCode());
    assertEquals(expectedMessageCode, response.getBody().get("messageCode"));
  }
}
