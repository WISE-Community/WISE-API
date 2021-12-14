package org.wise.portal.service.peergroup.impl;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.isA;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;

import org.easymock.EasyMockRunner;
import org.easymock.Mock;
import org.easymock.TestSubject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wise.portal.dao.peergroup.PeerGroupDao;
import org.wise.portal.domain.peergroup.PeerGroup;
import org.wise.portal.domain.peergroup.impl.PeerGroupImpl;
import org.wise.portal.domain.workgroup.Workgroup;

/**
 * @author Hiroki Terashima
 */
@RunWith(EasyMockRunner.class)
public class PeerGroupMembershipServiceImplTest extends PeerGroupServiceTest {

  @TestSubject
  private PeerGroupMembershipServiceImpl service = new PeerGroupMembershipServiceImpl();

  @Mock
  private PeerGroupDao<PeerGroup> peerGroupDao;

  @Test
  public void addMember_NotMemberOfAnotherPeerGroup_ReturnUpdatedGroup() {
    expectPeerGroup(run1Workgroup3, null);
    expectSavePeerGroup(1);
    replay(peerGroupDao);
    PeerGroup peerGroup = service.addMember(peerGroup2, run1Workgroup3);
    assertEquals(1, peerGroup.getMembers().size());
    verify(peerGroupDao);
  }

  @Test
  public void addMember_MemberOfAnotherPeerGroup_RemoveMemberFirst() {
    expectPeerGroup(run1Workgroup1, peerGroup1);
    expectSavePeerGroup(2);
    replay(peerGroupDao);
    PeerGroup peerGroup = service.addMember(peerGroup2, run1Workgroup1);
    assertEquals(1, peerGroup.getMembers().size());
    verify(peerGroupDao);
  }

  @Test
  public void removeMember_ReturnUpdatedGroup() {
    expectSavePeerGroup(1);
    replay(peerGroupDao);
    assertEquals(2, peerGroup1.getMembers().size());
    PeerGroup peerGroup = service.removeMember(peerGroup1, run1Workgroup1);
    assertEquals(1, peerGroup.getMembers().size());
    assertEquals(1, peerGroup1.getMembers().size());
    verify(peerGroupDao);
  }

  private void expectSavePeerGroup(int times) {
    peerGroupDao.save(isA(PeerGroupImpl.class));
    expectLastCall().times(times);
  }

  private void expectPeerGroup(Workgroup workgroup, PeerGroup peerGroup) {
    expect(peerGroupDao.getByWorkgroupAndActivity(workgroup, activity)).andReturn(
        peerGroup);
  }
}
