package org.wise.portal.dao.teacherpresentation;

import org.wise.portal.dao.SimpleDao;
import org.wise.portal.domain.group.Group;
import org.wise.portal.domain.run.Run;
import org.wise.vle.domain.teacherpresentation.TeacherPresentationConfig;

public interface TeacherPresentationConfigDao<T extends TeacherPresentationConfig>
    extends SimpleDao<T> {

  /**
   * @return the config for the given run, period, node and component, or null if none exists
   */
  TeacherPresentationConfig getConfig(Run run, Group period, String nodeId, String componentId);
}
