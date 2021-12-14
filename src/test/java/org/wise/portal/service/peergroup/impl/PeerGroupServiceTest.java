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
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
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
 * @author Geoffrey Kwan
 */
public class PeerGroupServiceTest extends WISEServiceTest {

  PeerGroupActivity activity, manualActivity;

  PeerGroup peerGroup1, peerGroup2, peerGroup3;

  List<PeerGroup> peerGroups = new ArrayList<PeerGroup>();

  @Before
  public void setUp() throws Exception {
    super.setUp();
    activity = createPeerGroupActivity(run1, run1Component2, run1Component2Id,
        "maximizeDifferentIdeas", run1Node1Id, run1Component1Id, 3, 50, 2);
    peerGroup1 = new PeerGroupImpl(activity, run1Period1,
        new HashSet<Workgroup>(Arrays.asList(run1Workgroup1, run1Workgroup2)));
    peerGroups.add(peerGroup1);
    peerGroup2 = new PeerGroupImpl(activity, run1Period1, new HashSet<Workgroup>());
    manualActivity = createPeerGroupActivity(run1, run1Component2, run1Component2Id, "manual",
        run1Node1Id, run1Component1Id, 2, 50, 2);
  }

  private PeerGroupActivity createPeerGroupActivity(Run run, Component component, String componentId,
      String logicName, String logicNodeId, String logicComponentId, Integer logicThresholdCount,
      Integer logicThresholdPercent, Integer maxMembershipCount) throws JSONException {
    String peerGroupActivityComponentString = createPeerGroupActivityComponentString(componentId,
        logicName, logicNodeId, logicComponentId, logicThresholdCount, logicThresholdPercent,
        maxMembershipCount);
    return new PeerGroupActivityImpl(run, component.nodeId,
        new ProjectComponent(new JSONObject(peerGroupActivityComponentString)));
  }

  private String createPeerGroupActivityComponentString(String componentId, String logicName,
      String logicNodeId, String logicComponentId, Integer logicThresholdCount,
      Integer logicThresholdPercent, Integer maxMembershipCount) {
    String logic = createLogicString(logicName, logicNodeId, logicComponentId);
    return createPeerGroupActivityComponentString(componentId, logic, logicThresholdCount,
        logicThresholdPercent, maxMembershipCount);
  }

  private String createPeerGroupActivityComponentString(String componentId, String logic,
      Integer logicThresholdCount, Integer logicThresholdPercent, Integer maxMembershipCount) {
    return new StringBuilder()
        .append("{")
        .append("  \"id\": \"" + componentId + "\",")
        .append("  \"logic\": " + logic + ",")
        .append("  \"logicThresholdCount\": \"" + logicThresholdCount + "\",")
        .append("  \"logicThresholdPercent\": \"" + logicThresholdPercent + "\",")
        .append("  \"maxMembershipCount\": \"" + maxMembershipCount + "\"")
        .append("}")
        .toString();
  }

  private String createLogicString(String name, String nodeId, String componentId) {
    return new StringBuilder()
        .append("[")
        .append("  {")
        .append("    \"name\": \"" + name + "\",")
        .append("    \"nodeId\": \"" + nodeId + "\",")
        .append("    \"componentId\": \"" + componentId + "\"")
        .append("  }")
        .append("]")
        .toString();
  }
}
