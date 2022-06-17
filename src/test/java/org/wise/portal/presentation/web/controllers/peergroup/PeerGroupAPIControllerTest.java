package org.wise.portal.presentation.web.controllers.peergroup;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import org.easymock.EasyMockRunner;
import org.easymock.TestSubject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.security.access.AccessDeniedException;
import org.wise.portal.service.peergroup.PeerGroupCreationException;
import org.wise.portal.service.peergrouping.PeerGroupingNotFoundException;

@RunWith(EasyMockRunner.class)
public class PeerGroupAPIControllerTest extends AbstractPeerGroupAPIControllerTest {

  @TestSubject
  private PeerGroupAPIController controller = new PeerGroupAPIController();

  @Test
  public void getPeerGroup_UserNotAssociatedWithRun_AccessDenied() throws Exception {
    expectWorkgroupAssociatedWithRun(false);
    replayAll();
    try {
      controller.getPeerGroup(run1, workgroup1, peerGrouping1Tag, studentAuth);
      fail("Expected AccessDeniedException, but was not thrown");
    } catch (AccessDeniedException e) {
    }
    verifyAll();
  }

  @Test
  public void getPeerGroupByComponent_ErrorCreatingPeerGroup_ThrowException() throws Exception {
    expectWorkgroupAssociatedWithRunAndPeerGroupingFound();
    expectPeerGroupCreationException();
    replayAll();
    try {
      controller.getPeerGroup(run1, workgroup1, peerGrouping1Tag, studentAuth);
      fail("Expected PeerGroupCreationException, but was not thrown");
    } catch (PeerGroupCreationException e) {
    }
    verifyAll();
  }

  @Test
  public void getPeerGroup_FoundExistingGroupOrGroupCreated_ReturnGroup() throws Exception {
    expectWorkgroupAssociatedWithRunAndPeerGroupingFound();
    expectPeerGroupCreated();
    replayAll();
    assertNotNull(controller.getPeerGroup(run1, workgroup1, peerGrouping1Tag, studentAuth));
    verifyAll();
  }

  private void expectWorkgroupAssociatedWithRunAndPeerGroupingFound() throws Exception,
      PeerGroupingNotFoundException {
    expectWorkgroupAssociatedWithRun(true);
    expectPeerGroupingByTagFound();
  }

  private void expectWorkgroupAssociatedWithRun(boolean isAssociated) throws Exception {
    expect(userService.retrieveUserByUsername(studentAuth.getName())).andReturn(student1);
    expect(workgroupService.isUserInWorkgroupForRun(student1, run1, workgroup1))
        .andReturn(isAssociated);
  }

  private void expectPeerGroupCreated() throws Exception {
    expect(peerGroupService.getPeerGroup(workgroup1, peerGrouping)).andReturn(peerGroup1);
  }
}
