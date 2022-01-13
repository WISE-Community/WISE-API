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
package org.wise.portal.dao.peergroupactivity.impl;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.wise.portal.dao.Component;
import org.wise.portal.dao.WISEHibernateTest;
import org.wise.portal.domain.peergroupactivity.impl.PeerGroupActivityImpl;
import org.wise.portal.domain.run.Run;

@SpringBootTest
@RunWith(SpringRunner.class)
public class HibernatePeerGroupActivityDaoTest extends WISEHibernateTest {

  @Autowired
  HibernatePeerGroupActivityDao peerGroupActivityDao;

  @Before
  public void setUp() throws Exception {
    super.setUp();
    createPeerGroupActivityWithComponent(component1);
    createPeerGroupActivityWithTag(run1, peerGroupActivityTag1);
  }

  @Test
  public void getByComponent() {
    assertNotNull(peerGroupActivityDao.getByComponent(component1.run, component1.nodeId,
        component1.componentId));
    assertNull(peerGroupActivityDao.getByComponent(componentNotExists.run,
        componentNotExists.nodeId, componentNotExists.componentId));
  }

  @Test
  public void getByTag() {
    assertNotNull(peerGroupActivityDao.getByTag(run1, peerGroupActivityTag1));
    assertNull(peerGroupActivityDao.getByTag(run1, "tagNotInDB"));
  }

  private void createPeerGroupActivityWithComponent(Component component) {
    PeerGroupActivityImpl peerGroupActivity = new PeerGroupActivityImpl();
    peerGroupActivity.setRun(component.run);
    peerGroupActivity.setNodeId(component.nodeId);
    peerGroupActivity.setComponentId(component.componentId);
    peerGroupActivityDao.save(peerGroupActivity);
  }

  private void createPeerGroupActivityWithTag(Run run, String peerGroupActivityTag) {
    PeerGroupActivityImpl peerGroupActivity = new PeerGroupActivityImpl();
    peerGroupActivity.setRun(run);
    peerGroupActivity.setTag(peerGroupActivityTag);
    peerGroupActivityDao.save(peerGroupActivity);
  }
}
