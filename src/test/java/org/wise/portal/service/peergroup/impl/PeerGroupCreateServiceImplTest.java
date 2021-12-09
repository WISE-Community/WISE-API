package org.wise.portal.service.peergroup.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;

import static org.junit.Assert.*;
import static org.easymock.EasyMock.*;

import org.easymock.EasyMockRunner;
import org.easymock.Mock;
import org.easymock.TestSubject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wise.portal.dao.peergroup.PeerGroupDao;
import org.wise.portal.domain.peergroup.PeerGroup;
import org.wise.portal.domain.peergroup.impl.PeerGroupImpl;
import org.wise.portal.service.WISEServiceTest;

/**
 * @author Hiroki Terashima
 */
@RunWith(EasyMockRunner.class)
public class PeerGroupCreateServiceImplTest extends WISEServiceTest {

  @TestSubject
  private PeerGroupCreateServiceImpl service = new PeerGroupCreateServiceImpl();

  @Mock
  private PeerGroupDao<PeerGroup> peerGroupDao;

  PeerGroupServiceTestHelper testHelper;

  @Before
  public void setUp() throws Exception {
    super.setUp();
    testHelper = new PeerGroupServiceTestHelper(run1, run1Component2);
  }

  @Test
  public void create_ReturnPeerGroupWithNoMembers() {
    peerGroupDao.save(isA(PeerGroupImpl.class));
    expectLastCall();
    replay(peerGroupDao);
    PeerGroup peerGroup = service.create(testHelper.activity, run1Period1);
    assertEquals(0, peerGroup.getMembers().size());
    verify(peerGroupDao);
  }
}
