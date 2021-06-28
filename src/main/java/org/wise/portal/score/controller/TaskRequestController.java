package org.wise.portal.score.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.wise.portal.score.domain.TaskRequest;
import org.wise.portal.score.repository.TaskRepository;
import org.wise.portal.score.repository.TaskRequestRepository;

import java.util.List;

@RestController
@RequestMapping(value = "/api", produces = "application/json;charset=UTF-8")
public class TaskRequestController {


  private TaskRepository taskRepository;
  private TaskRequestRepository taskRequestRepository;

  @Autowired
  public TaskRequestController(TaskRequestRepository taskRequestRepository,
                               TaskRepository taskRepository) {
    this.taskRequestRepository = taskRequestRepository;
    this.taskRepository = taskRepository;
  }

  @GetMapping("/taskrequests")
  public List<TaskRequest> taskRequests() {
    return this.taskRequestRepository.findAll();
  }

}
