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
import org.wise.portal.service.teacherpresentation.TeacherPresentationConfigService;
import org.wise.portal.service.user.UserService;
import org.wise.portal.service.workgroup.WorkgroupService;
import org.wise.vle.domain.teacherpresentation.TeacherPresentationConfig;
import org.wise.vle.domain.teacherpresentation.TeacherPresentationReflectionAnswer;

/**
 * REST API for TeacherPresentation: the teacher's selected student work, student name display
 * setting, prompt and reflection answers for a component in a period.
 */
@RestController
@RequestMapping("/api/teacher/presentation-config")
@PreAuthorize("hasRole('ROLE_TEACHER')")
public class TeacherPresentationConfigAPIController {

  @Autowired
  private TeacherPresentationConfigService teacherPresentationConfigService;

  @Autowired
  private RunService runService;

  @Autowired
  private UserService userService;

  @Autowired
  private WorkgroupService workgroupService;

  public record Item(Integer studentWorkId) {
  }

  public record ConfigRequest(Long runId, Long periodId, String nodeId, String componentId,
      String componentType, List<Item> items, String studentNamesDisplay, String prompt) {
  }

  public record AnswerRequest(Long runId, Long periodId, String nodeId, String componentId,
      String componentType, String questionId, String questionText, String answerText) {
  }

  public record AnswerResponse(String questionId, String questionText, String answerText,
      Long answeredByWorkgroupId, Long updatedAt) {
  }

  public record ConfigResponse(Long id, Long runId, Long periodId, String nodeId,
      String componentId, String componentType, List<Item> items, String studentNamesDisplay,
      String prompt, Long updatedByWorkgroupId, Long updatedAt, List<AnswerResponse> answers) {
  }

  @GetMapping
  ConfigResponse getConfig(Authentication auth, @RequestParam Long runId,
      @RequestParam Long periodId, @RequestParam String nodeId,
      @RequestParam String componentId) throws ObjectNotFoundException {
    Run run = runService.retrieveById(runId);
    User user = userService.retrieveUserByUsername(auth.getName());
    if (!runService.isAllowedToViewStudentWork(run, user)) {
      throw new AccessDeniedException("Not permitted");
    }
    Group period = getPeriod(run, periodId);
    TeacherPresentationConfig config = teacherPresentationConfigService.getConfig(run, period,
        nodeId, componentId);
    if (config == null) {
      return new ConfigResponse(null, runId, periodId, nodeId, componentId, null, new ArrayList<>(),
          TeacherPresentationConfig.NAMES_HIDE, null, null, null, new ArrayList<>());
    }
    return toResponse(config);
  }

  @PutMapping
  ConfigResponse saveConfig(Authentication auth, @RequestBody ConfigRequest request)
      throws ObjectNotFoundException {
    Run run = runService.retrieveById(request.runId());
    User user = userService.retrieveUserByUsername(auth.getName());
    if (!runService.isAllowedToGradeStudentWork(run, user)) {
      throw new AccessDeniedException("Not permitted");
    }
    Group period = getPeriod(run, request.periodId());
    List<Integer> studentWorkIds = new ArrayList<>();
    if (request.items() != null) {
      for (Item item : request.items()) {
        studentWorkIds.add(item == null ? null : item.studentWorkId());
      }
    }
    try {
      TeacherPresentationConfig config = teacherPresentationConfigService.saveConfig(run, period,
          request.nodeId(), request.componentId(), request.componentType(), studentWorkIds,
          request.studentNamesDisplay(), request.prompt(), getTeacherWorkgroup(run, user));
      return toResponse(config);
    } catch (IllegalArgumentException e) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
    }
  }

  @PutMapping("/answers")
  AnswerResponse saveAnswer(Authentication auth, @RequestBody AnswerRequest request)
      throws ObjectNotFoundException {
    Run run = runService.retrieveById(request.runId());
    User user = userService.retrieveUserByUsername(auth.getName());
    if (!runService.isAllowedToGradeStudentWork(run, user)) {
      throw new AccessDeniedException("Not permitted");
    }
    Group period = getPeriod(run, request.periodId());
    try {
      TeacherPresentationReflectionAnswer answer = teacherPresentationConfigService.saveAnswer(run,
          period, request.nodeId(), request.componentId(), request.componentType(),
          request.questionId(), request.questionText(), request.answerText(),
          getTeacherWorkgroup(run, user));
      return toAnswerResponse(answer);
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

  private Workgroup getTeacherWorkgroup(Run run, User user) {
    List<Workgroup> workgroups = workgroupService.getWorkgroupListByRunAndUser(run, user);
    return workgroups.isEmpty() ? null : workgroups.get(0);
  }

  private ConfigResponse toResponse(TeacherPresentationConfig config) {
    List<Item> items = new ArrayList<>();
    for (Integer id : teacherPresentationConfigService.getStudentWorkIds(config)) {
      items.add(new Item(id));
    }
    List<AnswerResponse> answers = new ArrayList<>();
    for (TeacherPresentationReflectionAnswer answer : teacherPresentationConfigService
        .getAnswers(config)) {
      answers.add(toAnswerResponse(answer));
    }
    Workgroup updatedBy = config.getUpdatedByWorkgroup();
    return new ConfigResponse(config.getId(), config.getRun().getId(), config.getPeriod().getId(),
        config.getNodeId(), config.getComponentId(), config.getComponentType(), items,
        config.getStudentNamesDisplay(), config.getPrompt(),
        updatedBy == null ? null : updatedBy.getId(), config.getUpdatedAt().getTime(), answers);
  }

  private AnswerResponse toAnswerResponse(TeacherPresentationReflectionAnswer answer) {
    Workgroup answeredBy = answer.getAnsweredByWorkgroup();
    return new AnswerResponse(answer.getQuestionId(), answer.getQuestionText(),
        answer.getAnswerText(), answeredBy == null ? null : answeredBy.getId(),
        answer.getUpdatedAt().getTime());
  }
}
