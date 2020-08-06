package org.wise.portal.score.controller;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.wise.portal.score.domain.Task;
import org.wise.portal.score.domain.TaskRequest;
import org.wise.portal.score.repository.TaskRepository;
import org.wise.portal.score.repository.TaskRequestRepository;

import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.databind.node.ObjectNode;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping(value = "/api", produces = "application/json;charset=UTF-8")
public class TimerTaskController {

  private TaskRepository taskRepository;
  private TaskRequestRepository taskRequestRepository;

  @Autowired
  public TimerTaskController(TaskRepository taskRepository,
      TaskRequestRepository taskRequestRepository) {
    this.taskRepository = taskRepository;
    this.taskRequestRepository = taskRequestRepository;
  }

  @GetMapping("/tasks")
  public List<Task> tasks() {
    return this.taskRepository.findAll();
  }

  /**
   * Creates tasks for a project in batch
   */
  @PostMapping(value = { "/task" })
  protected List<Task> createTasksBatch(@RequestBody ObjectNode objectNode,
      HttpServletResponse response) throws JSONException {
    String runIdString = objectNode.get("runId").asText();
    String periodIdString = objectNode.get("periodId").asText();
    String periodName = objectNode.get("periodName").asText();
    String projectIdString = objectNode.get("projectId").asText();
    String workgroupIdString = objectNode.get("workgroupId").asText();
    String tasksString = objectNode.get("tasks").asText();

    if (tasksString != null && runIdString != null && periodIdString != null
        && workgroupIdString != null && projectIdString != null) {
      JSONObject nodeVisitJSON = new JSONObject(tasksString);
      JSONArray nodes = nodeVisitJSON.getJSONArray("nodes");
      for (int i = 0; i < nodes.length(); i++) {
        JSONObject n = nodes.getJSONObject(i);
        System.out.println("NODE " + n);
        String activityId = n.getString("id");
        String activityName = n.getString("title");
        Integer duration = n.getInt("duration");
        this.createTask(Long.parseLong(runIdString),
            Long.parseLong(periodIdString), periodName,
            Long.parseLong(projectIdString), Long.parseLong(workgroupIdString),
            activityId, activityName, duration);
      }
    }

    return null;
  }

  /**
   * Creates a task
   *
   * @param runId
   * @param periodId
   * @param projectId
   * @param workgroupId
   * @param activityId
   * @param activityName
   * @param duration
   */
  public void createTask(Long runId, Long periodId, String periodName,
      Long projectId, Long workgroupId, String activityId, String activityName,
      Integer duration) {
    Optional<Task> found = this.taskRepository
        .findByRunIdAndPeriodIdAndWorkgroupIdAndActivityId(runId, periodId,
            workgroupId, activityId);
    if (!found.isPresent()) {
      Task task = Task.builder().runId(runId).complete(false)
          .workgroupId(workgroupId).projectId(projectId).periodName(periodName)
          .periodId(periodId).duration(duration).complete(false).active(false)
          .name(activityName).activityId(activityId).build();
      this.taskRepository.save(task);
    }
  }

  /**
   * Invokes the SCORE Teaching Assistant Tool based on the specified run
   *
   * @param periodName
   *                     periodName
   * @param runId
   *                     ID of the run
   */
  @GetMapping(value = { "/tasks/name/{runId}/{periodName}" })
  protected List<Task> findAllTasksByRunIdAndPeriodName(
      @PathVariable Long runId, @PathVariable String periodName) {
    System.out.println("RunId: " + runId);
    System.out.println("PeriodName: " + periodName);
    if (periodName != null && runId != null) {
      return this.taskRepository.findAllByRunIdAndPeriodName(runId, periodName);
    }
    return null;
  }

  /**
   * Invokes the SCORE Teaching Assistant Tool based on the specified run
   *
   * @param periodId
   *                   periodId
   * @param runId
   *                   ID of the run
   */
  @GetMapping(value = { "/tasks/id/{runId}/{periodId}" })
  protected List<Task> findAllTasksByRunIdAndPeriodId(@PathVariable Long runId,
      @PathVariable Long periodId) {
    System.out.println("RunId: " + runId);
    System.out.println("PeriodId: " + periodId);
    if (periodId != null && runId != null) {
      return this.taskRepository.findAllByRunIdAndPeriodId(runId, periodId);
    }
    return null;
  }

