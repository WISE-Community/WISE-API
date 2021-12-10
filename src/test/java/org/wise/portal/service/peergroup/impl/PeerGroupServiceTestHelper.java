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
package org.wise.portal.service.peergroup.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.json.JSONObject;
import org.wise.portal.dao.Component;
import org.wise.portal.domain.peergroup.PeerGroup;
import org.wise.portal.domain.peergroup.impl.PeerGroupImpl;
import org.wise.portal.domain.peergroupactivity.PeerGroupActivity;
import org.wise.portal.domain.peergroupactivity.impl.PeerGroupActivityImpl;
import org.wise.portal.domain.project.impl.ProjectComponent;
import org.wise.portal.domain.run.Run;
import org.wise.portal.domain.workgroup.Workgroup;
import org.wise.portal.service.WISEServiceTest;

/**
 * @author Hiroki Terashima
 */
public class PeerGroupServiceTestHelper extends WISEServiceTest {

  String logic = "[{\"name\": \"maximizeDifferentIdeas\"," +
      "\"nodeId\": \"" + run1Node1Id + "\", \"componentId\": \"" + run1Component1Id + "\"}]";

  int logicThresholdCount = 3;

  int logicThresholdPercent = 50;

  int maxMembershipCount = 2;

  String peerGroupActivityComponentString =
      "{\"id\":\"" + run1Component2Id + "\"," +
      "\"logic\":" + logic + "," +
      "\"logicThresholdCount\":" + logicThresholdCount + "," +
      "\"logicThresholdPercent\":" + logicThresholdPercent + "," +
      "\"maxMembershipCount\":" + maxMembershipCount + "}";

  PeerGroupActivity activity;

  PeerGroup peerGroup1, peerGroup2, peerGroup3;

  List<PeerGroup> peerGroups = new ArrayList<PeerGroup>();

  public PeerGroupServiceTestHelper(Run run, Component component) throws Exception {
    super.setUp();
    activity = new PeerGroupActivityImpl(run, component.nodeId,
        new ProjectComponent(new JSONObject(peerGroupActivityComponentString)));
    Set<Workgroup> members = new HashSet<Workgroup>();
    members.add(run1Workgroup1);
    members.add(run1Workgroup2);
    peerGroup1 = new PeerGroupImpl(activity, run1Period1, members);
    peerGroups.add(peerGroup1);
    peerGroup2 = new PeerGroupImpl(activity, run1Period1, new HashSet<Workgroup>());
  }
}
