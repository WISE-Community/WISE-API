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
  public void removeStudent_Student_RemoveStudentFromRun() throws Exception {
    expect(runService.retrieveById(runId1)).andReturn(run1);
    expect(runService.hasWritePermission(teacherAuth, run1)).andReturn(true);
    expect(userService.retrieveById(student1Id)).andReturn(student1);
    studentService.removeStudentFromRun(student1, run1);
    replayServices();
    controller.removeStudent(teacherAuth, runId1, student1Id);
    verifyServices();
  }

  /**
   * Both steps of removeStudentFromRun are scoped to the run, so a student who is not in the run
   * is left untouched. Rejecting them here instead would turn a repeated removal into an error,
   * and would leave a student who is still in a workgroup but no longer in a period with no way
   * to be cleaned up.
   */
  @Test
  public void removeStudent_StudentNotInRun_RemoveStudentFromRun() throws Exception {
    expect(runService.retrieveById(runId1)).andReturn(run1);
    expect(runService.hasWritePermission(teacherAuth, run1)).andReturn(true);
    expect(userService.retrieveById(student2Id)).andReturn(student2);
    studentService.removeStudentFromRun(student2, run1);
    replayServices();
    controller.removeStudent(teacherAuth, runId1, student2Id);
    verifyServices();
  }
}
