package org.wise.portal.service.peergrouping.logic;

import org.wise.portal.domain.peergroup.PeerGroup;
import org.wise.portal.domain.peergrouping.PeerGrouping;
import org.wise.portal.domain.workgroup.Workgroup;
import org.wise.portal.service.peergroup.PeerGroupCreationException;

public interface PeerGroupLogicService {

  /**
   * Creates a new PeerGroup with the Workgroup as a member for the PeerGrouping activity
   * @param workgroup Workgroup requesting to be put into a new PeerGroup
   * @param peerGrouping PeerGroup activity to create the new PeerGroup for
   * @return newly created PeerGroup, or null if the PeerGroup could not be created, for example
   * if there were not enough pair-able members
   * @throws PeerGroupCreationException when there was an error during the creation
   */
  PeerGroup createPeerGroup(Workgroup workgroup, PeerGrouping peerGrouping)
      throws PeerGroupCreationException;
}
