package org.wise.portal.presentation.web.controllers.teacher.run;

import com.fasterxml.jackson.databind.JsonNode;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.wise.portal.dao.ObjectNotFoundException;
import org.wise.portal.domain.impl.ChangeWorkgroupParameters;
import org.wise.portal.domain.run.Run;
import org.wise.portal.domain.run.impl.RunImpl;
import org.wise.portal.domain.workgroup.Workgroup;
import org.wise.portal.service.run.RunService;
import org.wise.portal.service.user.UserService;
import org.wise.portal.service.workgroup.WorkgroupService;

@Secured({ "ROLE_TEACHER" })
@RestController
public class WorkgroupMembershipAPIController {

  @Autowired
  private RunService runService;

  @Autowired
  private UserService userService;

  @Autowired
  private WorkgroupService workgroupService;

  @PostMapping("/api/teacher/run/{runId}/workgroup/move-user/{userId}")
  protected Long moveUserBetweenWorkgroups(Authentication auth, @PathVariable("runId") RunImpl run,
      @PathVariable Long userId, @RequestBody JsonNode postedParams) throws Exception {
    Workgroup workgroup = null;
    if (runService.hasWritePermission(auth, run)) {
      ChangeWorkgroupParameters params = createChangeWorkgroupParameters(run, userId, postedParams);
      workgroup = workgroupService.updateWorkgroupMembership(params);
    }
    if (workgroup == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Error: Could not move student");
    }
    return workgroup.getId();
  }

  private ChangeWorkgroupParameters createChangeWorkgroupParameters(Run run, Long userId,
      JsonNode postedParams) throws ObjectNotFoundException {
    ChangeWorkgroupParameters params = new ChangeWorkgroupParameters();
    params.setRun(run);
    params.setStudent(userService.retrieveById(userId));
    Long toWorkgroupId = postedParams.get("workgroupIdTo").asLong();
    params.setWorkgroupTo(workgroupService.retrieveById(toWorkgroupId));
    return params;
  }
}
