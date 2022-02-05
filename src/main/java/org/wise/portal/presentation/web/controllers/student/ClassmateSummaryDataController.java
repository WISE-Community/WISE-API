package org.wise.portal.presentation.web.controllers.student;

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
import org.wise.portal.domain.project.impl.ProjectComponent;
import org.wise.portal.domain.run.Run;
import org.wise.portal.domain.run.impl.RunImpl;
import org.wise.vle.domain.annotation.wise5.Annotation;
import org.wise.vle.domain.work.StudentWork;

@RestController
@Secured("ROLE_STUDENT")
@RequestMapping("/api/classmate/summary")
public class ClassmateSummaryDataController extends ClassmateDataController {

  final String ALL_PERIODS_SOURCE = "allPeriods";
  final String PERIOD_SOURCE = "period";
  final String SUMMARY_TYPE = "Summary";

  @GetMapping("/student-work/{runId}/{periodId}/{nodeId}/{componentId}/{otherNodeId}/{otherComponentId}/{source}")
  public List<StudentWork> getClassmateSummaryWork(Authentication auth,
      @PathVariable("runId") RunImpl run, @PathVariable Long periodId, @PathVariable String nodeId,
      @PathVariable String componentId, @PathVariable String otherNodeId,
      @PathVariable String otherComponentId, @PathVariable String source)
      throws IOException, JSONException, ObjectNotFoundException {
    Group period = groupService.retrieveById(periodId);
    if (isAllowedToGetData(auth, run, period, nodeId, componentId, otherNodeId, otherComponentId)) {
      if (source.equals(PERIOD_SOURCE)) {
        return getStudentWork(run, period, otherNodeId, otherComponentId);
      } else if (source.equals(ALL_PERIODS_SOURCE)) {
        return getStudentWork(run, otherNodeId, otherComponentId);
      }
    }
    throw new AccessDeniedException(NOT_PERMITTED);
  }

  @GetMapping("/annotations/{runId}/{periodId}/{nodeId}/{componentId}/{otherNodeId}/{otherComponentId}/{source}")
  public List<Annotation> getClassmateSummaryAnnotations(Authentication auth,
      @PathVariable("runId") RunImpl run, @PathVariable Long periodId, @PathVariable String nodeId,
      @PathVariable String componentId, @PathVariable String otherNodeId,
      @PathVariable String otherComponentId, @PathVariable String source)
      throws IOException, JSONException, ObjectNotFoundException {
    Group period = groupService.retrieveById(periodId);
    if (isAllowedToGetData(auth, run, period, nodeId, componentId, otherNodeId, otherComponentId)) {
      if (source.equals(PERIOD_SOURCE)) {
        return getAnnotations(run, period, otherNodeId, otherComponentId);
      } else if (source.equals(ALL_PERIODS_SOURCE)) {
        return getAnnotations(run, otherNodeId, otherComponentId);
      }
    }
    throw new AccessDeniedException(NOT_PERMITTED);
  }

  private boolean isAllowedToGetData(Authentication auth, Run run, Group period, String nodeId,
      String componentId, String otherNodeId, String otherComponentId)
      throws IOException, JSONException, ObjectNotFoundException {
    return isUserInRunAndPeriod(auth, run, period)
        && isValidSummaryComponent(run, nodeId, componentId, otherNodeId, otherComponentId);
  }

  private boolean isValidSummaryComponent(Run run, String nodeId, String componentId,
      String otherNodeId, String otherComponentId)
      throws IOException, JSONException, ObjectNotFoundException {
    ProjectComponent projectComponent = getProjectComponent(run, nodeId, componentId);
    return projectComponent.getString("type").equals(SUMMARY_TYPE)
        && projectComponent.getString("summaryNodeId").equals(otherNodeId)
        && projectComponent.getString("summaryComponentId").equals(otherComponentId);
  }
}