package org.wise.portal.service.teacherreflection.impl;

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Arrays;
import java.util.List;

import org.easymock.EasyMockExtension;
import org.easymock.Mock;
import org.easymock.TestSubject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.wise.portal.dao.teacherreflection.TeacherReflectionDao;
import org.wise.portal.domain.group.impl.PersistentGroup;
import org.wise.portal.domain.run.impl.RunImpl;
import org.wise.portal.domain.workgroup.impl.WorkgroupImpl;
import org.wise.vle.domain.teacherreflection.TeacherReflection;

@ExtendWith(EasyMockExtension.class)
public class TeacherReflectionServiceImplTest {

  @TestSubject
  private TeacherReflectionServiceImpl service = new TeacherReflectionServiceImpl();

  @Mock
  private TeacherReflectionDao<TeacherReflection> teacherReflectionDao;

  private RunImpl run = new RunImpl();
  private PersistentGroup period = new PersistentGroup();
  private WorkgroupImpl teacherWorkgroup = new WorkgroupImpl();

  @BeforeEach
  public void setUp() {
    run.setId(1L);
    period.setId(10L);
    teacherWorkgroup.setId(20L);
  }

  @Test
  public void getQuestions_DelegatesToDao() {
    TeacherReflection r = new TeacherReflection();
    expect(teacherReflectionDao.getQuestions(run, period, "node1", "comp1"))
        .andReturn(Arrays.asList(r));
    replay(teacherReflectionDao);

    List<TeacherReflection> result = service.getQuestions(run, period, "node1", "comp1");
    assertEquals(1, result.size());
    assertEquals(r, result.get(0));
  }

  @Test
  public void saveAnswer_InvalidQuestionId_ThrowsIllegalArgument() {
    replay(teacherReflectionDao);
    assertThrows(IllegalArgumentException.class, () -> service.saveAnswer(run, period, "node1",
        "comp1", "Discussion", " ", "Why?", "Answer", teacherWorkgroup));
  }

  @Test
  public void saveAnswer_QuestionTextTooLong_ThrowsIllegalArgument() {
    replay(teacherReflectionDao);
    assertThrows(IllegalArgumentException.class, () -> service.saveAnswer(run, period, "node1",
        "comp1", "Discussion", "q1", "x".repeat(5001), "Answer", teacherWorkgroup));
  }

  @Test
  public void saveAnswer_NullTeacherWorkgroup_ThrowsIllegalArgument() {
    replay(teacherReflectionDao);
    assertThrows(IllegalArgumentException.class, () -> service.saveAnswer(run, period, "node1",
        "comp1", "Discussion", "q1", "Question text", "Answer text", null));
  }

  @Test
  public void saveAnswer_NewQuestion_CreatesAndSaves() {
    expect(teacherReflectionDao.getQuestion(run, period, "node1", "comp1", "q1"))
        .andReturn(null);
    teacherReflectionDao.save(anyObject(TeacherReflection.class));
    expectLastCall();
    replay(teacherReflectionDao);

    TeacherReflection r = service.saveAnswer(run, period, "node1", "comp1", "Discussion",
        "q1", "Question text", "Answer text", teacherWorkgroup);
    assertNotNull(r);
    assertEquals("q1", r.getQuestionId());
    assertEquals("Question text", r.getQuestionText());
    assertEquals("Answer text", r.getAnswerText());
    assertEquals("Discussion", r.getComponentType());
    assertEquals(teacherWorkgroup, r.getAnsweredByWorkgroup());
  }

  @Test
  public void saveAnswer_ExistingQuestion_UpdatesFields() {
    TeacherReflection existing = new TeacherReflection();
    existing.setId(5L);
    existing.setQuestionId("q1");
    existing.setQuestionText("Old question");
    existing.setAnswerText("Old answer");

    expect(teacherReflectionDao.getQuestion(run, period, "node1", "comp1", "q1"))
        .andReturn(existing);
    teacherReflectionDao.save(existing);
    expectLastCall();
    replay(teacherReflectionDao);

    TeacherReflection r = service.saveAnswer(run, period, "node1", "comp1", "Discussion",
        "q1", "New question", "New answer", teacherWorkgroup);
    assertEquals(5L, r.getId());
    assertEquals("New question", r.getQuestionText());
    assertEquals("New answer", r.getAnswerText());
    assertEquals(teacherWorkgroup, r.getAnsweredByWorkgroup());
  }
}
