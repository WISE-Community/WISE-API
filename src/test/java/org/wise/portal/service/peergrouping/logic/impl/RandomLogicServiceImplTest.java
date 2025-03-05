package org.wise.portal.service.peergrouping.logic.impl;

import static org.junit.Assert.*;

import java.util.HashSet;
import java.util.Set;

import org.easymock.EasyMockExtension;
import org.easymock.TestSubject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.wise.portal.domain.workgroup.Workgroup;

@ExtendWith(EasyMockExtension.class)
public class RandomLogicServiceImplTest extends PeerGroupLogicServiceImplTest {

  @TestSubject
  private RandomLogicServiceImpl service = new RandomLogicServiceImpl();

  @Test
  public void canCreatePeerGroup_ReturnTrueIFFAtLeastTwoWorkgroupsInPeerGroup() {
    assertEquals(true, service.canCreatePeerGroup(workgroup1, possibleMembers, randomPeerGrouping));
    assertEquals(false,
        service.canCreatePeerGroup(workgroup1, new HashSet<>(), randomPeerGrouping));
  }

  @Test
  public void groupMembersUpToMaxMembership_AddMembersUpToMaxMembershipCount() {
    int maxMembers = 2;
    Set<Workgroup> peerGroupMembers = service.groupMembersUpToMaxMembership(workgroup1,
        randomPeerGrouping, possibleMembers);
    assertEquals(maxMembers, peerGroupMembers.size());
    assertTrue(peerGroupMembers.contains(workgroup1));
  }
}
