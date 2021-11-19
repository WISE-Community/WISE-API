/**
 * Copyright (c) 2007 Regents of the University of California (Regents). Created
 * by TELS, Graduate School of Education, University of California at Berkeley.
 *
 * This software is distributed under the GNU Lesser General Public License, v2.
 *
 * Permission is hereby granted, without written agreement and without license
 * or royalty fees, to use, copy, modify, and distribute this software and its
 * documentation for any purpose, provided that the above copyright notice and
 * the following two paragraphs appear in all copies of this software.
 *
 * REGENTS SPECIFICALLY DISCLAIMS ANY WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE. THE SOFTWAREAND ACCOMPANYING DOCUMENTATION, IF ANY, PROVIDED
 * HEREUNDER IS PROVIDED "AS IS". REGENTS HAS NO OBLIGATION TO PROVIDE
 * MAINTENANCE, SUPPORT, UPDATES, ENHANCEMENTS, OR MODIFICATIONS.
 *
 * IN NO EVENT SHALL REGENTS BE LIABLE TO ANY PARTY FOR DIRECT, INDIRECT,
 * SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES, INCLUDING LOST PROFITS,
 * ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
 * REGENTS HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.wise.portal.dao.run.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.wise.portal.dao.ObjectNotFoundException;
import org.wise.portal.dao.WISEHibernateTest;
import org.wise.portal.domain.run.Run;
import org.wise.portal.domain.run.impl.RunImpl;
import org.wise.portal.domain.workgroup.Workgroup;

/**
 * @author Hiroki Terashima
 * @author Geoffrey Kwan
 */
@SpringBootTest
@RunWith(SpringRunner.class)
public class HibernateRunDaoTest extends WISEHibernateTest {

  private final String UNUSED_RUN_CODE = "Pig123";

  @Autowired
  private HibernateRunDao runDao;

  private void assertNumRuns(int expected) {
    assertEquals("Number of rows in the [runs] table.", expected, countRowsInTable("runs"));
  }

  @Test
  public void getById_ExistingRunId_Success() throws Exception {
    runDao.save(run1);
    assertNotNull(runDao.getById(run1.getId()));
  }

  @Test
  public void save_NewRun_Success() {
    assertNumRuns(3);
    Long id = getNextAvailableProjectId();
    String projectName = "How to be a Fry Cook";
    Date startTime = Calendar.getInstance().getTime();
    String runCode = UNUSED_RUN_CODE;
    run1 = createProjectAndRun(id, projectName, teacher2, startTime, runCode);
    runDao.save(run1);
    toilet.flush();
    assertNumRuns(4);
  }

  @Test
  public void save_WithoutProject_ShouldThrowException() {
    run1.setProject(null);
    try {
      runDao.save(run1);
      toilet.flush();
      fail("Exception expected to be thrown but was not");
    } catch (Exception e) {
    }
  }

  @Test
  public void retrieveByRunCode_ValidRunCode_Success() throws Exception {
    Run run = runDao.retrieveByRunCode(RUN_CODE1);
    assertTrue(run instanceof RunImpl);
    assertTrue(run.getClass() == RunImpl.class);
    assertEquals(RUN_CODE1, run.getRuncode());
    assertEquals(runStartTime, run.getStarttime());
  }

  @Test
  public void retrieveByRunCode_NonExistingRunCode_ShouldThrowException() {
    try {
      runDao.retrieveByRunCode(UNUSED_RUN_CODE);
      fail("Expected ObjectNotFoundException");
    } catch (ObjectNotFoundException e) {
    }
  }

  @Test
  public void getWorkgroupsForRun_OnePeriod_Success() throws Exception {
    Long runId = run1.getId();
    List<Workgroup> workgroups = runDao.getWorkgroupsForRun(runId);
    assertEquals(3, workgroups.size());
    addUserToRun(student3, run1, run1Period1);
    workgroups = runDao.getWorkgroupsForRun(runId);
    assertEquals(4, workgroups.size());
  }

