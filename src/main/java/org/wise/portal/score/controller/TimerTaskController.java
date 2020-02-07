package org.wise.portal.score.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.wise.portal.score.domain.Task;
import org.wise.portal.score.repository.TaskRepository;

import java.util.List;

@RestController
@RequestMapping(value = "/api", produces = "application/json;charset=UTF-8")
public class TimerTaskController {

  @Autowired
  private TaskRepository taskRepository;
  public TimerTaskController() {
  }

  @GetMapping("/tasks")
  public List<Task> tasks() {
    return this.taskRepository.findAll();
  }


  /**
   * Invokes the SCORE Teaching Assistant Tool based on the specified run
   *
   * @param appType type of app
   * @param runId   ID of the run
   */
  @GetMapping(value = {"/tasks/{runId}/{periodId}"})
  protected List<Task> launchTeachingAssistantMonitor(@PathVariable Long runId,
                                                  @PathVariable Long periodId
                                                 ) {
    System.out.println("TA RunId: " + runId);
    System.out.println("TA PeriodId: " + periodId);
    if (periodId != null && runId != null) {
      return this.taskRepository.findAllByRunIdAndPeriodId(runId, periodId);
    }
    return null;
  }
}
