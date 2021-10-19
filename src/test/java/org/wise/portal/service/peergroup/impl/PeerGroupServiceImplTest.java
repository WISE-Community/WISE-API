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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.easymock.EasyMockRunner;
import org.easymock.Mock;
import org.easymock.TestSubject;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wise.portal.dao.peergroup.PeerGroupDao;
import org.wise.portal.dao.peergroupactivity.PeerGroupActivityDao;
import org.wise.portal.dao.work.StudentWorkDao;
import org.wise.portal.domain.peergroup.PeerGroup;
import org.wise.portal.domain.peergroup.impl.PeerGroupImpl;
import org.wise.portal.domain.peergroupactivity.PeerGroupActivity;
import org.wise.portal.domain.peergroupactivity.impl.PeerGroupActivityImpl;
import org.wise.portal.domain.project.impl.ProjectComponent;
import org.wise.portal.domain.workgroup.Workgroup;
import org.wise.portal.service.WISEServiceTest;
import org.wise.portal.service.peergroup.PeerGroupActivityThresholdNotSatisfiedException;
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
  private PeerGroupDao<PeerGroup> peerGroupDao;

  @Mock
  private PeerGroupActivityDao<PeerGroupActivity> peerGroupActivityDao;

  @Mock
  private StudentWorkDao<StudentWork> studentWorkDao;

  @Mock
  private RunService runService;

  PeerGroupActivity activity;

  PeerGroup peerGroup;

  ProjectComponent peerGroupActivityComponent;

  String logic = "[{\"name\": \"maximizeDifferentIdeas\"," +
      "\"nodeId\": \"" + run1Node1Id + "\", \"componentId\": \"" + run1Component1Id + "\"}]";

  int logicThresholdCount = 2;

  int logicThresholdPercent = 50;

  int maxMembershipCount = 2;

  String peerGroupActivityComponentString =
      "{\"id\":\"" + run1Component2Id + "\"," +
      "\"logic\":" + logic + "," +
      "\"logicThresholdCount\":" + logicThresholdCount + "," +
      "\"logicThresholdPercent\":" + logicThresholdPercent + "," +
      "\"maxMembershipCount\":" + maxMembershipCount + "}";

  @Before
  public void setUp() throws Exception {
    super.setUp();
    activity = createPeerGroupActivity();
    peerGroup = new PeerGroupImpl();
  }

  private PeerGroupActivity createPeerGroupActivity() throws JSONException {
    return new PeerGroupActivityImpl(run1, run1Component2.nodeId,
        new ProjectComponent(new JSONObject(peerGroupActivityComponentString)));
  }

  @Test
  public void getPeerGroup_PeerGroupInDB_ReturnPeerGroup() throws Exception {
    expectPeerGroupFromDB(run1Workgroup1, activity, peerGroup);
    replayAll();
    assertNotNull(service.getPeerGroup(run1Workgroup1, activity));
    verifyAll();
  }

  @Test
  public void getPeerGroup_PeerGroupNotInDBAndNoWorkForLogicComponent_ThrowException()
      throws Exception {
    expectPeerGroupFromDB(run1Workgroup1, activity, null);
    expectWorkForComponentByWorkgroup(run1Workgroup1, activity, Arrays.asList());
    replayAll();
    try {
      service.getPeerGroup(run1Workgroup1, activity);
      fail("PeerGroupActivityThresholdNotSatisfiedException expected, but wasn't thrown");
    } catch (PeerGroupActivityThresholdNotSatisfiedException e) {
    }
    verifyAll();
  }

  @Test
  public void getPeerGroup_PeerGroupNotInDBAndLogicThresholdNotSatisfied_ThrowException()
      throws Exception {
    expectPeerGroupFromDB(run1Workgroup1, activity, null);
    expectWorkForComponentByWorkgroup(run1Workgroup1, activity,
        Arrays.asList(componentWorkSubmit1));
    expect(runService.getWorkgroups(run1Id, run1Period1.getId())).andReturn(period1Workgroups);
    expect(studentWorkDao.getWorkForComponentByPeriod(run1, run1Period1, run1Node1Id,
      run1Component1Id)).andReturn(Arrays.asList());
    replayAll();
    try {
      service.getPeerGroup(run1Workgroup1, activity);
      fail("PeerGroupActivityThresholdNotSatisfiedException expected, but wasn't thrown");
    } catch (PeerGroupActivityThresholdNotSatisfiedException e) {
    }
    verifyAll();
  }

  @Test(timeout = 250)
  public void getPeerGroup_PeerGroupNotInDBAndLogicThresholdSatisfied_CreateAndReturnPeerGroup()
      throws Exception {
    expectPeerGroupFromDB(run1Workgroup1, activity, null);
    expectWorkForComponentByWorkgroup(run1Workgroup1, activity,
        Arrays.asList(componentWorkSubmit1));
    expect(runService.getWorkgroups(run1Id, run1Period1.getId())).andReturn(period1Workgroups);
    expect(peerGroupDao.getWorkgroupsInPeerGroup(activity)).andReturn(Arrays.asList());
    List<StudentWork> workForLogicComponent = createStudentWorkList(componentWorkSubmit1,
        componentWorkSubmit2, componentWorkNonSubmit1);
    expect(studentWorkDao.getWorkForComponentByPeriod(run1, run1Period1, run1Node1Id,
        run1Component1Id)).andReturn(workForLogicComponent).times(2);
    peerGroupDao.save(isA(PeerGroupImpl.class));
    expectLastCall();
    replayAll();
    assertNotNull(service.getPeerGroup(run1Workgroup1, activity));
    verifyAll();
  }

  private List<StudentWork> createStudentWorkList(StudentWork... componentWorks) {
    List<StudentWork> list = new ArrayList<StudentWork>();
    for (StudentWork work : componentWorks) {
      list.add(work);
    }
    return list;
  }

  private void expectPeerGroupFromDB(Workgroup workgroup, PeerGroupActivity activity,
      PeerGroup peerGroup) {
    expect(peerGroupDao.getByWorkgroupAndActivity(workgroup, activity)).andReturn(peerGroup);
  }

  private void expectWorkForComponentByWorkgroup(Workgroup workgroup,
      PeerGroupActivity activity, List<StudentWork> expectedWork) throws JSONException {
    expect(studentWorkDao.getWorkForComponentByWorkgroup(workgroup, activity.getLogicNodeId(),
        activity.getLogicComponentId())).andReturn(expectedWork);
  }

  private void verifyAll() {
    verify(peerGroupDao, runService, studentWorkDao);
  }

  private void replayAll() {
    replay(peerGroupDao, runService, studentWorkDao);
  }
}
