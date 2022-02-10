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

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

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
import org.wise.portal.domain.peergroupactivity.PeerGroupActivity;
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

  PeerGroupActivity activity1, activity2;

  String peerGroupActivity1Tag = "peerGroupActivity1";

  String peerGroupActivity2Tag = "peerGroupActivity2";

  @Before
  public void setUp() throws Exception {
    super.setUp();
    activity1 = createPeerGroupActivity(component1, peerGroupActivity1Tag);
    activity2 = createPeerGroupActivity(component2, peerGroupActivity2Tag);
    createPeerGroup(activity1, workgroup1);
    createPeerGroup(activity2, workgroup1, workgroup2);
  }

  private void createPeerGroup(PeerGroupActivity activity, Workgroup... workgroups) {
    PeerGroupImpl peerGroup = new PeerGroupImpl();
    peerGroup.setPeerGroupActivity(activity);
    peerGroup.setMembers(Stream.of(workgroups).collect(Collectors.toCollection(HashSet::new)));
    peerGroupDao.save(peerGroup);
  }

  private PeerGroupActivityImpl createPeerGroupActivity(Component component, String tag) {
    PeerGroupActivityImpl peerGroupActivity = new PeerGroupActivityImpl();
    peerGroupActivity.setTag(tag);
    peerGroupActivity.setRun(component.run);
    peerGroupActivity.setNodeId(component.nodeId);
    peerGroupActivity.setComponentId(component.componentId);
    peerGroupActivityDao.save(peerGroupActivity);
    return peerGroupActivity;
  }

  @Test
  public void getByWorkgroupAndActivity_WorkgroupIsInPeerGroupForActivity_ReturnPeerGroup() {
    assertNotNull(peerGroupDao.getByWorkgroupAndActivity(workgroup1, activity1));
    assertNotNull(peerGroupDao.getByWorkgroupAndActivity(workgroup1, activity2));
  }

  @Test
  public void getByWorkgroupAndActivity_WorkgroupNotInPeerGroupForActivity_ReturnNull() {
    assertNull(peerGroupDao.getByWorkgroupAndActivity(workgroup2, activity1));
  }

  @Test
  public void getListByActivity_ReturnListByActivity() {
    assertEquals(1, peerGroupDao.getListByActivity(activity1).size());
    assertEquals(1, peerGroupDao.getListByActivity(activity2).size());
  }

  @Test
  public void getListByWorkgroup_WorkgroupInAndNotInPeerGroups_ReturnListByWorkgroup() {
    assertEquals(2, peerGroupDao.getListByWorkgroup(workgroup1).size());
    assertEquals(1, peerGroupDao.getListByWorkgroup(workgroup2).size());
    assertEquals(0, peerGroupDao.getListByWorkgroup(workgroup3).size());
  }

  @Test
  public void getWorkgroupsInPeerGroup_ActivityWithPeerGroups_ReturnWorkgroupList() {
    assertEquals(1, peerGroupDao.getWorkgroupsInPeerGroup(activity1, run1Period1).size());
    assertEquals(2, peerGroupDao.getWorkgroupsInPeerGroup(activity2, run1Period1).size());
  }
}
