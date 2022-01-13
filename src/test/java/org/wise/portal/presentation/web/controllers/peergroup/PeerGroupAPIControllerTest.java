package org.wise.portal.presentation.web.controllers.peergroup;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import org.easymock.EasyMockRunner;
import org.easymock.TestSubject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.security.access.AccessDeniedException;
import org.wise.portal.service.peergroup.PeerGroupActivityThresholdNotSatisfiedException;
import org.wise.portal.service.peergroup.PeerGroupCreationException;
import org.wise.portal.service.peergroupactivity.PeerGroupActivityNotFoundException;

@RunWith(EasyMockRunner.class)
public class PeerGroupAPIControllerTest extends AbstractPeerGroupAPIControllerTest {

  @TestSubject
  private PeerGroupAPIController controller = new PeerGroupAPIController();

  @Test
  public void getPeerGroupByComponent_WorkgroupNotAssociatedWithRun_AccessDenied() throws Exception {
    expectWorkgroupAssociatedWithRun(false);
    replayAll();
    try {
      controller.getPeerGroup(runId1, workgroup1Id, run1Node1Id, run1Component1Id, studentAuth);
      fail("Expected AccessDeniedException, but was not thrown");
    } catch (AccessDeniedException e) {
    }
    verifyAll();
  }

  @Test
  public void getPeerGroupByComponent_PeerGroupActivityNotFound_ThrowException() throws Exception {
    expectWorkgroupAssociatedWithRun(true);
    expectPeerGroupActivityNotFound();
    replayAll();
    try {
      controller.getPeerGroup(runId1, workgroup1Id, run1Node1Id, run1Component1Id, studentAuth);
      fail("Expected PeerGroupActivityNotFoundException, but was not thrown");
    } catch (PeerGroupActivityNotFoundException e) {
    }
    verifyAll();
  }

  @Test
  public void getPeerGroupByComponent_PeerGroupThresholdNotMet_ThrowException() throws Exception {
    expectWorkgroupAssociatedWithRunAndActivityFound();
    expectPeerGroupThresholdNotSatisifed();
    replayAll();
    try {
      controller.getPeerGroup(runId1, workgroup1Id, run1Node1Id, run1Component1Id, studentAuth);
      fail("Expected PeerGroupCreationException, but was not thrown");
    } catch (PeerGroupActivityThresholdNotSatisfiedException e) {
    }
    verifyAll();
  }

  @Test
  public void getPeerGroupByComponent_ErrorCreatingPeerGroup_ThrowException() throws Exception {
    expectWorkgroupAssociatedWithRunAndActivityFound();
    expectPeerGroupCreationException();
    replayAll();
    try {
      controller.getPeerGroup(runId1, workgroup1Id, run1Node1Id, run1Component1Id, studentAuth);
      fail("Expected PeerGroupCreationException, but was not thrown");
    } catch (PeerGroupCreationException e) {
    }
    verifyAll();
  }

  @Test
  public void getPeerGroupByComponent_FoundExistingGroupOrGroupCreated_ReturnGroup() throws Exception {
    expectWorkgroupAssociatedWithRunAndActivityFound();
    expectPeerGroupCreated();
    replayAll();
    assertNotNull(controller.getPeerGroup(runId1, workgroup1Id, run1Node1Id, run1Component1Id,
        studentAuth));
    verifyAll();
  }

  private void expectWorkgroupAssociatedWithRunAndActivityFound() throws Exception,
      PeerGroupActivityNotFoundException {
    expectWorkgroupAssociatedWithRun(true);
    expectPeerGroupActivityFound();
  }

  private void expectWorkgroupAssociatedWithRun(boolean isAssociated) throws Exception {
    expect(runService.retrieveById(runId1)).andReturn(run1);
    expect(workgroupService.retrieveById(workgroup1Id)).andReturn(workgroup1);
    expect(userService.retrieveUserByUsername(studentAuth.getName())).andReturn(student1);
    expect(workgroupService.isUserInWorkgroupForRun(student1, run1, workgroup1))
        .andReturn(isAssociated);
  }

  private void expectPeerGroupThresholdNotSatisifed() throws Exception {
    expect(peerGroupService.getPeerGroup(workgroup1, peerGroupActivity))
        .andThrow(new PeerGroupActivityThresholdNotSatisfiedException());
  }

  private void expectPeerGroupCreated() throws Exception {
    expect(peerGroupService.getPeerGroup(workgroup1, peerGroupActivity)).andReturn(peerGroup1);
  }
}
