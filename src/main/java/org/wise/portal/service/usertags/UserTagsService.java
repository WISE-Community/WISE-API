package org.wise.portal.service.usertags;

import java.util.List;
import java.util.Set;

import org.wise.portal.domain.project.Project;
import org.wise.portal.domain.user.User;
import org.wise.portal.domain.usertag.UserTag;

public interface UserTagsService {

  UserTag get(User user, String text);

  UserTag createTag(User user, String tag);

  Boolean isOwner(User user, Long tagId);

  UserTag editTag(Long tagId, String tag);

  UserTag deleteTag(Long tagId);

  List<UserTag> getTags(User user);

  Set<UserTag> getTags(User user, Project project);

  Boolean hasTag(User user, Project project, String tag);

  UserTag applyTag(Project project, Long tagId);

  UserTag removeTag(Project project, Long tagId);
}
