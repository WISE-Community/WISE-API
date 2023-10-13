package org.wise.portal.dao.usertags;

import java.util.List;

import org.wise.portal.dao.SimpleDao;
import org.wise.portal.domain.user.User;
import org.wise.portal.domain.usertag.UserTag;

public interface UserTagsDao<T extends UserTag> extends SimpleDao<T> {

  UserTag get(Long tagId);

  List<UserTag> get(User user);

  UserTag get(User user, String text);
}
