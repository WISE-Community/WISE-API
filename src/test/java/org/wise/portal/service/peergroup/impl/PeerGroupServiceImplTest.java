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
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.isA;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import org.easymock.EasyMockRunner;
import org.easymock.Mock;
import org.easymock.TestSubject;
import org.json.JSONException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wise.portal.dao.peergroup.PeerGroupDao;
import org.wise.portal.dao.peergrouping.PeerGroupingDao;
import org.wise.portal.dao.run.RunDao;
import org.wise.portal.dao.work.StudentWorkDao;
import org.wise.portal.domain.peergroup.PeerGroup;
import org.wise.portal.domain.peergroup.impl.PeerGroupImpl;
import org.wise.portal.domain.peergrouping.PeerGrouping;
import org.wise.portal.domain.run.impl.RunImpl;
import org.wise.portal.domain.workgroup.Workgroup;
import org.wise.portal.service.peergroup.PeerGroupCreationException;
import org.wise.portal.service.peergroup.PeerGroupThresholdService;
import org.wise.portal.service.run.RunService;
import org.wise.vle.domain.work.StudentWork;

/**
 * @author Hiroki Terashima
 */
@RunWith(EasyMockRunner.class)
public class PeerGroupServiceImplTest extends PeerGroupServiceTest {

  @TestSubject
  private PeerGroupServiceImpl service = new PeerGroupServiceImpl();

  @Mock
  private PeerGroupThresholdService peerGroupThresholdService;

  @Mock
  private PeerGroupDao<PeerGroup> peerGroupDao;

  @Mock
  private PeerGroupingDao<PeerGrouping> peerGroupingDao;

  @Mock
  private RunDao<RunImpl> runDao;

  @Mock
  private RunService runService;

  @Mock
  private StudentWorkDao<StudentWork> studentWorkDao;

  @Test
  public void getPeerGroup_PeerGroupInDB_ReturnPeerGroup() throws Exception {
    expectPeerGroupFromDB(peerGroup1);
    replayAll();
    assertNotNull(service.getPeerGroup(run1Workgroup1, peerGrouping));
    verifyAll();
  }

  @Test
  public void getPeerGroup_WorkgroupCountThresholdNotSatisfied_ReturnNull() throws Exception {
    expectPeerGroupFromDB(null);
    expectWorkgroupCountThresholdSatisfied(false);
    replayAll();
    assertNull(service.getPeerGroup(run1Workgroup1, peerGrouping));
    verifyAll();
  }

  @Test(timeout = 250)
  public void getPeerGroup_AllThresholdsSatisfied3WorkgroupsLeft_Create3WorkgroupPeerGroup()
      throws Exception {
    expectAllThresholdsSatisfied();
    expectWorkgroupsInPeerGroup(Arrays.asList());
    expectIsLastOnesLeftToPair();
    peerGroupDao.save(isA(PeerGroupImpl.class));
    expectLastCall();
    replayAll();
    assertEquals(3, service.getPeerGroup(run1Workgroup1, peerGrouping).getMembers().size());
    verifyAll();
  }

  @Test(timeout = 250)
  public void getPeerGroup_AllThresholdsSatisfiedMoreThan3WorkgroupsLeft_Create2WorkgroupPeerGroup()
      throws Exception {
    expectAllThresholdsSatisfied();
    expectWorkgroupsInPeerGroup(Arrays.asList());
    expectIsMultiplePairingsLeft();
    peerGroupDao.save(isA(PeerGroupImpl.class));
    expectLastCall();
    replayAll();
    assertEquals(2, service.getPeerGroup(run1Workgroup1, peerGrouping).getMembers().size());
    verifyAll();
  }

  @Test
  public void getPeerGroup_ManualLogicPeerGroupNotExist_ReturnNull() throws Exception {
    expectPeerGroupFromDB(null, manualPeerGrouping);
    replayAll();
    assertNull(service.getPeerGroup(run1Workgroup1, manualPeerGrouping));
    verifyAll();
  }

  @Test
  public void getPeerGroup_ManualLogicPeerGroupExists_ReturnPeerGroup() throws Exception {
    PeerGroup peerGroup = new PeerGroupImpl(manualPeerGrouping, run1Period1,
        new HashSet<Workgroup>(Arrays.asList(run1Workgroup1, run1Workgroup2)));
    expectPeerGroupFromDB(peerGroup, manualPeerGrouping);
    replayAll();
    assertEquals(peerGroup, service.getPeerGroup(run1Workgroup1, manualPeerGrouping));
    verifyAll();
  }

