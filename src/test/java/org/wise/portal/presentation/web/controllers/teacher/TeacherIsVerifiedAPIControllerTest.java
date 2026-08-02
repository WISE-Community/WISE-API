package org.wise.portal.presentation.web.controllers.teacher;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.easymock.EasyMockExtension;
import org.easymock.TestSubject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.wise.portal.presentation.web.controllers.APIControllerTest;

@ExtendWith(EasyMockExtension.class)
public class TeacherIsVerifiedAPIControllerTest extends APIControllerTest {
  @TestSubject
  private TeacherIsVerifiedAPIController teacherIsVerifiedAPIController = new TeacherIsVerifiedAPIController();

  @Test
  public void isVerifiedTeacherOrNonTeacher_UserIsAStudent_ReturnTrue() {
    expect(userService.retrieveTeacherByUsername(STUDENT_USERNAME)).andReturn(null);
    replay(userService);
    boolean isVerifiedTeacher = teacherIsVerifiedAPIController.isVerifiedTeacherOrNonTeacher(STUDENT_USERNAME);
    assertTrue(isVerifiedTeacher);
    verify(userService); 
  }

  @Test
  public void isVerifiedTeacherOrNonTeacher_UserDoesNotExist_ReturnTrue() {
    expect(userService.retrieveTeacherByUsername("")).andReturn(null);
    replay(userService);
    boolean isVerifiedTeacher = teacherIsVerifiedAPIController.isVerifiedTeacherOrNonTeacher("");
    assertTrue(isVerifiedTeacher);
    verify(userService); 
  }

  @Test
  public void isVerifiedTeacherOrNonTeacher_TeacherIsVerified_ReturnTrue() {
    this.createTeachers();
    expect(userService.retrieveTeacherByUsername(TEACHER_USERNAME)).andReturn(teacher1);
    replay(userService);
    boolean isVerifiedTeacher = teacherIsVerifiedAPIController.isVerifiedTeacherOrNonTeacher(TEACHER_USERNAME);
    assertTrue(isVerifiedTeacher);
    verify(userService); 
  }

  @Test
  public void isVerifiedTeacherOrNonTeacher_TeacherIsUnverified_ReturnFalse() {
    this.createTeachers();
    expect(userService.retrieveTeacherByUsername(TEACHER2_USERNAME)).andReturn(teacher2);
    replay(userService);
    boolean isVerifiedTeacher = teacherIsVerifiedAPIController.isVerifiedTeacherOrNonTeacher(TEACHER2_USERNAME);
    assertFalse(isVerifiedTeacher);
    verify(userService); 
  } 
}
