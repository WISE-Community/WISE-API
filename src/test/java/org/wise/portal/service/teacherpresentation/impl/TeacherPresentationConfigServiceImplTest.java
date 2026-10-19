package org.wise.portal.service.teacherpresentation.impl;

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.wise.portal.service.teacherpresentation.DiscussionItem;
import org.wise.portal.service.teacherpresentation.IdeasItem;
import org.wise.portal.service.teacherpresentation.IdeasResponse;

import org.easymock.EasyMockExtension;
import org.easymock.Mock;
import org.easymock.TestSubject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.wise.portal.dao.ObjectNotFoundException;
import org.wise.portal.dao.teacherpresentation.TeacherPresentationConfigDao;
import org.wise.portal.dao.work.StudentWorkDao;
import org.wise.portal.domain.group.impl.PersistentGroup;
import org.wise.portal.domain.run.impl.RunImpl;
import org.wise.vle.domain.teacherpresentation.TeacherPresentationConfig;
import org.wise.vle.domain.work.StudentWork;

@ExtendWith(EasyMockExtension.class)
public class TeacherPresentationConfigServiceImplTest {

  @TestSubject
  private TeacherPresentationConfigServiceImpl service = new TeacherPresentationConfigServiceImpl();

  @Mock
  private TeacherPresentationConfigDao<TeacherPresentationConfig> teacherPresentationConfigDao;

  @Mock
  private StudentWorkDao<StudentWork> studentWorkDao;

  private RunImpl run = new RunImpl();
  private RunImpl otherRun = new RunImpl();
  private PersistentGroup period = new PersistentGroup();
  private PersistentGroup otherPeriod = new PersistentGroup();

  @BeforeEach
  public void setUp() {
    run.setId(1L);
    otherRun.setId(2L);
    period.setId(10L);
    otherPeriod.setId(11L);
  }

  private StudentWork work(RunImpl workRun, PersistentGroup workPeriod, Integer id) {
    StudentWork work = new StudentWork();
    work.setId(id);
    work.setRun(workRun);
    work.setPeriod(workPeriod);
    return work;
  }

  private TeacherPresentationConfig save(List<DiscussionItem> items, String namesDisplay, String prompt) {
    return service.saveConfig(run, period, "node1", "comp1", "Discussion",
        TeacherPresentationConfig.TYPE_DISCUSSION, items, namesDisplay, prompt, null);
  }

  @Test
  public void saveConfig_InvalidNamesDisplay_ThrowIllegalArgument() {
    replay(teacherPresentationConfigDao, studentWorkDao);
    assertThrows(IllegalArgumentException.class, () -> save(new ArrayList<>(), "bogus", null));
  }

  @Test
  public void saveConfig_MoreThan500Items_ThrowIllegalArgument() {
    List<DiscussionItem> items = new ArrayList<>();
    for (int i = 0; i < 501; i++) {
      items.add(new DiscussionItem(i));
    }
    replay(teacherPresentationConfigDao, studentWorkDao);
    assertThrows(IllegalArgumentException.class, () -> save(items, "hide", null));
  }

  @Test
  public void saveConfig_PromptTooLong_ThrowIllegalArgument() {
    replay(teacherPresentationConfigDao, studentWorkDao);
    assertThrows(IllegalArgumentException.class,
        () -> save(new ArrayList<>(), "hide", "x".repeat(1001)));
  }

  @Test
  public void saveConfig_StudentWorkFromOtherPeriod_ThrowIllegalArgument() throws Exception {
    expect(studentWorkDao.getStudentWorkListByIds(Arrays.asList(5)))
        .andReturn(Arrays.asList(work(run, otherPeriod, 5)));
    replay(teacherPresentationConfigDao, studentWorkDao);
    assertThrows(IllegalArgumentException.class,
        () -> save(Arrays.asList(new DiscussionItem(5)), "hide", null));
  }

