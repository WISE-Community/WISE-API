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
package org.wise.portal.dao;

import java.util.Calendar;
import java.util.Date;
import java.util.Set;
import java.util.TreeSet;

import org.junit.Before;
import org.wise.portal.domain.authentication.Gender;
import org.wise.portal.domain.authentication.Schoollevel;
import org.wise.portal.domain.group.Group;
import org.wise.portal.domain.project.Project;
import org.wise.portal.domain.run.Run;
import org.wise.portal.domain.user.User;
import org.wise.portal.domain.workgroup.Workgroup;
import org.wise.portal.junit.AbstractTransactionalDbTests;

/**
 * @author Geoffrey Kwan
 * @author Hiroki Terashima
 */
public abstract class WISEHibernateTest extends AbstractTransactionalDbTests {

  protected final String COMPONENT_ID1 = "component1";
  protected final String COMPONENT_ID2 = "component2";
  protected final String NODE_ID1 = "node1";
  protected final String NODE_ID2 = "node2";
  protected final String PROJECT_NAME = "How to be a Fry Cook";
  protected final String RUN_CODE1 = "Panda123";
  protected final String RUN_CODE2 = "Rhino123";
  protected final String RUN_CODE3 = "Rhino789";

  protected Component component1, component2, componentNotExists;
  protected Project project1;
  protected Run run1, run2, run3;
  protected Group run1Period1, run1Period2, run2Period1;
  protected Date runStartTime;
  protected User student1, student2, student3, student4, teacher1, teacher2;
  protected Workgroup teacherWorkgroup1, workgroup1, workgroup2, workgroup3;

  @Before
  public void setUp() throws Exception {
    super.setUp();
    student1 = createStudentUser("Spongebob", "Squarepants", "SpongebobS0101", "burger", 1, 1,
        Gender.MALE);
    student2 = createStudentUser("Patrick", "Starr", "PatrickS0101", "rock", 1, 1, Gender.MALE);
    student3 = createStudentUser("Sandy", "Cheeks", "SandyC0101", "karate", 1, 1, Gender.FEMALE);
    student4 = createStudentUser("Squidward", "Tentacles", "SquidwardT0101", "clarinet", 1, 1,
        Gender.MALE);
    runStartTime = Calendar.getInstance().getTime();
    teacher1 = createTeacherUser("Mrs", "Puff", "MrsPuff", "Mrs. Puff", "boat", "Bikini Bottom",
        "Water State", "Pacific Ocean", "mrspuff@bikinibottom.com", "Boating School",
        Schoollevel.COLLEGE, "1234567890");
    teacher2 = createTeacherUser("Mr", "Krabs", "MrKrabs", "Mr. Krabs", "restaurant",
        "Bikini Bottom", "Water State", "Pacific Ocean", "mrkrabs@bikinibottom.com", "Krusty Krab",
        Schoollevel.HIGH_SCHOOL, "abcdefghij");
    run1 = createProjectAndRun(getNextAvailableProjectId(), PROJECT_NAME, teacher1, runStartTime,
        "Panda123");
    run2 = createProjectAndRun(getNextAvailableProjectId(), PROJECT_NAME, teacher1, runStartTime,
        "Rhino456");
    run3 = createProjectAndRun(getNextAvailableProjectId(), PROJECT_NAME, teacher1, runStartTime,
        "Rhino789");
    project1 = run1.getProject();
    run1Period1 = createPeriod("Run 1 Period 1");
    run1Period2 = createPeriod("Run 1 Period 2");
    run2Period1 = createPeriod("Run 2 Period 1");
    Set<Group> periods = new TreeSet<Group>();
    periods.add(run1Period1);
    periods.add(run1Period2);
    run1.setPeriods(periods);
    teacherWorkgroup1 = addUserToRun(teacher1, run1, null);
    workgroup1 = addUserToRun(student1, run1, run1Period1);
    workgroup2 = addUserToRun(student2, run1, run1Period1);
    workgroup3 = addUserToRun(student2, run2, run2Period1);
    component1 = new Component(run1, NODE_ID1, COMPONENT_ID1);
    component2 = new Component(run1, NODE_ID2, COMPONENT_ID2);
    componentNotExists = new Component(run1, "nodeX", "componentX");
  }
}