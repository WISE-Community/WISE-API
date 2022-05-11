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
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

import java.util.Arrays;

import org.easymock.EasyMockRunner;
import org.easymock.Mock;
import org.easymock.TestSubject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wise.portal.dao.peergroup.PeerGroupDao;
import org.wise.portal.dao.work.StudentWorkDao;
import org.wise.portal.domain.peergroup.PeerGroup;
import org.wise.portal.service.run.RunService;
import org.wise.vle.domain.work.StudentWork;

/**
 * @author Hiroki Terashima
 */
@RunWith(EasyMockRunner.class)
public class PeerGroupThresholdServiceTest extends PeerGroupServiceTest {

  @TestSubject
  private PeerGroupThresholdServiceImpl service = new PeerGroupThresholdServiceImpl();

  @Mock
  private RunService runService;

  @Mock
  private PeerGroupDao<PeerGroup> peerGroupDao;

  @Mock
  private StudentWorkDao<StudentWork> studentWorkDao;

  @Test
  public void canCreatePeerGroup_OneWorkgroupNotInPeerGroup_ReturnFalse() {
    expectTwoWorkgroupsInPeerGroup();
    expectThreeWorkgroupsInPeriod();
    replayAll();
    assertFalse(service.canCreatePeerGroup(peerGrouping, run1Period1));
    verifyAll();
  }

  @Test
  public void canCreatePeerGroup_NoThresholdMet_ReturnFalse() {
    expectNoWorkgroupsInPeerGroup();
    expectThreeWorkgroupsInPeriod();
    replayAll();
    assertTrue(service.canCreatePeerGroup(peerGrouping, run1Period1));
    verifyAll();
  }

  private void expectNoWorkgroupsInPeerGroup() {
    expect(peerGroupDao.getListByPeerGrouping(peerGrouping)).andReturn(Arrays.asList());
  }

  private void expectTwoWorkgroupsInPeerGroup() {
    expect(peerGroupDao.getListByPeerGrouping(peerGrouping)).andReturn(Arrays.asList(peerGroup1));
  }

  private void expectThreeWorkgroupsInPeriod() {
    expect(runService.getWorkgroups(run1Id, run1Period1.getId())).andReturn(Arrays.asList(
        run1Workgroup1, run1Workgroup2, run1Workgroup3));
  }

  private void verifyAll() {
    verify(peerGroupDao, runService, studentWorkDao);
  }

  private void replayAll() {
    replay(peerGroupDao, runService, studentWorkDao);
  }
}
