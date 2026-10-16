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
import java.util.Collections;
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
import org.wise.portal.presentation.web.controllers.teacher.TeacherPresentationConfigAPIController.ConfigRequest;
import org.wise.portal.presentation.web.controllers.teacher.TeacherPresentationConfigAPIController.ConfigResponse;
import org.wise.portal.presentation.web.controllers.teacher.TeacherPresentationConfigAPIController.Item;
import org.wise.portal.service.teacherpresentation.DiscussionItem;
import org.wise.portal.service.teacherpresentation.IdeasItem;
import org.wise.portal.service.teacherpresentation.IdeasResponse;
import org.wise.portal.service.teacherpresentation.TeacherPresentationConfigService;
import org.wise.vle.domain.teacherpresentation.TeacherPresentationConfig;

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
    return new ConfigRequest(runId1, periodId, run1Node1Id, run1Component1Id, "Discussion",
        TeacherPresentationConfig.ITEM_TYPE_DISCUSSION, items, namesDisplay, true, prompt);
  }

  private ConfigRequest ideasRequest(Long periodId, List<Item> items, String namesDisplay,
      String prompt) {
    return new ConfigRequest(runId1, periodId, run1Node1Id, run1Component1Id, "OpenResponse",
        TeacherPresentationConfig.ITEM_TYPE_IDEAS, items, namesDisplay, true, prompt);
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
    assertNull(response.itemType());
    assertEquals("hide", response.studentNamesDisplay());
    assertTrue(response.showComponentPrompt());
    assertNull(response.prompt());
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
        eq(run1Node1Id), eq(run1Component1Id), eq("Discussion"), eq(TeacherPresentationConfig.ITEM_TYPE_DISCUSSION),
        anyObject(), eq("bogus"), eq(true), isNull(), eq(teacher1Run1Workgroup)))
        .andThrow(new IllegalArgumentException("Invalid"));
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
    saved.setItemType("Discussion");
    saved.setUpdatedAt(new Timestamp(1000));
    saved.setPrompt("My prompt");
    saved.setStudentNamesDisplay("anonymize");
    saved.setShowComponentPrompt(true);
    saved.setUpdatedByWorkgroup(teacher1Run1Workgroup);
    expect(teacherPresentationConfigService.saveConfig(eq(run1), eq(run1Period1),
        eq(run1Node1Id), eq(run1Component1Id), eq("Discussion"), eq(TeacherPresentationConfig.ITEM_TYPE_DISCUSSION),
        eq(Arrays.asList(new DiscussionItem(7), new DiscussionItem(8))),
        eq("anonymize"), eq(true), eq("My prompt"), eq(teacher1Run1Workgroup))).andReturn(saved);
    expect(teacherPresentationConfigService.getItems(saved))
        .andReturn((List) Arrays.asList(new DiscussionItem(7), new DiscussionItem(8)));
    replay(runService, userService, workgroupService, teacherPresentationConfigService);
    ConfigResponse response = controller.saveConfig(teacherAuth,
        request(run1Period1Id, Arrays.asList(new Item(7), new Item(8)), "anonymize", "My prompt"));
    assertEquals(5L, response.id());
    assertEquals(2, response.items().size());
    assertEquals("My prompt", response.prompt());
    assertTrue(response.showComponentPrompt());
    assertEquals(teacher1Run1Workgroup.getId(), response.updatedByWorkgroupId());
    verify(teacherPresentationConfigService);
  }

  @Test
  public void saveConfig_ValidIdeasRequest_SaveAndReturnConfig() throws Exception {
    expectTeacherWithPermissions(true, true);
    TeacherPresentationConfig saved = new TeacherPresentationConfig();
    saved.setId(8L);
    saved.setRun(run1);
    saved.setPeriod(run1Period1);
    saved.setNodeId(run1Node1Id);
    saved.setComponentId(run1Component1Id);
    saved.setComponentType("OpenResponse");
    saved.setItemType("Ideas");
    saved.setUpdatedAt(new Timestamp(1000));
    saved.setPrompt("Ideas prompt");
    saved.setStudentNamesDisplay("hide");
    saved.setShowComponentPrompt(true);
    saved.setUpdatedByWorkgroup(teacher1Run1Workgroup);

    List<IdeasResponse> responses1 = Arrays.asList(new IdeasResponse(101, 1L, 1000L, "First"),
        new IdeasResponse(101, 1L, 2000L, "Second"));
    List<IdeasResponse> responses3 = Arrays.asList(new IdeasResponse(103, 2L, null, "Standalone"));
    List<IdeasItem> expectedSavedItems = Arrays.asList(
        new IdeasItem("idea_1", responses1),
        new IdeasItem("idea_2", Collections.emptyList()),
        new IdeasItem(null, responses3)
    );
    expect(teacherPresentationConfigService.saveConfig(eq(run1), eq(run1Period1),
        eq(run1Node1Id), eq(run1Component1Id), eq("OpenResponse"), eq(TeacherPresentationConfig.ITEM_TYPE_IDEAS),
        eq(expectedSavedItems), eq("hide"), eq(true), eq("Ideas prompt"), eq(teacher1Run1Workgroup))).andReturn(saved);
    expect(teacherPresentationConfigService.getItems(saved))
        .andReturn((List) expectedSavedItems);

    replay(runService, userService, workgroupService, teacherPresentationConfigService);

    List<Item> requestItems = Arrays.asList(
        new Item("idea_1", responses1),
        new Item("idea_2", Collections.emptyList()),
        new Item(null, responses3)
    );
    ConfigResponse response = controller.saveConfig(teacherAuth,
        ideasRequest(run1Period1Id, requestItems, "hide", "Ideas prompt"));

    assertEquals(8L, response.id());
    assertTrue(response.showComponentPrompt());
    assertEquals("Ideas", response.itemType());
    assertEquals(3, response.items().size());
    verify(teacherPresentationConfigService);
  }

  @Test
  public void getConfig_ExistingConfig_ReturnPopulatedResponse() throws Exception {
    expectTeacherWithPermissions(true, true);

    TeacherPresentationConfig config = new TeacherPresentationConfig();
    config.setId(10L);
    config.setRun(run1);
    config.setPeriod(run1Period1);
    config.setNodeId(run1Node1Id);
    config.setComponentId(run1Component1Id);
    config.setComponentType("Discussion");
    config.setItemType("Discussion");
    config.setPrompt("Prompt override");
    config.setStudentNamesDisplay("show");
    config.setUpdatedAt(new Timestamp(1000));
    config.setUpdatedByWorkgroup(teacher1Run1Workgroup);

    expect(teacherPresentationConfigService.getConfig(run1, run1Period1, run1Node1Id,
        run1Component1Id)).andReturn(config);
    expect(teacherPresentationConfigService.getItems(config))
        .andReturn((List) Arrays.asList(new DiscussionItem(101), new DiscussionItem(102)));

    replay(runService, userService, workgroupService, teacherPresentationConfigService);

    ConfigResponse response = controller.getConfig(teacherAuth, runId1, run1Period1Id,
        run1Node1Id, run1Component1Id);

    assertEquals(10L, response.id());
    assertEquals(2, response.items().size());
    assertEquals(101, ((DiscussionItem) response.items().get(0)).studentWorkId());
    assertEquals("Prompt override", response.prompt());
    assertEquals("show", response.studentNamesDisplay());
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
    saved.setItemType("Discussion");
    saved.setUpdatedAt(new Timestamp(1000));
    saved.setStudentNamesDisplay("hide");
    expect(teacherPresentationConfigService.saveConfig(eq(run1), eq(run1Period1),
        eq(run1Node1Id), eq(run1Component1Id), eq("Discussion"), eq(TeacherPresentationConfig.ITEM_TYPE_DISCUSSION),
        eq(new ArrayList<>()), eq("hide"), eq(true), isNull(), eq(teacher1Run1Workgroup))).andReturn(saved);
    expect(teacherPresentationConfigService.getItems(saved))
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
    saved.setItemType("Discussion");
    saved.setUpdatedAt(new Timestamp(1000));
    saved.setStudentNamesDisplay("hide");
    saved.setUpdatedByWorkgroup(null);

    expect(teacherPresentationConfigService.saveConfig(eq(run1), eq(run1Period1),
        eq(run1Node1Id), eq(run1Component1Id), eq("Discussion"), eq(TeacherPresentationConfig.ITEM_TYPE_DISCUSSION),
        anyObject(), eq("hide"), eq(true), isNull(), isNull())).andReturn(saved);
    expect(teacherPresentationConfigService.getItems(saved))
        .andReturn(new ArrayList<>());

    replay(runService, userService, workgroupService, teacherPresentationConfigService);
    ConfigResponse response = controller.saveConfig(teacherAuth,
        request(run1Period1Id, new ArrayList<>(), "hide", null));
    assertEquals(7L, response.id());
    assertNull(response.updatedByWorkgroupId());
    verify(teacherPresentationConfigService);
  }
}
