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
import org.junit.Before;
import org.wise.portal.domain.peergroup.PeerGroup;
import org.wise.portal.domain.peergroup.impl.PeerGroupImpl;
import org.wise.portal.domain.peergrouping.PeerGrouping;
import org.wise.portal.domain.peergrouping.impl.PeerGroupingImpl;
import org.wise.portal.domain.run.Run;
import org.wise.portal.domain.workgroup.Workgroup;
import org.wise.portal.service.WISEServiceTest;

/**
 * @author Hiroki Terashima
 * @author Geoffrey Kwan
 */
public class PeerGroupServiceTest extends WISEServiceTest {

  PeerGrouping peerGrouping, manualPeerGrouping;

  PeerGroup peerGroup1, peerGroup2, peerGroup3;

  List<PeerGroup> peerGroups = new ArrayList<PeerGroup>();

  @Before
  public void setUp() throws Exception {
    super.setUp();
    peerGrouping = createPeerGrouping(run1, "tag1", "random", 3, 50, 2);
    peerGroup1 = new PeerGroupImpl(peerGrouping, run1Period1,
        new HashSet<Workgroup>(Arrays.asList(run1Workgroup1, run1Workgroup2)));
    peerGroups.add(peerGroup1);
    peerGroup2 = new PeerGroupImpl(peerGrouping, run1Period1, new HashSet<Workgroup>());
    manualPeerGrouping = createPeerGrouping(run1, "tag1", "manual", 2, 50, 2);
  }

  private PeerGrouping createPeerGrouping(Run run, String peerGroupingTag, String logicName,
      Integer logicThresholdCount, Integer logicThresholdPercent, Integer maxMembershipCount)
      throws JSONException {
    return new PeerGroupingImpl(run, peerGroupingTag, logicName, logicThresholdCount,
        logicThresholdPercent, maxMembershipCount);
  }
}