  /**
   * starts stops the timer for a task
   */
  @PostMapping(value = { "/tasks/timer" })
  protected String timer(@RequestBody ObjectNode objectNode,
      HttpServletResponse response) throws Exception {
    String runIdString = objectNode.get("runId").asText();
    String periodIdString = objectNode.get("periodId").asText();
    String projectIdString = objectNode.get("projectId").asText();
    String workgroupIdString = objectNode.get("workgroupId").asText();
    String activityId = objectNode.get("activityId").asText();
    String eventType = objectNode.get("eventType").asText();

    if (runIdString != null && periodIdString != null
        && workgroupIdString != null && activityId != null && eventType != null
        && projectIdString != null) {
      Optional<Task> byId = this.taskRepository
          .findByRunIdAndPeriodIdAndWorkgroupIdAndActivityId(
              Long.parseLong(runIdString), Long.parseLong(periodIdString),
              Long.parseLong(workgroupIdString), activityId);
      if (eventType.equalsIgnoreCase("start_timer")) {
        byId.ifPresent(task -> {
          long startTime = System.currentTimeMillis();
          task.setActive(true);
          task.setStartTime(new Timestamp(startTime));
          task.setEndTime(
              new Timestamp(startTime + (task.getDuration() * 1000)));
          this.taskRepository.save(task);
        });
      } else {
        byId.ifPresent(task -> {
          task.setActive(false);
          this.taskRepository.save(task);
        });
      }
    }

    return null;
  }

  @PostMapping("/tasks/taskrequest")
  protected String createTaskRequest(@RequestBody ObjectNode objectNode,
      HttpServletResponse response) throws Exception {
    String runIdString = objectNode.get("runId").asText();
    String periodIdString = objectNode.get("periodId").asText();
    String projectIdString = objectNode.get("projectId").asText();
    String workgroupIdString = objectNode.get("workgroupId").asText();
    String activityId = objectNode.get("activityId").asText();
    String requestType = objectNode.get("requestType").asText();

    if (runIdString != null && periodIdString != null
        && workgroupIdString != null && activityId != null
        && requestType != null && projectIdString != null) {
      Optional<Task> byId = this.taskRepository
          .findByRunIdAndPeriodIdAndWorkgroupIdAndActivityId(
              Long.parseLong(runIdString), Long.parseLong(periodIdString),
              Long.parseLong(workgroupIdString), activityId);
      byId.ifPresent(task -> {
        TaskRequest taskRequest = TaskRequest.builder()
            .periodId(Long.parseLong(periodIdString))
            .projectId(Long.parseLong(projectIdString))
            .runId(Long.parseLong(runIdString)).complete(false)
            .workgroupId(Long.parseLong(workgroupIdString)).status(requestType)
            .task(task).build();
        task.addTaskRequest(taskRequest);
        this.taskRepository.save(task);
      });
    }
    return null;
  }

  /**
   * mark a task request complete
   *
   * @param taskRequestId
   *                        the group associated with the task
   */
  @GetMapping(value = { "/tasks/taskrequest/{taskRequestId}/{status}" })
  protected TaskRequest markCompleteTaskRequest(
      @PathVariable Long taskRequestId, @PathVariable String status) {
    System.out.println("taskRequestId: " + taskRequestId);

    if (taskRequestId != null && status != null) {
      Optional<TaskRequest> tr = this.taskRequestRepository
          .findById(taskRequestId);
      if (tr.isPresent()) {
        TaskRequest taskRequest = tr.get();
        if (status.equals("approved")) {
          taskRequest.setComplete(true);
          taskRequest.setStatus(status);
        } else if (status.equals("needs_work")) {
          taskRequest.setComplete(false);
          taskRequest.setStatus(status);
        } else if (status.equals("helped")) {
          taskRequest.setComplete(true);
          taskRequest.setStatus(status);
        }
        return this.taskRequestRepository.save(taskRequest);
      }
    }
    return null;
  }
}
