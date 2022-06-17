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

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.wise.portal.dao.peergroup.PeerGroupDao;
import org.wise.portal.domain.group.Group;
import org.wise.portal.domain.peergroup.PeerGroup;
import org.wise.portal.domain.peergrouping.PeerGrouping;
import org.wise.portal.domain.workgroup.Workgroup;
import org.wise.portal.service.peergroup.PeerGroupThresholdService;
import org.wise.portal.service.run.RunService;

/**
 * @author Hiroki Terashima
 */
@Service
public class PeerGroupThresholdServiceImpl implements PeerGroupThresholdService {

  @Autowired
  private RunService runService;

  @Autowired
  private PeerGroupDao<PeerGroup> peerGroupDao;

  public boolean canCreatePeerGroup(PeerGrouping peerGrouping, Group period) {
    int numWorkgroupsNotInPeerGroup = getNumNonEmptyWorkgroupsInPeriod(peerGrouping, period) -
        getNumNonEmptyWorkgroupsInPeerGroup(peerGrouping, period);
    return numWorkgroupsNotInPeerGroup > 1;
  }

  private int getNumNonEmptyWorkgroupsInPeriod(PeerGrouping peerGrouping, Group period) {
    List<Workgroup> workgroupsInPeriod = runService.getWorkgroups(
        peerGrouping.getRun().getId(), period.getId());
    workgroupsInPeriod.removeIf(workgroupInPeriod -> workgroupInPeriod.getMembers().size() == 0);
    return workgroupsInPeriod.size();
  }

  private int getNumNonEmptyWorkgroupsInPeerGroup(PeerGrouping activity, Group period) {
    int numWorkgroupsInPeerGroup = 0;
    List<PeerGroup> peerGroups = peerGroupDao.getListByPeerGrouping(activity);
    for (PeerGroup peerGroup : peerGroups) {
      for (Workgroup workgroup : peerGroup.getMembers()) {
        if (workgroup.getMembers().size() > 0 && workgroup.getPeriod().equals(period)) {
          numWorkgroupsInPeerGroup++;
        }
      }
    }
    return numWorkgroupsInPeerGroup;
  }
}
