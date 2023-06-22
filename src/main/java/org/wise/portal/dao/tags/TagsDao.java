package org.wise.portal.dao.tags;

import org.wise.portal.dao.SimpleDao;
import org.wise.portal.domain.run.Run;
import org.wise.portal.domain.tags.Tags;
import org.wise.portal.domain.user.User;

public interface TagsDao<T extends Tags> extends SimpleDao<T> {
  Tags get(User user, Run run);
}
