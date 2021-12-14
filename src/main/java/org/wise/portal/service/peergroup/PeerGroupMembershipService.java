package org.wise.portal.service.peergroup;

import org.wise.portal.domain.peergroup.PeerGroup;
import org.wise.portal.domain.workgroup.Workgroup;

/**
 * @author Hiroki Terashima
 */
public interface PeerGroupMembershipService {

  /**
   * Add a workgroup to the PeerGroup. If the workgroup is a member of another PeerGroup for the
   * same PeerGroupActivity, remove it from the PeerGroup first.
   * @param peerGroup existing PeerGroup to add workgroup as a member
   * @param workgroup Workgroup to move into PeerGroup
   * @return PeerGroup updated PeerGroup
   */
  PeerGroup addMember(PeerGroup peerGroup, Workgroup workgroup);

  /**
   * Remove a workgroup from the PeerGroup. If the workgroup is not a member of the PeerGroup,
   * do nothing.
   * @param peerGroup PeerGroup to remove workgroup from membership
   * @param workgroup Workgroup to remove from PeerGroup
   * @return PeerGroup updated PeerGroup
   */
  PeerGroup removeMember(PeerGroup peerGroup, Workgroup workgroup);
}
