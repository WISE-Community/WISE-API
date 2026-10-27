package org.wise.portal.dao.teacherreflection;

import java.util.List;

import org.wise.portal.dao.SimpleDao;
import org.wise.portal.domain.group.Group;
import org.wise.portal.domain.run.Run;
import org.wise.vle.domain.teacherreflection.TeacherReflection;

public interface TeacherReflectionDao<T extends TeacherReflection> extends SimpleDao<T> {

  List<T> getQuestions(Run run, Group period, String nodeId, String componentId);

  T getQuestion(Run run, Group period, String nodeId, String componentId, String questionId);
}
