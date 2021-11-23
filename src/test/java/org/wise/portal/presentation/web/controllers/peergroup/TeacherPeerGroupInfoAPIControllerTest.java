package org.wise.portal.presentation.web.controllers.peergroup;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.easymock.EasyMockRunner;
import org.easymock.TestSubject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.security.access.AccessDeniedException;
import org.wise.portal.dao.ObjectNotFoundException;
import org.wise.portal.domain.peergroup.PeerGroup;
import org.wise.portal.service.peergroupactivity.PeerGroupActivityNotFoundException;

/**
 * @author Hiroki Terashima
 */
@RunWith(EasyMockRunner.class)
public class TeacherPeerGroupInfoAPIControllerTest extends AbstractPeerGroupAPIControllerTest {

  @TestSubject
  private TeacherPeerGroupInfoAPIController controller = new TeacherPeerGroupInfoAPIController();

  @Before
  public void setUp() {
    super.setUp();
    expectRunExists();
  }

  @Test
  public void getPeerGroupsInfo_NoPermissions_AccessDenied() throws Exception {
    expectTeacherHasAccessToRun(false);
    replayAll();
    try {
      controller.getPeerGroupsInfo(runId1, run1Node1Id, run1Component1Id, teacherAuth);
      fail("Expected AccessDeniedException, but was not thrown");
    } catch (AccessDeniedException e) {
    }
    verifyAll();
  }

  @Test
  public void getPeerGroupsInfo_PeerGroupActivityNotFound_ThrowException() throws Exception {
    expectTeacherHasAccessToRun(true);
    expectPeerGroupActivityNotFound();
    replayAll();
    try {
      controller.getPeerGroupsInfo(runId1, run1Node1Id, run1Component1Id, teacherAuth);
      fail("Expected PeerGroupActivityNotFoundException, but was not thrown");
    } catch (PeerGroupActivityNotFoundException e) {
    }
    verifyAll();
  }

  @SuppressWarnings("unchecked")
  @Test
  public void getPeerGroupsInfo_ActivityFound_ReturnInfo() throws Exception {
    expectTeacherHasAccessToRun(true);
    expectPeerGroupActivityFound();
    expectPeerGroupInfo();
    replayAll();
    Map<String, Object> peerGroupsInfo = controller.getPeerGroupsInfo(runId1, run1Node1Id,
        run1Component1Id, teacherAuth);
    assertEquals(2, peerGroupsInfo.size());
    assertEquals(2, ((List<PeerGroup>) peerGroupsInfo.get("peerGroups")).size());
    assertEquals(0, ((List<PeerGroup>) peerGroupsInfo.get("workgroupsNotInPeerGroups")).size());
    verifyAll();
  }

  private void expectPeerGroupInfo() {
    Map<String, Object> peerGroupInfo = new HashMap<String, Object>();
    peerGroupInfo.put("peerGroups", peerGroups);
    peerGroupInfo.put("workgroupsNotInPeerGroups", workgroupsNotInPeerGroups);
    expect(peerGroupInfoService.getPeerGroupInfo(peerGroupActivity)).andReturn(peerGroupInfo);
  }

  private void expectRunExists() {
    try {
      expect(runService.retrieveById(runId1)).andReturn(run1);
    } catch (ObjectNotFoundException e) {
    }
  }

  private void expectTeacherHasAccessToRun(boolean hasAccess) {
    expect(runService.hasReadPermission(teacherAuth, run1)).andReturn(hasAccess);
  }
}
