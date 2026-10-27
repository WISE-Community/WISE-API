package org.wise.portal.service.teacherreflection;

import java.util.List;

import org.wise.portal.domain.group.Group;
import org.wise.portal.domain.run.Run;
import org.wise.portal.domain.workgroup.Workgroup;
import org.wise.vle.domain.teacherreflection.TeacherReflection;

public interface TeacherReflectionService {

  List<TeacherReflection> getQuestions(Run run, Group period, String nodeId,
      String componentId);

  TeacherReflection saveAnswer(Run run, Group period, String nodeId,
      String componentId, String componentType, String questionId, String questionText,
      String answerText, Workgroup teacherWorkgroup);
}
