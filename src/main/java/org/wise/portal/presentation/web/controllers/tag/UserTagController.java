package org.wise.portal.presentation.web.controllers.tag;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.wise.portal.domain.user.User;
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

  @GetMapping("/user/tags")
  protected ResponseEntity<List<Map<String, Object>>> getTags(Authentication auth) {
    User user = userService.retrieveUserByUsername(auth.getName());
    List<Map<String, Object>> tags = userTagsService.getTags(user).stream().map(t -> {
      Map<String, Object> tag = new HashMap<>();
      tag.put("id", t.getId());
      tag.put("text", t.getText());
      return tag;
    }).collect(Collectors.toList());
    return ResponseEntityGenerator.createSuccess(tags);
  }
}
