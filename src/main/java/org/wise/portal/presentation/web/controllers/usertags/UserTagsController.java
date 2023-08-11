package org.wise.portal.presentation.web.controllers.usertags;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.wise.portal.dao.ObjectNotFoundException;
import org.wise.portal.domain.project.Project;
import org.wise.portal.domain.project.impl.ProjectImpl;
import org.wise.portal.domain.user.User;
import org.wise.portal.domain.usertag.UserTag;
import org.wise.portal.presentation.web.controllers.ControllerUtil;
import org.wise.portal.presentation.web.response.ResponseEntityGenerator;
import org.wise.portal.service.project.ProjectService;
import org.wise.portal.service.user.UserService;
import org.wise.portal.service.usertags.UserTagsService;

@RestController
@Secured("ROLE_USER")
@RequestMapping("/api/user-tags")
public class UserTagsController {

  @Autowired
  ProjectService projectService;

  @Autowired
  protected UserService userService;

  @Autowired
  private UserTagsService userTagsService;

  @GetMapping("")
  ResponseEntity<Map<String, Object>> getTags() throws ObjectNotFoundException {
    HashMap<String, Object> response = new HashMap<>();
    User user = ControllerUtil.getSignedInUser();
    response.put("tags", userTagsService.getTags(user));
    return ResponseEntityGenerator.createSuccess(response);
  }

  @PostMapping("")
  ResponseEntity<Map<String, Object>> createTag(@RequestBody String tag) throws Exception {
    HashMap<String, Object> response = new HashMap<>();
    User user = ControllerUtil.getSignedInUser();
    UserTag existingTag = userTagsService.get(user, tag);
    if (existingTag == null) {
      UserTag newTag = userTagsService.createTag(user, tag);
      response.put("id", newTag.getId());
      response.put("text", newTag.getText());
    }
    return ResponseEntityGenerator.createSuccess(response);
  }

  @PutMapping("/{tagId}")
  ResponseEntity<Map<String, Object>> updateTag(@PathVariable("tagId") Long tagId,
      @RequestBody String tag) {
    HashMap<String, Object> response = new HashMap<>();
    User user = ControllerUtil.getSignedInUser();
    if (userTagsService.isOwner(user, tagId) && userTagsService.get(user, tag) == null) {
      UserTag userTag = userTagsService.editTag(tagId, tag);
      response.put("id", userTag.getId());
      response.put("text", userTag.getText());
    }
    return ResponseEntityGenerator.createSuccess(response);
  }

  @DeleteMapping("/{tagId}")
  ResponseEntity<Map<String, Object>> deleteTag(@PathVariable("tagId") Long tagId) {
    User user = ControllerUtil.getSignedInUser();
    if (userTagsService.isOwner(user, tagId)) {
      projectService.getProjectList(user).forEach(project -> {
        userTagsService.removeTag(project, tagId);
      });
      userTagsService.deleteTag(tagId);
    }
    return ResponseEntityGenerator.createSuccess("tagDeleted");
  }

  @PutMapping("/{tagId}/project")
  ResponseEntity<Map<String, Object>> applyTag(@PathVariable("tagId") Long tagId,
      @RequestBody String projectId) throws Exception {
    User user = ControllerUtil.getSignedInUser();
    if (userTagsService.isOwner(user, tagId)) {
      Project project = projectService.getById(projectId);
      userTagsService.applyTag(project, tagId);
    }
    return ResponseEntityGenerator.createSuccess("tagApplied");
  }

  @PutMapping("/{tagId}/projects")
  ResponseEntity<Map<String, Object>> applyTags(@PathVariable("tagId") Long tagId,
      @RequestBody JsonNode postedParams) throws Exception {
    User user = ControllerUtil.getSignedInUser();
    if (userTagsService.isOwner(user, tagId)) {
      postedParams.forEach(projectId -> {
        try {
          Project project = projectService.getById(projectId.asText());
          userTagsService.applyTag(project, tagId);
        } catch (ObjectNotFoundException e) {
        }
      });
    }
    return ResponseEntityGenerator.createSuccess("tagApplied");
  }

  @DeleteMapping("/{tagId}/project/{projectId}")
  ResponseEntity<Map<String, Object>> removeTag(@PathVariable("tagId") Long tagId,
      @PathVariable("projectId") ProjectImpl project) throws Exception {
    User user = ControllerUtil.getSignedInUser();
    if (userTagsService.isOwner(user, tagId)) {
      userTagsService.removeTag(project, tagId);
    }
    return ResponseEntityGenerator.createSuccess("tagRemoved");
  }

  @DeleteMapping("/{tagId}/projects")
  ResponseEntity<Map<String, Object>> removeTags(@PathVariable("tagId") Long tagId,
      @RequestBody JsonNode postedParams) throws Exception {
    User user = ControllerUtil.getSignedInUser();
    if (userTagsService.isOwner(user, tagId)) {
      postedParams.forEach(projectId -> {
        try {
          Project project = projectService.getById(projectId.asText());
          userTagsService.removeTag(project, tagId);
        } catch (ObjectNotFoundException e) {
        }
      });
    }
    return ResponseEntityGenerator.createSuccess("tagRemoved");
  }

}
