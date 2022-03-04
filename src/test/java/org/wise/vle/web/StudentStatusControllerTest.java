package org.wise.vle.web;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import org.easymock.EasyMockRunner;
import org.easymock.TestSubject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.security.access.AccessDeniedException;
import org.wise.portal.domain.run.Run;
import org.wise.portal.domain.user.User;
import org.wise.portal.domain.workgroup.Workgroup;
import org.wise.portal.presentation.web.controllers.APIControllerTest;
import org.wise.vle.domain.status.StudentStatus;

@RunWith(EasyMockRunner.class)
public class StudentStatusControllerTest extends APIControllerTest {
  
  @TestSubject
  private StudentStatusController controller = new StudentStatusController();

  @Test
  public void getStudentStatus_NotInWorkgroup_ThrowException() {
    expectRetrieveUserByUsername(STUDENT_USERNAME, student1);
    expectIsUserInWorkgroupForRun(student1, run1, workgroup2, false);
    replayAll();
    assertThrows(AccessDeniedException.class,
        () -> controller.getStudentStatus(studentAuth, workgroup2));
    verifyAll();
  }

  @Test
  public void getStudentStatus_InWorkgroup_ReturnStudentStatus() {
    expectRetrieveUserByUsername(STUDENT_USERNAME, student1);
    expectIsUserInWorkgroupForRun(student1, run1, workgroup1, true);
    StudentStatus studentStatus = new StudentStatus();
    expectGetStudentStatusByWorkgroupId(workgroup1, studentStatus);
    replayAll();
    assertEquals(studentStatus, controller.getStudentStatus(studentAuth, workgroup1));
    verifyAll();
  }

  private void expectRetrieveUserByUsername(String username, User user) {
    expect(userService.retrieveUserByUsername(username)).andReturn(user);
  }

  private void expectIsUserInWorkgroupForRun(
      User user, Run run, Workgroup workgroup, boolean isInWorkgroup) {
    expect(workgroupService.isUserInWorkgroupForRun(user, run, workgroup)).andReturn(isInWorkgroup);
  }

  private void expectGetStudentStatusByWorkgroupId(
      Workgroup workgroup, StudentStatus studentStatus) {
    expect(vleService.getStudentStatusByWorkgroupId(workgroup.getId())).andReturn(studentStatus);
  }

  protected void replayAll() {
    replay(userService, vleService, workgroupService);
  }

  protected void verifyAll() {
    verify(userService, vleService, workgroupService);
  }
}
