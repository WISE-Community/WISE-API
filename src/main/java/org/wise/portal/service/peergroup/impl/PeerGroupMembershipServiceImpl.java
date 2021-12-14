package org.wise.portal.service.peergroup.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.wise.portal.dao.peergroup.PeerGroupDao;
import org.wise.portal.domain.peergroup.PeerGroup;
import org.wise.portal.domain.workgroup.Workgroup;
import org.wise.portal.service.peergroup.PeerGroupMembershipService;

/**
 * @author Hiroki Terashima
 */
@Service
public class PeerGroupMembershipServiceImpl implements PeerGroupMembershipService {

  @Autowired
  private PeerGroupDao<PeerGroup> peerGroupDao;

  public PeerGroup addMember(PeerGroup peerGroup, Workgroup workgroup) {
    PeerGroup oldPeerGroup = peerGroupDao.getByWorkgroupAndActivity(workgroup,
        peerGroup.getPeerGroupActivity());
    if (oldPeerGroup != null) {
      removeMember(oldPeerGroup, workgroup);
    }
    peerGroup.addMember(workgroup);
    peerGroupDao.save(peerGroup);
    return peerGroup;
  }

  public PeerGroup removeMember(PeerGroup peerGroup, Workgroup workgroup) {
    peerGroup.removeMember(workgroup);
    peerGroupDao.save(peerGroup);
    return peerGroup;
  }
}
