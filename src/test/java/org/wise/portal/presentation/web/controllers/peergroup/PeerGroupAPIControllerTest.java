package org.wise.portal.presentation.web.controllers.peergroup;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import org.easymock.EasyMockRunner;
import org.easymock.Mock;
import org.easymock.TestSubject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.security.access.AccessDeniedException;
import org.wise.portal.domain.peergroup.PeerGroup;
import org.wise.portal.domain.peergroup.impl.PeerGroupImpl;
import org.wise.portal.domain.peergroupactivity.PeerGroupActivity;
import org.wise.portal.domain.peergroupactivity.impl.PeerGroupActivityImpl;
import org.wise.portal.presentation.web.controllers.APIControllerTest;
import org.wise.portal.service.peergroup.PeerGroupActivityThresholdNotSatisfiedException;
import org.wise.portal.service.peergroup.PeerGroupCreationException;
import org.wise.portal.service.peergroup.PeerGroupService;
import org.wise.portal.service.peergroupactivity.PeerGroupActivityNotFoundException;
import org.wise.portal.service.peergroupactivity.PeerGroupActivityService;

@RunWith(EasyMockRunner.class)
public class PeerGroupAPIControllerTest extends APIControllerTest {

  @TestSubject
  private PeerGroupAPIController controller = new PeerGroupAPIController();

  @Mock
  private PeerGroupService peerGroupService;

  @Mock
  private PeerGroupActivityService peerGroupActivityService;

  private String run1Node1Id = "run1Node1";

  private String run1Component1Id = "run1Component1";

  private PeerGroupActivity peerGroupActivity;

  private PeerGroup peerGroup;

  @Before
  public void setUp() {
    super.setUp();
    peerGroupActivity = new PeerGroupActivityImpl();
    peerGroup = new PeerGroupImpl();
  }

  @Test
  public void getPeerGroup_WorkgroupNotAssociatedWithRun_AccessDenied() throws Exception {
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
  public void getPeerGroup_PeerGroupActivityNotFound_ThrowException() throws Exception {
    expectWorkgroupAssociatedWithRun(true);
    expectPeerGroupActivityFound(false);
    replayAll();
    try {
      controller.getPeerGroup(runId1, workgroup1Id, run1Node1Id, run1Component1Id, studentAuth);
      fail("Expected PeerGroupActivityNotFoundException, but was not thrown");
    } catch (PeerGroupActivityNotFoundException e) {
    }
    verifyAll();
  }

  @Test
  public void getPeerGroup_PeerGroupThresholdNotMet_ThrowException() throws Exception {
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
  public void getPeerGroup_ErrorCreatingPeerGroup_ThrowException() throws Exception {
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
  public void getPeerGroup_FoundExistingGroupOrGroupCreated_ReturnGroup() throws Exception {
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
    expectPeerGroupActivityFound(true);
  }

  private void expectWorkgroupAssociatedWithRun(boolean isAssociated) throws Exception {
    expect(runService.retrieveById(runId1)).andReturn(run1);
    expect(workgroupService.retrieveById(workgroup1Id)).andReturn(workgroup1);
    expect(userService.retrieveUserByUsername(studentAuth.getName())).andReturn(student1);
    expect(workgroupService.isUserInWorkgroupForRun(student1, run1, workgroup1))
        .andReturn(isAssociated);
  }

  private void expectPeerGroupActivityFound(boolean isFound)
      throws PeerGroupActivityNotFoundException {
    if (isFound) {
      expect(peerGroupActivityService.getByComponent(run1, run1Node1Id, run1Component1Id))
          .andReturn(peerGroupActivity);
    } else {
      expect(peerGroupActivityService.getByComponent(run1, run1Node1Id, run1Component1Id))
          .andThrow(new PeerGroupActivityNotFoundException());
    }
  }

  private void expectPeerGroupThresholdNotSatisifed() throws Exception {
    expect(peerGroupService.getPeerGroup(workgroup1, peerGroupActivity))
        .andThrow(new PeerGroupActivityThresholdNotSatisfiedException());
  }

  private void expectPeerGroupCreationException() throws Exception {
    expect(peerGroupService.getPeerGroup(workgroup1, peerGroupActivity))
        .andThrow(new PeerGroupCreationException());
  }

  private void expectPeerGroupCreated() throws Exception {
    expect(peerGroupService.getPeerGroup(workgroup1, peerGroupActivity)).andReturn(peerGroup);
  }

  private void verifyAll() {
    verify(peerGroupActivityService, peerGroupService, runService, userService, workgroupService);
  }

  private void replayAll() {
    replay(peerGroupActivityService, peerGroupService, runService, userService, workgroupService);
  }
}
