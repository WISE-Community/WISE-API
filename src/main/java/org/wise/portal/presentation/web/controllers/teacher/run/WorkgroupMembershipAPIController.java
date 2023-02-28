package org.wise.portal.presentation.web.controllers.teacher.run;

import com.fasterxml.jackson.databind.JsonNode;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.wise.portal.domain.impl.ChangeWorkgroupParameters;
import org.wise.portal.domain.run.impl.RunImpl;
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
  protected void moveUserBetweenWorkgroups(@PathVariable("runId") RunImpl run,
      @PathVariable Long userId, @RequestBody JsonNode postedParams) throws Exception {
    ChangeWorkgroupParameters params = new ChangeWorkgroupParameters();
    params.setRun(run);
    params.setStudent(userService.retrieveById(userId));
    Long fromWorkgroupId = postedParams.get("workgroupIdFrom").asLong();
    if (fromWorkgroupId > 0) {
      params.setWorkgroupFrom(workgroupService.retrieveById(fromWorkgroupId));
    }
    Long toWorkgroupId = postedParams.get("workgroupIdTo").asLong();
    if (toWorkgroupId > 0) {
      params.setWorkgroupTo(workgroupService.retrieveById(toWorkgroupId));
    }
    params.setPeriodId(postedParams.get("periodId").asLong());
    workgroupService.updateWorkgroupMembership(params);
  }
}
