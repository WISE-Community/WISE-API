/**
 * Copyright (c) 2008-2019 Regents of the University of California (Regents).
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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.wise.portal.dao.WISEHibernateTest;
import org.wise.portal.dao.work.StudentWorkDao;
import org.wise.portal.domain.workgroup.Workgroup;
import org.wise.vle.domain.work.StudentWork;

/**
 * @author Geoffrey Kwan
 */
@SpringBootTest
@RunWith(SpringRunner.class)
public class HibernateStudentWorkDaoTest extends WISEHibernateTest {

  private final String DUMMY_STUDENT_WORK1 = "Dummy Student Work 1";
  private final String DUMMY_STUDENT_WORK2 = "Dummy Student Work 2";
  private final String DUMMY_STUDENT_WORK3 = "Dummy Student Work 3";

  @Autowired
  private StudentWorkDao<StudentWork> studentWorkDao;

  @Test
  public void getStudentWorkListByParams_WithRunThatHasNoStudentWork_ShouldReturnNoStudentWork() {
    List<StudentWork> studentWorkList = studentWorkDao.getStudentWorkListByParams(null, run1, null,
        null, null, null, null, null, null, null);
    assertEquals(0, studentWorkList.size());
  }

  @Test
  public void getStudentWorkListByParams_WithRunThatHasStudentWork_ShouldReturnStudentWork() {
    createStudentWork(workgroup1, NODE_ID1, COMPONENT_ID1, DUMMY_STUDENT_WORK1);
    createStudentWork(workgroup1, NODE_ID2, COMPONENT_ID2, DUMMY_STUDENT_WORK2);
    createStudentWork(workgroup2, NODE_ID1, COMPONENT_ID1, DUMMY_STUDENT_WORK3);
    List<StudentWork> studentWorkList = studentWorkDao.getStudentWorkListByParams(null, run1, null,
        null, null, null, null, null, null, null);
    assertEquals(3, studentWorkList.size());
    assertEquals(DUMMY_STUDENT_WORK1, studentWorkList.get(0).getStudentData());
    assertEquals(DUMMY_STUDENT_WORK2, studentWorkList.get(1).getStudentData());
    assertEquals(DUMMY_STUDENT_WORK3, studentWorkList.get(2).getStudentData());
  }

  @Test
  public void getStudentWorkListByParams_ByWorkgroup_ShouldReturnStudentWork() {
    createStudentWork(workgroup1, NODE_ID1, COMPONENT_ID1, DUMMY_STUDENT_WORK1);
    createStudentWork(workgroup1, NODE_ID2, COMPONENT_ID2, DUMMY_STUDENT_WORK2);
    createStudentWork(workgroup2, NODE_ID1, COMPONENT_ID1, DUMMY_STUDENT_WORK3);
    List<StudentWork> studentWorkList = studentWorkDao.getStudentWorkListByParams(null, run1, null,
        workgroup1, null, null, null, null, null, null);
    assertEquals(2, studentWorkList.size());
    assertEquals(DUMMY_STUDENT_WORK1, studentWorkList.get(0).getStudentData());
    assertEquals(DUMMY_STUDENT_WORK2, studentWorkList.get(1).getStudentData());
  }

  @Test
  public void getStudentWorkListByParams_ByNodeId_ShouldReturnStudentWork() {
    createStudentWork(workgroup1, NODE_ID1, COMPONENT_ID1, DUMMY_STUDENT_WORK1);
    createStudentWork(workgroup1, NODE_ID2, COMPONENT_ID2, DUMMY_STUDENT_WORK2);
    createStudentWork(workgroup2, NODE_ID1, COMPONENT_ID1, DUMMY_STUDENT_WORK3);
    List<StudentWork> studentWorkList = studentWorkDao.getStudentWorkListByParams(null, run1, null,
        null, null, null, NODE_ID1, null, null, null);
    assertEquals(2, studentWorkList.size());
    assertEquals(DUMMY_STUDENT_WORK1, studentWorkList.get(0).getStudentData());
    assertEquals(DUMMY_STUDENT_WORK3, studentWorkList.get(1).getStudentData());
  }

  @Test
  public void getStudentWork_WorkExists_ShouldReturnWork() {
    StudentWork studentWork = createStudentWork(workgroup1, NODE_ID1, COMPONENT_ID1,
        DUMMY_STUDENT_WORK1);
    List<StudentWork> studentWorkList = studentWorkDao.getStudentWork(run1, run1Period1, NODE_ID1,
        COMPONENT_ID1);
    assertEquals(studentWorkList.size(), 1);
    assertEquals(studentWorkList.get(0), studentWork);
  }

  @Test
  public void getStudentWork_WorkDoesNotExist_ShouldReturnEmptyList() {
    createStudentWork(workgroup1, NODE_ID1, COMPONENT_ID1, DUMMY_STUDENT_WORK1);
    List<StudentWork> studentWorkList = studentWorkDao.getStudentWork(run1, run1Period1, NODE_ID2,
        COMPONENT_ID2);
    assertEquals(studentWorkList.size(), 0);
  }

  @Test
  public void getStudentWork_FromAllPeriods_ShouldReturnWork() {
    StudentWork studentWork1 = createStudentWork(workgroup1, NODE_ID1, COMPONENT_ID1,
        DUMMY_STUDENT_WORK1);
    addUserToRun(student4, run1, run1Period2);
    Workgroup workgroup4 = addUserToRun(student4, run1, run1Period2);
    StudentWork studentWork2 = createStudentWork(workgroup4, NODE_ID1, COMPONENT_ID1,
        DUMMY_STUDENT_WORK1);
    List<StudentWork> studentWorkList = studentWorkDao.getStudentWork(run1, null, NODE_ID1,
        COMPONENT_ID1);
    assertEquals(studentWorkList.size(), 2);
    assertTrue(studentWorkList.contains(studentWork1));
    assertTrue(studentWorkList.contains(studentWork2));
  }

  private StudentWork createStudentWork(Workgroup workgroup, String nodeId, String componentId,
      String studentData) {
    StudentWork studentWork = new StudentWork();
    Calendar now = Calendar.getInstance();
    Timestamp timestamp = new Timestamp(now.getTimeInMillis());
    studentWork.setClientSaveTime(timestamp);
    studentWork.setServerSaveTime(timestamp);
    studentWork.setRun(run1);
    studentWork.setPeriod(run1Period1);
    studentWork.setNodeId(nodeId);
    studentWork.setComponentId(componentId);
    studentWork.setWorkgroup(workgroup);
    studentWork.setIsAutoSave(false);
    studentWork.setIsSubmit(false);
    studentWork.setStudentData(studentData);
    studentWorkDao.save(studentWork);
    return studentWork;
  }

}
