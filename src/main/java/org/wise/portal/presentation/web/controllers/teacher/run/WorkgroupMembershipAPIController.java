package org.wise.portal.presentation.web.controllers.teacher.run;

import com.fasterxml.jackson.databind.node.ObjectNode;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.wise.portal.domain.impl.ChangeWorkgroupParameters;
import org.wise.portal.domain.workgroup.Workgroup;
import org.wise.portal.service.user.UserService;
import org.wise.portal.service.workgroup.WorkgroupService;

@Secured({ "ROLE_TEACHER" })
@RestController
public class WorkgroupMembershipAPIController {

  @Autowired
  protected UserService userService;

  @Autowired
  protected WorkgroupService workgroupService;

  @PostMapping("/api/teacher/run/{runId}/workgroup/move-user/{userId}")
  protected void moveUserBetweenWorkgroups(@PathVariable Long runId, @PathVariable Long userId,
      @RequestBody ObjectNode postedParams) throws Exception {
    ChangeWorkgroupParameters params = new ChangeWorkgroupParameters();
    params.setRunId(runId);
    params.setStudent(userService.retrieveById(userId));
    Workgroup fromWorkgroup = workgroupService.retrieveById(postedParams.get("workgroupIdFrom").asLong());
    params.setWorkgroupFrom(fromWorkgroup);
    params.setWorkgroupTo(workgroupService.retrieveById(postedParams.get("workgroupIdTo").asLong()));
    params.setPeriodId(fromWorkgroup.getPeriod().getId());
    workgroupService.updateWorkgroupMembership(params);
  }
}
