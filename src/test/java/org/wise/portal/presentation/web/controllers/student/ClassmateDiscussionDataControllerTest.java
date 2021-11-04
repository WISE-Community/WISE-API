package org.wise.portal.presentation.web.controllers.student;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.easymock.EasyMockRunner;
import org.easymock.TestSubject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.security.access.AccessDeniedException;
import org.wise.portal.dao.ObjectNotFoundException;
import org.wise.portal.presentation.web.controllers.APIControllerTest;
import org.wise.vle.domain.annotation.wise5.Annotation;
import org.wise.vle.domain.work.StudentWork;
import org.wise.vle.web.wise5.ClassmateDiscussionDataController;

@RunWith(EasyMockRunner.class)
public class ClassmateDiscussionDataControllerTest extends APIControllerTest {

  String nodeId = "node1";
  String componentId = "component1";

  @TestSubject
  private ClassmateDiscussionDataController controller = new ClassmateDiscussionDataController();

  @Test
  public void getClassmateDiscussionWork_NotInRun_ThrowException()
      throws NoSuchMethodException, ObjectNotFoundException {
    expectIsUserInRun(false);
    replayAll();
    assertThrows(AccessDeniedException.class, () -> controller
        .getClassmateDiscussionWork(studentAuth, runId1, run1Period1Id, nodeId, componentId));
    verifyAll();
  }

  @Test
  public void getClassmateDiscussionWork_NotDiscussionComponent_ThrowException()
      throws IOException, NoSuchMethodException, ObjectNotFoundException {
    expectIsUserInRun(true);
    expectComponentType("OpenResponse");
    replayAll();
    assertThrows(AccessDeniedException.class, () -> controller
        .getClassmateDiscussionWork(studentAuth, runId1, run1Period1Id, nodeId, componentId));
    verifyAll();
  }

  @Test
  public void getClassmateDiscussionWork_InRunDiscussionComponent_ReturnWork()
      throws IOException, NoSuchMethodException, ObjectNotFoundException {
    expectIsUserInRun(true);
    expectComponentType("Discussion");
    List<StudentWork> studentWork = Arrays.asList(new StudentWork(), new StudentWork());
    expectStudentWork(studentWork);
    replayAll();
    try {
      List<StudentWork> classmateDiscussionWork = controller.getClassmateDiscussionWork(studentAuth,
          runId1, run1Period1Id, nodeId, componentId);
      assertEquals(classmateDiscussionWork, studentWork);
    } catch (Exception e) {
      fail("Should not have thrown an exception");
    }
    verifyAll();
  }

  private void expectStudentWork(List<StudentWork> studentWork) {
    expect(vleService.getStudentWork(run1, run1Period1, nodeId, componentId))
        .andReturn(studentWork);
  }

  @Test
  public void getClassmateDiscussionAnnotations_InRunDiscussionComponent_ReturnAnnotations()
      throws IOException, NoSuchMethodException, ObjectNotFoundException {
    expectIsUserInRun(true);
    expectComponentType("Discussion");
    List<Annotation> annotations = Arrays.asList(new Annotation(), new Annotation());
    expectAnnotations(annotations);
    replayAll();
    try {
      List<Annotation> classmateDiscussionAnnotations = controller
          .getClassmateDiscussionAnnotations(studentAuth, runId1, run1Period1Id, nodeId,
              componentId);
      assertEquals(classmateDiscussionAnnotations, annotations);
    } catch (Exception e) {
      fail("Should not have thrown an exception");
    }
    verifyAll();
  }

  private void expectAnnotations(List<Annotation> annotations) {
    expect(vleService.getAnnotations(run1, run1Period1, nodeId, componentId))
        .andReturn(annotations);
  }

  private void expectIsUserInRun(boolean isInRun)
      throws NoSuchMethodException, ObjectNotFoundException {
    expect(runService.retrieveById(runId1)).andReturn(run1);
    expect(groupService.retrieveById(run1Period1Id)).andReturn(run1Period1);
    expect(userService.retrieveUser(student1UserDetails)).andReturn(student1);
    expect(runService.isUserInRunAndPeriod(student1, run1, run1Period1)).andReturn(isInRun);
  }

  private void expectComponentType(String componentType)
      throws IOException, ObjectNotFoundException {
    expect(projectService.getProjectContent(project1)).andReturn(
        "{ \"nodes\": [ { \"id\": \"node1\", \"components\": [ { \"id\": \"component1\", \"type\": \""
            + componentType + "\" } ] } ] }");
  }

  private void replayAll() {
    replay(groupService, projectService, runService, userService, vleService);
  }

  private void verifyAll() {
    verify(groupService, projectService, runService, userService, vleService);
  }
}
