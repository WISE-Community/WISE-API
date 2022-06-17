package org.wise.portal.presentation.web.controllers.student;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
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

  @GetMapping("/student-work/{runId}/{periodId}/{nodeId}/{componentId}/{source}")
  public List<StudentWork> getClassmateSummaryWork(Authentication auth,
      @PathVariable("runId") RunImpl run, @PathVariable Long periodId, @PathVariable String nodeId,
      @PathVariable String componentId, @PathVariable String source)
      throws IOException, JSONException, ObjectNotFoundException {
    Group period = groupService.retrieveById(periodId);
    if (isAllowedToGetData(auth, run, period, nodeId, componentId)) {
      if (isPeriodSource(source)) {
        return getLatestStudentWork(run, period, nodeId, componentId);
      } else if (isAllPeriodsSource(source)) {
        return getLatestStudentWork(run, nodeId, componentId);
      }
    }
    throw new AccessDeniedException(NOT_PERMITTED);
  }

  @GetMapping("/scores/{runId}/{periodId}/{nodeId}/{componentId}/{source}")
  public List<Annotation> getClassmateSummaryScores(Authentication auth,
      @PathVariable("runId") RunImpl run, @PathVariable Long periodId, @PathVariable String nodeId,
      @PathVariable String componentId, @PathVariable String source)
      throws IOException, JSONException, ObjectNotFoundException {
    Group period = groupService.retrieveById(periodId);
    if (isAllowedToGetData(auth, run, period, nodeId, componentId)) {
      if (isPeriodSource(source)) {
        return getLatestScoreAnnotations(getAnnotations(run, period, nodeId, componentId));
      } else if (isAllPeriodsSource(source)) {
        return getLatestScoreAnnotations(getAnnotations(run, nodeId, componentId));
      }
    }
    throw new AccessDeniedException(NOT_PERMITTED);
  }

  private boolean isAllowedToGetData(Authentication auth, Run run, Group period, String nodeId,
      String componentId)
      throws IOException, JSONException, ObjectNotFoundException {
    return isUserInRunAndPeriod(auth, run, period)
        && isValidSummaryComponent(run, nodeId, componentId);
  }

  private boolean isValidSummaryComponent(Run run, String nodeId, String componentId)
      throws IOException, JSONException, ObjectNotFoundException {
    List<ProjectComponent> projectComponents = getProjectComponents(run);
    for (ProjectComponent projectComponent : projectComponents) {
      if (projectComponent.getString("type").equals(SUMMARY_TYPE) &&
          projectComponent.getString("summaryNodeId").equals(nodeId) &&
          projectComponent.getString("summaryComponentId").equals(componentId)) {
        return true;
      }
    }
    return false;
  }

  private boolean isPeriodSource(String source) {
    return source.equals(PERIOD_SOURCE);
  }

  private boolean isAllPeriodsSource(String source) {
    return source.equals(ALL_PERIODS_SOURCE);
  }

  private List<Annotation> getLatestScoreAnnotations(List<Annotation> annotations) {
    HashMap<Long, Annotation> latestScoreAnnotationPerWorkgroup = new HashMap<Long, Annotation>();
    for (Annotation annotation : annotations) {
      if (annotation.isScoreType()) {
        Long key = annotation.getToWorkgroup().getId();
        if (latestScoreAnnotationPerWorkgroup.containsKey(key)) {
          if (isAfter(annotation, latestScoreAnnotationPerWorkgroup.get(key))) {
            latestScoreAnnotationPerWorkgroup.put(key, annotation);
          }
        } else {
          latestScoreAnnotationPerWorkgroup.put(key, annotation);
        }
      }
    }
    return new ArrayList<Annotation>(latestScoreAnnotationPerWorkgroup.values());
  }

  private boolean isAfter(Annotation annotation1, Annotation annotation2) {
    return annotation1.getServerSaveTime().after(annotation2.getServerSaveTime());
  }
}