  @Test
  public void getWorkgroupsForRun_TwoPeriods_Success() throws Exception {
    Long runId = run1.getId();
    List<Workgroup> workgroups = runDao.getWorkgroupsForRun(runId);
    assertEquals(3, workgroups.size());
    addUserToRun(student3, run1, run1Period1);
    addUserToRun(student4, run1, run1Period2);
    workgroups = runDao.getWorkgroupsForRun(runId);
    assertEquals(5, workgroups.size());
  }

  @Test
  public void getWorkgroupsForRunAndPeriod_OnePeriod_Success() throws Exception {
    Long runId = run1.getId();
    Long period1Id = run1Period1.getId();
    List<Workgroup> workgroups1 = runDao.getWorkgroupsForRunAndPeriod(runId, period1Id);
    assertEquals(2, workgroups1.size());
    addUserToRun(student1, run1, run1Period1);
    workgroups1 = runDao.getWorkgroupsForRunAndPeriod(runId, run1Period1.getId());
    assertEquals(3, workgroups1.size());
  }

  @Test
  public void getWorkgroupsForRunAndPeriod_TwoPeriods_Success() throws Exception {
    Long runId = run1.getId();
    Long run1Period1Id = run1Period1.getId();
    Long run1Period2Id = run1Period2.getId();
    List<Workgroup> workgroups1 = runDao.getWorkgroupsForRunAndPeriod(runId, run1Period1Id);
    assertEquals(2, workgroups1.size());
    addUserToRun(student1, run1, run1Period1);
    List<Workgroup> workgroups2 = runDao.getWorkgroupsForRunAndPeriod(runId, run1Period2Id);
    assertEquals(0, workgroups2.size());
    addUserToRun(student3, run1, run1Period2);
    workgroups1 = runDao.getWorkgroupsForRunAndPeriod(runId, run1Period1Id);
    assertEquals(3, workgroups1.size());
    workgroups2 = runDao.getWorkgroupsForRunAndPeriod(runId, run1Period2Id);
    assertEquals(1, workgroups2.size());
  }

  @Test
  public void retrieveByField_Name_Success() {
    List<Run> recyclingRuns = runDao.retrieveByField("name", "like", "Name that does not exist");
    assertEquals(0, recyclingRuns.size());
    List<Run> runsWithProjectName = runDao.retrieveByField("name", "like", PROJECT_NAME);
    assertEquals(3, runsWithProjectName.size());
    assertEquals(PROJECT_NAME, runsWithProjectName.get(0).getName());
  }

  @Test
  public void retrieveByField_StartTime_Success() {
    Date yesterday = getDateXDaysFromNow(-1);
    List<Run> runsStartedAfterYesterday = runDao.retrieveByField("starttime", ">", yesterday);
    assertEquals(3, runsStartedAfterYesterday.size());
    Date tomorrow = getDateXDaysFromNow(1);
    List<Run> runsStartedAfterTomorrow = runDao.retrieveByField("starttime", ">", tomorrow);
    assertEquals(0, runsStartedAfterTomorrow.size());
  }

  @Test
  public void getRunListByUser_StudentNotInRun_ShouldReturnNoRuns() throws Exception {
    List<Run> runsByUser = runDao.getRunListByUser(student1);
    assertEquals(0, runsByUser.size());
  }

  @Test
  public void getRunListByUser_StudentInPeriodButNotWorkgroup_ShouldReturnRun() throws Exception {
    List<Run> runsByUser = runDao.getRunListByUser(student1);
    assertEquals(0, runsByUser.size());
    run1Period1.addMember(student1);
    runsByUser = runDao.getRunListByUser(student1);
    assertEquals(1, runsByUser.size());
  }

  @Test
  public void getRunListByUser_StudentInPeriodAndWorkgroup_ShouldReturnRun() throws Exception {
    List<Run> runsByUser = runDao.getRunListByUser(student2);
    assertEquals(0, runsByUser.size());
    run1Period2.addMember(student2);
    addUserToRun(student2, run1, run1Period2);
    runsByUser = runDao.getRunListByUser(student2);
    assertEquals(1, runsByUser.size());
  }

  @Test
  public void getRunsOfProject_NoRuns_Success() {
    List<Run> runs = runDao.getRunsOfProject(0L);
    assertEquals(0, runs.size());
  }

