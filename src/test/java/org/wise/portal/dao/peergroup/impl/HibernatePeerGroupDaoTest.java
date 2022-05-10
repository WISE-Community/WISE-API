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
import org.wise.portal.dao.peergrouping.impl.HibernatePeerGroupingDao;
import org.wise.portal.domain.peergroup.impl.PeerGroupImpl;
import org.wise.portal.domain.peergrouping.PeerGrouping;
import org.wise.portal.domain.peergrouping.impl.PeerGroupingImpl;
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
  HibernatePeerGroupingDao peerGroupingDao;

  PeerGrouping peerGrouping1, peerGrouping2;

  String peerGrouping1Tag = "peerGrouping1";

  String peerGrouping2Tag = "peerGrouping2";

  @Before
  public void setUp() throws Exception {
    super.setUp();
    peerGrouping1 = createPeerGrouping(component1, peerGrouping1Tag);
    peerGrouping2 = createPeerGrouping(component2, peerGrouping2Tag);
    createPeerGroup(peerGrouping1, workgroup1);
    createPeerGroup(peerGrouping2, workgroup1, workgroup2);
  }

  private void createPeerGroup(PeerGrouping peerGrouping, Workgroup... workgroups) {
    PeerGroupImpl peerGroup = new PeerGroupImpl();
    peerGroup.setPeerGrouping(peerGrouping);
    peerGroup.setMembers(Stream.of(workgroups).collect(Collectors.toCollection(HashSet::new)));
    peerGroupDao.save(peerGroup);
  }

  private PeerGroupingImpl createPeerGrouping(Component component, String tag) {
    PeerGroupingImpl peerGrouping = new PeerGroupingImpl();
    peerGrouping.setTag(tag);
    peerGrouping.setRun(component.run);
    peerGroupingDao.save(peerGrouping);
    return peerGrouping;
  }

  @Test
  public void getByWorkgroupAndPeerGrouping_WorkgroupIsInPeerGroupForPeerGrouping_ReturnPeerGroup() {
    assertNotNull(peerGroupDao.getByWorkgroupAndPeerGrouping(workgroup1, peerGrouping1));
    assertNotNull(peerGroupDao.getByWorkgroupAndPeerGrouping(workgroup1, peerGrouping2));
  }

  @Test
  public void getByWorkgroupAndPeerGrouping_WorkgroupNotInPeerGroupForPeerGrouping_ReturnNull() {
    assertNull(peerGroupDao.getByWorkgroupAndPeerGrouping(workgroup2, peerGrouping1));
  }

  @Test
  public void getListByPeerGrouping_ReturnListByPeerGrouping() {
    assertEquals(1, peerGroupDao.getListByPeerGrouping(peerGrouping1).size());
    assertEquals(1, peerGroupDao.getListByPeerGrouping(peerGrouping2).size());
  }

  @Test
  public void getListByWorkgroup_WorkgroupInAndNotInPeerGroups_ReturnListByWorkgroup() {
    assertEquals(2, peerGroupDao.getListByWorkgroup(workgroup1).size());
    assertEquals(1, peerGroupDao.getListByWorkgroup(workgroup2).size());
    assertEquals(0, peerGroupDao.getListByWorkgroup(workgroup3).size());
  }

  @Test
  public void getWorkgroupsInPeerGroup_PeerGroupingWithPeerGroups_ReturnWorkgroupList() {
    assertEquals(1, peerGroupDao.getWorkgroupsInPeerGroup(peerGrouping1, run1Period1).size());
    assertEquals(2, peerGroupDao.getWorkgroupsInPeerGroup(peerGrouping2, run1Period1).size());
  }
}
