package org.wise.portal.presentation.web.controllers.peergroup;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import org.easymock.EasyMockRunner;
import org.easymock.Mock;
import org.easymock.TestSubject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.security.access.AccessDeniedException;
import org.wise.portal.domain.peergroup.impl.PeerGroupImpl;
import org.wise.portal.service.peergroup.PeerGroupCreateService;
import org.wise.portal.service.peergroupactivity.PeerGroupActivityNotFoundException;

@RunWith(EasyMockRunner.class)
public class PeerGroupCreateControllerTest extends AbstractPeerGroupAPIControllerTest {

  @TestSubject
  private PeerGroupCreateController controller = new PeerGroupCreateController();

  @Mock
  private PeerGroupCreateService peerGroupCreateService;

  @Test
  public void create_UserHasNoWritePermission_ThrowAccessDenied() throws Exception {
    expectUserHasRunWritePermission(false);
    replayAll();
    try {
      controller.create(run1, run1Period1, run1Node1Id, run1Component1Id, teacherAuth);
      fail("Expected AccessDeniedException, but was not thrown");
    } catch (AccessDeniedException e) {
    }
    verifyAll();
  }

  @Test
  public void create_PeriodNotInRun_ThrowException() throws Exception {
    expectUserHasRunWritePermission(true);
    replayAll();
    try {
      controller.create(run1, run2Period2, run1Node1Id, run1Component1Id, teacherAuth);
      fail("Expected AccessDeniedException, but was not thrown");
    } catch (AccessDeniedException e) {
    }
    verifyAll();
  }

  @Test
  public void create_PeerGroupActivityNotFound_ThrowException() throws Exception {
    expectUserHasRunWritePermission(true);
    expectPeerGroupActivityNotFound();
    replayAll();
    try {
      controller.create(run1, run1Period1, run1Node1Id, run1Component1Id, teacherAuth);
      fail("Expected PeerGroupActivityNotFoundException, but was not thrown");
    } catch (PeerGroupActivityNotFoundException e) {
    }
    verifyAll();
  }

  @Test
  public void create_PeerGroupActivityFound_CreateGroup() throws Exception {
    expectUserHasRunWritePermission(true);
    expectPeerGroupActivityFound();
    expectCreatePeerGroup();
    replayAll();
    assertNotNull(controller.create(run1, run1Period1, run1Node1Id, run1Component1Id, teacherAuth));
    verifyAll();
  }

  private void expectCreatePeerGroup() {
    expect(peerGroupCreateService.create(peerGroupActivity, run1Period1))
        .andReturn(new PeerGroupImpl());
  }

  @Override
  public void replayAll() {
    super.replayAll();
    replay(peerGroupCreateService);
  }

  @Override
  public void verifyAll() {
    super.verifyAll();
    verify(peerGroupCreateService);
  }
}
