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
package org.wise.portal.dao.work.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.wise.portal.dao.WISEHibernateTest;
import org.wise.portal.dao.work.StudentWorkDao;
import org.wise.portal.domain.peergroup.PeerGroup;
import org.wise.portal.domain.peergroup.impl.PeerGroupImpl;
import org.wise.portal.domain.peergrouping.PeerGrouping;
import org.wise.portal.domain.peergrouping.impl.PeerGroupingImpl;
import org.wise.portal.domain.workgroup.Workgroup;
import org.wise.vle.domain.work.StudentWork;

/**
 * @author Geoffrey Kwan
 * @author Hiroki Terashima
 */
@SpringBootTest
@RunWith(SpringRunner.class)
public class HibernateStudentWorkDaoTest extends WISEHibernateTest {

  private final String DUMMY_STUDENT_WORK1 = "Dummy Student Work 1";
  private final String DUMMY_STUDENT_WORK2 = "Dummy Student Work 2";
  private final String DUMMY_STUDENT_WORK3 = "Dummy Student Work 3";
  private final String DUMMY_STUDENT_WORK4 = "Dummy Student Work 4";

  @Autowired
  private StudentWorkDao<StudentWork> studentWorkDao;

  protected PeerGrouping peerGrouping;
  protected PeerGroup peerGroup1, peerGroup2;

  @Before
  public void setUp() throws Exception {
    super.setUp();
    createPeerGrouping();
    createPeerGroups();
    createStudentWork(workgroup1, peerGroup1, NODE_ID1, COMPONENT_ID1, DUMMY_STUDENT_WORK1);
    createStudentWork(workgroup1, peerGroup1, NODE_ID2, COMPONENT_ID2, DUMMY_STUDENT_WORK2);
    createStudentWork(workgroup2, peerGroup1, NODE_ID1, COMPONENT_ID1, DUMMY_STUDENT_WORK3);
    createStudentWork(workgroup3, peerGroup2, NODE_ID1, COMPONENT_ID1, DUMMY_STUDENT_WORK4);
  }

  @Test
  public void getStudentWorkListByParams_ByRun_ShouldReturnStudentWorkByRun() {
    assertEquals(3,
        studentWorkDao
            .getStudentWorkListByParams(null, run1, null, null, null, null, null, null, null, null)
            .size());
    assertEquals(1,
        studentWorkDao
            .getStudentWorkListByParams(null, run2, null, null, null, null, null, null, null, null)
            .size());
    assertEquals(0,
        studentWorkDao
            .getStudentWorkListByParams(null, run3, null, null, null, null, null, null, null, null)
            .size());
  }

  @Test
  public void getStudentWorkListByParams_ByWorkgroup_ShouldReturnStudentWork() {
    List<StudentWork> studentWorkList = studentWorkDao.getStudentWorkListByParams(null, run1, null,
        workgroup1, null, null, null, null, null, null);
    assertEquals(2, studentWorkList.size());
    assertEquals(DUMMY_STUDENT_WORK1, getStudentData(studentWorkList, 0));
    assertEquals(DUMMY_STUDENT_WORK2, getStudentData(studentWorkList, 1));
  }

  @Test
  public void getStudentWorkListByParams_ByNodeId_ShouldReturnStudentWork() {
    List<StudentWork> studentWorkList = studentWorkDao.getStudentWorkListByParams(null, run1, null,
        null, null, null, NODE_ID1, null, null, null);
    assertEquals(2, studentWorkList.size());
    assertEquals(DUMMY_STUDENT_WORK1, getStudentData(studentWorkList, 0));
    assertEquals(DUMMY_STUDENT_WORK3, getStudentData(studentWorkList, 1));
  }

  @Test
  public void getStudentWorkListByParams_WithRunThatHasNoStudentWork_ShouldReturnNoStudentWork() {
    List<StudentWork> studentWorkList = studentWorkDao.getStudentWorkListByParams(null, run3, null,
        null, null, null, null, null, null, null);
    assertEquals(0, studentWorkList.size());
  }

  @Test
  public void getStudentWorkListByParams_WithRunThatHasStudentWork_ShouldReturnStudentWork() {
    List<StudentWork> studentWorkList = studentWorkDao.getStudentWorkListByParams(null, run1, null,
        null, null, null, null, null, null, null);
    assertEquals(3, studentWorkList.size());
    assertEquals(DUMMY_STUDENT_WORK1, getStudentData(studentWorkList, 0));
    assertEquals(DUMMY_STUDENT_WORK2, getStudentData(studentWorkList, 1));
    assertEquals(DUMMY_STUDENT_WORK3, getStudentData(studentWorkList, 2));
  }

