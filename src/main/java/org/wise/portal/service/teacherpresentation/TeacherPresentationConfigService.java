package org.wise.portal.service.teacherpresentation;

import java.util.List;

import org.wise.portal.domain.group.Group;
import org.wise.portal.domain.run.Run;
import org.wise.portal.domain.workgroup.Workgroup;
import org.wise.vle.domain.teacherpresentation.TeacherPresentationConfig;
import org.wise.vle.domain.teacherpresentation.TeacherPresentationReflectionAnswer;

public interface TeacherPresentationConfigService {

  int MAX_ITEMS = 500;
  int MAX_PROMPT_LENGTH = 1000;

  /**
   * @return the config, or null if the teacher has not saved one yet
   */
  TeacherPresentationConfig getConfig(Run run, Group period, String nodeId, String componentId);

  List<TeacherPresentationReflectionAnswer> getAnswers(TeacherPresentationConfig config);

  List<Integer> getStudentWorkIds(TeacherPresentationConfig config);

  /**
   * Creates or updates the config for the run, period, node and component.
   *
   * @param prompt the teacher's prompt. null or blank resets to the component default.
   * @param studentNamesDisplay one of show, hide or anonymize. null keeps the current value (or
   * hide for a new config).
   * @throws IllegalArgumentException if the input is invalid
   */
  TeacherPresentationConfig saveConfig(Run run, Group period, String nodeId, String componentId,
      String componentType, List<Integer> studentWorkIds, String studentNamesDisplay,
      String prompt, Workgroup teacherWorkgroup);

  /**
   * Creates or updates the answer to a reflection question. Creates the config if it does not
   * exist yet.
   *
   * @throws IllegalArgumentException if the input is invalid
   */
  TeacherPresentationReflectionAnswer saveAnswer(Run run, Group period, String nodeId,
      String componentId, String componentType, String questionId, String questionText,
      String answerText, Workgroup teacherWorkgroup);
}
