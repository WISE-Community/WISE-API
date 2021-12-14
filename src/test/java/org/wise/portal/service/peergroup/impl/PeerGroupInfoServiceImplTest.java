/**
 * Copyright (c) 2008-2021 Regents of the University of California (Regents).
 * Created by WISE, Graduate School of Education, University of California, Berkeley.
 *
 * This software is distributed under the GNU General Public License, v3,
 * or (at your option) any later version.
 *
 * Permission is hereby granted, without written agreement and without license
 * or royalty fees, to use, copy, modify, and distribute this software and its
 * documentation for any purpose, provided that the above copyright notice and
 * the following two paragraphs appear in all copies of this software.
 *
 * REGENTS SPECIFICALLY DISCLAIMS ANY WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE. THE SOFTWARE AND ACCOMPANYING DOCUMENTATION, IF ANY, PROVIDED
 * HEREUNDER IS PROVIDED "AS IS". REGENTS HAS NO OBLIGATION TO PROVIDE
 * MAINTENANCE, SUPPORT, UPDATES, ENHANCEMENTS, OR MODIFICATIONS.
 *
 * IN NO EVENT SHALL REGENTS BE LIABLE TO ANY PARTY FOR DIRECT, INDIRECT,
 * SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES, INCLUDING LOST PROFITS,
 * ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
 * REGENTS HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.wise.portal.service.peergroup.impl;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.Map;

import org.easymock.EasyMockRunner;
import org.easymock.Mock;
import org.easymock.TestSubject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wise.portal.dao.ObjectNotFoundException;
import org.wise.portal.domain.peergroup.PeerGroup;
import org.wise.portal.domain.workgroup.Workgroup;
import org.wise.portal.service.peergroup.PeerGroupService;
import org.wise.portal.service.run.RunService;

/**
 * @author Hiroki Terashima
 */
@SuppressWarnings("unchecked")
@RunWith(EasyMockRunner.class)
public class PeerGroupInfoServiceImplTest extends PeerGroupServiceTest {

  @TestSubject
  private PeerGroupInfoServiceImpl service = new PeerGroupInfoServiceImpl();

  @Mock
  private PeerGroupService peerGroupService;

  @Mock
  private RunService runService;

  @Test
  public void getPeerGroupInfo_ReturnPeerGroupInfo() {
    expectGetPeerGroups();
    expectGetWorkgroups();
    replay(peerGroupService, runService);
    Map<String, Object> peerGroupInfo = service.getPeerGroupInfo(activity);
    assertEquals(1, ((List<PeerGroup>) peerGroupInfo.get("peerGroups")).size());
    assertEquals(3, ((List<Workgroup>) peerGroupInfo.get("workgroupsNotInPeerGroup")).size());
    verify(peerGroupService, runService);
  }

  private void expectGetPeerGroups() {
    expect(peerGroupService.getPeerGroups(activity)).andReturn(peerGroups);
  }

  private void expectGetWorkgroups() {
    try {
      expect(runService.getWorkgroups(run1Id)).andReturn(run1Workgroups);
    } catch (ObjectNotFoundException e) {
    }
  }
}
