package org.wise.portal.presentation.web.controllers.archive;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.wise.portal.dao.ObjectNotFoundException;
import org.wise.portal.domain.project.Project;
import org.wise.portal.domain.project.impl.ProjectImpl;
import org.wise.portal.domain.user.User;
import org.wise.portal.domain.usertag.UserTag;
import org.wise.portal.presentation.web.controllers.ControllerUtil;
import org.wise.portal.presentation.web.response.ResponseEntityGenerator;
import org.wise.portal.service.project.ProjectService;
import org.wise.portal.service.usertags.UserTagsService;

@RestController
@Secured({ "ROLE_TEACHER" })
@RequestMapping("/api")
public class ArchiveProjectController {

  @Autowired
  private ProjectService projectService;

  @Autowired
  private UserTagsService userTagsService;

  private static final String ARCHIVED_TAG = "archived";

  @PutMapping("/project/{projectId}/archived")
  protected ResponseEntity<Map<String, Object>> archiveProject(
      @PathVariable("projectId") ProjectImpl project) throws JSONException {
    User user = ControllerUtil.getSignedInUser();
    UserTag userTag = userTagsService.get(user, ARCHIVED_TAG);
    if (userTag == null) {
      userTag = userTagsService.createTag(user, ARCHIVED_TAG);
    }
    userTagsService.applyTag(project, (Long) userTag.getId());
    return ResponseEntityGenerator.createSuccess(createProjectResponse(user, project));
  }

  @PutMapping("/projects/archived")
  protected ResponseEntity<List<Map<String, Object>>> archiveProjects(
      @RequestBody List<Long> projectIds) throws Exception {
    User user = ControllerUtil.getSignedInUser();
    UserTag userTag = userTagsService.get(user, ARCHIVED_TAG);
    if (userTag == null) {
      userTag = userTagsService.createTag(user, ARCHIVED_TAG);
    }
    List<Project> projects = getProjects(projectIds);
    for (Project project : projects) {
      userTagsService.applyTag(project, (Long) userTag.getId());
    }
    return ResponseEntityGenerator.createSuccess(createProjectsResponse(user, projects));
  }

  @DeleteMapping("/project/{projectId}/archived")
  protected ResponseEntity<Map<String, Object>> unarchiveProject(
      @PathVariable("projectId") ProjectImpl project) throws JSONException {
    User user = ControllerUtil.getSignedInUser();
    UserTag userTag = userTagsService.get(user, ARCHIVED_TAG);
    if (userTag != null) {
      userTagsService.removeTag(project, (Long) userTag.getId());
    }
    return ResponseEntityGenerator.createSuccess(createProjectResponse(user, project));
  }

  @DeleteMapping("/projects/archived")
  protected ResponseEntity<List<Map<String, Object>>> unarchiveProjects(
      @RequestParam List<Long> projectIds) throws Exception {
    User user = ControllerUtil.getSignedInUser();
    UserTag userTag = userTagsService.get(user, ARCHIVED_TAG);
    List<Project> projects = getProjects(projectIds);
    if (userTag != null) {
      for (Project project : projects) {
        userTagsService.removeTag(project, (Long) userTag.getId());
      }
    }
    return ResponseEntityGenerator.createSuccess(createProjectsResponse(user, projects));
  }

  private List<Project> getProjects(List<Long> projectIds) throws ObjectNotFoundException {
    List<Project> projects = new ArrayList<Project>();
    for (Long projectId : projectIds) {
      projects.add(projectService.getById(projectId));
    }
    return projects;
  }

  private Map<String, Object> createProjectResponse(User user, Project project)
      throws JSONException {
    Map<String, Object> response = new HashMap<String, Object>();
    response.put("id", project.getId());
    response.put("archived", userTagsService.hasTag(user, project, ARCHIVED_TAG));
    return response;
  }

  private List<Map<String, Object>> createProjectsResponse(User user, List<Project> projects)
      throws JSONException {
    List<Map<String, Object>> response = new ArrayList<Map<String, Object>>();
    for (Project project : projects) {
      response.add(createProjectResponse(user, project));
    }
    return response;
  }
}
