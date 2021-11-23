package org.wise.portal.presentation.web.controllers.peergroup;

import static org.easymock.EasyMock.*;

import java.util.ArrayList;
import java.util.List;

import org.easymock.Mock;
import org.junit.Before;
import org.wise.portal.domain.peergroup.PeerGroup;
import org.wise.portal.domain.peergroup.impl.PeerGroupImpl;
import org.wise.portal.domain.peergroupactivity.PeerGroupActivity;
import org.wise.portal.domain.peergroupactivity.impl.PeerGroupActivityImpl;
import org.wise.portal.domain.workgroup.Workgroup;
import org.wise.portal.presentation.web.controllers.APIControllerTest;
import org.wise.portal.service.peergroup.PeerGroupCreationException;
import org.wise.portal.service.peergroup.PeerGroupInfoService;
import org.wise.portal.service.peergroup.PeerGroupService;
import org.wise.portal.service.peergroupactivity.PeerGroupActivityNotFoundException;
import org.wise.portal.service.peergroupactivity.PeerGroupActivityService;

public abstract class AbstractPeerGroupAPIControllerTest extends APIControllerTest {

  @Mock
  protected PeerGroupService peerGroupService;

  @Mock
  protected PeerGroupActivityService peerGroupActivityService;

  @Mock
  protected PeerGroupInfoService peerGroupInfoService;

  protected PeerGroupActivity peerGroupActivity;

  protected PeerGroup peerGroup1, peerGroup2;

  protected Long peerGroup1Id = 1L;

  protected Long peerGroup2Id = 2L;

  protected List<PeerGroup> peerGroups = new ArrayList<PeerGroup>();

  protected List<Workgroup> workgroupsNotInPeerGroups = new ArrayList<Workgroup>();

  @Before
  public void setUp() {
    super.setUp();
    peerGroupActivity = new PeerGroupActivityImpl();
    peerGroup1 = new PeerGroupImpl();
    peerGroup1.addMember(workgroup1);
    peerGroups.add(peerGroup1);
    peerGroup2 = new PeerGroupImpl();
    peerGroup2.addMember(workgroup2);
    peerGroups.add(peerGroup2);
  }

  protected void expectPeerGroupActivityFound() throws PeerGroupActivityNotFoundException {
    expect(peerGroupActivityService.getByComponent(run1, run1Node1Id, run1Component1Id))
        .andReturn(peerGroupActivity);
  }

  protected void expectPeerGroupActivityNotFound() throws PeerGroupActivityNotFoundException {
    expect(peerGroupActivityService.getByComponent(run1, run1Node1Id, run1Component1Id))
        .andThrow(new PeerGroupActivityNotFoundException());
  }

  protected void expectPeerGroupCreationException() throws Exception {
    expect(peerGroupService.getPeerGroup(workgroup1, peerGroupActivity))
        .andThrow(new PeerGroupCreationException());
  }

  protected void verifyAll() {
    verify(peerGroupActivityService, peerGroupInfoService, peerGroupService, runService,
       userService, workgroupService);
  }

  protected void replayAll() {
    replay(peerGroupActivityService, peerGroupInfoService, peerGroupService, runService,
        userService, workgroupService);
  }
}
