package org.wise.portal.presentation.web.controllers.peergroup;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.easymock.EasyMockRunner;
import org.easymock.Mock;
import org.easymock.TestSubject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.security.access.AccessDeniedException;
import org.wise.portal.dao.ObjectNotFoundException;
import org.wise.portal.domain.peergroup.PeerGroup;
import org.wise.portal.domain.peergroup.impl.PeerGroupImpl;
import org.wise.portal.presentation.web.controllers.APIControllerTest;
import org.wise.portal.service.peergroup.PeerGroupService;
import org.wise.vle.domain.work.StudentWork;

@RunWith(EasyMockRunner.class)
public class PeerGroupWorkAPIControllerTest extends APIControllerTest {

  @TestSubject
  private PeerGroupWorkAPIController controller = new PeerGroupWorkAPIController();

  @Mock
  private PeerGroupService peerGroupService;

  private Long peerGroup1Id = 1L;

  private PeerGroup peerGroup1;

  private List<StudentWork> peerGroup1StudentWork = new ArrayList<StudentWork>();

  @Before
  public void setUp() {
    super.setUp();
    peerGroup1 = new PeerGroupImpl();
    peerGroup1.addMember(workgroup1);
  }

  @Test
  public void getPeerGroupWork_NonExistingPeerGroupId_ThrowObjectNotFound() {
    expectPeerGroupIdNotExist();
    replayAll();
    try {
      controller.getPeerGroupWork(peerGroup1Id, studentAuth);
      fail("Expected ObjectNotFoundException, but was not thrown");
    } catch (ObjectNotFoundException e) {
    }
    verifyAll();
  }

  @Test
  public void getPeerGroupWork_UserNotInPeerGroup_ThrowAccessDenied()
      throws ObjectNotFoundException {
    expectPeerGroup();
    expectUserNotInPeerGroup();
    replayAll();
    try {
      controller.getPeerGroupWork(peerGroup1Id, studentAuth);
      fail("Expected AccessDeniedException, but was not thrown");
    } catch (AccessDeniedException e) {
    }
    verifyAll();
  }

  @Test
  public void getPeerGroupWork_UserInPeerGroup_ReturnStudentWork() throws ObjectNotFoundException {
    expectPeerGroup();
    expectUserInPeerGroup();
    expectGetStudentWork();
    replayAll();
    assertNotNull(controller.getPeerGroupWork(peerGroup1Id, studentAuth));
    verifyAll();
  }

  private void expectGetStudentWork() {
    expect(peerGroupService.getStudentWork(peerGroup1)).andReturn(peerGroup1StudentWork);
  }

  private void expectUserNotInPeerGroup() {
    expect(userService.retrieveUserByUsername(studentAuth.getName())).andReturn(student2);
  }

  private void expectUserInPeerGroup() {
    expect(userService.retrieveUserByUsername(studentAuth.getName())).andReturn(student1);
  }

  private void expectPeerGroup() throws ObjectNotFoundException {
    expect(peerGroupService.getById(peerGroup1Id)).andReturn(peerGroup1);
  }

  private void expectPeerGroupIdNotExist() {
    try {
      expect(peerGroupService.getById(peerGroup1Id)).andThrow(new ObjectNotFoundException(
          peerGroup1Id, PeerGroup.class));
    } catch (ObjectNotFoundException e) {
    }
  }

  private void verifyAll() {
    verify(peerGroupService, runService, userService, workgroupService);
  }

  private void replayAll() {
    replay(peerGroupService, runService, userService, workgroupService);
  }
}
