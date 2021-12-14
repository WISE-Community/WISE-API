package org.wise.portal.service.peergroup.impl;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import org.easymock.EasyMockRunner;
import org.easymock.Mock;
import org.easymock.TestSubject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wise.portal.dao.peergroup.PeerGroupDao;
import org.wise.portal.domain.peergroup.PeerGroup;
import org.wise.portal.domain.peergroup.impl.PeerGroupImpl;
import org.wise.portal.domain.workgroup.Workgroup;
import org.wise.portal.service.WISEServiceTest;

/**
 * @author Hiroki Terashima
 */
@RunWith(EasyMockRunner.class)
public class PeerGroupMembershipServiceImplTest extends WISEServiceTest {

  @TestSubject
  private PeerGroupMembershipServiceImpl service = new PeerGroupMembershipServiceImpl();

  @Mock
  private PeerGroupDao<PeerGroup> peerGroupDao;


  PeerGroupServiceTestHelper testHelper;

  @Before
  public void setUp() throws Exception {
    super.setUp();
    testHelper = new PeerGroupServiceTestHelper(run1, run1Component2);
  }

  @Test
  public void addMember_NotMemberOfAnotherPeerGroup_ReturnUpdatedGroup() {
    expectPeerGroup(run1Workgroup3, null);
    expectSavePeerGroup(1);
    replay(peerGroupDao);
    PeerGroup peerGroup = service.addMember(testHelper.peerGroup2, run1Workgroup3);
    assertEquals(1, peerGroup.getMembers().size());
    verify(peerGroupDao);
  }

  @Test
  public void addMember_MemberOfAnotherPeerGroup_RemoveMemberFirst() {
    expectPeerGroup(run1Workgroup1, testHelper.peerGroup1);
    expectSavePeerGroup(2);
    replay(peerGroupDao);
    PeerGroup peerGroup = service.addMember(testHelper.peerGroup2, run1Workgroup1);
    assertEquals(1, peerGroup.getMembers().size());
    verify(peerGroupDao);
  }

  @Test
  public void removeMember_ReturnUpdatedGroup() {
    expectSavePeerGroup(1);
    replay(peerGroupDao);
    PeerGroup peerGroup = service.removeMember(testHelper.peerGroup1, run1Workgroup1);
    assertEquals(2, peerGroup.getMembers().size());
    verify(peerGroupDao);
  }

  private void expectSavePeerGroup(int times) {
    peerGroupDao.save(isA(PeerGroupImpl.class));
    expectLastCall().times(times);
  }

  private void expectPeerGroup(Workgroup workgroup, PeerGroup peerGroup) {
    expect(peerGroupDao.getByWorkgroupAndActivity(workgroup, testHelper.activity)).andReturn(
        peerGroup);
  }
}
