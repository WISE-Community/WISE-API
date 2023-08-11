package org.wise.portal.presentation.web.controllers.archive;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.wise.portal.domain.project.Project;
import org.wise.portal.domain.project.impl.ProjectImpl;
import org.wise.portal.domain.user.User;
import org.wise.portal.domain.usertag.UserTag;
import org.wise.portal.presentation.web.controllers.ControllerUtil;
import org.wise.portal.presentation.web.response.ResponseEntityGenerator;
import org.wise.portal.service.usertags.UserTagsService;

@RestController
@RequestMapping(value = "/api")
public class ArchiveController {

  @Autowired
  private UserTagsService userTagsService;

  private static final String archivedTag = "archived";

  @Secured({ "ROLE_TEACHER" })
  @PostMapping("/archive/project")
  protected ResponseEntity<Map<String, Object>> archiveProject(
      @RequestParam("projectId") ProjectImpl project) {
    User user = ControllerUtil.getSignedInUser();
    UserTag userTag = userTagsService.get(user, archivedTag);
    if (userTag == null) {
      userTag = userTagsService.createTag(user, archivedTag);
    }
    userTagsService.applyTag(project, (Long) userTag.getId());
    return ResponseEntityGenerator.createSuccess("projectArchived");
  }

  @Secured({ "ROLE_TEACHER" })
  @PostMapping("/archive/projects")
  protected ResponseEntity<Map<String, Object>> archiveProjects(
      @RequestParam("projectIds") List<ProjectImpl> projects) {
    User user = ControllerUtil.getSignedInUser();
    UserTag userTag = userTagsService.get(user, archivedTag);
    if (userTag == null) {
      userTag = userTagsService.createTag(user, archivedTag);
    }
    for (Project project : projects) {
      userTagsService.applyTag(project, (Long) userTag.getId());
    }
    return ResponseEntityGenerator.createSuccess("projectsArchived");
  }

  @Secured({ "ROLE_TEACHER" })
  @PostMapping("/unarchive/project")
  protected ResponseEntity<Map<String, Object>> unarchiveProject(
      @RequestParam("projectId") ProjectImpl project) {
    User user = ControllerUtil.getSignedInUser();
    UserTag userTag = userTagsService.get(user, archivedTag);
    if (userTag != null) {
      userTagsService.removeTag(project, (Long) userTag.getId());
    }
    return ResponseEntityGenerator.createSuccess("projectUnarchived");
  }

  @Secured({ "ROLE_TEACHER" })
  @PostMapping("/unarchive/projects")
  protected ResponseEntity<Map<String, Object>> unarchiveProjects(
      @RequestParam("projectIds") List<ProjectImpl> projects) {
    User user = ControllerUtil.getSignedInUser();
    UserTag userTag = userTagsService.get(user, archivedTag);
    if (userTag != null) {
      for (Project project : projects) {
        userTagsService.removeTag(project, (Long) userTag.getId());
      }
    }
    return ResponseEntityGenerator.createSuccess("projectsUnarchived");
  }

}
