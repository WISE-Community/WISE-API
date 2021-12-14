package org.wise.portal.service.peergroup.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;

import static org.easymock.EasyMock.*;

import org.easymock.EasyMockRunner;
import org.easymock.Mock;
import org.easymock.TestSubject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wise.portal.dao.peergroup.PeerGroupDao;
import org.wise.portal.domain.peergroup.PeerGroup;
import org.wise.portal.domain.peergroup.impl.PeerGroupImpl;

/**
 * @author Hiroki Terashima
 */
@RunWith(EasyMockRunner.class)
public class PeerGroupCreateServiceImplTest extends PeerGroupServiceTest {

  @TestSubject
  private PeerGroupCreateServiceImpl service = new PeerGroupCreateServiceImpl();

  @Mock
  private PeerGroupDao<PeerGroup> peerGroupDao;

  @Test
  public void create_ReturnPeerGroupWithNoMembers() {
    peerGroupDao.save(isA(PeerGroupImpl.class));
    expectLastCall();
    replay(peerGroupDao);
    PeerGroup peerGroup = service.create(activity, run1Period1);
    assertEquals(0, peerGroup.getMembers().size());
    verify(peerGroupDao);
  }
}
