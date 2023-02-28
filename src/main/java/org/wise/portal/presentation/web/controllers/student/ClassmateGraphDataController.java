package org.wise.portal.presentation.web.controllers.student;

import java.io.IOException;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
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
import org.wise.vle.domain.work.StudentWork;

@RestController
@Secured("ROLE_STUDENT")
@RequestMapping("/api/classmate/graph")
public class ClassmateGraphDataController extends ClassmateDataController {

  final String CLASS_SOURCE = "class";
  final String GRAPH_TYPE = "Graph";
  final String PERIOD_SOURCE = "period";
  final String SHOW_CLASSMATE_WORK_TYPE = "showClassmateWork";

  @GetMapping("/student-work/{runId}/{nodeId}/{componentId}/{showWorkNodeId}/{showWorkComponentId}/period/{periodId}")
  public List<StudentWork> getClassmateGraphWorkInPeriod(Authentication auth,
      @PathVariable("runId") RunImpl run, @PathVariable String nodeId,
      @PathVariable String componentId, @PathVariable String showWorkNodeId,
      @PathVariable String showWorkComponentId, @PathVariable Long periodId)
      throws IOException, JSONException, ObjectNotFoundException {
    Group period = groupService.retrieveById(periodId);
    if (isAllowedToGetData(auth, run, period, nodeId, componentId, showWorkNodeId,
        showWorkComponentId, PERIOD_SOURCE)) {
      return getLatestStudentWork(run, period, showWorkNodeId, showWorkComponentId);
    }
    throw new AccessDeniedException(NOT_PERMITTED);
  }

  @GetMapping("/student-work/{runId}/{nodeId}/{componentId}/{showWorkNodeId}/{showWorkComponentId}/class")
  public List<StudentWork> getClassmateGraphWorkInClass(Authentication auth,
      @PathVariable("runId") RunImpl run, @PathVariable String nodeId,
      @PathVariable String componentId, @PathVariable String showWorkNodeId,
      @PathVariable String showWorkComponentId)
      throws IOException, JSONException, ObjectNotFoundException {
    if (isAllowedToGetData(auth, run, nodeId, componentId, showWorkNodeId, showWorkComponentId,
        CLASS_SOURCE)) {
      return getLatestStudentWork(run, showWorkNodeId, showWorkComponentId);
    }
    throw new AccessDeniedException(NOT_PERMITTED);
  }

  private boolean isAllowedToGetData(Authentication auth, Run run, Group period, String nodeId,
      String componentId, String showWorkNodeId, String showWorkComponentId,
      String showClassmateWorkSource) throws IOException, JSONException, ObjectNotFoundException {
    return isStudentInRunAndPeriod(auth, run, period) && isValidGraphComponent(run, nodeId,
        componentId, showWorkNodeId, showWorkComponentId, showClassmateWorkSource);
  }

  private boolean isAllowedToGetData(Authentication auth, Run run, String nodeId,
      String componentId, String showWorkNodeId, String showWorkComponentId,
      String showClassmateWorkSource) throws IOException, JSONException, ObjectNotFoundException {
    return isStudentInRun(auth, run) && isValidGraphComponent(run, nodeId, componentId,
        showWorkNodeId, showWorkComponentId, showClassmateWorkSource);
  }

  private boolean isValidGraphComponent(Run run, String nodeId, String componentId,
      String showWorkNodeId, String showWorkComponentId, String showClassmateWorkSource)
      throws IOException, JSONException, ObjectNotFoundException {
    ProjectComponent projectComponent = getProjectComponent(run, nodeId, componentId);
    return isGraphComponent(projectComponent) && hasMatchingConnectedComponent(projectComponent,
        showWorkNodeId, showWorkComponentId, showClassmateWorkSource);
  }

  private boolean isGraphComponent(ProjectComponent projectComponent) throws JSONException {
    return GRAPH_TYPE.equals(projectComponent.getString("type"));
  }

  private boolean hasMatchingConnectedComponent(ProjectComponent projectComponent,
      String showWorkNodeId, String showWorkComponentId, String showClassmateWorkSource)
      throws JSONException {
    JSONArray connectedComponents = projectComponent.getJSONArray("connectedComponents");
    for (int c = 0; c < connectedComponents.length(); c++) {
      JSONObject connectedComponent = connectedComponents.getJSONObject(c);
      if (isMatchingConnectedComponent(connectedComponent, showWorkNodeId, showWorkComponentId,
          showClassmateWorkSource)) {
        return true;
      }
    }
    return false;
  }

  private boolean isMatchingConnectedComponent(JSONObject connectedComponent, String showWorkNodeId,
      String showWorkComponentId, String showClassmateWorkSource) throws JSONException {
    return connectedComponent.getString("nodeId").equals(showWorkNodeId)
        && connectedComponent.getString("componentId").equals(showWorkComponentId)
        && connectedComponent.getString("type").equals(SHOW_CLASSMATE_WORK_TYPE)
        && connectedComponent.getString("showClassmateWorkSource").equals(showClassmateWorkSource);
  }
}
