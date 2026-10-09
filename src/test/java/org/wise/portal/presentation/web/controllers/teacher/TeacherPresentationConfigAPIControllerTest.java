package org.wise.portal.presentation.web.controllers.teacher;

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.isNull;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.easymock.EasyMockExtension;
import org.easymock.Mock;
import org.easymock.TestSubject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.server.ResponseStatusException;
import org.wise.portal.dao.ObjectNotFoundException;
import org.wise.portal.presentation.web.controllers.APIControllerTest;
import org.wise.portal.presentation.web.controllers.teacher.TeacherPresentationConfigAPIController.AnswerRequest;
import org.wise.portal.presentation.web.controllers.teacher.TeacherPresentationConfigAPIController.AnswerResponse;
import org.wise.portal.presentation.web.controllers.teacher.TeacherPresentationConfigAPIController.ConfigRequest;
import org.wise.portal.presentation.web.controllers.teacher.TeacherPresentationConfigAPIController.ConfigResponse;
import org.wise.portal.presentation.web.controllers.teacher.TeacherPresentationConfigAPIController.Item;
import org.wise.portal.service.teacherpresentation.TeacherPresentationConfigService;
import org.wise.vle.domain.teacherpresentation.TeacherPresentationConfig;
import org.wise.vle.domain.teacherpresentation.TeacherPresentationReflectionAnswer;

@ExtendWith(EasyMockExtension.class)
public class TeacherPresentationConfigAPIControllerTest extends APIControllerTest {

  @TestSubject
  private TeacherPresentationConfigAPIController controller = new TeacherPresentationConfigAPIController();

  @Mock
  private TeacherPresentationConfigService teacherPresentationConfigService;

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

  private ConfigRequest request(Long periodId, List<Item> items, String namesDisplay,
      String prompt) {
    return new ConfigRequest(runId1, periodId, run1Node1Id, run1Component1Id, "Discussion", items,
        namesDisplay, prompt);
  }

  @Test
  public void getConfig_NoConfigSaved_ReturnDefaults() throws Exception {
    expectTeacherWithPermissions(true, true);
    expect(teacherPresentationConfigService.getConfig(run1, run1Period1, run1Node1Id,
        run1Component1Id)).andReturn(null);
    replay(runService, userService, workgroupService, teacherPresentationConfigService);
    ConfigResponse response = controller.getConfig(teacherAuth, runId1, run1Period1Id,
        run1Node1Id, run1Component1Id);
    assertTrue(response.items().isEmpty());
    assertEquals("hide", response.studentNamesDisplay());
    assertNull(response.prompt());
    assertTrue(response.answers().isEmpty());
    verify(teacherPresentationConfigService);
  }

  @Test
  public void getConfig_NoViewPermission_ThrowAccessDenied() throws Exception {
    expectTeacherWithPermissions(false, false);
    replay(runService, userService, workgroupService, teacherPresentationConfigService);
    assertThrows(AccessDeniedException.class, () -> controller.getConfig(teacherAuth, runId1,
        run1Period1Id, run1Node1Id, run1Component1Id));
  }

  @Test
  public void getConfig_PeriodNotInRun_ThrowBadRequest() throws Exception {
    expectTeacherWithPermissions(true, true);
    replay(runService, userService, workgroupService, teacherPresentationConfigService);
    ResponseStatusException e = assertThrows(ResponseStatusException.class,
        () -> controller.getConfig(teacherAuth, runId1, 99999L, run1Node1Id, run1Component1Id));
    assertEquals(HttpStatus.BAD_REQUEST, e.getStatusCode());
  }

  @Test
  public void saveConfig_NoGradePermission_ThrowAccessDenied() throws Exception {
    expectTeacherWithPermissions(true, false);
    replay(runService, userService, workgroupService, teacherPresentationConfigService);
    assertThrows(AccessDeniedException.class, () -> controller.saveConfig(teacherAuth,
        request(run1Period1Id, Arrays.asList(new Item(1)), "hide", null)));
  }

  @Test
  public void saveConfig_ServiceRejectsInput_ThrowBadRequest() throws Exception {
    expectTeacherWithPermissions(true, true);
    expect(teacherPresentationConfigService.saveConfig(eq(run1), eq(run1Period1),
        eq(run1Node1Id), eq(run1Component1Id), eq("Discussion"), anyObject(), eq("bogus"),
        isNull(), eq(teacher1Run1Workgroup))).andThrow(new IllegalArgumentException("Invalid"));
    replay(runService, userService, workgroupService, teacherPresentationConfigService);
    ResponseStatusException e = assertThrows(ResponseStatusException.class, () -> controller
        .saveConfig(teacherAuth, request(run1Period1Id, Arrays.asList(new Item(1)), "bogus", null)));
    assertEquals(HttpStatus.BAD_REQUEST, e.getStatusCode());
  }

