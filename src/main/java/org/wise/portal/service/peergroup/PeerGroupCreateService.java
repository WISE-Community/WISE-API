package org.wise.portal.service.peergroup;

import org.springframework.security.access.annotation.Secured;
import org.wise.portal.domain.group.Group;
import org.wise.portal.domain.peergroup.PeerGroup;
import org.wise.portal.domain.peergrouping.PeerGrouping;

/**
 * @author Hiroki Terashima
 */
@Secured("ROLE_TEACHER")
public interface PeerGroupCreateService {

  /**
   * Creates a new Peer Group with no members for the given Peer Grouping and period
   * @param peerGrouping PeerGrouping
   * @param period Group containing students in the PeerGroup
   * @return newly created Peer Group
   */
  PeerGroup create(PeerGrouping peerGrouping, Group period);
}
