package org.wise.portal.presentation.web.controllers.user;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.easymock.EasyMockRunner;
import org.easymock.TestSubject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.test.util.ReflectionTestUtils;
import org.wise.portal.dao.ObjectNotFoundException;
import org.wise.portal.domain.run.Run;
import org.wise.portal.presentation.web.controllers.APIControllerTest;
import org.wise.portal.presentation.web.exception.IncorrectPasswordException;
import org.wise.portal.service.password.impl.PasswordServiceImpl;

@RunWith(EasyMockRunner.class)
public class UserAPIControllerTest extends APIControllerTest {

  @TestSubject
  private UserAPIController userAPIController = new UserAPIController();

  @Before
  public void setUp() {
    super.setUp();
    ReflectionTestUtils.setField(userAPIController, "passwordService", new PasswordServiceImpl());
  }

  @Test
  public void getUserInfo_UnAuthenticatedUser_ReturnPassedInUsername() {
    Authentication auth = null;
    String username = "SpongeBobS0101";
    HashMap<String, Object> userMap = userAPIController.getUserInfo(auth, username);
    assertEquals(username, userMap.get("username"));
  }

  @Test
  public void getUserInfo_Student_ReturnInfo() {
    expect(userService.retrieveUserByUsername(STUDENT_USERNAME)).andReturn(student1);
    replay(userService);
    String username = "";
    HashMap<String, Object> userMap = userAPIController.getUserInfo(studentAuth, username);
    assertEquals(STUDENT_FIRSTNAME, userMap.get("firstName"));
    assertEquals("student", userMap.get("role"));
    assertTrue((boolean) userMap.get("isGoogleUser"));
    verify(userService);
  }

  @Test
  public void getUserInfo_Teacher_ReturnInfo() {
    expect(userService.retrieveUserByUsername(TEACHER_USERNAME)).andReturn(teacher1);
    replay(userService);
    String username = "";
    HashMap<String, Object> userMap = userAPIController.getUserInfo(teacherAuth, username);
    assertEquals(TEACHER_FIRSTNAME, userMap.get("firstName"));
    assertEquals("teacher", userMap.get("role"));
    assertFalse((boolean) userMap.get("isGoogleUser"));
    verify(userService);
  }

  @Test
  public void getConfig_WISEContextPath_ReturnConfig() {
    expect(request.getContextPath()).andReturn("wise");
    replay(request);
    expect(appProperties.getProperty("google_analytics_id")).andReturn("UA-XXXXXX-1");
    expect(appProperties.getProperty("recaptcha_public_key")).andReturn("recaptcha-123-abc");
    expect(appProperties.getProperty("wise4.hostname")).andReturn("http://localhost:8080/legacy");
    expect(appProperties.getProperty("discourse_url")).andReturn("http://localhost:9292");
    expect(appProperties.getProperty("wise.hostname")).andReturn("http://localhost:8080");
    expect(appProperties.getProperty("discourse_news_category")).andReturn("");
    replay(appProperties);
    HashMap<String, Object> config = userAPIController.getConfig(request);
    assertEquals("wise", config.get("contextPath"));
    assertEquals("wise/api/logout", config.get("logOutURL"));
    assertFalse((boolean) config.get("isGoogleClassroomEnabled"));
    assertEquals("UA-XXXXXX-1", config.get("googleAnalyticsId"));
    verify(request);
    verify(appProperties);
  }

  @Test
  public void checkAuthentication_UserNotInDB_ReturnInvalidUsernameResponse() {
    expect(userService.retrieveUserByUsername(USERNAME_NOT_IN_DB)).andReturn(null);
    replay(userService);
    String password = "s3cur3";
    HashMap<String, Object> response = userAPIController.checkAuthentication(USERNAME_NOT_IN_DB,
        password);
    assertFalse((boolean) response.get("isUsernameValid"));
    assertFalse((boolean) response.get("isPasswordValid"));
    verify(userService);
  }

  @Test
  public void checkAuthentication_StudentUserCorrectPassword_ReturnValidUsernameResponse() {
    expect(userService.retrieveUserByUsername(STUDENT_USERNAME)).andReturn(student1);
    expect(userService.isPasswordCorrect(student1, STUDENT_PASSWORD)).andReturn(true);
    replay(userService);
    HashMap<String, Object> response = userAPIController.checkAuthentication(STUDENT_USERNAME,
        STUDENT_PASSWORD);
    assertTrue((boolean) response.get("isUsernameValid"));
    assertTrue((boolean) response.get("isPasswordValid"));
    assertEquals(student1Id, (Long) response.get("userId"));
    verify(userService);
  }

  @Test
  public void changePassword_InvalidPassword_ReturnError() {
    ResponseEntity<Map<String, Object>> response = userAPIController.changePassword(studentAuth,
        STUDENT_PASSWORD, PasswordServiceImpl.INVALID_PASSWORD_TOO_SHORT);
    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    Map<String, Object> body = response.getBody();
    assertEquals("invalidPassword", body.get("messageCode"));
  }

  @Test
  public void changePassword_CorrectOldPassword_ChangePassword() throws IncorrectPasswordException {
    String newPassword = PasswordServiceImpl.VALID_PASSWORD;
    expect(userService.retrieveUserByUsername(STUDENT_USERNAME)).andReturn(student1);
    expect(userService.updateUserPassword(student1, STUDENT_PASSWORD, newPassword))
        .andReturn(student1);
    replay(userService);
    ResponseEntity<Map<String, Object>> response = userAPIController.changePassword(studentAuth,
        STUDENT_PASSWORD, newPassword);
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals("passwordUpdated", response.getBody().get("messageCode"));
    verify(userService);
  }

