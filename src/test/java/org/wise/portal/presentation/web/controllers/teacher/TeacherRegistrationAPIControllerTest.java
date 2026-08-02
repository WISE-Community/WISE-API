package org.wise.portal.presentation.web.controllers.teacher;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.isA;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.easymock.EasyMockExtension;
import org.easymock.Mock;
import org.easymock.TestSubject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.wise.portal.domain.authentication.impl.TeacherUserDetails;
import org.wise.portal.presentation.web.controllers.APIControllerTest;
import org.wise.portal.presentation.web.exception.InvalidNameException;
import org.wise.portal.service.authentication.DuplicateUsernameException;
import org.wise.portal.service.authentication.UserDetailsService;
import org.wise.portal.service.mail.MailService;
import org.wise.portal.service.password.PasswordService;
import org.wise.portal.service.password.impl.PasswordServiceImpl;
import org.wise.portal.service.usertags.UserTagsService;

@ExtendWith(EasyMockExtension.class)
public class TeacherRegistrationAPIControllerTest extends APIControllerTest {
  
  @TestSubject
  private TeacherRegistrationAPIController teacherRegistrationAPIController = new TeacherRegistrationAPIController();

  @Mock
  private HttpServletResponse response;
  
  @Mock
  private MailService mailService;
  
  @Mock
  private MessageSource messageSource;

  @Mock
  private UserDetailsService userDetailsService;

  @Mock
  private UserTagsService userTagsService;

  @Mock
  private PasswordService passwordService;

  @Test
  public void createTeacherAccount_InvalidPassword_ReturnError()
      throws DuplicateUsernameException, InvalidNameException {
    HashMap<String, String> teacherFields = createDefaultTeacherFields();
    teacherFields.put("password", PasswordServiceImpl.INVALID_PASSWORD_TOO_SHORT);
    expect(passwordService.isValid(PasswordServiceImpl.INVALID_PASSWORD_TOO_SHORT)).andReturn(false);
    Map<String, Object> errors = new HashMap<>();
    errors.put("messageCode", "invalidPassword");
    expect(passwordService.getErrors(PasswordServiceImpl.INVALID_PASSWORD_TOO_SHORT)).andReturn(errors);
    replay(passwordService);
    ResponseEntity<Map<String, Object>> response = teacherRegistrationAPIController
        .createTeacherAccount(teacherFields, request);
    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    assertEquals("invalidPassword", response.getBody().get("messageCode"));
  }

  @Test
  public void createTeacherAccount_WithGoogleUserId_CreateUser()
      throws DuplicateUsernameException, InvalidNameException {
    HashMap<String, String> teacherFields = createDefaultTeacherFields();
    teacherFields.put("googleUserId", "123456789");
    expect(request.getLocale()).andReturn(Locale.US);
    replay(request);
    expect(userService.createUser(isA(TeacherUserDetails.class))).andReturn(teacher1);
    replay(userService);
    mailService.sendWelcomeTeacherEmail("", TEACHER_FIRSTNAME + " " + TEACHER_LASTNAME, TEACHER_USERNAME, 
                                true, Locale.US, request);
    expectLastCall();
    replay(mailService);
    ResponseEntity<Map<String, Object>> response = teacherRegistrationAPIController
        .createTeacherAccount(teacherFields, request);
    assertEquals(TEACHER_USERNAME, response.getBody().get("username"));
    verify(request);
    verify(userService);
    verify(mailService);
  } 

  private HashMap<String, String> createDefaultTeacherFields() {
    HashMap<String, String> fields = new HashMap<String, String>();
    fields.put("firstName", TEACHER_FIRSTNAME);
    fields.put("lastName", TEACHER_LASTNAME);
    fields.put("schoolLevel", "COLLEGE");
    fields.put("birthMonth", "1");
    fields.put("birthDay", "1");
    fields.put("gender", "MALE");
    return fields;
  }

  @Test
  public void sendVerificationEmail_UserIsUnverifiedTeacher_SendEmail() {
    this.createTeachers();
    expect(userService.retrieveTeacherByUsername(TEACHER2_USERNAME)).andReturn(teacher2);
    replay(userService);
    mailService.sendVerifyTeacherEmail("", "efgh5678", null, request);
    expectLastCall();
    replay(mailService);
    ResponseEntity<Map<String, Object>> response = 
      teacherRegistrationAPIController.sendVerificationEmail(TEACHER2_USERNAME, request);
    assertEquals(TEACHER2_USERNAME, response.getBody().get("username"));
    verify(userService);
    verify(mailService);

  }

  @Test
  public void sendVerificationEmail_UserIsVerifiedTeacher_ReturnError() {
    this.createTeachers();
    expect(userService.retrieveTeacherByUsername(TEACHER_USERNAME)).andReturn(teacher1);
    replay(userService);
    ResponseEntity<Map<String, Object>> response = 
      teacherRegistrationAPIController.sendVerificationEmail(TEACHER_USERNAME, request);
    assertEquals("Teacher already verified", response.getBody().get("messageCode"));
    verify(userService);
  }

  @Test
  public void sendVerificationEmail_UserIsNotTeacher_ReturnError() {
    expect(userService.retrieveTeacherByUsername(STUDENT_USERNAME)).andReturn(null);
    replay(userService);
    ResponseEntity<Map<String, Object>> response = 
      teacherRegistrationAPIController.sendVerificationEmail(STUDENT_USERNAME, request);
    assertEquals("Not a teacher", response.getBody().get("messageCode"));
    verify(userService);
  }
}