  @Test
  public void getPeerGroups_ReturnPeerGroupList() {
    expectGetPeerGroupsByPeerGrouping();
    replayAll();
    assertEquals(1, service.getPeerGroups(peerGrouping).size());
    verifyAll();
  }

  @Test
  public void getStudentWork_SpecificNodeIdComponentId_ReturnStudentWorkList() {
    StudentWork studentWork1 = createComponentWork(run1Workgroup1, run1Node1Id, run1Component1Id,
        true);
    StudentWork studentWork2 = createComponentWork(run1Workgroup2, run1Node1Id, run1Component1Id,
        true);
    expectGetWorkForComponent(peerGroup1, run1Node1Id, run1Component1Id,
        createStudentWorkList(studentWork1, studentWork2));
    expectGetWorkForComponent(peerGroup1, run1Node2Id, run1Component2Id,
        createStudentWorkList());
    replayAll();
    assertEquals(2, service.getStudentWork(peerGroup1, run1Node1Id, run1Component1Id).size());
    assertEquals(0, service.getStudentWork(peerGroup1, run1Node2Id, run1Component2Id).size());
    verifyAll();
  }

  private void expectGetPeerGroupsByPeerGrouping() {
    expect(peerGroupDao.getListByPeerGrouping(peerGrouping)).andReturn(peerGroups);
  }

  private void expectAllThresholdsSatisfied() throws JSONException {
    expectPeerGroupFromDB(null);
    expectWorkgroupCountThresholdSatisfied(true);
  }

  private void expectGetWorkForComponent(PeerGroup peerGroup, String nodeId, String componentId,
      List<StudentWork> studentWorkList) {
    expect(studentWorkDao.getStudentWork(peerGroup, nodeId, componentId))
        .andReturn(studentWorkList);
  }

  private void expectWorkgroupsInPeerGroup(List<Object> asList) {
    expect(peerGroupDao.getWorkgroupsInPeerGroup(peerGrouping, run1Period1)).andReturn(Arrays.asList());
  }

  private void expectWorkgroupCountThresholdSatisfied(boolean isSatisfied) {
    expect(peerGroupThresholdService.canCreatePeerGroup(peerGrouping, run1Period1))
        .andReturn(isSatisfied);
  }

  private void expectWorkForLogicComponent(List<StudentWork> workForLogicComponent) {
    expect(studentWorkDao.getWorkForComponentByPeriod(run1, run1Period1, run1Node1Id,
        run1Component1Id)).andReturn(workForLogicComponent);
  }

  private void expectPeerGroupFromDB(PeerGroup peerGroup) {
    expectPeerGroupFromDB(peerGroup, peerGrouping);
  }

  private void expectPeerGroupFromDB(PeerGroup peerGroup, PeerGrouping peerGrouping) {
    expect(peerGroupDao.getByWorkgroupAndPeerGrouping(run1Workgroup1, peerGrouping))
      .andReturn(peerGroup);
  }

  private void expectWorkForComponentByWorkgroup(List<StudentWork> expectedWork)
      throws JSONException {
    expect(studentWorkDao.getWorkForComponentByWorkgroup(run1Workgroup1,
        peerGrouping.getLogicNodeId(), peerGrouping.getLogicComponentId())).andReturn(expectedWork);
  }

  private void expectIsLastOnesLeftToPair() {
    expect(runDao.getWorkgroupsForRunAndPeriod(run1Id, run1Period1.getId()))
        .andReturn(Arrays.asList(run1Workgroup1, run1Workgroup2, run1Workgroup3));
  }

  private void expectIsMultiplePairingsLeft() {
    expect(runDao.getWorkgroupsForRunAndPeriod(run1Id, run1Period1.getId()))
        .andReturn(Arrays.asList(run1Workgroup1, run1Workgroup2, run1Workgroup3, run1Workgroup4));
  }

  private void verifyAll() {
    verify(peerGroupDao, peerGroupThresholdService, runDao, runService, studentWorkDao);
  }

  private void replayAll() {
    replay(peerGroupDao, peerGroupThresholdService, runDao, runService, studentWorkDao);
  }
}
