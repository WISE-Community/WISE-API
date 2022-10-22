package org.wise.portal.service.peergroup.impl;

import org.wise.portal.domain.peergroup.PeerGroup;
import org.wise.portal.domain.peergrouping.PeerGrouping;
import org.wise.portal.domain.workgroup.Workgroup;
import org.wise.portal.service.peergroup.PeerGroupCreationException;
import org.wise.portal.service.peergrouping.logic.PeerGroupLogicService;

public class CreatePeerGroupContext {

  PeerGroupLogicService strategy;

  protected void setStrategy(PeerGroupLogicService strategy) {
    this.strategy = strategy;
  }

  public PeerGroup execute(Workgroup workgroup, PeerGrouping peerGrouping)
      throws PeerGroupCreationException {
    return this.strategy.createPeerGroup(workgroup, peerGrouping);
  }
}
