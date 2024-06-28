package org.wise.portal.presentation.web.controllers.tag;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.wise.portal.domain.user.User;
import org.wise.portal.domain.usertag.UserTag;
import org.wise.portal.presentation.web.exception.TagAlreadyExistsException;
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
  protected Map<String, Object> createTag(Authentication auth, @RequestBody Map<String, Object> tag)
      throws TagAlreadyExistsException {
    User user = userService.retrieveUserByUsername(auth.getName());
    String text = ((String) tag.get("text")).trim();
    if (userTagsService.hasTag(user, text)) {
      throw new TagAlreadyExistsException();
    }
    String color = ((String) tag.get("color")).trim();
    UserTag userTag = userTagsService.createTag(user, text, color);
    return userTag.toMap();
  }

  @GetMapping("/user/tags")
  protected List<Map<String, Object>> getTags(Authentication auth) {
    User user = userService.retrieveUserByUsername(auth.getName());
    List<UserTag> userTags = userTagsService.getTags(user);
    Collections.sort(userTags,
        (tag1, tag2) -> tag1.getText().toLowerCase().compareTo(tag2.getText().toLowerCase()));
    List<Map<String, Object>> tags = userTags.stream().map(tag -> {
      return tag.toMap();
    }).collect(Collectors.toList());
    return tags;
  }

  @PutMapping("/user/tag/{tagId}")
  protected Map<String, Object> updateTag(Authentication auth, @PathVariable("tagId") Long tagId,
      @RequestBody Map<String, Object> tag) throws TagAlreadyExistsException {
    User user = userService.retrieveUserByUsername(auth.getName());
    String tagText = ((String) tag.get("text")).trim();
    if (userTagsService.hasTag(user, tagText, tagId)) {
      throw new TagAlreadyExistsException();
    }
    UserTag userTag = userTagsService.get(tagId);
    userTag.setText(tagText);
    userTag.setColor((String) tag.get("color"));
    userTagsService.updateTag(user, userTag);
    return userTag.toMap();
  }

  @DeleteMapping("/user/tag/{tagId}")
  protected Map<String, Object> deleteTag(Authentication auth, @PathVariable("tagId") Long tagId) {
    User user = userService.retrieveUserByUsername(auth.getName());
    UserTag tag = userTagsService.get(tagId);
    userTagsService.deleteTag(user, tag);
    return tag.toMap();
  }
}
