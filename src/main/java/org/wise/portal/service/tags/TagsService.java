package org.wise.portal.service.tags;

import java.util.List;

import org.wise.portal.domain.run.Run;
import org.wise.portal.domain.tags.Tags;
import org.wise.portal.domain.user.User;

public interface TagsService {

  List<String> getTags(User user, Run run);

  Tags addTag(User user, Run run, String tag);

  Tags removeTag(User user, Run run, String tag);
}
