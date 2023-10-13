package org.wise.portal.presentation.web.controllers.archive;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.Authentication;
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
import org.wise.portal.presentation.web.response.ResponseEntityGenerator;
import org.wise.portal.service.project.ProjectService;
import org.wise.portal.service.user.UserService;
import org.wise.portal.service.usertags.UserTagsService;

@RestController
@Secured({ "ROLE_TEACHER" })
@RequestMapping("/api")
public class ArchiveProjectController {

  @Autowired
  private ProjectService projectService;

  @Autowired
  private UserService userService;

  @Autowired
  private UserTagsService userTagsService;

  private static final String ARCHIVED_TAG = "archived";

  @PutMapping("/project/{projectId}/archived")
  protected ResponseEntity<Map<String, Object>> archiveProject(Authentication auth,
      @PathVariable("projectId") ProjectImpl project) {
    User user = userService.retrieveUserByUsername(auth.getName());
    UserTag archivedTag = getOrCreateArchivedTag(user);
    userTagsService.applyTag(project, archivedTag);
    return ResponseEntityGenerator.createSuccess(createProjectResponse(user, project));
  }

  @PutMapping("/projects/archived")
  protected ResponseEntity<List<Map<String, Object>>> archiveProjects(Authentication auth,
      @RequestBody List<Long> projectIds) throws Exception {
    User user = userService.retrieveUserByUsername(auth.getName());
    UserTag archivedTag = getOrCreateArchivedTag(user);
    List<Project> projects = getProjects(projectIds);
    for (Project project : projects) {
      userTagsService.applyTag(project, archivedTag);
    }
    return ResponseEntityGenerator.createSuccess(createProjectsResponse(user, projects));
  }

  private UserTag getOrCreateArchivedTag(User user) {
    UserTag archivedTag = userTagsService.get(user, ARCHIVED_TAG);
    if (archivedTag == null) {
      archivedTag = userTagsService.createTag(user, ARCHIVED_TAG);
    }
    return archivedTag;
  }

  @DeleteMapping("/project/{projectId}/archived")
  protected ResponseEntity<Map<String, Object>> unarchiveProject(Authentication auth,
      @PathVariable("projectId") ProjectImpl project) {
    User user = userService.retrieveUserByUsername(auth.getName());
    UserTag archivedTag = userTagsService.get(user, ARCHIVED_TAG);
    if (archivedTag != null) {
      userTagsService.removeTag(project, archivedTag);
    }
    return ResponseEntityGenerator.createSuccess(createProjectResponse(user, project));
  }

  @DeleteMapping("/projects/archived")
  protected ResponseEntity<List<Map<String, Object>>> unarchiveProjects(Authentication auth,
      @RequestParam List<Long> projectIds) throws Exception {
    User user = userService.retrieveUserByUsername(auth.getName());
    UserTag archivedTag = userTagsService.get(user, ARCHIVED_TAG);
    List<Project> projects = getProjects(projectIds);
    if (archivedTag != null) {
      for (Project project : projects) {
        userTagsService.removeTag(project, archivedTag);
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

  private Map<String, Object> createProjectResponse(User user, Project project) {
    Map<String, Object> response = new HashMap<String, Object>();
    response.put("id", project.getId());
    response.put("archived", userTagsService.hasTag(user, project, ARCHIVED_TAG));
    return response;
  }

  private List<Map<String, Object>> createProjectsResponse(User user, List<Project> projects) {
    List<Map<String, Object>> response = new ArrayList<Map<String, Object>>();
    for (Project project : projects) {
      response.add(createProjectResponse(user, project));
    }
    return response;
  }
}
