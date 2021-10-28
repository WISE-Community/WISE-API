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
import java.util.Collections;
import java.util.List;

import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.wise.portal.dao.peergroup.PeerGroupDao;
import org.wise.portal.dao.work.StudentWorkDao;
import org.wise.portal.domain.group.Group;
import org.wise.portal.domain.peergroup.PeerGroup;
import org.wise.portal.domain.peergroupactivity.PeerGroupActivity;
import org.wise.portal.domain.workgroup.Workgroup;
import org.wise.portal.service.peergroup.PeerGroupThresholdService;
import org.wise.portal.service.run.RunService;
import org.wise.vle.domain.work.StudentWork;

/**
 * @author Hiroki Terashima
 */
@Service
public class PeerGroupThresholdServiceImpl implements PeerGroupThresholdService {

  @Autowired
  private RunService runService;

  @Autowired
  private PeerGroupDao<PeerGroup> peerGroupDao;

  @Autowired
  private StudentWorkDao<StudentWork> studentWorkDao;

  public boolean isCompletionThresholdSatisfied(PeerGroupActivity activity, Group period) {
    float logicComponentCompletionCount = getLogicComponentCompletionCount(activity, period);
    float logicComponentCompletionPercent =
        (logicComponentCompletionCount / getNumWorkgroupsInPeriod(activity, period)) * 100;
    return logicComponentCompletionCount >= 2 &&
        (logicComponentCompletionCount >= activity.getLogicThresholdCount() ||
        logicComponentCompletionPercent >= activity.getLogicThresholdPercent());
  }

  public boolean canCreatePeerGroup(PeerGroupActivity activity, Group period) {
    int numWorkgroupsInPeerGroup = getNumWorkgroupsInPeerGroup(activity, period);
    int numWorkgroupsNotInPeerGroup = getNumWorkgroupsInPeriod(activity, period) -
        numWorkgroupsInPeerGroup;
    int numWorkgroupsCompletedLogicComponentButNotInPeerGroup =
        getLogicComponentCompletionCount(activity, period) - numWorkgroupsInPeerGroup;
    return numWorkgroupsNotInPeerGroup == 1 ||
        numWorkgroupsCompletedLogicComponentButNotInPeerGroup >= 2;
  }

  private int getNumWorkgroupsInPeriod(PeerGroupActivity activity, Group period) {
    return runService.getWorkgroups(activity.getRun().getId(), period.getId()).size();
  }

  private int getLogicComponentCompletionCount(PeerGroupActivity activity, Group period) {
    try {
      return getLogicComponentStudentWorkForPeriod(activity, period).size();
    } catch (JSONException e) {
    }
    return 0;
  }

  private List<StudentWork> getLogicComponentStudentWorkForPeriod(PeerGroupActivity activity,
      Group period) throws JSONException {
    List<StudentWork> logicComponentStudentWorkForPeriod = studentWorkDao
        .getWorkForComponentByPeriod(activity.getRun(), period,
        activity.getLogicNodeId(), activity.getLogicComponentId());
    Collections.reverse(logicComponentStudentWorkForPeriod);
    List<Workgroup> workgroups = new ArrayList<Workgroup>();
    List<StudentWork> studentWorkUniqueWorkgroups = new ArrayList<StudentWork>();
    for (StudentWork studentWork : logicComponentStudentWorkForPeriod) {
      if (studentWork.getIsSubmit() && !workgroups.contains(studentWork.getWorkgroup())) {
        studentWorkUniqueWorkgroups.add(studentWork);
        workgroups.add(studentWork.getWorkgroup());
      }
    }
    return studentWorkUniqueWorkgroups;
  }

  private int getNumWorkgroupsInPeerGroup(PeerGroupActivity activity, Group period) {
    int numWorkgroupsInPeerGroup = 0;
    List<PeerGroup> peerGroups = peerGroupDao.getListByComponent(activity.getRun(),
        activity.getNodeId(), activity.getComponentId());
    for (PeerGroup peerGroup : peerGroups) {
      for (Workgroup workgroup : peerGroup.getMembers()) {
        if (workgroup.getPeriod().equals(period)) {
          numWorkgroupsInPeerGroup++;
        }
      }
    }
    return numWorkgroupsInPeerGroup;
  }
}
