package org.wise.portal.presentation.web.controllers.student;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;

import org.easymock.EasyMockExtension;
import org.easymock.TestSubject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.security.access.AccessDeniedException;
import org.wise.portal.dao.ObjectNotFoundException;
import org.wise.portal.presentation.web.controllers.peergroup.AbstractPeerGroupWorkControllerTest;

@ExtendWith(EasyMockExtension.class)
public class PeerGroupWorkControllerTest extends AbstractPeerGroupWorkControllerTest {

  @TestSubject
  private PeerGroupWorkController controller = new PeerGroupWorkController();

  @Test
  public void getPeerGroupWork_NotShowGroupWorkComponent_ThrowException()
      throws IOException, ObjectNotFoundException {
    expectUserInPeerGroup();
    expectComponentType(run1Node1Id, run1Component1Id, "OpenResponse");
    replayAll();
    assertThrows(AccessDeniedException.class, () -> controller.getPeerGroupWork(studentAuth,
        peerGroup1, run1Node1Id, run1Component1Id, run1Node2Id, run1Component2Id));
    verifyAll();
  }

  @Test
  public void getPeerGroupWork_ComponentNotInProject_ThrowException()
      throws IOException, ObjectNotFoundException {
    expectUserInPeerGroup();
    expectComponentType(run1Node1Id, run1Component1Id,
        controller.SHOW_PEER_GROUP_WORK_TYPE);
    replayAll();
    assertThrows(AccessDeniedException.class, () -> controller.getPeerGroupWork(studentAuth,
        peerGroup1, run1Node1Id, run1Component2Id, run1Node2Id, run1Component2Id));
    verifyAll();
  }
}
