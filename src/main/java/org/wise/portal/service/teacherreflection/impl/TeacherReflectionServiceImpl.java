package org.wise.portal.service.teacherreflection.impl;

import java.sql.Timestamp;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.wise.portal.dao.teacherreflection.TeacherReflectionDao;
import org.wise.portal.domain.group.Group;
import org.wise.portal.domain.run.Run;
import org.wise.portal.domain.workgroup.Workgroup;
import org.wise.portal.service.teacherreflection.TeacherReflectionService;
import org.wise.vle.domain.teacherreflection.TeacherReflection;

@Service
public class TeacherReflectionServiceImpl implements TeacherReflectionService {

  private static final int MAX_ID_LENGTH = 30;
  private static final int MAX_QUESTION_TEXT_LENGTH = 5000;
  private static final int MAX_ANSWER_TEXT_LENGTH = 20000;

  @Autowired
  private TeacherReflectionDao<TeacherReflection> teacherReflectionDao;

  @Override
  @Transactional(readOnly = true)
  public List<TeacherReflection> getQuestions(Run run, Group period, String nodeId,
      String componentId) {
    return teacherReflectionDao.getQuestions(run, period, nodeId, componentId);
  }

  @Override
  @Transactional
  public TeacherReflection saveAnswer(Run run, Group period, String nodeId, String componentId,
      String componentType, String questionId, String questionText, String answerText,
      Workgroup teacherWorkgroup) {
    validateComponent(nodeId, componentId, componentType);
    if (questionId == null || questionId.isBlank() || questionId.length() > MAX_ID_LENGTH) {
      throw new IllegalArgumentException("Invalid questionId");
    }
    if (questionText == null || questionText.length() > MAX_QUESTION_TEXT_LENGTH) {
      throw new IllegalArgumentException("Invalid questionText");
    }
    if (answerText == null || answerText.length() > MAX_ANSWER_TEXT_LENGTH) {
      throw new IllegalArgumentException("Invalid answerText");
    }
    if (teacherWorkgroup == null) {
      throw new IllegalArgumentException("teacherWorkgroup cannot be null");
    }

    Timestamp now = new Timestamp(System.currentTimeMillis());
    TeacherReflection reflection = teacherReflectionDao.getQuestion(run, period, nodeId,
        componentId, questionId);
    if (reflection == null) {
      reflection = new TeacherReflection();
      reflection.setRun(run);
      reflection.setPeriod(period);
      reflection.setNodeId(nodeId);
      reflection.setComponentId(componentId);
      reflection.setComponentType(componentType);
      reflection.setQuestionId(questionId);
      reflection.setCreatedAt(now);
    }
    reflection.setQuestionText(questionText);
    reflection.setAnswerText(answerText);
    reflection.setAnsweredByWorkgroup(teacherWorkgroup);
    reflection.setUpdatedAt(now);
    teacherReflectionDao.save(reflection);
    return reflection;
  }

  private void validateComponent(String nodeId, String componentId, String componentType) {
    if (nodeId == null || nodeId.isBlank() || nodeId.length() > MAX_ID_LENGTH) {
      throw new IllegalArgumentException("Invalid nodeId");
    }
    if (componentId == null || componentId.isBlank() || componentId.length() > MAX_ID_LENGTH) {
      throw new IllegalArgumentException("Invalid componentId");
    }
    if (componentType == null || componentType.isBlank()
        || componentType.length() > MAX_ID_LENGTH) {
      throw new IllegalArgumentException("Invalid componentType");
    }
  }
}
