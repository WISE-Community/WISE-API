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
package org.wise.portal.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.wise.portal.dao.Component;
import org.wise.portal.domain.group.Group;
import org.wise.portal.domain.group.impl.PersistentGroup;
import org.wise.portal.domain.run.Run;
import org.wise.portal.domain.run.impl.RunImpl;
import org.wise.portal.domain.user.User;
import org.wise.portal.domain.workgroup.Workgroup;
import org.wise.portal.domain.workgroup.impl.WorkgroupImpl;
import org.wise.vle.domain.work.StudentWork;

/**
 * @author Hiroki Terashima
 */
public class WISEServiceTest {

  protected Run run1;

  protected Long run1Id = 1L;

  protected Group run1Period1;

  protected Workgroup run1Workgroup1, run1Workgroup2, run1Workgroup3, run1Workgroup4,
      run1Workgroup5;

  protected List<Workgroup> run1Workgroups = new ArrayList<Workgroup>();

  protected Component run1Component1, run1Component2;

  protected String run1Node1Id = "run1Node1";

  protected String run1Component1Id = "run1Component1";

  protected String run1Node2Id = "run1Node2";

  protected String run1Component2Id = "run1Component2";

  protected User student1, student2, student3, student4, student5;

  protected StudentWork componentWorkSubmit1, componentWorkSubmit2, componentWorkSubmit3,
      componentWorkSubmit4, componentWorkNonSubmit1;

  @Before
  public void setUp() throws Exception {
    run1 = new RunImpl();
    run1.setId(run1Id);
    run1Period1 = new PersistentGroup();
    run1Period1.setId(run1Id);
    run1Workgroup1 = createWorkgroupInPeriod(run1, 1L, run1Period1, student1);
    run1Workgroup2 = createWorkgroupInPeriod(run1, 2L, run1Period1, student2);
    run1Workgroup3 = createWorkgroupInPeriod(run1, 3L, run1Period1, student3);
    run1Workgroup4 = createWorkgroupInPeriod(run1, 4L, run1Period1, student4);
    run1Workgroup5 = createWorkgroupInPeriod(run1, 5L, run1Period1, student5);
    run1Component1 = new Component(run1, run1Node1Id, run1Component1Id);
    run1Component2 = new Component(run1, run1Node2Id, run1Component2Id);
    componentWorkSubmit1 = createComponentWork(run1Workgroup1, true);
    componentWorkSubmit2 = createComponentWork(run1Workgroup2, true);
    componentWorkSubmit3 = createComponentWork(run1Workgroup3, true);
    componentWorkSubmit4 = createComponentWork(run1Workgroup4, true);
    componentWorkNonSubmit1 = createComponentWork(run1Workgroup1, false);
  }

  private Workgroup createWorkgroupInPeriod(Run run, Long id, Group period, User member) {
    Workgroup workgroup = new WorkgroupImpl();
    workgroup.setId(id);
    workgroup.setRun(run);
    workgroup.setPeriod(period);
    Set<User> members = new HashSet<User>();
    members.add(member);
    workgroup.setMembers(members);
    workgroup.getGroup().setName("Group " + id);
    run1Workgroups.add(workgroup);
    return workgroup;
  }

  private StudentWork createComponentWork(Workgroup workgroup, boolean isSubmit) {
    StudentWork work = new StudentWork();
    work.setWorkgroup(workgroup);
    work.setIsSubmit(isSubmit);
    return work;
  }

  protected StudentWork createComponentWork(Workgroup workgroup, String nodeId, String componentId,
      boolean isSubmit) {
    StudentWork work = new StudentWork();
    work.setWorkgroup(workgroup);
    work.setNodeId(nodeId);
    work.setComponentId(componentId);
    work.setIsSubmit(isSubmit);
    return work;
  }

  protected List<StudentWork> createStudentWorkList(StudentWork... componentWorks) {
    List<StudentWork> list = new ArrayList<StudentWork>();
    for (StudentWork work : componentWorks) {
      list.add(work);
    }
    return list;
  }
}
