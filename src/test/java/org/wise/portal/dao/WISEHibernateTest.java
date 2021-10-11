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
import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.wise.portal.domain.authentication.Gender;
import org.wise.portal.domain.authentication.Schoollevel;
import org.wise.portal.domain.group.Group;
import org.wise.portal.domain.run.Run;
import org.wise.portal.domain.user.User;
import org.wise.portal.domain.workgroup.Workgroup;
import org.wise.portal.junit.AbstractTransactionalDbTests;

public abstract class WISEHibernateTest extends AbstractTransactionalDbTests {

  protected User student1, student2;

  protected Set<User> workgroup1Members = new HashSet<User>();

  protected Set<User> workgroup2Members = new HashSet<User>();

  protected Set<User> workgroup3Members = new HashSet<User>();

  protected Workgroup workgroup1, workgroup2, workgroup3;

  protected Run run1, run2;

  protected Group period1;

  protected Component component1, component2, componentNotExists;

  @Before
  public void setUp() throws Exception {
    super.setUp();
    student1 = createStudentUser("Spongebob", "Squarepants", "SpongebobS0101", "burger", 1, 1,
        Gender.MALE);
    workgroup1Members.add(student1);
    student2 = createStudentUser("Patrick", "Starr", "PatrickS0101", "burger", 1, 1,
        Gender.MALE);
    workgroup2Members.add(student2);
    workgroup3Members.add(student2);
    String projectName = "How to be a Fry Cook";
    Date startTime = Calendar.getInstance().getTime();
    User teacher = createTeacherUser("Mrs", "Puff", "MrsPuff", "Mrs. Puff", "boat", "Bikini Bottom",
        "Water State", "Pacific Ocean", "mrspuff@bikinibottom.com", "Boating School",
        Schoollevel.COLLEGE, "1234567890");
    run1 = createProjectAndRun(getNextAvailableProjectId(), projectName, teacher, startTime, "Panda123");
    run2 = createProjectAndRun(getNextAvailableProjectId(), projectName, teacher, startTime, "Rhino456");
    period1 = createPeriod("Run 1 Period 1");
    workgroup1 = createWorkgroup(workgroup1Members, run1, period1);
    workgroup2 = createWorkgroup(workgroup2Members, run1, period1);
    workgroup3 = createWorkgroup(workgroup3Members, run2, createPeriod("Run 2 Period 1"));
    component1 = new Component(run1, "node1", "component1");
    component2 = new Component(run1, "node2", "component2");
    componentNotExists = new Component(run1, "nodeX", "componentX");
  }
}
