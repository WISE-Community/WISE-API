package org.wise.portal.presentation.web.controllers.peergroup;

import static org.easymock.EasyMock.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.easymock.EasyMockExtension;
import org.easymock.TestSubject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.security.access.AccessDeniedException;
import org.wise.portal.domain.peergroup.PeerGroup;

/**
 * @author Hiroki Terashima
 */
@ExtendWith(EasyMockExtension.class)
public class TeacherPeerGroupInfoAPIControllerTest extends AbstractPeerGroupAPIControllerTest {

  @TestSubject
  private TeacherPeerGroupInfoAPIController controller = new TeacherPeerGroupInfoAPIController();

  @BeforeEach
  public void setUp() {
    super.setUp();
  }

  @Test
  public void getPeerGroupsInfo_NoPermissions_AccessDenied() throws Exception {
    expectTeacherHasAccessToRun(false);
    replayAll();
    try {
      controller.getPeerGroupsInfo(run1, peerGrouping1Tag, teacherAuth);
      fail("Expected AccessDeniedException, but was not thrown");
    } catch (AccessDeniedException e) {
    }
    verifyAll();
  }

  @SuppressWarnings("unchecked")
  @Test
  public void getPeerGroupsInfo_PeerGroupingFound_ReturnInfo() throws Exception {
    expectTeacherHasAccessToRun(true);
    expectPeerGroupingByTagFound();
    expectPeerGroupInfo();
    replayAll();
    Map<String, Object> peerGroupsInfo = controller.getPeerGroupsInfo(run1, peerGrouping1Tag,
        teacherAuth);
    assertEquals(2, peerGroupsInfo.size());
    assertEquals(2, ((List<PeerGroup>) peerGroupsInfo.get("peerGroups")).size());
    assertEquals(0, ((List<PeerGroup>) peerGroupsInfo.get("workgroupsNotInPeerGroups")).size());
    verifyAll();
  }

  private void expectPeerGroupInfo() {
    Map<String, Object> peerGroupInfo = new HashMap<String, Object>();
    peerGroupInfo.put("peerGroups", peerGroups);
    peerGroupInfo.put("workgroupsNotInPeerGroups", workgroupsNotInPeerGroups);
    expect(peerGroupInfoService.getPeerGroupInfo(peerGrouping)).andReturn(peerGroupInfo);
  }

  private void expectTeacherHasAccessToRun(boolean hasAccess) {
    expect(runService.hasReadPermission(teacherAuth, run1)).andReturn(hasAccess);
  }
}
