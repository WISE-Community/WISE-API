package org.wise.portal.presentation.web.controllers.teacher;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.wise.portal.dao.ObjectNotFoundException;
import org.wise.portal.domain.group.Group;
import org.wise.portal.domain.run.Run;
import org.wise.portal.domain.user.User;
import org.wise.portal.domain.workgroup.Workgroup;
import org.wise.portal.service.run.RunService;
import org.wise.portal.service.teacherreflection.TeacherReflectionService;
import org.wise.portal.service.user.UserService;
import org.wise.portal.service.workgroup.WorkgroupService;
import org.wise.vle.domain.teacherreflection.TeacherReflection;

/**
 * REST API for TeacherReflection: viewing and answering teacher reflection questions
 * for a component in a period of a run.
 */
@RestController
@RequestMapping("/api/teacher/reflection")
@PreAuthorize("hasRole('ROLE_TEACHER')")
public class TeacherReflectionAPIController {

  @Autowired
  private TeacherReflectionService teacherReflectionService;

  @Autowired
  private RunService runService;

  @Autowired
  private UserService userService;

  @Autowired
  private WorkgroupService workgroupService;

  public record AnswerRequest(Long runId, Long periodId, String nodeId, String componentId,
      String componentType, String questionId, String questionText, String answerText,
      Long answeredByWorkgroupId) {
  }

  public record TeacherReflectionResponse(Long id, Long runId, Long periodId, String nodeId,
      String componentId, String componentType, String questionId, String questionText,
      String answerText, Long answeredByWorkgroupId, Long createdAt, Long updatedAt) {
  }

  @GetMapping
  public List<TeacherReflectionResponse> getQuestions(Authentication auth, @RequestParam Long runId,
      @RequestParam Long periodId, @RequestParam String nodeId, @RequestParam String componentId)
      throws ObjectNotFoundException {
    Run run = runService.retrieveById(runId);
    User user = userService.retrieveUserByUsername(auth.getName());
    if (!runService.isAllowedToViewStudentWork(run, user)) {
      throw new AccessDeniedException("Not permitted");
    }
    Group period = getPeriod(run, periodId);
    List<TeacherReflection> reflections = teacherReflectionService.getQuestions(run, period, nodeId,
        componentId);
    List<TeacherReflectionResponse> response = new ArrayList<>();
    for (TeacherReflection reflection : reflections) {
      response.add(toResponse(reflection));
    }
    return response;
  }

  @PutMapping("/answer")
  public TeacherReflectionResponse saveAnswer(Authentication auth,
      @RequestBody AnswerRequest request) throws ObjectNotFoundException {
    Run run = runService.retrieveById(request.runId());
    User user = userService.retrieveUserByUsername(auth.getName());
    if (!runService.isAllowedToGradeStudentWork(run, user)) {
      throw new AccessDeniedException("Not permitted");
    }
    Group period = getPeriod(run, request.periodId());
    if (request.answeredByWorkgroupId() == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          "answeredByWorkgroupId cannot be null");
    }
    Workgroup teacherWorkgroup = workgroupService.retrieveById(request.answeredByWorkgroupId());
    if (teacherWorkgroup != null && !teacherWorkgroup.isTeacherWorkgroup()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          "Workgroup is not a teacher workgroup");
    }
    if (teacherWorkgroup != null && !run.equals(teacherWorkgroup.getRun())) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          "Workgroup does not belong to the run");
    }
    try {
      TeacherReflection reflection = teacherReflectionService.saveAnswer(run, period,
          request.nodeId(), request.componentId(), request.componentType(), request.questionId(),
          request.questionText(), request.answerText(), teacherWorkgroup);
      return toResponse(reflection);
    } catch (IllegalArgumentException e) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
    }
  }

  private Group getPeriod(Run run, Long periodId) {
    for (Group period : run.getPeriods()) {
      if (period.getId().equals(periodId)) {
        return period;
      }
    }
    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Period does not belong to the run");
  }

  private TeacherReflectionResponse toResponse(TeacherReflection reflection) {
    return new TeacherReflectionResponse(reflection.getId(), reflection.getRun().getId(),
        reflection.getPeriod().getId(), reflection.getNodeId(), reflection.getComponentId(),
        reflection.getComponentType(), reflection.getQuestionId(), reflection.getQuestionText(),
        reflection.getAnswerText(), reflection.getAnsweredByWorkgroup().getId(),
        reflection.getCreatedAt().getTime(), reflection.getUpdatedAt().getTime());
  }
}
