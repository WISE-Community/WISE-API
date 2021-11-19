package org.wise.portal.presentation.web.controllers.student;

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
import org.wise.vle.domain.annotation.wise5.Annotation;
import org.wise.vle.domain.work.StudentWork;

@RunWith(EasyMockRunner.class)
public class ClassmateDiscussionDataControllerTest extends AbstractClassmateDataControllerTest {

  @TestSubject
  private ClassmateDiscussionDataController controller = new ClassmateDiscussionDataController();

  @Test
  public void getClassmateDiscussionWork_NotInRun_ThrowException()
      throws NoSuchMethodException, ObjectNotFoundException {
    expectIsUserInRun(false);
    replayAll();
    assertThrows(AccessDeniedException.class, () -> controller
        .getClassmateDiscussionWork(studentAuth, runId1, run1Period1Id, NODE_ID1, COMPONENT_ID1));
    verifyAll();
  }

  @Test
  public void getClassmateDiscussionWork_NotDiscussionComponent_ThrowException()
      throws IOException, NoSuchMethodException, ObjectNotFoundException {
    expectIsUserInRun(true);
    expectComponentType(OPEN_RESPONSE_TYPE);
    replayAll();
    assertThrows(AccessDeniedException.class, () -> controller
        .getClassmateDiscussionWork(studentAuth, runId1, run1Period1Id, NODE_ID1, COMPONENT_ID1));
    verifyAll();
  }

  @Test
  public void getClassmateDiscussionWork_InRunDiscussionComponent_ReturnWork()
      throws IOException, NoSuchMethodException, ObjectNotFoundException {
    expectIsUserInRun(true);
    expectComponentType(controller.DISCUSSION_TYPE);
    List<StudentWork> studentWork = Arrays.asList(new StudentWork(), new StudentWork());
    expectStudentWork(studentWork);
    replayAll();
    try {
      List<StudentWork> classmateDiscussionWork = controller.getClassmateDiscussionWork(studentAuth,
          runId1, run1Period1Id, NODE_ID1, COMPONENT_ID1);
      assertEquals(classmateDiscussionWork, studentWork);
    } catch (Exception e) {
      fail(SHOULD_NOT_HAVE_THROWN_EXCEPTION);
    }
    verifyAll();
  }

  @Test
  public void getClassmateDiscussionAnnotations_InRunDiscussionComponent_ReturnAnnotations()
      throws IOException, NoSuchMethodException, ObjectNotFoundException {
    expectIsUserInRun(true);
    expectComponentType(controller.DISCUSSION_TYPE);
    List<Annotation> annotations = Arrays.asList(new Annotation(), new Annotation());
    expectAnnotations(annotations);
    replayAll();
    try {
      List<Annotation> classmateDiscussionAnnotations = controller
          .getClassmateDiscussionAnnotations(studentAuth, runId1, run1Period1Id, NODE_ID1,
              COMPONENT_ID1);
      assertEquals(classmateDiscussionAnnotations, annotations);
    } catch (Exception e) {
      fail(SHOULD_NOT_HAVE_THROWN_EXCEPTION);
    }
    verifyAll();
  }
}