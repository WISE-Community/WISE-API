package org.wise.portal.presentation.web.controllers.peergroup;

import static org.easymock.EasyMock.*;

import java.util.ArrayList;
import java.util.List;

import org.easymock.Mock;
import org.junit.Before;
import org.wise.portal.domain.peergroup.PeerGroup;
import org.wise.portal.domain.peergroup.impl.PeerGroupImpl;
import org.wise.portal.domain.peergrouping.PeerGrouping;
import org.wise.portal.domain.peergrouping.impl.PeerGroupingImpl;
import org.wise.portal.domain.workgroup.Workgroup;
import org.wise.portal.presentation.web.controllers.APIControllerTest;
import org.wise.portal.service.peergroup.PeerGroupCreationException;
import org.wise.portal.service.peergroup.PeerGroupInfoService;
import org.wise.portal.service.peergroup.PeerGroupService;
import org.wise.portal.service.peergrouping.PeerGroupingNotFoundException;
import org.wise.portal.service.peergrouping.PeerGroupingService;

public abstract class AbstractPeerGroupAPIControllerTest extends APIControllerTest {

  @Mock
  protected PeerGroupService peerGroupService;

  @Mock
  protected PeerGroupingService peerGroupingService;

  @Mock
  protected PeerGroupInfoService peerGroupInfoService;

  protected PeerGrouping peerGrouping;

  protected PeerGroupImpl peerGroup1, peerGroup2;

  protected Long peerGroup1Id = 1L;

  protected Long peerGroup2Id = 2L;

  protected String peerGrouping1Tag = "peerGrouping1";

  protected List<PeerGroup> peerGroups = new ArrayList<PeerGroup>();

  protected List<Workgroup> workgroupsNotInPeerGroups = new ArrayList<Workgroup>();

  @Before
  public void setUp() {
    super.setUp();
    peerGrouping = new PeerGroupingImpl();
    peerGrouping.setRun(run1);
    peerGrouping.setTag(peerGrouping1Tag);
    peerGroup1 = new PeerGroupImpl();
    peerGroup1.setPeerGrouping(peerGrouping);
    peerGroup1.addMember(workgroup1);
    peerGroups.add(peerGroup1);
    peerGroup2 = new PeerGroupImpl();
    peerGroup2.addMember(workgroup2);
    peerGroups.add(peerGroup2);
  }

  protected void expectPeerGroupingFound() throws PeerGroupingNotFoundException {
    expect(peerGroupingService.getByComponent(run1, run1Node1Id, run1Component1Id))
        .andReturn(peerGrouping);
  }

  protected void expectPeerGroupingNotFound() throws PeerGroupingNotFoundException {
    expect(peerGroupingService.getByComponent(run1, run1Node1Id, run1Component1Id))
        .andThrow(new PeerGroupingNotFoundException());
  }

  protected void expectPeerGroupingByTagFound() {
    expect(peerGroupingService.getByTag(run1, peerGrouping1Tag))
        .andReturn(peerGrouping);
  }

  protected void expectPeerGroupCreationException() throws Exception {
    expect(peerGroupService.getPeerGroup(workgroup1, peerGrouping))
        .andThrow(new PeerGroupCreationException());
  }

  protected void expectUserHasRunWritePermission(boolean hasPermission) {
    expect(runService.hasWritePermission(teacherAuth, run1)).andReturn(hasPermission);
  }

  protected void verifyAll() {
    verify(peerGroupingService, peerGroupInfoService, peerGroupService, runService,
       userService, workgroupService);
  }

  protected void replayAll() {
    replay(peerGroupingService, peerGroupInfoService, peerGroupService, runService,
        userService, workgroupService);
  }
}
