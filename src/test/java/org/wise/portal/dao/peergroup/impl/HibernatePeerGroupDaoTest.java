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
package org.wise.portal.dao.peergroup.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.HashSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.wise.portal.dao.Component;
import org.wise.portal.dao.WISEHibernateTest;
import org.wise.portal.dao.peergroupactivity.impl.HibernatePeerGroupActivityDao;
import org.wise.portal.domain.peergroup.impl.PeerGroupImpl;
import org.wise.portal.domain.peergroupactivity.impl.PeerGroupActivityImpl;
import org.wise.portal.domain.workgroup.Workgroup;

/**
 * @author Hiroki Terashima
 */
@SpringBootTest
@RunWith(SpringRunner.class)
public class HibernatePeerGroupDaoTest extends WISEHibernateTest {

  @Autowired
  HibernatePeerGroupDao peerGroupDao;

  @Autowired
  HibernatePeerGroupActivityDao peerGroupActivityDao;

  @Before
  public void setUp() throws Exception {
    super.setUp();
    createPeerGroup(component1, workgroup1);
    createPeerGroup(component2, workgroup1, workgroup2);
  }

  private void createPeerGroup(Component component, Workgroup... workgroups) {
    PeerGroupImpl peerGroup = new PeerGroupImpl();
    peerGroup.setPeerGroupActivity(createPeerGroupActivity(component));
    peerGroup.setMembers(Stream.of(workgroups).collect(Collectors.toCollection(HashSet::new)));
    peerGroupDao.save(peerGroup);
  }

  private PeerGroupActivityImpl createPeerGroupActivity(Component component) {
    PeerGroupActivityImpl peerGroupActivity = new PeerGroupActivityImpl();
    peerGroupActivity.setRun(component.run);
    peerGroupActivity.setNodeId(component.nodeId);
    peerGroupActivity.setComponentId(component.componentId);
    peerGroupActivityDao.save(peerGroupActivity);
    return peerGroupActivity;
  }

  @Test
  public void getListByWorkgroup_WorkgroupIsInPeerGroups_ReturnListByWorkgroup() {
    assertEquals(2, peerGroupDao.getListByWorkgroup(workgroup1).size());
    assertEquals(1, peerGroupDao.getListByWorkgroup(workgroup2).size());
    assertEquals(0, peerGroupDao.getListByWorkgroup(workgroup3).size());
  }

  @Test
  public void getListByRun_ReturnListByRun() {
    assertEquals(2, peerGroupDao.getListByRun(run1).size());
    assertEquals(0, peerGroupDao.getListByRun(run2).size());
  }

  @Test
  public void getListByComponent_ReturnListByComponent() {
    assertEquals(1, peerGroupDao.getListByComponent(component1.run, component1.nodeId,
        component1.componentId).size());
    assertEquals(1, peerGroupDao.getListByComponent(component2.run, component2.nodeId,
        component2.componentId).size());
    assertEquals(0, peerGroupDao.getListByComponent(componentNotExists.run,
        componentNotExists.nodeId, componentNotExists.componentId).size());
  }
}
