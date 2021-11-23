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

import static org.junit.Assert.*;
import static org.easymock.EasyMock.*;

import java.util.Arrays;
import java.util.List;

import org.easymock.EasyMockRunner;
import org.easymock.Mock;
import org.easymock.TestSubject;
import org.json.JSONException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wise.portal.dao.peergroup.PeerGroupDao;
import org.wise.portal.dao.peergroupactivity.PeerGroupActivityDao;
import org.wise.portal.dao.run.RunDao;
import org.wise.portal.dao.work.StudentWorkDao;
import org.wise.portal.domain.peergroup.PeerGroup;
import org.wise.portal.domain.peergroup.impl.PeerGroupImpl;
import org.wise.portal.domain.peergroupactivity.PeerGroupActivity;
import org.wise.portal.domain.run.impl.RunImpl;
import org.wise.portal.service.WISEServiceTest;
import org.wise.portal.service.peergroup.PeerGroupActivityThresholdNotSatisfiedException;
import org.wise.portal.service.peergroup.PeerGroupCreationException;
import org.wise.portal.service.peergroup.PeerGroupThresholdService;
import org.wise.portal.service.run.RunService;
import org.wise.vle.domain.work.StudentWork;

/**
 * @author Hiroki Terashima
 */
@RunWith(EasyMockRunner.class)
public class PeerGroupServiceImplTest extends WISEServiceTest {

  @TestSubject
  private PeerGroupServiceImpl service = new PeerGroupServiceImpl();

  @Mock
  private PeerGroupThresholdService peerGroupThresholdService;

  @Mock
  private PeerGroupDao<PeerGroup> peerGroupDao;

  @Mock
  private PeerGroupActivityDao<PeerGroupActivity> peerGroupActivityDao;

  @Mock
  private RunDao<RunImpl> runDao;

  @Mock
  private RunService runService;

  @Mock
  private StudentWorkDao<StudentWork> studentWorkDao;

  PeerGroupActivity activity;

  PeerGroup peerGroup;

  List<PeerGroup> peerGroups;

  @Before
  public void setUp() throws Exception {
    super.setUp();
    PeerGroupServiceTestHelper testHelper = new PeerGroupServiceTestHelper(run1, run1Component2);
    activity = testHelper.activity;
    peerGroup = testHelper.peerGroup1;
    peerGroups = testHelper.peerGroups;
  }

  @Test
  public void getPeerGroup_PeerGroupInDB_ReturnPeerGroup() throws Exception {
    expectPeerGroupFromDB(peerGroup);
    replayAll();
    assertNotNull(service.getPeerGroup(run1Workgroup1, activity));
    verifyAll();
  }

  @Test
  public void getPeerGroup_NoWorkForLogicComponent_ThrowException()
      throws Exception {
    expectPeerGroupFromDB(null);
    expectWorkForComponentByWorkgroup(Arrays.asList());
    replayAll();
    try {
      service.getPeerGroup(run1Workgroup1, activity);
      fail("PeerGroupActivityThresholdNotSatisfiedException expected, but wasn't thrown");
    } catch (PeerGroupActivityThresholdNotSatisfiedException e) {
    }
    verifyAll();
  }

  @Test
  public void getPeerGroup_CompletionThresholdNotSatisfied_ThrowException()
      throws Exception {
    expectPeerGroupFromDB(null);
    expectWorkForComponentByWorkgroup(Arrays.asList(componentWorkSubmit1));
    expectCompletionThresholdSatisfied(false);
    replayAll();
    try {
      service.getPeerGroup(run1Workgroup1, activity);
      fail("PeerGroupActivityThresholdNotSatisfiedException expected, but wasn't thrown");
    } catch (PeerGroupActivityThresholdNotSatisfiedException e) {
    }
    verifyAll();
  }

  @Test
  public void getPeerGroup_WorkgroupCountThresholdNotSatisfied_ThrowException()
      throws Exception {
    expectPeerGroupFromDB(null);
    expectWorkForComponentByWorkgroup(Arrays.asList(componentWorkSubmit1));
    expectCompletionThresholdSatisfied(true);
    expectWorkgroupCountThresholdSatisfied(false);
    replayAll();
    try {
      service.getPeerGroup(run1Workgroup1, activity);
      fail("PeerGroupCreationException expected, but wasn't thrown");
    } catch (PeerGroupCreationException e) {
    }
    verifyAll();
  }

  @Test(timeout = 250)
  public void getPeerGroup_AllThresholdsSatisfied3WorkgroupsLeft_Create3WorkgroupPeerGroup()
      throws Exception {
    expectAllThresholdsSatisfied();
    expectWorkgroupsInPeerGroup(Arrays.asList());
    expectWorkForLogicComponent(createStudentWorkList(componentWorkSubmit1, componentWorkSubmit2,
        componentWorkSubmit3, componentWorkNonSubmit1));
    expectIsLastOnesLeftToPair();
    peerGroupDao.save(isA(PeerGroupImpl.class));
    expectLastCall();
    replayAll();
    assertEquals(3, service.getPeerGroup(run1Workgroup1, activity).getMembers().size());
    verifyAll();
  }

