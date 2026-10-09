package org.wise.portal.dao.teacherpresentation.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.wise.portal.domain.authentication.Schoollevel;
import org.wise.portal.domain.group.Group;
import org.wise.portal.domain.run.Run;
import org.wise.portal.domain.user.User;
import org.wise.portal.domain.workgroup.Workgroup;
import org.wise.portal.junit.AbstractTransactionalDbTests;
import org.wise.vle.domain.teacherpresentation.TeacherPresentationConfig;
import org.wise.vle.domain.teacherpresentation.TeacherPresentationReflectionAnswer;

@SpringBootTest
public class HibernateTeacherPresentationConfigDaoTest extends AbstractTransactionalDbTests {

  private Run run;
  private Run otherRun;
  private Group period1;
  private Group period2;
  private Workgroup teacherWorkgroup;

  @Autowired
  private HibernateTeacherPresentationConfigDao teacherPresentationConfigDao;

  @BeforeEach
  public void setUp() throws Exception {
    super.setUp();

    Long projectId1 = getNextAvailableProjectId();
    Long projectId2 = getNextAvailableProjectId();
    Date startTime = Calendar.getInstance().getTime();

    User teacher = createTeacherUser("Albus", "Dumbledore", "adumbledore", "Prof. Dumbledore",
        "secret", "Hogwarts", "Scotland", "UK", "albus@hogwarts.edu", "Hogwarts School",
        Schoollevel.COLLEGE, "google-teacher-1");

    run = createProjectAndRun(projectId1, "Potions 101", teacher, startTime, "POT101");
    otherRun = createProjectAndRun(projectId2, "Defense Against the Dark Arts", teacher, startTime,
        "DADA101");

    period1 = createPeriod("Period 1");
    period2 = createPeriod("Period 2");

    Set<Group> periods = new TreeSet<>();
    periods.add(period1);
    periods.add(period2);
    run.setPeriods(periods);

    teacherWorkgroup = addUserToRun(teacher, run, period1);
  }

  private TeacherPresentationConfig createConfig(Run run, Group period, String nodeId,
      String componentId, String componentType) {
    TeacherPresentationConfig config = new TeacherPresentationConfig();
    config.setRun(run);
    config.setPeriod(period);
    config.setNodeId(nodeId);
    config.setComponentId(componentId);
    config.setComponentType(componentType);
    config.setItems("[{\"studentWorkId\":101}]");
    config.setStudentNamesDisplay(TeacherPresentationConfig.NAMES_HIDE);
    config.setPrompt("Sample discussion prompt");
    config.setUpdatedByWorkgroup(teacherWorkgroup);
    Timestamp now = new Timestamp(System.currentTimeMillis());
    config.setCreatedAt(now);
    config.setUpdatedAt(now);
    teacherPresentationConfigDao.save(config);
    return config;
  }

  private TeacherPresentationReflectionAnswer createAnswer(TeacherPresentationConfig config,
      String questionId, String questionText, String answerText) {
    TeacherPresentationReflectionAnswer answer = new TeacherPresentationReflectionAnswer();
    answer.setTeacherPresentationConfig(config);
    answer.setQuestionId(questionId);
    answer.setQuestionText(questionText);
    answer.setAnswerText(answerText);
    answer.setAnsweredByWorkgroup(teacherWorkgroup);
    Timestamp now = new Timestamp(System.currentTimeMillis());
    answer.setCreatedAt(now);
    answer.setUpdatedAt(now);
    teacherPresentationConfigDao.saveAnswer(answer);
    return answer;
  }

  @Test
  public void getConfig_WhenNoneExists_ReturnsNull() {
    TeacherPresentationConfig config = teacherPresentationConfigDao.getConfig(run, period1,
        "node1", "component1");
    assertNull(config);
  }

  @Test
  public void saveAndGetConfig_WhenExists_ReturnsMatchingConfig() {
    TeacherPresentationConfig saved = createConfig(run, period1, "node1", "component1",
        "Discussion");

    TeacherPresentationConfig retrieved = teacherPresentationConfigDao.getConfig(run, period1,
        "node1", "component1");
    assertNotNull(retrieved);
    assertEquals(saved.getId(), retrieved.getId());
    assertEquals(run.getId(), retrieved.getRun().getId());
    assertEquals(period1.getId(), retrieved.getPeriod().getId());
    assertEquals("node1", retrieved.getNodeId());
    assertEquals("component1", retrieved.getComponentId());
    assertEquals("Discussion", retrieved.getComponentType());
    assertEquals("[{\"studentWorkId\":101}]", retrieved.getItems());
    assertEquals(TeacherPresentationConfig.NAMES_HIDE, retrieved.getStudentNamesDisplay());
    assertEquals("Sample discussion prompt", retrieved.getPrompt());
    assertEquals(teacherWorkgroup.getId(), retrieved.getUpdatedByWorkgroup().getId());
  }

