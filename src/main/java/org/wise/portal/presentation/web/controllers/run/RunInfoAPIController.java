package org.wise.portal.presentation.web.controllers.run;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.wise.portal.dao.ObjectNotFoundException;
import org.wise.portal.domain.group.Group;
import org.wise.portal.domain.run.Run;
import org.wise.portal.domain.user.User;
import org.wise.portal.service.run.RunService;
import org.wise.portal.service.user.UserService;

/**
 * Run Info REST API
 *
 * @author Hiroki Terashima
 * @author Geoffrey Kwan
 * @author Jonathan Lim-Breitbart
 * @author Aaron Detre
 */
@RestController
@RequestMapping("/api/run")
public class RunInfoAPIController {

  @Autowired
  private RunService runService;

  @Autowired
  private UserService userService;

  @Secured("ROLE_USER")
  @GetMapping("/info-by-id")
  HashMap<String, Object> getRunInfoById(Authentication auth, @RequestParam Long runId) {
    try {
      User user = userService.retrieveUserByUsername(auth.getName());
      Run run = runService.retrieveById(runId);
      if (userService.isUserAssociatedWithRun(user, run)) {
        return getRunInfo(run);
      }
    } catch (ObjectNotFoundException e) {
    }
    return createRunNotFoundInfo();
  }

  private HashMap<String, Object> getRunInfo(Run run) {
    HashMap<String, Object> info = new HashMap<String, Object>();
    info.put("id", String.valueOf(run.getId()));
    info.put("name", run.getName());
    info.put("runCode", run.getRuncode());
    info.put("startTime", run.getStartTimeMilliseconds());
    info.put("endTime", run.getEndTimeMilliseconds());
    info.put("periods", getPeriodNames(run));
    User owner = run.getOwner();
    info.put("teacherFirstName", owner.getUserDetails().getFirstname());
    info.put("teacherLastName", owner.getUserDetails().getLastname());
    info.put("wiseVersion", run.getProject().getWiseVersion());
    return info;
  }

  private List<String> getPeriodNames(Run run) {
    List<String> periods = new ArrayList<String>();
    for (Group period : run.getPeriods()) {
      periods.add(period.getName());
    }
    return periods;
  }

  private HashMap<String, Object> createRunNotFoundInfo() {
    HashMap<String, Object> info = new HashMap<String, Object>();
    info.put("error", "runNotFound");
    return info;
  }

  @GetMapping("/info")
  HashMap<String, Object> getRunInfoByRunCode(@RequestParam String runCode) {
    try {
      return getRunInfo(runService.retrieveRunByRuncode(runCode));
    } catch (ObjectNotFoundException e) {
      return createRunNotFoundInfo();
    }
  }

}
