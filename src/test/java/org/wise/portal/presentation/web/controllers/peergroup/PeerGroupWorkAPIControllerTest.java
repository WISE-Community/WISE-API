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
import org.wise.portal.domain.peergroup.PeerGroup;
import org.wise.portal.domain.run.impl.RunImpl;
import org.wise.portal.domain.workgroup.impl.WorkgroupImpl;
import org.wise.portal.service.peergroup.PeerGroupCreationException;
import org.wise.portal.service.peergroupactivity.PeerGroupActivityNotFoundException;
import org.wise.vle.domain.work.StudentWork;

@RunWith(EasyMockRunner.class)
public class PeerGroupWorkAPIControllerTest extends AbstractPeerGroupAPIControllerTest {

  @TestSubject
  private PeerGroupWorkAPIController controller = new PeerGroupWorkAPIController();

  private List<StudentWork> peerGroup1StudentWork = new ArrayList<StudentWork>();

  @Test
  public void getPeerGroupWork_NonExistingPeerGroupIdSpecifiedByPeerGroupId_ThrowObjectNotFound()
      throws ObjectNotFoundException {
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
  public void getPeerGroupWork_UserNotInPeerGroupSpecifiedByPeerGroupId_ThrowAccessDenied()
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
  public void getPeerGroupWork_UserInPeerGroupSpecifiedByPeerGroupId_ReturnStudentWork()
      throws ObjectNotFoundException {
    expectPeerGroup();
    expectUserInPeerGroup();
    expectGetStudentWork();
    replayAll();
    assertNotNull(controller.getPeerGroupWork(peerGroup1Id, studentAuth));
    verifyAll();
  }

  @Test
  public void getPeerGroupWork_NonExistingRunIdSpecifiedByComponent_ThrowObjectNotFound()
      throws Exception {
    expectRunNotExists();
    replayAll();
    try {
      controller.getPeerGroupWork(runId1, workgroup1Id, run1Node1Id, run1Component1Id, teacherAuth);
      fail("Expected ObjectNotFoundException, but was not thrown");
    } catch (ObjectNotFoundException e) {
    }
    verifyAll();
  }

  @Test
  public void getPeerGroupWork_TeacherNotAllowedToViewRunTeacherSpecifiedByComponent_ThrowAccessDenied()
      throws Exception {
    expectRunExists();
    expectRetrieveTeacherUser();
    expectUserNotAllowedToViewStudentWorkForRun();
    replayAll();
    try {
      controller.getPeerGroupWork(runId1, workgroup1Id, run1Node1Id, run1Component1Id, teacherAuth);
      fail("Expected AccessDeniedException, but was not thrown");
    } catch (AccessDeniedException e) {
    }
    verifyAll();
  }

  @Test
  public void getPeerGroupWork_PeerGroupActivityNotFoundSpecifiedByComponent_ThrowException()
      throws Exception {
    expectUserAllowedToViewStudentWorkForRun();
    expectPeerGroupActivityNotFound();
    replayAll();
    try {
      controller.getPeerGroupWork(runId1, workgroup1Id, run1Node1Id, run1Component1Id,
          teacherAuth);
      fail("PeerGroupActivityNotFoundException expected, but was not thrown");
    } catch (PeerGroupActivityNotFoundException e) {}
    verifyAll();
  }

  @Test
  public void getPeerGroupWork_WorkgroupNotFoundSpecifiedByComponent_ThrowObjectNotFound()
      throws Exception {
    expectUserAllowedToViewStudentWorkForRun();
    expectPeerGroupActivityFound();
    expectWorkgroupNotFound();
    replayAll();
    try {
      controller.getPeerGroupWork(runId1, workgroup1Id, run1Node1Id, run1Component1Id,
          teacherAuth);
      fail("ObjectNotFoundException expected, but was not thrown");
    } catch (ObjectNotFoundException e) {}
    verifyAll();
  }

  @Test
  public void getPeerGroupWork_PeerGroupNotFoundSpecifiedByComponent_ThrowException()
      throws Exception {
    expectUserAllowedToViewStudentWorkForRun();
    expectPeerGroupActivityFound();
    expectWorkgroupFound();
    expectPeerGroupCreationException();
    replayAll();
    try {
      controller.getPeerGroupWork(runId1, workgroup1Id, run1Node1Id, run1Component1Id,
          teacherAuth);
      fail("PeerGroupCreationException expected, but was not thrown");
    } catch (PeerGroupCreationException e) {}
    verifyAll();
  }

  @Test
  public void getPeerGroupWork_PeerGroupFoundSpecifiedByComponent_ReturnStudentWork()
      throws Exception {
    expectUserAllowedToViewStudentWorkForRun();
    expectPeerGroupActivityFound();
    expectWorkgroupFound();
    expectPeerGroupFound();
    expectGetStudentWork();
    replayAll();
    assertNotNull(controller.getPeerGroupWork(runId1, workgroup1Id, run1Node1Id, run1Component1Id,
        teacherAuth));
    verifyAll();
  }

  private void expectRetrieveTeacherUser() {
    expect(userService.retrieveUserByUsername(teacher1UserDetails.getUsername())).andReturn(
        teacher1);
  }

  private void expectRunNotExists() throws ObjectNotFoundException {
    expect(runService.retrieveById(runId1)).andThrow(new ObjectNotFoundException(runId1,
        RunImpl.class));
  }

  private void expectRunExists() throws ObjectNotFoundException {
    expect(runService.retrieveById(runId1)).andReturn(run1);
  }

  private void expectPeerGroupFound() throws Exception {
    expect(peerGroupService.getPeerGroup(workgroup1, peerGroupActivity)).andReturn(peerGroup1);
  }

  private void expectWorkgroupFound() throws ObjectNotFoundException {
    expect(workgroupService.retrieveById(workgroup1Id)).andReturn(workgroup1);
  }

  private void expectWorkgroupNotFound() throws ObjectNotFoundException {
    expect(workgroupService.retrieveById(workgroup1Id)).andThrow(
        new ObjectNotFoundException(workgroup1Id, WorkgroupImpl.class));
  }

  private void expectUserAllowedToViewStudentWorkForRun() throws ObjectNotFoundException {
    expectRunExists();
    expectRetrieveTeacherUser();
    expect(runService.isAllowedToViewStudentWork(run1, teacher1)).andReturn(true);
  }

  private void expectUserNotAllowedToViewStudentWorkForRun() {
    expect(runService.isAllowedToViewStudentWork(run1, teacher1)).andReturn(false);
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

  private void expectPeerGroupIdNotExist() throws ObjectNotFoundException {
    expect(peerGroupService.getById(peerGroup1Id)).andThrow(new ObjectNotFoundException(
        peerGroup1Id, PeerGroup.class));
  }
}
