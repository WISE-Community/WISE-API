package org.wise.portal.presentation.web.controllers.tag;

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
import org.wise.portal.domain.user.User;
import org.wise.portal.domain.usertag.UserTag;
import org.wise.portal.presentation.web.response.ResponseEntityGenerator;
import org.wise.portal.service.project.ProjectService;
import org.wise.portal.service.user.UserService;
import org.wise.portal.service.usertags.UserTagsService;

@RestController
@Secured({ "ROLE_TEACHER" })
@RequestMapping("/api")
public class TagProjectController {

  @Autowired
  private ProjectService projectService;

  @Autowired
  private UserService userService;

  @Autowired
  private UserTagsService userTagsService;

  @PutMapping("/projects/tag/{tag}")
  protected ResponseEntity<List<Map<String, Object>>> addTagToProjects(Authentication auth,
      @RequestBody List<Long> projectIds, @PathVariable("tag") String tag) throws Exception {
    User user = userService.retrieveUserByUsername(auth.getName());
    UserTag usertag = getOrCreateTag(user, tag);
    List<Project> projects = getProjects(projectIds);
    for (Project project : projects) {
      userTagsService.applyTag(project, usertag);
    }
    return ResponseEntityGenerator.createSuccess(createProjectsResponse(user, projects));
  }

  @DeleteMapping("/projects/tag/{tag}")
  protected ResponseEntity<List<Map<String, Object>>> removeTagFromProjects(Authentication auth,
      @RequestParam List<Long> projectIds, @PathVariable("tag") String tag) throws Exception {
    User user = userService.retrieveUserByUsername(auth.getName());
    UserTag userTag = getOrCreateTag(user, tag);
    List<Project> projects = getProjects(projectIds);
    for (Project project : projects) {
      userTagsService.removeTag(project, userTag);
    }
    return ResponseEntityGenerator.createSuccess(createProjectsResponse(user, projects));
  }

  private UserTag getOrCreateTag(User user, String tag) {
    UserTag archivedTag = userTagsService.get(user, tag);
    if (archivedTag == null) {
      archivedTag = userTagsService.createTag(user, tag);
    }
    return archivedTag;
  }

  private List<Project> getProjects(List<Long> projectIds) throws ObjectNotFoundException {
    List<Project> projects = new ArrayList<Project>();
    for (Long projectId : projectIds) {
      projects.add(projectService.getById(projectId));
    }
    return projects;
  }

  private List<Map<String, Object>> createProjectsResponse(User user, List<Project> projects) {
    List<Map<String, Object>> response = new ArrayList<Map<String, Object>>();
    for (Project project : projects) {
      response.add(createProjectResponse(user, project));
    }
    return response;
  }

  private Map<String, Object> createProjectResponse(User user, Project project) {
    Map<String, Object> response = new HashMap<String, Object>();
    response.put("projectId", project.getId());
    response.put("tags", userTagsService.getTagsList(user, project));
    return response;
  }
}
