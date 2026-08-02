package org.wise.portal.presentation.web.controllers.teacher;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;

import org.easymock.EasyMockExtension;
import org.easymock.Mock;
import org.easymock.TestSubject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.http.ResponseEntity;
import org.wise.portal.presentation.web.controllers.APIControllerTest;
import org.wise.portal.service.mail.teacher.TeacherMailService;

@ExtendWith(EasyMockExtension.class)
public class TeacherVerificationEmailAPIControllerTest extends APIControllerTest {

  @TestSubject
  private TeacherVerificationEmailAPIController teacherVerificationEmailAPIController = new TeacherVerificationEmailAPIController();

  @Mock
  private TeacherMailService teacherMailService;

  @Test
  public void sendVerificationEmail_UserIsUnverifiedTeacher_SendEmail() {
    this.createTeachers();
    expect(userService.retrieveTeacherByUsername(TEACHER2_USERNAME)).andReturn(teacher2);
    replay(userService);
    teacherMailService.sendVerifyTeacherEmail("", "efgh5678", null, request);
    expectLastCall();
    replay(teacherMailService);
    ResponseEntity<Map<String, Object>> response = 
      teacherVerificationEmailAPIController.sendVerificationEmail(TEACHER2_USERNAME, request);
    assertEquals(TEACHER2_USERNAME, response.getBody().get("username"));
    verify(userService);
    verify(teacherMailService);

  }

  @Test
  public void sendVerificationEmail_UserIsVerifiedTeacher_ReturnError() {
    this.createTeachers();
    expect(userService.retrieveTeacherByUsername(TEACHER_USERNAME)).andReturn(teacher1);
    replay(userService);
    ResponseEntity<Map<String, Object>> response = 
      teacherVerificationEmailAPIController.sendVerificationEmail(TEACHER_USERNAME, request);
    assertEquals("Teacher already verified", response.getBody().get("messageCode"));
    verify(userService);
  }

  @Test
  public void sendVerificationEmail_UserIsNotTeacher_ReturnError() {
    expect(userService.retrieveTeacherByUsername(STUDENT_USERNAME)).andReturn(null);
    replay(userService);
    ResponseEntity<Map<String, Object>> response = 
      teacherVerificationEmailAPIController.sendVerificationEmail(STUDENT_USERNAME, request);
    assertEquals("Not a teacher", response.getBody().get("messageCode"));
    verify(userService);
  }
}
