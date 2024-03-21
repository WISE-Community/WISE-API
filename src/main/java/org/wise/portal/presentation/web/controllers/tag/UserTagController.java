package org.wise.portal.presentation.web.controllers.tag;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.wise.portal.domain.user.User;
import org.wise.portal.domain.usertag.UserTag;
import org.wise.portal.presentation.web.response.ResponseEntityGenerator;
import org.wise.portal.service.user.UserService;
import org.wise.portal.service.usertags.UserTagsService;

@RestController
@Secured({ "ROLE_TEACHER" })
@RequestMapping("/api")
public class UserTagController {

  @Autowired
  private UserService userService;

  @Autowired
  private UserTagsService userTagsService;

  @PostMapping("/user/tag")
  protected ResponseEntity<Map<String, Object>> createTag(Authentication auth,
      @RequestBody Map<String, Object> tag) {
    User user = userService.retrieveUserByUsername(auth.getName());
    UserTag userTag = userTagsService.createTag(user, (String) tag.get("text"));
    return ResponseEntityGenerator.createSuccess(userTag.toMap());
  }

  @GetMapping("/user/tags")
  protected ResponseEntity<List<Map<String, Object>>> getTags(Authentication auth) {
    User user = userService.retrieveUserByUsername(auth.getName());
    List<Map<String, Object>> tags = userTagsService.getTags(user).stream().map(tag -> {
      return tag.toMap();
    }).collect(Collectors.toList());
    return ResponseEntityGenerator.createSuccess(tags);
  }

  @PutMapping("/user/tag/{tagId}")
  protected ResponseEntity<Map<String, Object>> updateTag(Authentication auth,
      @PathVariable("tagId") Long tagId, @RequestBody Map<String, Object> tag) {
    User user = userService.retrieveUserByUsername(auth.getName());
    UserTag userTag = userTagsService.get(tagId);
    userTag.setText((String) tag.get("text"));
    userTagsService.updateTag(user, userTag);
    return ResponseEntityGenerator.createSuccess(userTag.toMap());
  }
}
