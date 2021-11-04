package org.wise.vle.web.wise5;

import java.io.IOException;
import java.util.List;

import org.json.JSONException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.wise.portal.dao.ObjectNotFoundException;
import org.wise.portal.domain.group.Group;
import org.wise.portal.domain.run.Run;
import org.wise.vle.domain.annotation.wise5.Annotation;
import org.wise.vle.domain.work.StudentWork;

@RestController
@Secured("ROLE_STUDENT")
@RequestMapping("/api/classmate/discussion")
public class ClassmateDiscussionDataController extends ClassmateDataController {

  @GetMapping("/student-work/{runId}/{periodId}/{nodeId}/{componentId}")
  public List<StudentWork> getClassmateDiscussionWork(Authentication auth, @PathVariable Long runId,
      @PathVariable Long periodId, @PathVariable String nodeId, @PathVariable String componentId)
      throws IOException, JSONException, ObjectNotFoundException {
    Run run = runService.retrieveById(runId);
    Group period = groupService.retrieveById(periodId);
    if (isAllowedToGetData(auth, run, period, nodeId, componentId)) {
      return getStudentWork(run, period, nodeId, componentId);
    } else {
      throw new AccessDeniedException("Not permitted");
    }
  }

  @GetMapping("/annotations/{runId}/{periodId}/{nodeId}/{componentId}")
  public List<Annotation> getClassmateDiscussionAnnotations(Authentication auth,
      @PathVariable Long runId, @PathVariable Long periodId, @PathVariable String nodeId,
      @PathVariable String componentId) throws IOException, JSONException, ObjectNotFoundException {
    Run run = runService.retrieveById(runId);
    Group period = groupService.retrieveById(periodId);
    if (isAllowedToGetData(auth, run, period, nodeId, componentId)) {
      return getAnnotations(run, period, nodeId, componentId);
    } else {
      throw new AccessDeniedException("Not permitted");
    }
  }

  private boolean isAllowedToGetData(Authentication auth, Run run, Group period, String nodeId,
      String componentId) throws IOException, JSONException, ObjectNotFoundException {
    return isUserInRunAndPeriod(auth, run, period)
        && isDiscussionComponent(run, nodeId, componentId);
  }

  private boolean isDiscussionComponent(Run run, String nodeId, String componentId)
      throws IOException, JSONException, ObjectNotFoundException {
    return isComponentType(run, nodeId, componentId, "Discussion");
  }
}
