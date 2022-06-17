package org.wise.portal.presentation.web.controllers.peergroup;

import static org.easymock.EasyMock.expect;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;

import org.easymock.EasyMockRunner;
import org.easymock.TestSubject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.security.access.AccessDeniedException;
import org.wise.portal.dao.ObjectNotFoundException;
import org.wise.portal.service.peergroup.PeerGroupCreationException;
import org.wise.portal.service.peergrouping.PeerGroupingNotFoundException;
import org.wise.vle.domain.work.StudentWork;

@RunWith(EasyMockRunner.class)
public class PeerGroupWorkAPIControllerTest extends AbstractPeerGroupAPIControllerTest {

  @TestSubject
  private PeerGroupWorkAPIController controller = new PeerGroupWorkAPIController();

  private List<StudentWork> peerGroup1StudentWork = new ArrayList<StudentWork>();

  @Test
  public void getPeerGroupWork_UserNotInPeerGroupSpecifiedByPeerGroupId_ThrowAccessDenied()
      throws ObjectNotFoundException {
    expectUserNotInPeerGroup();
    replayAll();
    try {
      controller.getPeerGroupWork(peerGroup1, run1Node1Id, run1Component1Id,  studentAuth);
      fail("Expected AccessDeniedException, but was not thrown");
    } catch (AccessDeniedException e) {
    }
    verifyAll();
  }

  @Test
  public void getPeerGroupWork_UserInPeerGroupSpecifiedByPeerGroupId_ReturnStudentWork()
      throws ObjectNotFoundException {
    expectUserInPeerGroup();
    expectGetStudentWork();
    replayAll();
    assertNotNull(controller.getPeerGroupWork(peerGroup1,  run1Node1Id, run1Component1Id,
        studentAuth));
    verifyAll();
  }

  @Test
  public void getPeerGroupWork_TeacherNotAllowedToViewRunTeacherSpecifiedByComponent_ThrowAccessDenied()
      throws Exception {
    expectRetrieveTeacherUser();
    expectUserNotAllowedToViewStudentWorkForRun();
    replayAll();
    try {
      controller.getPeerGroupWork(run1, workgroup1, run1Node1Id, run1Component1Id, teacherAuth);
      fail("Expected AccessDeniedException, but was not thrown");
    } catch (AccessDeniedException e) {
    }
    verifyAll();
  }

  @Test
  public void getPeerGroupWork_PeerGroupingNotFoundSpecifiedByComponent_ThrowException()
      throws Exception {
    expectUserAllowedToViewStudentWorkForRun();
    expectPeerGroupingNotFound();
    replayAll();
    try {
      controller.getPeerGroupWork(run1, workgroup1, run1Node1Id, run1Component1Id,
          teacherAuth);
      fail("PeerGroupingNotFoundException expected, but was not thrown");
    } catch (PeerGroupingNotFoundException e) {}
    verifyAll();
  }

  @Test
  public void getPeerGroupWork_PeerGroupNotFoundSpecifiedByComponent_ThrowException()
      throws Exception {
    expectUserAllowedToViewStudentWorkForRun();
    expectPeerGroupingFound();
    expectPeerGroupCreationException();
    replayAll();
    try {
      controller.getPeerGroupWork(run1, workgroup1, run1Node1Id, run1Component1Id,
          teacherAuth);
      fail("PeerGroupCreationException expected, but was not thrown");
    } catch (PeerGroupCreationException e) {}
    verifyAll();
  }

  @Test
  public void getPeerGroupWork_PeerGroupFoundSpecifiedByComponent_ReturnStudentWork()
      throws Exception {
    expectUserAllowedToViewStudentWorkForRun();
    expectPeerGroupingFound();
    expectPeerGroupFound();
    expectGetStudentWork();
    replayAll();
    assertNotNull(controller.getPeerGroupWork(run1, workgroup1, run1Node1Id, run1Component1Id,
        teacherAuth));
    verifyAll();
  }

  private void expectRetrieveTeacherUser() {
    expect(userService.retrieveUserByUsername(teacher1UserDetails.getUsername())).andReturn(
        teacher1);
  }

  private void expectPeerGroupFound() throws Exception {
    expect(peerGroupService.getPeerGroup(workgroup1, peerGrouping)).andReturn(peerGroup1);
  }

  private void expectUserAllowedToViewStudentWorkForRun() throws ObjectNotFoundException {
    expectRetrieveTeacherUser();
    expect(runService.isAllowedToViewStudentWork(run1, teacher1)).andReturn(true);
  }

  private void expectUserNotAllowedToViewStudentWorkForRun() {
    expect(runService.isAllowedToViewStudentWork(run1, teacher1)).andReturn(false);
  }

  private void expectGetStudentWork() {
    expect(peerGroupService.getStudentWork(peerGroup1, run1Node1Id, run1Component1Id))
        .andReturn(peerGroup1StudentWork);
  }

  private void expectUserNotInPeerGroup() {
    expect(userService.retrieveUserByUsername(studentAuth.getName())).andReturn(student2);
  }

  private void expectUserInPeerGroup() {
    expect(userService.retrieveUserByUsername(studentAuth.getName())).andReturn(student1);
  }
}