  @Test(timeout = 250)
  public void getPeerGroup_AllThresholdsSatisfiedMoreThan3WorkgroupsLeft_Create2WorkgroupPeerGroup()
      throws Exception {
    expectAllThresholdsSatisfied();
    expectWorkgroupsInPeerGroup(Arrays.asList());
    expectWorkForLogicComponent(createStudentWorkList(componentWorkSubmit1, componentWorkSubmit2,
        componentWorkSubmit3, componentWorkSubmit4, componentWorkNonSubmit1));
    expectIsMultiplePairingsLeft();
    peerGroupDao.save(isA(PeerGroupImpl.class));
    expectLastCall();
    replayAll();
    assertEquals(2, service.getPeerGroup(run1Workgroup1, activity).getMembers().size());
    verifyAll();
  }

  @Test
  public void getStudentWork_PeerGroupExist_ReturnStudentWorkList() {
    expectGetWorkForComponentByWorkgroups();
    replayAll();
    assertEquals(3, service.getStudentWork(peerGroup).size());
    verifyAll();
  }

  @Test
  public void getPeerGroups_ReturnPeerGroupList() {
    expectGetPeerGroupsByActivity();
    replayAll();
    assertEquals(1, service.getPeerGroups(activity).size());
    verifyAll();
  }

  private void expectGetPeerGroupsByActivity() {
    expect(peerGroupDao.getListByActivity(activity)).andReturn(peerGroups);
  }

  private void expectAllThresholdsSatisfied() throws JSONException {
    expectPeerGroupFromDB(null);
    expectWorkForComponentByWorkgroup(Arrays.asList(componentWorkSubmit1));
    expectCompletionThresholdSatisfied(true);
    expectWorkgroupCountThresholdSatisfied(true);
  }

  private void expectGetWorkForComponentByWorkgroups() {
    expect(studentWorkDao.getWorkForComponentByWorkgroups(peerGroup.getMembers(),
        peerGroup.getPeerGroupActivity().getNodeId(),
        peerGroup.getPeerGroupActivity().getComponentId())).andReturn(
        createStudentWorkList(componentWorkSubmit1, componentWorkSubmit2,
        componentWorkNonSubmit1));
  }

  private void expectWorkgroupsInPeerGroup(List<Object> asList) {
    expect(peerGroupDao.getWorkgroupsInPeerGroup(activity, run1Period1)).andReturn(Arrays.asList());
  }

  private void expectWorkgroupCountThresholdSatisfied(boolean isSatisfied) {
    expect(peerGroupThresholdService.canCreatePeerGroup(activity, run1Period1))
        .andReturn(isSatisfied);
  }

  private void expectCompletionThresholdSatisfied(boolean isSatisfied) {
    expect(peerGroupThresholdService.isCompletionThresholdSatisfied(activity, run1Period1))
        .andReturn(isSatisfied);
  }

  private void expectWorkForLogicComponent(List<StudentWork> workForLogicComponent) {
    expect(studentWorkDao.getWorkForComponentByPeriod(run1, run1Period1, run1Node1Id,
        run1Component1Id)).andReturn(workForLogicComponent);
  }

  private void expectPeerGroupFromDB(PeerGroup peerGroup) {
    expect(peerGroupDao.getByWorkgroupAndActivity(run1Workgroup1, activity)).andReturn(peerGroup);
  }

  private void expectWorkForComponentByWorkgroup(List<StudentWork> expectedWork)
      throws JSONException {
    expect(studentWorkDao.getWorkForComponentByWorkgroup(run1Workgroup1, activity.getLogicNodeId(),
        activity.getLogicComponentId())).andReturn(expectedWork);
  }

  private void expectIsLastOnesLeftToPair() {
    expect(runDao.getWorkgroupsForRunAndPeriod(run1Id, run1Period1.getId())).andReturn(
        Arrays.asList(run1Workgroup1, run1Workgroup2, run1Workgroup3));
  }

  private void expectIsMultiplePairingsLeft() {
    expect(runDao.getWorkgroupsForRunAndPeriod(run1Id, run1Period1.getId())).andReturn(
        Arrays.asList(run1Workgroup1, run1Workgroup2, run1Workgroup3, run1Workgroup4));
  }

  private void verifyAll() {
    verify(peerGroupDao, peerGroupThresholdService, runDao, runService, studentWorkDao);
  }

  private void replayAll() {
    replay(peerGroupDao, peerGroupThresholdService, runDao, runService, studentWorkDao);
  }
}
