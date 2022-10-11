package org.wise.portal.service.peergrouping.logic.impl;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.wise.portal.dao.peergroup.PeerGroupDao;
import org.wise.portal.dao.run.RunDao;
import org.wise.portal.domain.peergroup.PeerGroup;
import org.wise.portal.domain.peergroup.impl.PeerGroupImpl;
import org.wise.portal.domain.peergrouping.PeerGrouping;
import org.wise.portal.domain.run.Run;
import org.wise.portal.domain.workgroup.Workgroup;
import org.wise.portal.service.peergroup.PeerGroupCreationException;
import org.wise.portal.service.peergrouping.logic.PeerGroupLogicService;

public abstract class PeerGroupLogicServiceImpl implements PeerGroupLogicService {

  @Autowired
  PeerGroupDao<PeerGroup> peerGroupDao;

  @Autowired
  private RunDao<Run> runDao;

  public PeerGroup createPeerGroup(Workgroup workgroup, PeerGrouping peerGrouping)
      throws PeerGroupCreationException {
    List<Workgroup> workgroupsInPeriod = getWorkgroupsInSamePeriod(workgroup);
    List<Workgroup> workgroupsInAPeerGroup = peerGroupDao.getWorkgroupsInPeerGroup(peerGrouping,
        workgroup.getPeriod());
    Set<Workgroup> workgroupsNotInAPeerGroup = getWorkgroupsNotInPeerGroup(workgroupsInPeriod,
        workgroupsInAPeerGroup);
    if (canCreatePeerGroup(workgroup, workgroupsNotInAPeerGroup, peerGrouping)) {
      Set<Workgroup> members = getPeerGroupMembers(workgroup, peerGrouping, workgroupsInPeriod,
          workgroupsInAPeerGroup, workgroupsNotInAPeerGroup);
      PeerGroup peerGroup = new PeerGroupImpl(peerGrouping, workgroup.getPeriod(), members);
      peerGroupDao.save(peerGroup);
      return peerGroup;
    }
    return null;
  }

  Set<Workgroup> getPeerGroupMembers(Workgroup workgroup, PeerGrouping peerGrouping,
      List<Workgroup> workgroupsInPeriod, List<Workgroup> workgroupsInAPeerGroup,
      Set<Workgroup> workgroupsNotInAPeerGroup) throws PeerGroupCreationException {
    if (isLastOnesLeftToPair(workgroupsInAPeerGroup, workgroupsInPeriod,
        peerGrouping.getMaxMembershipCount())) {
      return workgroupsNotInAPeerGroup;
    } else {
      return groupMembersUpToMaxMembership(workgroup, peerGrouping, workgroupsNotInAPeerGroup);
    }
  }

  Set<Workgroup> getWorkgroupsNotInPeerGroup(List<Workgroup> workgroupsInPeriod,
      List<Workgroup> workgroupsInAPeerGroup) {
    Set<Workgroup> workgroupsNotInAPeerGroup = new HashSet<Workgroup>();
    for (Workgroup workgroup : workgroupsInPeriod) {
      if (!workgroupsInAPeerGroup.contains(workgroup)) {
        workgroupsNotInAPeerGroup.add(workgroup);
      }
    }
    return workgroupsNotInAPeerGroup;
  }

  List<Workgroup> getWorkgroupsInSamePeriod(Workgroup workgroup) {
    List<Workgroup> workgroups = runDao.getWorkgroupsForRunAndPeriod(workgroup.getRun().getId(),
        workgroup.getPeriod().getId());
    workgroups.removeIf(workgroupInPeriod -> workgroupInPeriod.getMembers().size() == 0);
    return workgroups;
  }

  private boolean isLastOnesLeftToPair(List<Workgroup> workgroupsInAPeerGroup,
      List<Workgroup> workgroupsInPeriod, int maxMembers) {
    int numWorkgroupsNotInAPeerGroup = workgroupsInPeriod.size() - workgroupsInAPeerGroup.size();
    return (numWorkgroupsNotInAPeerGroup - maxMembers) <= 1;
  }

  /**
   * Return true iff a new PeerGroup can be created
   * @param workgroup Workgroup requesting to create PeerGroup
   * @param workgroupsNotInAPeerGroup Set of workgroups that can be paired
   * @param peerGrouping PeerGrouping activity to create PeerGroup for
   * @return true if there are enough workgroups that can be paired
   */
  abstract boolean canCreatePeerGroup(Workgroup workgroup, Set<Workgroup> workgroupsNotInAPeerGroup,
      PeerGrouping peerGrouping);

  /**
   * Put together members that have different ideas into a PeerGroup
   * When this method is called, there is enough possibleMembers with detected ideas to pair
   * @param workgroup Workgroup making request to pair
   * @param peerGrouping PeerGrouping activity to create PeerGroup for
   * @param possibleMembers unpaired members
   * @return Set<Workgroup> members that will be in the PeerGroup
   */
  abstract Set<Workgroup> groupMembersUpToMaxMembership(Workgroup workgroup,
      PeerGrouping peerGrouping, Set<Workgroup> possibleMembers);
}