  @Test
  public void getWorkForComponentByPeriod_ShouldReturnStudentWork() {
    List<StudentWork> studentWorkList = studentWorkDao.getWorkForComponentByPeriod(run1,
        run1Period1, NODE_ID1, COMPONENT_ID1);
    assertEquals(2, studentWorkList.size());
    assertEquals(DUMMY_STUDENT_WORK1, getStudentData(studentWorkList, 0));
    assertEquals(DUMMY_STUDENT_WORK3, getStudentData(studentWorkList, 1));
  }

  @Test
  public void getWorkForComponentByWorkgroup_ShouldReturnStudentWork() {
    List<StudentWork> studentWorkList = studentWorkDao.getWorkForComponentByWorkgroup(workgroup1,
        NODE_ID1, COMPONENT_ID1);
    assertEquals(1, studentWorkList.size());
    assertEquals(DUMMY_STUDENT_WORK1, getStudentData(studentWorkList, 0));
  }

  @Test
  public void getWorkForComponentByPeerGroup_ShouldReturnStudentWork() {
    List<StudentWork> studentWorkList = studentWorkDao.getStudentWork(peerGroup1, NODE_ID1,
        COMPONENT_ID1);
    assertEquals(2, studentWorkList.size());
    assertEquals(DUMMY_STUDENT_WORK1, getStudentData(studentWorkList, 0));
    assertEquals(DUMMY_STUDENT_WORK3, getStudentData(studentWorkList, 1));
  }

  @Test
  public void getStudentWork_WorkExists_ShouldReturnWork() {
    List<StudentWork> studentWorkList = studentWorkDao.getStudentWork(run1, run1Period1, NODE_ID1,
        COMPONENT_ID1);
    assertEquals(2, studentWorkList.size());
    assertEquals(DUMMY_STUDENT_WORK1, getStudentData(studentWorkList, 0));
    assertEquals(DUMMY_STUDENT_WORK3, getStudentData(studentWorkList, 1));
  }

  @Test
  public void getStudentWork_WorkDoesNotExist_ShouldReturnEmptyList() {
    List<StudentWork> studentWorkList = studentWorkDao.getStudentWork(run1, run1Period1, NODE_ID1,
        COMPONENT_ID2);
    assertEquals(0, studentWorkList.size());
  }

  @Test
  public void getStudentWork_FromAllPeriods_ShouldReturnWork() {
    List<StudentWork> studentWorkList = studentWorkDao.getStudentWork(run1, null, NODE_ID1,
        COMPONENT_ID1);
    assertEquals(2, studentWorkList.size());
    assertEquals(DUMMY_STUDENT_WORK1, getStudentData(studentWorkList, 0));
    assertEquals(DUMMY_STUDENT_WORK3, getStudentData(studentWorkList, 1));
  }

  private StudentWork createStudentWork(Workgroup workgroup, PeerGroup peerGroup, String nodeId,
      String componentId, String studentData) {
    StudentWork studentWork = new StudentWork();
    Calendar now = Calendar.getInstance();
    Timestamp timestamp = new Timestamp(now.getTimeInMillis());
    studentWork.setClientSaveTime(timestamp);
    studentWork.setServerSaveTime(timestamp);
    studentWork.setRun(workgroup.getRun());
    studentWork.setPeriod(workgroup.getPeriod());
    studentWork.setNodeId(nodeId);
    studentWork.setComponentId(componentId);
    studentWork.setWorkgroup(workgroup);
    studentWork.setIsAutoSave(false);
    studentWork.setIsSubmit(false);
    studentWork.setStudentData(studentData);
    studentWork.setPeerGroup(peerGroup);
    studentWorkDao.save(studentWork);
    return studentWork;
  }

  private String getStudentData(List<StudentWork> studentWorkList, Integer index) {
    return studentWorkList.get(index).getStudentData();
  }

  private void createPeerGrouping() {
    peerGrouping = new PeerGroupingImpl();
    peerGrouping.setRun(run1);
    peerGrouping.setTag("tag1");
    savePeerGrouping(peerGrouping);
  }

  private void createPeerGroups() {
    peerGroup1 = new PeerGroupImpl();
    peerGroup1.setPeerGrouping(peerGrouping);
    peerGroup1.addMember(workgroup1);
    peerGroup1.addMember(workgroup2);
    savePeerGroup(peerGroup1);
    peerGroup2 = new PeerGroupImpl();
    peerGroup2.setPeerGrouping(peerGrouping);
    peerGroup2.addMember(workgroup3);
    savePeerGroup(peerGroup2);
  }
}
