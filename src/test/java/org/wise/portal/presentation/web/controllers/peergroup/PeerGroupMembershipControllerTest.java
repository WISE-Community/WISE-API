package org.wise.portal.presentation.web.controllers.peergroup;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import org.easymock.EasyMockRunner;
import org.easymock.Mock;
import org.easymock.TestSubject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.security.access.AccessDeniedException;
import org.wise.portal.service.peergroup.PeerGroupMembershipService;

@RunWith(EasyMockRunner.class)
public class PeerGroupMembershipControllerTest extends AbstractPeerGroupAPIControllerTest {

  @TestSubject
  private PeerGroupMembershipController controller = new PeerGroupMembershipController();

  @Mock
  private PeerGroupMembershipService peerGroupMembershipService;

  @Test
  public void addMember_UserHasNoWritePermission_ThrowAccessDenied() {
    expectUserHasRunWritePermission(false);
    replayAll();
    try {
      controller.addMember(peerGroup1, workgroup1, teacherAuth);
      fail("Expected AccessDeniedException, but was not thrown");
    } catch (AccessDeniedException e) {
    }
    verifyAll();
  }

  @Test
  public void addMember_WorkgroupNotInRun_ThrowAccessDenied() {
    expectUserHasRunWritePermission(true);
    replayAll();
    try {
      controller.addMember(peerGroup1, workgroup3, teacherAuth);
      fail("Expected AccessDeniedException, but was not thrown");
    } catch (AccessDeniedException e) {
    }
    verifyAll();
  }

  @Test
  public void addMember_ReturnModifiedGroup() {
    expectUserHasRunWritePermission(true);
    expectAddMember();
    replayAll();
    controller.addMember(peerGroup1, workgroup1, teacherAuth);
    verifyAll();
  }

  @Test
  public void removeMember_UserHasNoWritePermission_ThrowAccessDenied() {
    expectUserHasRunWritePermission(false);
    replayAll();
    try {
      controller.removeMember(peerGroup1, workgroup1, teacherAuth);
      fail("Expected AccessDeniedException, but was not thrown");
    } catch (AccessDeniedException e) {
    }
    verifyAll();
  }

  @Test
  public void removeMember_WorkgroupNotInRun_ThrowAccessDenied() {
    expectUserHasRunWritePermission(true);
    replayAll();
    try {
      controller.removeMember(peerGroup1, workgroup3, teacherAuth);
      fail("Expected AccessDeniedException, but was not thrown");
    } catch (AccessDeniedException e) {
    }
    verifyAll();
  }

  @Test
  public void removeMember_ReturnModifiedGroup() {
    expectUserHasRunWritePermission(true);
    expectRemoveMember();
    replayAll();
    controller.removeMember(peerGroup1, workgroup1, teacherAuth);
    verifyAll();
  }

  private void expectAddMember() {
    expect(peerGroupMembershipService.addMember(peerGroup1, workgroup1)).andReturn(peerGroup1);
  }

  private void expectRemoveMember() {
    expect(peerGroupMembershipService.removeMember(peerGroup1, workgroup1)).andReturn(peerGroup1);
  }

  @Override
  public void replayAll() {
    super.replayAll();
    replay(peerGroupMembershipService);
  }

  @Override
  public void verifyAll() {
    super.verifyAll();
    verify(peerGroupMembershipService);
  }
}