  @Test
  public void saveConfig_StudentWorkFromOtherRun_ThrowIllegalArgument() throws Exception {
    expect(studentWorkDao.getStudentWorkListByIds(Arrays.asList(5)))
        .andReturn(Arrays.asList(work(otherRun, period, 5)));
    replay(teacherPresentationConfigDao, studentWorkDao);
    assertThrows(IllegalArgumentException.class,
        () -> save(Arrays.asList(new DiscussionItem(5)), "hide", null));
  }

  @Test
  public void saveConfig_UnknownStudentWork_ThrowIllegalArgument() throws Exception {
    expect(studentWorkDao.getStudentWorkListByIds(Arrays.asList(5)))
        .andReturn(Collections.emptyList());
    replay(teacherPresentationConfigDao, studentWorkDao);
    assertThrows(IllegalArgumentException.class,
        () -> save(Arrays.asList(new DiscussionItem(5)), "hide", null));
  }

  @Test
  public void saveConfig_NewConfig_CreateWithDefaultsAndDeduplicateItems() throws Exception {
    expect(studentWorkDao.getStudentWorkListByIds(Arrays.asList(5)))
        .andReturn(Arrays.asList(work(run, period, 5)));
    expect(teacherPresentationConfigDao.getConfig(run, period, "node1", "comp1")).andReturn(null);
    teacherPresentationConfigDao.save(anyObject(TeacherPresentationConfig.class));
    expectLastCall();
    replay(teacherPresentationConfigDao, studentWorkDao);
    TeacherPresentationConfig config =
        save(Arrays.asList(new DiscussionItem(5), new DiscussionItem(5)), null, "   ");
    assertEquals("hide", config.getStudentNamesDisplay());
    assertTrue(config.isShowComponentPrompt());
    assertNull(config.getPrompt());
    assertEquals(Arrays.asList(5), service.getStudentWorkIds(config));
    assertEquals("Discussion", config.getComponentType());
  }

  @Test
  public void saveConfig_ExistingConfig_UpdatePromptAndNames() throws Exception {
    TeacherPresentationConfig existing = new TeacherPresentationConfig();
    existing.setStudentNamesDisplay("show");
    expect(teacherPresentationConfigDao.getConfig(run, period, "node1", "comp1"))
        .andReturn(existing);
    teacherPresentationConfigDao.save(existing);
    expectLastCall();
    replay(teacherPresentationConfigDao, studentWorkDao);
    TeacherPresentationConfig config = save(new ArrayList<>(), "anonymize", "My prompt");
    assertEquals("anonymize", config.getStudentNamesDisplay());
    assertEquals("My prompt", config.getPrompt());
    assertEquals(0, service.getStudentWorkIds(config).size());
  }

  @Test
  public void saveConfig_InvalidType_ThrowIllegalArgument() {
    replay(teacherPresentationConfigDao, studentWorkDao);
    assertThrows(IllegalArgumentException.class, () -> service.saveConfig(run, period, "node1",
        "comp1", "OpenResponse", "InvalidType", new ArrayList<>(), "hide", null, null));
  }

  @Test
  public void saveConfig_Ideas_CategoryOnly_SaveAndReturnConfig() throws Exception {
    expect(teacherPresentationConfigDao.getConfig(run, period, "node1", "comp1")).andReturn(null);
    teacherPresentationConfigDao.save(anyObject(TeacherPresentationConfig.class));
    expectLastCall();
    replay(teacherPresentationConfigDao, studentWorkDao);

    List<IdeasItem> items = Arrays.asList(new IdeasItem("idea_sunlight", Collections.emptyList()));
    TeacherPresentationConfig config = service.saveConfig(run, period, "node1", "comp1",
        "OpenResponse", TeacherPresentationConfig.TYPE_IDEAS, items, "hide", null, null);

    assertEquals(TeacherPresentationConfig.TYPE_IDEAS, config.getType());
    List<?> retrieved = service.getItems(config);
    assertEquals(1, retrieved.size());
    IdeasItem item = (IdeasItem) retrieved.get(0);
    assertEquals("idea_sunlight", item.ideaId());
    assertTrue(item.responses().isEmpty());
    assertEquals(0, service.getStudentWorkIds(config).size());
  }

