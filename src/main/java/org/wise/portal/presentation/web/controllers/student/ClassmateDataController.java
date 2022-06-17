package org.wise.portal.presentation.web.controllers.student;

import java.io.IOException;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.wise.portal.dao.ObjectNotFoundException;
import org.wise.portal.domain.authentication.impl.StudentUserDetails;
import org.wise.portal.domain.group.Group;
import org.wise.portal.domain.project.impl.ProjectComponent;
import org.wise.portal.domain.project.impl.ProjectContent;
import org.wise.portal.domain.run.Run;
import org.wise.portal.domain.user.User;
import org.wise.portal.service.group.GroupService;
import org.wise.portal.service.project.ProjectService;
import org.wise.portal.service.user.UserService;
import org.wise.portal.service.vle.wise5.VLEService;
import org.wise.vle.domain.annotation.wise5.Annotation;
import org.wise.vle.domain.work.StudentWork;

public abstract class ClassmateDataController {

  final String NOT_PERMITTED = "Not permitted";

  @Autowired
  protected GroupService groupService;

  @Autowired
  protected ProjectService projectService;

  @Autowired
  protected UserService userService;

  @Autowired
  protected VLEService vleService;

  protected boolean isUserInRun(Authentication auth, Run run) {
    User user = userService.retrieveUser((StudentUserDetails) auth.getPrincipal());
    return run.isStudentAssociatedToThisRun(user);
  }

  protected boolean isUserInRunAndPeriod(Authentication auth, Run run, Group period) {
    User user = userService.retrieveUser((StudentUserDetails) auth.getPrincipal());
    return run.isStudentAssociatedToThisRunAndPeriod(user, period);
  }

  protected boolean isComponentType(Run run, String nodeId, String componentId,
      String expectedComponentType) throws IOException, JSONException, ObjectNotFoundException {
    ProjectComponent projectComponent = getProjectComponent(run, nodeId, componentId);
    return projectComponent.getString("type").equals(expectedComponentType);
  }

  protected ProjectComponent getProjectComponent(Run run, String nodeId, String componentId)
      throws IOException, JSONException, ObjectNotFoundException {
    String projectString = projectService.getProjectContent(run.getProject());
    JSONObject projectJSON = new JSONObject(projectString);
    ProjectContent projectContent = new ProjectContent(projectJSON);
    return projectContent.getComponent(nodeId, componentId);
  }

  protected List<ProjectComponent> getProjectComponents(Run run)
      throws IOException, JSONException {
    String projectString = projectService.getProjectContent(run.getProject());
    JSONObject projectJSON = new JSONObject(projectString);
    ProjectContent projectContent = new ProjectContent(projectJSON);
    return projectContent.getComponents();
  }

  protected List<StudentWork> getStudentWork(Run run, String nodeId, String componentId) {
    return vleService.getStudentWork(run, nodeId, componentId);
  }

  protected List<StudentWork> getStudentWork(Run run, Group period, String nodeId,
      String componentId) {
    return vleService.getStudentWork(run, period, nodeId, componentId);
  }

  protected List<StudentWork> getLatestStudentWork(Run run, String nodeId,
      String componentId) {
    return vleService.getLatestStudentWork(run, nodeId, componentId);
  }

  protected List<StudentWork> getLatestStudentWork(Run run, Group period, String nodeId,
      String componentId) {
    return vleService.getLatestStudentWork(run, period, nodeId, componentId);
  }

  protected List<Annotation> getAnnotations(Run run, String nodeId, String componentId) {
    return vleService.getAnnotations(run, nodeId, componentId);
  }

  protected List<Annotation> getAnnotations(Run run, Group period, String nodeId,
      String componentId) {
    return vleService.getAnnotations(run, period, nodeId, componentId);
  }
}
