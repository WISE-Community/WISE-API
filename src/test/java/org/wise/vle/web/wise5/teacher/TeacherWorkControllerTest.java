package org.wise.vle.web.wise5.teacher;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.fail;

import java.nio.file.AccessDeniedException;

import org.easymock.EasyMockRunner;
import org.easymock.Mock;
import org.easymock.TestSubject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wise.portal.presentation.web.controllers.APIControllerTest;
import org.wise.portal.service.vle.wise5.TeacherWorkService;
import org.wise.portal.service.work.BroadcastStudentWorkService;
import org.wise.vle.domain.work.StudentWork;

@RunWith(EasyMockRunner.class)
public class TeacherWorkControllerTest extends APIControllerTest {

  @TestSubject
  private TeacherWorkController controller = new TeacherWorkController();

  @Mock
  private TeacherWorkService teacherWorkService;

  @Mock
  private BroadcastStudentWorkService broadcastStudentWorkService;

  private StudentWork studentWork;

  @Before
  public void setUp() {
    super.setUp();
    studentWork = new StudentWork();
    studentWork.setRun(run1);
  }

  @Test
  public void save_NoPermission_ThrowAccessDenied() {
    expect(userService.retrieveUserByUsername(TEACHER_USERNAME)).andReturn(teacher2);
    replay(userService, teacherWorkService);
    try {
      controller.save(studentWork, teacherAuth);
      fail("AccessDeniedException expected, but was not thrown");
    } catch (AccessDeniedException e) {
      e.printStackTrace();
    }
    verify(userService, teacherWorkService);
  }

  @Test
  public void save_HasPermission_SaveBroadcastAndReturnWork() throws AccessDeniedException {
    expect(userService.retrieveUserByUsername(TEACHER_USERNAME)).andReturn(teacher1);
    expect(teacherWorkService.save(studentWork)).andReturn(studentWork);
    broadcastStudentWorkService.broadcastToClassroom(studentWork);
    expectLastCall();
    replay(userService, teacherWorkService);
    controller.save(studentWork, teacherAuth);
    verify(userService, teacherWorkService);
  }
}
