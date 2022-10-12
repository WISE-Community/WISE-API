package org.wise.portal.service.peergrouping.logic.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.wise.portal.domain.peergrouping.PeerGrouping;
import org.wise.portal.domain.workgroup.Workgroup;

@Service
public class RandomLogicServiceImpl extends PeerGroupLogicServiceImpl {

  boolean canCreatePeerGroup(Workgroup workgroup, Set<Workgroup> workgroupsNotInPeerGroup,
      PeerGrouping peerGrouping) {
    return workgroupsNotInPeerGroup.size() >= 2;
  }

  Set<Workgroup> groupMembersUpToMaxMembership(Workgroup workgroup, PeerGrouping peerGrouping,
      Set<Workgroup> possibleMembers) {
    Set<Workgroup> members = new HashSet<Workgroup>();
    members.add(workgroup);
    possibleMembers.remove(workgroup);
    List<Workgroup> possibleMembersList = new ArrayList<Workgroup>(possibleMembers);
    Random random = new Random();
    while (members.size() < peerGrouping.getMaxMembershipCount()) {
      int randomInt = random.nextInt(possibleMembersList.size());
      Workgroup randomWorkgroup = possibleMembersList.get(randomInt);
      members.add(randomWorkgroup);
      possibleMembersList.remove(randomWorkgroup);
    }
    return members;
  }
}
