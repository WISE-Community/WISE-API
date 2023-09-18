package org.wise.portal.service.usertags;

import java.util.Set;

import org.wise.portal.domain.project.Project;
import org.wise.portal.domain.user.User;
import org.wise.portal.domain.usertag.UserTag;

public interface UserTagsService {

  UserTag get(User user, String text);

  UserTag createTag(User user, String tag);

  Set<UserTag> getTags(User user, Project project);

  Boolean hasTag(User user, Project project, String tag);

  void applyTag(Project project, UserTag tag);

  void removeTag(Project project, UserTag tag);
}
