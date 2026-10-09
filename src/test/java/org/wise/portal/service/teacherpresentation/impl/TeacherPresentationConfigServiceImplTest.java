package org.wise.portal.service.teacherpresentation.impl;

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
import org.wise.vle.domain.teacherpresentation.TeacherPresentationReflectionAnswer;
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

  private StudentWork work(RunImpl workRun, PersistentGroup workPeriod) {
    StudentWork work = new StudentWork();
    work.setRun(workRun);
    work.setPeriod(workPeriod);
    return work;
  }

  private TeacherPresentationConfig save(List<Integer> ids, String namesDisplay, String prompt) {
    return service.saveConfig(run, period, "node1", "comp1", "Discussion", ids, namesDisplay,
        prompt, null);
  }

  @Test
  public void saveConfig_InvalidNamesDisplay_ThrowIllegalArgument() {
    replay(teacherPresentationConfigDao, studentWorkDao);
    assertThrows(IllegalArgumentException.class, () -> save(new ArrayList<>(), "bogus", null));
  }

  @Test
  public void saveConfig_MoreThan500Items_ThrowIllegalArgument() {
    List<Integer> ids = new ArrayList<>();
    for (int i = 0; i < 501; i++) {
      ids.add(i);
    }
    replay(teacherPresentationConfigDao, studentWorkDao);
    assertThrows(IllegalArgumentException.class, () -> save(ids, "hide", null));
  }

  @Test
  public void saveConfig_PromptTooLong_ThrowIllegalArgument() {
    replay(teacherPresentationConfigDao, studentWorkDao);
    assertThrows(IllegalArgumentException.class,
        () -> save(new ArrayList<>(), "hide", "x".repeat(1001)));
  }

  @Test
  public void saveConfig_StudentWorkFromOtherPeriod_ThrowIllegalArgument() throws Exception {
    expect(studentWorkDao.getById(5)).andReturn(work(run, otherPeriod));
    replay(teacherPresentationConfigDao, studentWorkDao);
    assertThrows(IllegalArgumentException.class, () -> save(Arrays.asList(5), "hide", null));
  }

  @Test
  public void saveConfig_StudentWorkFromOtherRun_ThrowIllegalArgument() throws Exception {
    expect(studentWorkDao.getById(5)).andReturn(work(otherRun, period));
    replay(teacherPresentationConfigDao, studentWorkDao);
    assertThrows(IllegalArgumentException.class, () -> save(Arrays.asList(5), "hide", null));
  }

  @Test
  public void saveConfig_UnknownStudentWork_ThrowIllegalArgument() throws Exception {
    expect(studentWorkDao.getById(5)).andThrow(new ObjectNotFoundException(5L, StudentWork.class));
    replay(teacherPresentationConfigDao, studentWorkDao);
    assertThrows(IllegalArgumentException.class, () -> save(Arrays.asList(5), "hide", null));
  }

  @Test
  public void saveConfig_NewConfig_CreateWithDefaultsAndDeduplicateItems() throws Exception {
    expect(studentWorkDao.getById(5)).andReturn(work(run, period));
    expect(teacherPresentationConfigDao.getConfig(run, period, "node1", "comp1")).andReturn(null);
    teacherPresentationConfigDao.save(anyObject(TeacherPresentationConfig.class));
    expectLastCall();
    replay(teacherPresentationConfigDao, studentWorkDao);
    TeacherPresentationConfig config = save(Arrays.asList(5, 5), null, "   ");
    assertEquals("hide", config.getStudentNamesDisplay());
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
  public void saveAnswer_ExistingAnswer_UpdateInsteadOfCreatingHistory() {
    TeacherPresentationConfig config = new TeacherPresentationConfig();
    config.setId(3L);
    TeacherPresentationReflectionAnswer existing = new TeacherPresentationReflectionAnswer();
    existing.setId(9L);
    existing.setQuestionId("q1");
    expect(teacherPresentationConfigDao.getConfig(run, period, "node1", "comp1")).andReturn(config);
    expect(teacherPresentationConfigDao.getAnswer(config, "q1")).andReturn(existing);
    teacherPresentationConfigDao.saveAnswer(existing);
    expectLastCall();
    replay(teacherPresentationConfigDao, studentWorkDao);
    TeacherPresentationReflectionAnswer answer = service.saveAnswer(run, period, "node1", "comp1",
        "Discussion", "q1", "Why?", "New answer", null);
    assertEquals(9L, answer.getId());
    assertEquals("New answer", answer.getAnswerText());
    assertEquals("Why?", answer.getQuestionText());
  }

  @Test
  public void saveAnswer_InvalidQuestionId_ThrowIllegalArgument() {
    replay(teacherPresentationConfigDao, studentWorkDao);
    assertThrows(IllegalArgumentException.class, () -> service.saveAnswer(run, period, "node1",
        "comp1", "Discussion", " ", "Why?", "a", null));
  }
}
