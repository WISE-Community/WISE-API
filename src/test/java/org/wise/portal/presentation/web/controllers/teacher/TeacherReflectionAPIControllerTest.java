package org.wise.portal.presentation.web.controllers.teacher;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.List;

import org.easymock.EasyMockExtension;
import org.easymock.Mock;
import org.easymock.TestSubject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.server.ResponseStatusException;
import org.wise.portal.dao.ObjectNotFoundException;
import org.wise.portal.presentation.web.controllers.APIControllerTest;
import org.wise.portal.presentation.web.controllers.teacher.TeacherReflectionAPIController.AnswerRequest;
import org.wise.portal.presentation.web.controllers.teacher.TeacherReflectionAPIController.TeacherReflectionResponse;
import org.wise.portal.service.teacherreflection.TeacherReflectionService;
import org.wise.vle.domain.teacherreflection.TeacherReflection;

@ExtendWith(EasyMockExtension.class)
public class TeacherReflectionAPIControllerTest extends APIControllerTest {

  @TestSubject
  private TeacherReflectionAPIController controller = new TeacherReflectionAPIController();

  @Mock
  private TeacherReflectionService teacherReflectionService;

  private void expectTeacherWithPermissions(boolean canView, boolean canGrade)
      throws ObjectNotFoundException {
    expect(runService.retrieveById(runId1)).andReturn(run1).anyTimes();
    expect(userService.retrieveUserByUsername(teacherAuth.getName())).andReturn(teacher1)
        .anyTimes();
    expect(runService.isAllowedToViewStudentWork(run1, teacher1)).andReturn(canView).anyTimes();
    expect(runService.isAllowedToGradeStudentWork(run1, teacher1)).andReturn(canGrade).anyTimes();
    expect(workgroupService.getWorkgroupListByRunAndUser(run1, teacher1))
        .andReturn(Arrays.asList(teacher1Run1Workgroup)).anyTimes();
  }

  @Test
  public void getQuestions_TeacherWithoutPermission_ThrowsAccessDenied() throws Exception {
    expectTeacherWithPermissions(false, false);
    replay(teacherReflectionService, runService, userService, workgroupService);

    assertThrows(AccessDeniedException.class, () -> controller.getQuestions(teacherAuth, runId1,
        run1Period1Id, run1Node1Id, run1Component1Id));
  }

  @Test
  public void getQuestions_PeriodNotInRun_ThrowsBadRequest() throws Exception {
    expectTeacherWithPermissions(true, false);
    replay(teacherReflectionService, runService, userService, workgroupService);

    assertThrows(ResponseStatusException.class, () -> controller.getQuestions(teacherAuth, runId1,
        run2Period2Id, run1Node1Id, run1Component1Id));
  }

  @Test
  public void getQuestions_Success_ReturnsList() throws Exception {
    expectTeacherWithPermissions(true, false);
    TeacherReflection r = new TeacherReflection();
    r.setId(10L);
    r.setRun(run1);
    r.setPeriod(run1Period1);
    r.setNodeId(run1Node1Id);
    r.setComponentId(run1Component1Id);
    r.setComponentType("Discussion");
    r.setQuestionId("q1");
    r.setQuestionText("What did students say?");
    r.setAnswerText("Varied thoughts");
    r.setAnsweredByWorkgroup(teacher1Run1Workgroup);
    Timestamp now = new Timestamp(System.currentTimeMillis());
    r.setCreatedAt(now);
    r.setUpdatedAt(now);

    expect(teacherReflectionService.getQuestions(run1, run1Period1, run1Node1Id,
        run1Component1Id)).andReturn(Arrays.asList(r));
    replay(teacherReflectionService, runService, userService, workgroupService);

    List<TeacherReflectionResponse> res = controller.getQuestions(teacherAuth, runId1,
        run1Period1Id, run1Node1Id, run1Component1Id);
    assertEquals(1, res.size());
    assertEquals("q1", res.get(0).questionId());
    assertEquals("What did students say?", res.get(0).questionText());
    assertEquals("Varied thoughts", res.get(0).answerText());
  }

  @Test
  public void saveAnswer_TeacherWithoutPermission_ThrowsAccessDenied() throws Exception {
    expectTeacherWithPermissions(true, false);
    replay(teacherReflectionService, runService, userService, workgroupService);

    AnswerRequest req = new AnswerRequest(runId1, run1Period1Id, run1Node1Id, run1Component1Id,
        "Discussion", "q1", "Question text", "Answer text");
    assertThrows(AccessDeniedException.class, () -> controller.saveAnswer(teacherAuth, req));
  }

  @Test
  public void saveAnswer_Success_ReturnsResponse() throws Exception {
    expectTeacherWithPermissions(true, true);
    TeacherReflection r = new TeacherReflection();
    r.setId(10L);
    r.setRun(run1);
    r.setPeriod(run1Period1);
    r.setNodeId(run1Node1Id);
    r.setComponentId(run1Component1Id);
    r.setComponentType("Discussion");
    r.setQuestionId("q1");
    r.setQuestionText("Question text");
    r.setAnswerText("Answer text");
    r.setAnsweredByWorkgroup(teacher1Run1Workgroup);
    Timestamp now = new Timestamp(System.currentTimeMillis());
    r.setCreatedAt(now);
    r.setUpdatedAt(now);

    expect(teacherReflectionService.saveAnswer(run1, run1Period1, run1Node1Id,
        run1Component1Id, "Discussion", "q1", "Question text", "Answer text",
        teacher1Run1Workgroup)).andReturn(r);
    replay(teacherReflectionService, runService, userService, workgroupService);

    AnswerRequest req = new AnswerRequest(runId1, run1Period1Id, run1Node1Id, run1Component1Id,
        "Discussion", "q1", "Question text", "Answer text");
    TeacherReflectionResponse res = controller.saveAnswer(teacherAuth, req);
    assertNotNull(res);
    assertEquals("q1", res.questionId());
    assertEquals("Question text", res.questionText());
    assertEquals("Answer text", res.answerText());
    assertEquals(teacher1Run1Workgroup.getId(), res.answeredByWorkgroupId());
  }
}
