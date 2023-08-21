package org.wise.portal.presentation.web.controllers.archive;

import java.util.List;
import java.util.Map;

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
      @PathVariable("projectId") ProjectImpl project) {
    User user = ControllerUtil.getSignedInUser();
    UserTag userTag = userTagsService.get(user, ARCHIVED_TAG);
    if (userTag == null) {
      userTag = userTagsService.createTag(user, ARCHIVED_TAG);
    }
    userTagsService.applyTag(project, (Long) userTag.getId());
    return ResponseEntityGenerator.createSuccess("projectArchived");
  }

  @PutMapping("/projects/archived")
  protected ResponseEntity<Map<String, Object>> archiveProjects(@RequestBody List<Long> projectIds)
      throws Exception {
    User user = ControllerUtil.getSignedInUser();
    UserTag userTag = userTagsService.get(user, ARCHIVED_TAG);
    if (userTag == null) {
      userTag = userTagsService.createTag(user, ARCHIVED_TAG);
    }
    for (Long projectId : projectIds) {
      userTagsService.applyTag(projectService.getById(projectId), (Long) userTag.getId());
    }
    return ResponseEntityGenerator.createSuccess("projectsArchived");
  }

  @DeleteMapping("/project/{projectId}/archived")
  protected ResponseEntity<Map<String, Object>> unarchiveProject(
      @PathVariable("projectId") ProjectImpl project) {
    User user = ControllerUtil.getSignedInUser();
    UserTag userTag = userTagsService.get(user, ARCHIVED_TAG);
    if (userTag != null) {
      userTagsService.removeTag(project, (Long) userTag.getId());
    }
    return ResponseEntityGenerator.createSuccess("projectUnarchived");
  }

  @DeleteMapping("/projects/archived")
  protected ResponseEntity<Map<String, Object>> unarchiveProjects(
      @RequestParam List<Long> projectIds) throws Exception {
    User user = ControllerUtil.getSignedInUser();
    UserTag userTag = userTagsService.get(user, ARCHIVED_TAG);
    if (userTag != null) {
      for (Long projectId : projectIds) {
        userTagsService.removeTag(projectService.getById(projectId), (Long) userTag.getId());
      }
    }
    return ResponseEntityGenerator.createSuccess("projectsUnarchived");
  }

}