  @Test
  public void saveConfig_ValidRequest_SaveWithTeacherWorkgroupAndReturnConfig() throws Exception {
    expectTeacherWithPermissions(true, true);
    TeacherPresentationConfig saved = new TeacherPresentationConfig();
    saved.setId(5L);
    saved.setRun(run1);
    saved.setPeriod(run1Period1);
    saved.setNodeId(run1Node1Id);
    saved.setComponentId(run1Component1Id);
    saved.setComponentType("Discussion");
    saved.setUpdatedAt(new Timestamp(1000));
    saved.setPrompt("My prompt");
    saved.setStudentNamesDisplay("anonymize");
    saved.setUpdatedByWorkgroup(teacher1Run1Workgroup);
    expect(teacherPresentationConfigService.saveConfig(eq(run1), eq(run1Period1),
        eq(run1Node1Id), eq(run1Component1Id), eq("Discussion"), eq(Arrays.asList(7, 8)),
        eq("anonymize"), eq("My prompt"), eq(teacher1Run1Workgroup))).andReturn(saved);
    expect(teacherPresentationConfigService.getStudentWorkIds(saved))
        .andReturn(Arrays.asList(7, 8));
    expect(teacherPresentationConfigService.getAnswers(saved))
        .andReturn(Arrays.asList());
    replay(runService, userService, workgroupService, teacherPresentationConfigService);
    ConfigResponse response = controller.saveConfig(teacherAuth,
        request(run1Period1Id, Arrays.asList(new Item(7), new Item(8)), "anonymize", "My prompt"));
    assertEquals(5L, response.id());
    assertEquals(2, response.items().size());
    assertEquals("My prompt", response.prompt());
    assertEquals(teacher1Run1Workgroup.getId(), response.updatedByWorkgroupId());
    verify(teacherPresentationConfigService);
  }

  @Test
  public void saveAnswer_ValidRequest_ReturnAnswer() throws Exception {
    expectTeacherWithPermissions(true, true);
    TeacherPresentationReflectionAnswer answer = new TeacherPresentationReflectionAnswer();
    answer.setQuestionId("q1");
    answer.setQuestionText("Why?");
    answer.setAnswerText("Because");
    answer.setAnsweredByWorkgroup(teacher1Run1Workgroup);
    answer.setUpdatedAt(new Timestamp(2000));
    expect(teacherPresentationConfigService.saveAnswer(run1, run1Period1, run1Node1Id,
        run1Component1Id, "Discussion", "q1", "Why?", "Because", teacher1Run1Workgroup))
            .andReturn(answer);
    replay(runService, userService, workgroupService, teacherPresentationConfigService);
    AnswerResponse response = controller.saveAnswer(teacherAuth, new AnswerRequest(runId1,
        run1Period1Id, run1Node1Id, run1Component1Id, "Discussion", "q1", "Why?", "Because"));
    assertEquals("q1", response.questionId());
    assertEquals("Because", response.answerText());
    verify(teacherPresentationConfigService);
  }

  @Test
  public void saveAnswer_ServiceRejectsInput_ThrowBadRequest() throws Exception {
    expectTeacherWithPermissions(true, true);
    expect(teacherPresentationConfigService.saveAnswer(eq(run1), eq(run1Period1),
        eq(run1Node1Id), eq(run1Component1Id), eq("Discussion"), eq(""), anyObject(),
        anyObject(), eq(teacher1Run1Workgroup)))
            .andThrow(new IllegalArgumentException("Invalid questionId"));
    replay(runService, userService, workgroupService, teacherPresentationConfigService);

    ResponseStatusException e = assertThrows(ResponseStatusException.class, () -> controller
        .saveAnswer(teacherAuth, new AnswerRequest(runId1, run1Period1Id, run1Node1Id,
            run1Component1Id, "Discussion", "", "Why?", "Because")));
    assertEquals(HttpStatus.BAD_REQUEST, e.getStatusCode());
  }

  @Test
  public void saveAnswer_NoGradePermission_ThrowAccessDenied() throws Exception {
    expectTeacherWithPermissions(true, false);
    replay(runService, userService, workgroupService, teacherPresentationConfigService);

    assertThrows(AccessDeniedException.class, () -> controller.saveAnswer(teacherAuth,
        new AnswerRequest(runId1, run1Period1Id, run1Node1Id, run1Component1Id,
            "Discussion", "q1", "Why?", "Because")));
  }

  @Test
  public void saveAnswer_PeriodNotInRun_ThrowBadRequest() throws Exception {
    expectTeacherWithPermissions(true, true);
    replay(runService, userService, workgroupService, teacherPresentationConfigService);

    ResponseStatusException e = assertThrows(ResponseStatusException.class, () -> controller
        .saveAnswer(teacherAuth, new AnswerRequest(runId1, 99999L, run1Node1Id,
            run1Component1Id, "Discussion", "q1", "Why?", "Because")));
    assertEquals(HttpStatus.BAD_REQUEST, e.getStatusCode());
  }

