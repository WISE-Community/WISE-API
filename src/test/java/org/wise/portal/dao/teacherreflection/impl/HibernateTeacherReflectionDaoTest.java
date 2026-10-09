package org.wise.portal.dao.teacherreflection.impl;

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
import org.wise.vle.domain.teacherreflection.TeacherReflection;

@SpringBootTest
public class HibernateTeacherReflectionDaoTest extends AbstractTransactionalDbTests {

  private Run run;
  private Run otherRun;
  private Group period1;
  private Group period2;
  private Workgroup teacherWorkgroup;

  @Autowired
  private HibernateTeacherReflectionDao teacherReflectionDao;

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

  private TeacherReflection createReflection(Run run, Group period, String nodeId,
      String componentId, String componentType, String questionId, String questionText,
      String answerText) {
    TeacherReflection r = new TeacherReflection();
    r.setRun(run);
    r.setPeriod(period);
    r.setNodeId(nodeId);
    r.setComponentId(componentId);
    r.setComponentType(componentType);
    r.setQuestionId(questionId);
    r.setQuestionText(questionText);
    r.setAnswerText(answerText);
    r.setAnsweredByWorkgroup(teacherWorkgroup);
    Timestamp now = new Timestamp(System.currentTimeMillis());
    r.setCreatedAt(now);
    r.setUpdatedAt(now);
    teacherReflectionDao.save(r);
    return r;
  }

  @Test
  public void getQuestion_WhenNoneExists_ReturnsNull() {
    TeacherReflection r = teacherReflectionDao.getQuestion(run, period1, "node1",
        "comp1", "q1");
    assertNull(r);
  }

  @Test
  public void saveAndGetQuestion_WhenExists_ReturnsMatching() {
    TeacherReflection saved = createReflection(run, period1, "node1", "comp1", "Discussion",
        "q1", "What did you observe?", "Great engagement");

    TeacherReflection retrieved = teacherReflectionDao.getQuestion(run, period1,
        "node1", "comp1", "q1");
    assertNotNull(retrieved);
    assertEquals(saved.getId(), retrieved.getId());
    assertEquals(run.getId(), retrieved.getRun().getId());
    assertEquals(period1.getId(), retrieved.getPeriod().getId());
    assertEquals("node1", retrieved.getNodeId());
    assertEquals("comp1", retrieved.getComponentId());
    assertEquals("Discussion", retrieved.getComponentType());
    assertEquals("q1", retrieved.getQuestionId());
    assertEquals("What did you observe?", retrieved.getQuestionText());
    assertEquals("Great engagement", retrieved.getAnswerText());
    assertEquals(teacherWorkgroup.getId(), retrieved.getAnsweredByWorkgroup().getId());
  }

  @Test
  public void getQuestion_DifferentParameters_ReturnsNull() {
    createReflection(run, period1, "node1", "comp1", "Discussion", "q1", "Question 1", "Answer 1");

    assertNull(teacherReflectionDao.getQuestion(otherRun, period1, "node1", "comp1", "q1"));
    assertNull(teacherReflectionDao.getQuestion(run, period2, "node1", "comp1", "q1"));
    assertNull(teacherReflectionDao.getQuestion(run, period1, "node2", "comp1", "q1"));
    assertNull(teacherReflectionDao.getQuestion(run, period1, "node1", "comp2", "q1"));
    assertNull(teacherReflectionDao.getQuestion(run, period1, "node1", "comp1", "q2"));
  }

  @Test
  public void getQuestions_MultipleQuestions_ReturnsOnlyMatching() {
    createReflection(run, period1, "node1", "comp1", "Discussion", "q1", "Q1 text", "Answer 1");
    createReflection(run, period1, "node1", "comp1", "Discussion", "q2", "Q2 text", "Answer 2");
    createReflection(run, period1, "node2", "comp2", "Discussion", "q1", "Other Q", "Other Answer");

    List<TeacherReflection> list = teacherReflectionDao.getQuestions(run, period1,
        "node1", "comp1");
    assertEquals(2, list.size());
    assertTrue(list.stream().anyMatch(q -> "q1".equals(q.getQuestionId())));
    assertTrue(list.stream().anyMatch(q -> "q2".equals(q.getQuestionId())));
  }

  @Test
  public void saveQuestion_UpdateExisting_UpdatesFields() {
    TeacherReflection r = createReflection(run, period1, "node1", "comp1", "Discussion",
        "q1", "Original Q", "Initial answer");
    Long id = r.getId();

    r.setAnswerText("Updated answer");
    r.setUpdatedAt(new Timestamp(System.currentTimeMillis()));
    teacherReflectionDao.save(r);
    flush();

    TeacherReflection updated = teacherReflectionDao.getQuestion(run, period1,
        "node1", "comp1", "q1");
    assertNotNull(updated);
    assertEquals(id, updated.getId());
    assertEquals("Updated answer", updated.getAnswerText());
  }
}
