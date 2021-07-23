package org.wise.portal.presentation.web.controllers.teacher.run;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.wise.portal.dao.ObjectNotFoundException;
import org.wise.portal.domain.workgroup.Workgroup;
import org.wise.portal.service.run.RunService;

@Secured({ "ROLE_TEACHER" })
@RestController
public class WorkgroupGetAPIController {

  @Autowired
  protected RunService runService;

  @GetMapping("/api/teacher/run/{runId}/period/{periodId}/workgroups")
  protected List<Workgroup> getWorkgroups(@PathVariable Long runId, @PathVariable Long periodId)
      throws ObjectNotFoundException {
    return runService.getWorkgroups(runId, periodId);
  }

}