  @Test
  public void saveConfig_Ideas_CategoryAndResponsesAndNullIdeaId_SaveAndReturnConfig()
      throws Exception {
    expect(studentWorkDao.getStudentWorkListByIds(Arrays.asList(5, 7)))
        .andReturn(Arrays.asList(work(run, period, 5), work(run, period, 7)));
    expect(teacherPresentationConfigDao.getConfig(run, period, "node1", "comp1")).andReturn(null);
    teacherPresentationConfigDao.save(anyObject(TeacherPresentationConfig.class));
    expectLastCall();
    replay(teacherPresentationConfigDao, studentWorkDao);

    // two messages from the same chat (same student work id, different message timestamps)
    List<IdeasItem> items = Arrays.asList(
        new IdeasItem("idea_1",
            Arrays.asList(new IdeasResponse(5, 1L, 1000L, "First message"),
                new IdeasResponse(5, 1L, 2000L, "Second message"))),
        new IdeasItem(null, Arrays.asList(new IdeasResponse(7, 2L, null, "Standalone"))));
    TeacherPresentationConfig config = service.saveConfig(run, period, "node1", "comp1",
        "OpenResponse", TeacherPresentationConfig.TYPE_IDEAS, items, "show", "Custom prompt", null);

    assertEquals(TeacherPresentationConfig.TYPE_IDEAS, config.getType());
    List<?> retrieved = service.getItems(config);
    assertEquals(2, retrieved.size());
    IdeasItem first = (IdeasItem) retrieved.get(0);
    assertEquals("idea_1", first.ideaId());
    assertEquals(2, first.responses().size());
    assertEquals(Long.valueOf(1000L), first.responses().get(0).responseTimestamp());
    assertEquals("Second message", first.responses().get(1).text());
    IdeasItem second = (IdeasItem) retrieved.get(1);
    assertNull(second.ideaId());
    assertNull(second.responses().get(0).responseTimestamp());
    assertEquals(Arrays.asList(5, 5, 7), service.getStudentWorkIds(config));
  }

  @Test
  public void saveConfig_Ideas_NullIdeaIdAndEmptyResponses_ThrowIllegalArgument() {
    replay(teacherPresentationConfigDao, studentWorkDao);
    List<IdeasItem> items = Arrays.asList(new IdeasItem(null, Collections.emptyList()));
    assertThrows(IllegalArgumentException.class, () -> service.saveConfig(run, period, "node1",
        "comp1", "OpenResponse", TeacherPresentationConfig.TYPE_IDEAS, items, "hide", null, null));
  }

  @Test
  public void saveConfig_Ideas_ResponseWithoutWorkgroupId_ThrowIllegalArgument() {
    replay(teacherPresentationConfigDao, studentWorkDao);
    List<IdeasItem> items = Arrays
        .asList(new IdeasItem("idea_1", Arrays.asList(new IdeasResponse(5, null, 1000L, "text"))));
    assertThrows(IllegalArgumentException.class, () -> service.saveConfig(run, period, "node1",
        "comp1", "OpenResponse", TeacherPresentationConfig.TYPE_IDEAS, items, "hide", null, null));
  }

  @Test
  public void saveConfig_ShowComponentPrompt_SaveAndPersist() throws Exception {
    expect(teacherPresentationConfigDao.getConfig(run, period, "node1", "comp1")).andReturn(null);
    teacherPresentationConfigDao.save(anyObject(TeacherPresentationConfig.class));
    expectLastCall();
    replay(teacherPresentationConfigDao, studentWorkDao);
    TeacherPresentationConfig config = service.saveConfig(run, period, "node1", "comp1",
        "Discussion", TeacherPresentationConfig.TYPE_DISCUSSION, new ArrayList<>(), "hide", false,
        null, null);
    assertFalse(config.isShowComponentPrompt());
  }
}
