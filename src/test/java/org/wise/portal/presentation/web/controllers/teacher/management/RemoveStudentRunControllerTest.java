package org.wise.portal.presentation.web.controllers.teacher.management;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.fail;

import org.easymock.EasyMockExtension;
import org.easymock.Mock;
import org.easymock.TestSubject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.security.access.AccessDeniedException;
import org.wise.portal.presentation.web.controllers.APIControllerTest;
import org.wise.portal.service.student.StudentService;

@ExtendWith(EasyMockExtension.class)
public class RemoveStudentRunControllerTest extends APIControllerTest {

  @TestSubject
  private RemoveStudentRunController controller = new RemoveStudentRunController();

  @Mock
  private StudentService studentService;

  private void replayServices() {
    replay(runService, studentService, userService);
  }

  private void verifyServices() {
    verify(runService, studentService, userService);
  }

  @Test
  public void removeStudent_NoWritePermission_ThrowAccessDenied() throws Exception {
    expect(runService.retrieveById(runId1)).andReturn(run1);
    expect(runService.hasWritePermission(teacherAuth, run1)).andReturn(false);
    replayServices();
    try {
      controller.removeStudent(teacherAuth, runId1, student1Id);
      fail("Expected AccessDeniedException to be thrown");
    } catch (AccessDeniedException e) {
    }
    verifyServices();
  }

  @Test
  public void removeStudent_TargetUserIsTeacher_ThrowAccessDenied() throws Exception {
    expect(runService.retrieveById(runId1)).andReturn(run1);
    expect(runService.hasWritePermission(teacherAuth, run1)).andReturn(true);
    expect(userService.retrieveById(teacher2Id)).andReturn(teacher2);
    replayServices();
    try {
      controller.removeStudent(teacherAuth, runId1, teacher2Id);
      fail("Expected AccessDeniedException to be thrown");
    } catch (AccessDeniedException e) {
    }
    verifyServices();
  }

  @Test
  public void removeStudent_TargetUserIsStudent_RemoveStudentFromRun() throws Exception {
    expect(runService.retrieveById(runId1)).andReturn(run1);
    expect(runService.hasWritePermission(teacherAuth, run1)).andReturn(true);
    expect(userService.retrieveById(student1Id)).andReturn(student1);
    studentService.removeStudentFromRun(student1, run1);
    replayServices();
    controller.removeStudent(teacherAuth, runId1, student1Id);
    verifyServices();
  }

  /**
   * The student role check is the only thing this controller asks about the target user: it
   * deliberately does not ask whether they belong to the run. Write permission on the run is
   * checked before it, and removeStudent_NoWritePermission_ThrowAccessDenied covers that half.
   * Rejecting a non-member here would turn a repeated removal into an error, and would leave a
   * student who is still in a workgroup but no longer in a period with no way to be cleaned up.
   * student2 is exactly that case in the fixture, being a member of workgroup2 under run1 while
   * run1Period1's own member list holds only student1.
   *
   * <p>What this asserts is the delegation and nothing beyond it. studentService is mocked, so the
   * claim that both steps of removeStudentFromRun are themselves scoped to the run is not tested
   * here and belongs to that service's own tests.
   */
  @Test
  public void removeStudent_StudentInWorkgroupButNotInPeriod_RemoveStudentFromRun()
      throws Exception {
    expect(runService.retrieveById(runId1)).andReturn(run1);
    expect(runService.hasWritePermission(teacherAuth, run1)).andReturn(true);
    expect(userService.retrieveById(student2Id)).andReturn(student2);
    studentService.removeStudentFromRun(student2, run1);
    replayServices();
    controller.removeStudent(teacherAuth, runId1, student2Id);
    verifyServices();
  }
}