  @Test
  public void getConfig_WithDifferentParameters_ReturnsNull() {
    createConfig(run, period1, "node1", "component1", "Discussion");

    assertNull(teacherPresentationConfigDao.getConfig(otherRun, period1, "node1", "component1"));
    assertNull(teacherPresentationConfigDao.getConfig(run, period2, "node1", "component1"));
    assertNull(teacherPresentationConfigDao.getConfig(run, period1, "otherNode", "component1"));
    assertNull(teacherPresentationConfigDao.getConfig(run, period1, "node1", "otherComponent"));
  }

  @Test
  public void saveAnswer_NewAnswer_PersistsAndCanBeRetrievedByQuestionId() {
    TeacherPresentationConfig config = createConfig(run, period1, "node1", "component1",
        "Discussion");

    TeacherPresentationReflectionAnswer answer = createAnswer(config, "q1",
        "What patterns did you notice?", "Students struggled with step 2.");
    assertNotNull(answer.getId());

    TeacherPresentationReflectionAnswer retrieved = teacherPresentationConfigDao.getAnswer(config,
        "q1");
    assertNotNull(retrieved);
    assertEquals(answer.getId(), retrieved.getId());
    assertEquals("q1", retrieved.getQuestionId());
    assertEquals("What patterns did you notice?", retrieved.getQuestionText());
    assertEquals("Students struggled with step 2.", retrieved.getAnswerText());
    assertEquals(teacherWorkgroup.getId(), retrieved.getAnsweredByWorkgroup().getId());
  }

  @Test
  public void saveAnswer_ExistingAnswer_UpdatesAnswer() {
    TeacherPresentationConfig config = createConfig(run, period1, "node1", "component1",
        "Discussion");
    TeacherPresentationReflectionAnswer answer = createAnswer(config, "q1",
        "Initial question", "Initial answer");
    Long answerId = answer.getId();

    answer.setAnswerText("Updated answer");
    answer.setUpdatedAt(new Timestamp(System.currentTimeMillis()));
    teacherPresentationConfigDao.saveAnswer(answer);
    flush();

    TeacherPresentationReflectionAnswer updated = teacherPresentationConfigDao.getAnswer(config,
        "q1");
    assertNotNull(updated);
    assertEquals(answerId, updated.getId());
    assertEquals("Updated answer", updated.getAnswerText());
  }

  @Test
  public void getAnswers_MultipleAnswersForConfig_ReturnsOnlyAnswersForThatConfig() {
    TeacherPresentationConfig config1 = createConfig(run, period1, "node1", "component1",
        "Discussion");
    TeacherPresentationConfig config2 = createConfig(run, period1, "node2", "component2",
        "Discussion");

    createAnswer(config1, "q1", "Q1 text", "Answer 1");
    createAnswer(config1, "q2", "Q2 text", "Answer 2");
    createAnswer(config2, "q1", "Q1 for config2", "Answer other");

    List<TeacherPresentationReflectionAnswer> answersConfig1 = teacherPresentationConfigDao
        .getAnswers(config1);
    assertEquals(2, answersConfig1.size());
    assertTrue(answersConfig1.stream().anyMatch(a -> "q1".equals(a.getQuestionId())));
    assertTrue(answersConfig1.stream().anyMatch(a -> "q2".equals(a.getQuestionId())));

    List<TeacherPresentationReflectionAnswer> answersConfig2 = teacherPresentationConfigDao
        .getAnswers(config2);
    assertEquals(1, answersConfig2.size());
    assertEquals("q1", answersConfig2.get(0).getQuestionId());
    assertEquals("Answer other", answersConfig2.get(0).getAnswerText());
  }

  @Test
  public void getAnswer_NonExistentQuestionId_ReturnsNull() {
    TeacherPresentationConfig config = createConfig(run, period1, "node1", "component1",
        "Discussion");
    createAnswer(config, "q1", "Question 1", "Answer 1");

    assertNull(teacherPresentationConfigDao.getAnswer(config, "nonExistentQuestion"));
  }

  @Test
  public void delete_Config_RemovesConfig() {
    TeacherPresentationConfig config = createConfig(run, period1, "node1", "component1",
        "Discussion");
    flush();

    teacherPresentationConfigDao.delete(config);
    flush();

    assertNull(teacherPresentationConfigDao.getConfig(run, period1, "node1", "component1"));
  }
}