  @Test
  public void changePassword_IncorrectOldPassword_PasswordStaysSame()
      throws IncorrectPasswordException {
    String incorrectPassword = "incorrectPass";
    String newPassword = PasswordServiceImpl.VALID_PASSWORD;
    expect(userService.retrieveUserByUsername(STUDENT_USERNAME)).andReturn(student1);
    expect(userService.updateUserPassword(student1, incorrectPassword, newPassword))
        .andStubThrow(new IncorrectPasswordException());
    replay(userService);
    ResponseEntity<Map<String, Object>> response = userAPIController.changePassword(studentAuth,
        incorrectPassword, newPassword);
    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    assertEquals("incorrectPassword", response.getBody().get("messageCode"));
    verify(userService);
  }

  @Test
  public void getSupportedLanguages_ThreeSupportedLocales_ReturnLanguageArray() {
    expect(appProperties.getProperty("supportedLocales", "")).andReturn("en,ja,zh_tw");
    replay(appProperties);
    List<HashMap<String, String>> langs = userAPIController.getSupportedLanguages();
    assertEquals(3, langs.size());
    assertEquals("English", langs.get(0).get("language"));
    assertEquals("Japanese", langs.get(1).get("language"));
    assertEquals("Chinese (Traditional)", langs.get(2).get("language"));
    verify(appProperties);
  }

  @Test
  public void isNameValid_InvalidName_ReturnFalse() {
    assertFalse(userAPIController.isNameValid(""));
    assertFalse(userAPIController.isNameValid("Spongebob!"));
    assertFalse(userAPIController.isNameValid("Spongebób"));
    assertFalse(userAPIController.isNameValid("海绵宝宝"));
  }

  @Test
  public void isNameValid_ValidName_ReturnTrue() {
    assertTrue(userAPIController.isNameValid("Spongebob"));
  }

  @Test
  public void isFirstNameAndLastNameValid_ValidFirstNameInvalidLastName_ReturnFalse() {
    assertFalse(userAPIController.isFirstNameAndLastNameValid("Spongebob", "Squarepants!"));
  }

  @Test
  public void isFirstNameAndLastNameValid_InvalidFirstNameValidLastName_ReturnFalse() {
    assertFalse(userAPIController.isFirstNameAndLastNameValid("Spongebob!", "Squarepants"));
  }

  @Test
  public void isFirstNameAndLastNameValid_InvalidFirstNameInvalidLastName_ReturnFalse() {
    assertFalse(userAPIController.isFirstNameAndLastNameValid("Spongebob!", "Squarepants!"));
  }

  @Test
  public void isFirstNameAndLastNameValid_ValidFirstNameValidLastName_ReturnTrue() {
    assertTrue(userAPIController.isFirstNameAndLastNameValid("Spongebob", "Squarepants"));
  }

  @Test
  public void getInvalidNameMessageCode_InvalidFirstName_ReturnInvalidFirstNameMessageCode() {
    assertEquals(userAPIController.getInvalidNameMessageCode("a!", "a"), "invalidFirstName");
  }

  @Test
  public void getInvalidNameMessageCode_InvalidLastName_ReturnInvalidLastNameMessageCode() {
    assertEquals(userAPIController.getInvalidNameMessageCode("a", "a!"), "invalidLastName");
  }

  @Test
  public void getInvalidNameMessageCode_InvalidFirstAndLastName_ReturnInvalidNameMessageCode() {
    assertEquals(userAPIController.getInvalidNameMessageCode("a!", "a!"),
        "invalidFirstAndLastName");
  }

  @Test
  public void createRegisterSuccessResponse_Username_ReturnSuccessResponse() {
    String username = "Spongebob Squarepants";
    ResponseEntity<Map<String, Object>> response = userAPIController
        .createRegisterSuccessResponse(username);
    assertEquals(response.getStatusCode(), HttpStatus.OK);
    assertEquals(response.getBody().get("username"), username);
  }

  @Test
  public void getRunInfoById_RunExistsInDB_ReturnRunInfo() throws ObjectNotFoundException {
    expect(userService.retrieveUserByUsername(TEACHER_USERNAME)).andReturn(teacher1);
    expect(userService.isUserAssociatedWithRun(teacher1, run1)).andReturn(true);
    replay(userService);
    expect(runService.retrieveById(runId1)).andReturn(run1);
    replay(runService);
    HashMap<String, Object> info = userAPIController.getRunInfoById(teacherAuth, runId1);
    assertEquals("1", info.get("id"));
    assertEquals(RUN1_RUNCODE, info.get("runCode"));
    verify(runService);
  }

  @Test
  public void getRunInfoById_RunNotInDB_ReturnRunInfo() throws ObjectNotFoundException {
    Long runIdNotInDB = -1L;
    expect(runService.retrieveById(runIdNotInDB))
        .andThrow(new ObjectNotFoundException(runIdNotInDB, Run.class));
    replay(runService);
    HashMap<String, Object> info = userAPIController.getRunInfoById(teacherAuth, runIdNotInDB);
    assertEquals(1, info.size());
    assertEquals("runNotFound", info.get("error"));
    verify(runService);
  }

}
