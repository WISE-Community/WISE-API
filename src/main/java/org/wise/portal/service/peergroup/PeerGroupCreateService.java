package org.wise.portal.service.peergroup;

import org.springframework.security.access.annotation.Secured;
import org.wise.portal.domain.group.Group;
import org.wise.portal.domain.peergroup.PeerGroup;
import org.wise.portal.domain.peergroupactivity.PeerGroupActivity;

/**
 * @author Hiroki Terashima
 */
@Secured("ROLE_TEACHER")
public interface PeerGroupCreateService {

  /**
   * Creates a new Peer Group with no members for the given activity and period
   * @param activity PeerGroupActivity
   * @param period Group containing students in the PeerGroup
   * @return newly created Peer Group
   */
  PeerGroup create(PeerGroupActivity activity, Group period);
}