  @Test
  public void getConfig_ExistingConfigWithAnswers_ReturnPopulatedResponse() throws Exception {
    expectTeacherWithPermissions(true, true);

    TeacherPresentationConfig config = new TeacherPresentationConfig();
    config.setId(10L);
    config.setRun(run1);
    config.setPeriod(run1Period1);
    config.setNodeId(run1Node1Id);
    config.setComponentId(run1Component1Id);
    config.setComponentType("Discussion");
    config.setPrompt("Prompt override");
    config.setStudentNamesDisplay("show");
    config.setUpdatedAt(new Timestamp(1000));
    config.setUpdatedByWorkgroup(teacher1Run1Workgroup);

    TeacherPresentationReflectionAnswer answer = new TeacherPresentationReflectionAnswer();
    answer.setQuestionId("q1");
    answer.setQuestionText("Reflection question");
    answer.setAnswerText("Reflection answer");
    answer.setUpdatedAt(new Timestamp(2000));
    answer.setAnsweredByWorkgroup(teacher1Run1Workgroup);

    expect(teacherPresentationConfigService.getConfig(run1, run1Period1, run1Node1Id,
        run1Component1Id)).andReturn(config);
    expect(teacherPresentationConfigService.getStudentWorkIds(config))
        .andReturn(Arrays.asList(101, 102));
    expect(teacherPresentationConfigService.getAnswers(config))
        .andReturn(Arrays.asList(answer));

    replay(runService, userService, workgroupService, teacherPresentationConfigService);

    ConfigResponse response = controller.getConfig(teacherAuth, runId1, run1Period1Id,
        run1Node1Id, run1Component1Id);

    assertEquals(10L, response.id());
    assertEquals(2, response.items().size());
    assertEquals(101, response.items().get(0).studentWorkId());
    assertEquals("Prompt override", response.prompt());
    assertEquals("show", response.studentNamesDisplay());
    assertEquals(1, response.answers().size());
    assertEquals("q1", response.answers().get(0).questionId());
    assertEquals("Reflection answer", response.answers().get(0).answerText());
    verify(teacherPresentationConfigService);
  }

  @Test
  public void saveConfig_NullItems_SaveWithEmptyList() throws Exception {
    expectTeacherWithPermissions(true, true);
    TeacherPresentationConfig saved = new TeacherPresentationConfig();
    saved.setId(6L);
    saved.setRun(run1);
    saved.setPeriod(run1Period1);
    saved.setNodeId(run1Node1Id);
    saved.setComponentId(run1Component1Id);
    saved.setComponentType("Discussion");
    saved.setUpdatedAt(new Timestamp(1000));
    saved.setStudentNamesDisplay("hide");
    expect(teacherPresentationConfigService.saveConfig(eq(run1), eq(run1Period1),
        eq(run1Node1Id), eq(run1Component1Id), eq("Discussion"), eq(new ArrayList<>()),
        eq("hide"), isNull(), eq(teacher1Run1Workgroup))).andReturn(saved);
    expect(teacherPresentationConfigService.getStudentWorkIds(saved))
        .andReturn(new ArrayList<>());
    expect(teacherPresentationConfigService.getAnswers(saved))
        .andReturn(new ArrayList<>());
    replay(runService, userService, workgroupService, teacherPresentationConfigService);
    ConfigResponse response = controller.saveConfig(teacherAuth,
        request(run1Period1Id, null, "hide", null));
    assertEquals(6L, response.id());
    assertTrue(response.items().isEmpty());
    verify(teacherPresentationConfigService);
  }

  @Test
  public void saveConfig_TeacherWithNoWorkgroup_SaveWithNullWorkgroup() throws Exception {
    expect(runService.retrieveById(runId1)).andReturn(run1).anyTimes();
    expect(userService.retrieveUserByUsername(teacherAuth.getName())).andReturn(teacher1)
        .anyTimes();
    expect(runService.isAllowedToViewStudentWork(run1, teacher1)).andReturn(true).anyTimes();
    expect(runService.isAllowedToGradeStudentWork(run1, teacher1)).andReturn(true).anyTimes();
    expect(workgroupService.getWorkgroupListByRunAndUser(run1, teacher1))
        .andReturn(new ArrayList<>()).anyTimes();

    TeacherPresentationConfig saved = new TeacherPresentationConfig();
    saved.setId(7L);
    saved.setRun(run1);
    saved.setPeriod(run1Period1);
    saved.setNodeId(run1Node1Id);
    saved.setComponentId(run1Component1Id);
    saved.setComponentType("Discussion");
    saved.setUpdatedAt(new Timestamp(1000));
    saved.setStudentNamesDisplay("hide");
    saved.setUpdatedByWorkgroup(null);

    expect(teacherPresentationConfigService.saveConfig(eq(run1), eq(run1Period1),
        eq(run1Node1Id), eq(run1Component1Id), eq("Discussion"), anyObject(),
        eq("hide"), isNull(), isNull())).andReturn(saved);
    expect(teacherPresentationConfigService.getStudentWorkIds(saved))
        .andReturn(new ArrayList<>());
    expect(teacherPresentationConfigService.getAnswers(saved))
        .andReturn(new ArrayList<>());

    replay(runService, userService, workgroupService, teacherPresentationConfigService);
    ConfigResponse response = controller.saveConfig(teacherAuth,
        request(run1Period1Id, new ArrayList<>(), "hide", null));
    assertEquals(7L, response.id());
    assertNull(response.updatedByWorkgroupId());
    verify(teacherPresentationConfigService);
  }
}
