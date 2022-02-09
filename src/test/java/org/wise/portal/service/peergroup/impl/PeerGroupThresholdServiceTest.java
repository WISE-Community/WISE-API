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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

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
  public void isCompletionThresholdSatisfied_LessThan2Complete_ReturnFalse() {
    expectCompletionCountOne();
    expectThreeWorkgroupsInPeriod();
    replayAll();
    assertFalse(service.isCompletionThresholdSatisfied(activity, run1Period1));
    verifyAll();
  }

  @Test
  public void isCompletionThresholdSatisfied_ThresholdCountAndPercentNotMet_ReturnFalse() {
    expectCompletionCountTwo();
    expectFiveWorkgroupsInPeriod();
    replayAll();
    assertFalse(service.isCompletionThresholdSatisfied(activity, run1Period1));
    verifyAll();
  }

  @Test
  public void isCompletionThresholdSatisfied_AllThresholdsSatisfied_ReturnTrue() {
    expectCompletionCountTwo();
    expectThreeWorkgroupsInPeriod();
    replayAll();
    assertTrue(service.isCompletionThresholdSatisfied(activity, run1Period1));
    verifyAll();
  }

  @Test
  public void canCreatePeerGroup_OneWorkgroupNotInPeerGroup_ReturnTrue() {
    expectTwoWorkgroupsInPeerGroup();
    expectThreeWorkgroupsInPeriod();
    expectTwoWorkgroupsCompletedLogicComponent();
    replayAll();
    assertTrue(service.canCreatePeerGroup(activity, run1Period1));
    verifyAll();
  }

  @Test
  public void canCreatePeerGroup_TwoCompletedWorkgroupsButNotInPeerGroup_ReturnTrue() {
    expectNoWorkgroupsInPeerGroup();
    expectThreeWorkgroupsInPeriod();
    expectTwoWorkgroupsCompletedLogicComponent();
    replayAll();
    assertTrue(service.canCreatePeerGroup(activity, run1Period1));
    verifyAll();
  }

  @Test
  public void canCreatePeerGroup_NoThresholdMet_ReturnFalse() {
    expectNoWorkgroupsInPeerGroup();
    expectThreeWorkgroupsInPeriod();
    expectOneWorkgroupsCompletedLogicComponent();
    replayAll();
    assertFalse(service.canCreatePeerGroup(activity, run1Period1));
    verifyAll();
  }

  private void expectNoWorkgroupsInPeerGroup() {
    expect(peerGroupDao.getListByActivity(activity)).andReturn(Arrays.asList());
  }

  private void expectTwoWorkgroupsInPeerGroup() {
    expect(peerGroupDao.getListByActivity(activity)).andReturn(Arrays.asList(peerGroup1));
  }

  private void expectOneWorkgroupsCompletedLogicComponent() {
    expectWorkForLogicComponent(createStudentWorkList(componentWorkSubmit1));
  }

  private void expectTwoWorkgroupsCompletedLogicComponent() {
    expectWorkForLogicComponent(createStudentWorkList(componentWorkSubmit1, componentWorkSubmit2,
        componentWorkNonSubmit1));
  }

  private void expectWorkForLogicComponent(List<StudentWork> expectedWork) {
    expect(studentWorkDao.getWorkForComponentByPeriod(run1, run1Period1, run1Node1Id,
        run1Component1Id)).andReturn(expectedWork);
  }

  private void expectThreeWorkgroupsInPeriod() {
    expect(runService.getWorkgroups(run1Id, run1Period1.getId())).andReturn(Arrays.asList(
        run1Workgroup1, run1Workgroup2, run1Workgroup3));
  }

  private void expectFiveWorkgroupsInPeriod() {
    expect(runService.getWorkgroups(run1Id, run1Period1.getId())).andReturn(Arrays.asList(
        run1Workgroup1, run1Workgroup2, run1Workgroup3, run1Workgroup4, run1Workgroup5));
  }

  private void expectCompletionCountOne() {
    expect(studentWorkDao.getWorkForComponentByPeriod(run1, run1Period1, run1Node1Id,
        run1Component1Id)).andReturn(createStudentWorkList(componentWorkSubmit1));
  }

  private void expectCompletionCountTwo() {
    expect(studentWorkDao.getWorkForComponentByPeriod(run1, run1Period1, run1Node1Id,
        run1Component1Id)).andReturn(createStudentWorkList(componentWorkSubmit1,
        componentWorkSubmit2, componentWorkNonSubmit1));
  }

  private void verifyAll() {
    verify(peerGroupDao, runService, studentWorkDao);
  }

  private void replayAll() {
    replay(peerGroupDao, runService, studentWorkDao);
  }
}
