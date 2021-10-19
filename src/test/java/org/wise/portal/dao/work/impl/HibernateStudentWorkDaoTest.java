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
import org.wise.portal.domain.workgroup.Workgroup;
import org.wise.vle.domain.work.StudentWork;

/**
 * @author Geoffrey Kwan
 * @author Hiroki Terashima
 */
@SpringBootTest
@RunWith(SpringRunner.class)
public class HibernateStudentWorkDaoTest extends WISEHibernateTest {

  @Autowired
  private StudentWorkDao<StudentWork> studentWorkDao;

  @Before
  public void setUp() throws Exception {
    super.setUp();
    createStudentWork(workgroup1, "node1", "component1", "studentWork1");
    createStudentWork(workgroup1, "node2", "component2", "studentWork2");
    createStudentWork(workgroup2, "node1", "component1", "studentWork3");
    createStudentWork(workgroup3, "node1", "component1", "studentWork4");
  }

  @Test
  public void getStudentWorkListByParams_ByRun_ShouldReturnStudentWorkByRun() {
    assertEquals(3, studentWorkDao.getStudentWorkListByParams(null, run1, null, null, null, null,
        null, null, null, null).size());
    assertEquals(1, studentWorkDao.getStudentWorkListByParams(null, run2, null, null, null, null,
        null, null, null, null).size());
    assertEquals(0, studentWorkDao.getStudentWorkListByParams(null, run3, null, null, null, null,
        null, null, null, null).size());
  }

  @Test
  public void getStudentWorkListByParams_ByWorkgroup_ShouldReturnStudentWork() {
    List<StudentWork> studentWorkList = studentWorkDao.getStudentWorkListByParams(null, run1, null,
        workgroup1, null, null, null, null, null, null);
    assertEquals(2, studentWorkList.size());
    assertEquals("studentWork1", studentWorkList.get(0).getStudentData());
    assertEquals("studentWork2", studentWorkList.get(1).getStudentData());
  }

  @Test
  public void getStudentWorkListByParams_ByNodeId_ShouldReturnStudentWork() {
    List<StudentWork> studentWorkList = studentWorkDao.getStudentWorkListByParams(null, run1, null,
        null, null, null, "node1", null, null, null);
    assertEquals(2, studentWorkList.size());
    assertEquals("studentWork1", studentWorkList.get(0).getStudentData());
    assertEquals("studentWork3", studentWorkList.get(1).getStudentData());
  }

  @Test
  public void getWorkForComponentByPeriod_ShouldReturnStudentWork() {
    List<StudentWork> studentWorkList = studentWorkDao.getWorkForComponentByPeriod(run1,
        run1Period1, "node1", "component1");
    assertEquals(2, studentWorkList.size());
    assertEquals("studentWork1", studentWorkList.get(0).getStudentData());
    assertEquals("studentWork3", studentWorkList.get(1).getStudentData());
  }

  @Test
  public void getWorkForComponentByWorkgroup_ShouldReturnStudentWork() {
    List<StudentWork> studentWorkList = studentWorkDao.getWorkForComponentByWorkgroup(workgroup1,
        "node1", "component1");
    assertEquals(1, studentWorkList.size());
    assertEquals("studentWork1", studentWorkList.get(0).getStudentData());
  }

  private StudentWork createStudentWork(Workgroup workgroup, String nodeId, String componentId,
      String studentData) {
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
    studentWorkDao.save(studentWork);
    return studentWork;
  }
}
