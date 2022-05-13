package org.wise.portal.service.peergroup.impl;

import java.util.HashSet;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.wise.portal.dao.peergroup.PeerGroupDao;
import org.wise.portal.domain.group.Group;
import org.wise.portal.domain.peergroup.PeerGroup;
import org.wise.portal.domain.peergroup.impl.PeerGroupImpl;
import org.wise.portal.domain.peergrouping.PeerGrouping;
import org.wise.portal.domain.workgroup.Workgroup;
import org.wise.portal.service.peergroup.PeerGroupCreateService;

/**
 * @author Hiroki Terashima
 */
@Service
public class PeerGroupCreateServiceImpl implements PeerGroupCreateService {

  @Autowired
  private PeerGroupDao<PeerGroup> peerGroupDao;

  @Override
  public PeerGroup create(PeerGrouping peerGrouping, Group period) {
    PeerGroup peerGroup = new PeerGroupImpl(peerGrouping, period, new HashSet<Workgroup>());
    this.peerGroupDao.save(peerGroup);
    return peerGroup;
  }
}