  @Test
  public void getRunsOfProject_OneRun_Success() {
    List<Run> runs = runDao.getRunsOfProject(0L);
    assertEquals(0, runs.size());
    runs = runDao.getRunsOfProject((Long) project1.getId());
    assertEquals(1, runs.size());
  }

  @Test
  public void getRunListByOwner_NoRuns_Success() throws Exception {
    List<Run> runs = runDao.getRunListByOwner(teacher2);
    assertEquals(0, runs.size());
  }

  @Test
  public void getRunListByOwner_OneRun_Success() throws Exception {
    List<Run> runs = runDao.getRunListByOwner(teacher1);
    assertEquals(3, runs.size());
  }

  @Test
  public void getRunListBySharedOwner_NoRuns_Success() throws Exception {
    List<Run> runs = runDao.getRunListBySharedOwner(teacher2);
    assertEquals(0, runs.size());
  }

  @Test
  public void getRunListBySharedOwner_OneRun_Success() throws Exception {
    List<Run> runs = runDao.getRunListBySharedOwner(teacher2);
    assertEquals(0, runs.size());
    run1.getSharedowners().add(teacher2);
    runDao.save(run1);
    runs = runDao.getRunListBySharedOwner(teacher2);
    assertEquals(1, runs.size());
  }

  @Test
  public void getRunsRunWithinTimePeriod_Today_Success() {
    run1.setLastRun(getDateXDaysFromNow(-2));
    List<Run> runs = runDao.getRunsRunWithinTimePeriod("today");
    assertEquals(0, runs.size());
    run1.setLastRun(getDateXDaysFromNow(0));
    runs = runDao.getRunsRunWithinTimePeriod("today");
    assertEquals(1, runs.size());
  }

  @Test
  public void getRunsRunWithinTimePeriod_Week_Success() {
    run1.setLastRun(getDateXDaysFromNow(-8));
    List<Run> runs = runDao.getRunsRunWithinTimePeriod("week");
    assertEquals(0, runs.size());
    run1.setLastRun(getDateXDaysFromNow(-6));
    runs = runDao.getRunsRunWithinTimePeriod("week");
    assertEquals(1, runs.size());
  }

  @Test
  public void getRunsRunWithinTimePeriod_Month_Success() {
    run1.setLastRun(getDateXDaysFromNow(-31));
    List<Run> runs = runDao.getRunsRunWithinTimePeriod("month");
    assertEquals(0, runs.size());
    run1.setLastRun(getDateXDaysFromNow(-29));
    runs = runDao.getRunsRunWithinTimePeriod("month");
    assertEquals(1, runs.size());
  }

  @Test
  public void getRunsByActivity_NoneActive_Success() {
    List<Run> runs = runDao.getRunsByActivity();
    assertEquals(0, runs.size());
  }

  @Test
  public void getRunsByActivity_OneActive_Success() {
    List<Run> runs = runDao.getRunsByActivity();
    assertEquals(0, runs.size());
    run1.setTimesRun(1);
    runs = runDao.getRunsByActivity();
    assertEquals(1, runs.size());
  }

  @Test
  public void getRunsByActivity_TwoActive_Success() {
    List<Run> runs = runDao.getRunsByActivity();
    assertEquals(0, runs.size());
    run1.setTimesRun(1);
    Long id = getNextAvailableProjectId();
    Run run2 = createProjectAndRun(id, PROJECT_NAME, teacher1, runStartTime, UNUSED_RUN_CODE);
    run2.setTimesRun(1);
    runs = runDao.getRunsByActivity();
    assertEquals(2, runs.size());
  }

  @Test
  public void isUserInRunAndPeriod_NotInRun_ShouldReturnFalse() {
    assertFalse(runDao.isUserInRunAndPeriod(student1, run1, run1Period2));
  }

  @Test
  public void isUserInRunAndPeriod_InRunButNotPeriod_ShouldReturnFalse() {
    addUserToRun(student1, run1, run1Period1);
    assertFalse(runDao.isUserInRunAndPeriod(student1, run1, run1Period2));
  }

  @Test
  public void isUserInRunAndPeriod_InRunAndPeriod_ShouldReturnTrue() {
    addUserToRun(student1, run1, run1Period1);
    assertTrue(runDao.isUserInRunAndPeriod(student1, run1, run1Period1));
  }
}
