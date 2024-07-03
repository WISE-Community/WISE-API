package org.wise.portal.service.usertags;

import java.util.List;
import java.util.Set;

import org.wise.portal.domain.project.Project;
import org.wise.portal.domain.user.User;
import org.wise.portal.domain.usertag.UserTag;

public interface UserTagsService {

  UserTag get(User user, String text);

  UserTag get(Long id);

  UserTag createTag(User user, String text, String color);

  Set<UserTag> getTags(User user, Project project);

  boolean hasTag(User user, String text);

  boolean hasTag(User user, String text, Long idToIgnore);

  boolean hasTag(User user, Project project, String text);

  void applyTag(Project project, UserTag tag);

  void removeTag(Project project, UserTag tag);

  List<UserTag> getTagsList(User user, Project project);

  List<UserTag> getTags(User user);

  void updateTag(User user, UserTag tag);

  void deleteTag(User user, UserTag tag);
}
