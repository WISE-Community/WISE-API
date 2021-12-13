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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.wise.portal.dao.ObjectNotFoundException;
import org.wise.portal.dao.peergroup.PeerGroupDao;
import org.wise.portal.dao.run.RunDao;
import org.wise.portal.dao.work.StudentWorkDao;
import org.wise.portal.domain.group.Group;
import org.wise.portal.domain.peergroup.PeerGroup;
import org.wise.portal.domain.peergroup.impl.PeerGroupImpl;
import org.wise.portal.domain.peergroupactivity.PeerGroupActivity;
import org.wise.portal.domain.run.Run;
import org.wise.portal.domain.workgroup.Workgroup;
import org.wise.portal.service.peergroup.PeerGroupCreationException;
import org.wise.portal.service.peergroup.PeerGroupActivityThresholdNotSatisfiedException;
import org.wise.portal.service.peergroup.PeerGroupService;
import org.wise.portal.service.peergroup.PeerGroupThresholdService;
import org.wise.vle.domain.work.StudentWork;

/**
 * @author Hiroki Terashima
 */
@Service
public class PeerGroupServiceImpl implements PeerGroupService {

  @Autowired
  private PeerGroupDao<PeerGroup> peerGroupDao;

  @Autowired
  private PeerGroupThresholdService peerGroupThresholdService;

  @Autowired
  private StudentWorkDao<StudentWork> studentWorkDao;

  @Autowired
  private RunDao<Run> runDao;

  public PeerGroup getById(Long id) throws ObjectNotFoundException {
    return peerGroupDao.getById(id);
  }

  @Override
  public PeerGroup getPeerGroup(Workgroup workgroup, PeerGroupActivity activity)
      throws JSONException, PeerGroupActivityThresholdNotSatisfiedException,
      PeerGroupCreationException {
    PeerGroup peerGroup = peerGroupDao.getByWorkgroupAndActivity(workgroup, activity);
    if (activity.getLogicName().equals("manual")) {
      return peerGroup;
    } else {
      return peerGroup != null ? peerGroup : this.createPeerGroup(workgroup, activity);
    }
  }

  private PeerGroup createPeerGroup(Workgroup workgroup, PeerGroupActivity activity)
      throws PeerGroupActivityThresholdNotSatisfiedException, PeerGroupCreationException {
    if (!isCompletionThresholdSatisfied(activity, workgroup)) {
      throw new PeerGroupActivityThresholdNotSatisfiedException();
    } else if (!peerGroupThresholdService.canCreatePeerGroup(activity, workgroup.getPeriod())) {
      throw new PeerGroupCreationException();
    } else {
      PeerGroup peerGroup = new PeerGroupImpl(activity, workgroup.getPeriod(),
          getPeerGroupMembers(workgroup, activity));
      this.peerGroupDao.save(peerGroup);
      return peerGroup;
    }
  }

  private boolean isCompletionThresholdSatisfied(PeerGroupActivity activity, Workgroup workgroup) {
    return hasWorkForPeerGroupLogicActivity(workgroup, activity) && peerGroupThresholdService
        .isCompletionThresholdSatisfied(activity, workgroup.getPeriod());
  }

  private boolean hasWorkForPeerGroupLogicActivity(Workgroup workgroup,
      PeerGroupActivity activity) {
    try {
      return studentWorkDao.getWorkForComponentByWorkgroup(workgroup, activity.getLogicNodeId(),
          activity.getLogicComponentId()).size() > 0;
    } catch (JSONException e) {
      return false;
    }
  }

  private Set<Workgroup> getPeerGroupMembers(Workgroup workgroup, PeerGroupActivity activity)
      throws PeerGroupCreationException {
    try {
      List<Workgroup> workgroupsInPeerGroup = peerGroupDao.getWorkgroupsInPeerGroup(activity,
          workgroup.getPeriod());
      Set<Workgroup> workgroupsNotInPeerGroupAndCompletedLogicActivity = getWorkgroupsNotInPeerGroupAndCompletedLogicActivity(
          activity, workgroup.getPeriod(), workgroupsInPeerGroup);
      if (isLastOnesLeftToPair(workgroup.getRun(), workgroup.getPeriod(), workgroupsInPeerGroup,
          workgroupsNotInPeerGroupAndCompletedLogicActivity, activity.getMaxMembershipCount())) {
        return workgroupsNotInPeerGroupAndCompletedLogicActivity;
      } else {
        return getWorkgroupsInPeerGroupUpToMaxMembership(workgroup, activity,
            workgroupsNotInPeerGroupAndCompletedLogicActivity);
      }
    } catch (JSONException e) {
      throw new PeerGroupCreationException();
    }
  }

  private boolean isLastOnesLeftToPair(Run run, Group period, List<Workgroup> workgroupsInPeerGroup,
      Set<Workgroup> workgroupsNotInPeerGroupAndCompletedLogicActivity, int maxMembershipCount) {
    List<Workgroup> workgroupsInPeriod = runDao.getWorkgroupsForRunAndPeriod(run.getId(),
        period.getId());
    workgroupsInPeriod.removeIf(workgroup -> workgroup.getMembers().size() == 0);
    int numWorkgroupsNotInPeerGroup = workgroupsInPeriod.size() - workgroupsInPeerGroup.size();
    int numWorkgroupsNotInPeerGroupAndCompletedLogicActivity = workgroupsNotInPeerGroupAndCompletedLogicActivity
        .size();
    return numWorkgroupsNotInPeerGroup == numWorkgroupsNotInPeerGroupAndCompletedLogicActivity
        && (numWorkgroupsNotInPeerGroupAndCompletedLogicActivity - maxMembershipCount) <= 1;
  }

  private Set<Workgroup> getWorkgroupsInPeerGroupUpToMaxMembership(Workgroup workgroup,
      PeerGroupActivity activity, Set<Workgroup> possibleMembers) {
    Set<Workgroup> members = new HashSet<Workgroup>();
    members.add(workgroup);
    for (Workgroup possibleMember : possibleMembers) {
      members.add(possibleMember);
      if (members.size() == activity.getMaxMembershipCount()) {
        break;
      }
    }
    return members;
  }

  private Set<Workgroup> getWorkgroupsNotInPeerGroupAndCompletedLogicActivity(
      PeerGroupActivity activity, Group period, List<Workgroup> workgroupsInPeerGroup)
      throws JSONException {
    List<StudentWork> logicComponentStudentWork = getLogicComponentStudentWorkForPeriod(activity,
        period);
    Set<Workgroup> workgroupsNotInPeerGroup = new HashSet<Workgroup>();
    for (StudentWork work : logicComponentStudentWork) {
      if (!workgroupsInPeerGroup.contains(work.getWorkgroup())) {
        workgroupsNotInPeerGroup.add(work.getWorkgroup());
      }
    }
    return workgroupsNotInPeerGroup;
  }

  private List<StudentWork> getLogicComponentStudentWorkForPeriod(PeerGroupActivity activity,
      Group period) throws JSONException {
    List<StudentWork> logicComponentStudentWorkForPeriod = studentWorkDao
        .getWorkForComponentByPeriod(activity.getRun(), period, activity.getLogicNodeId(),
            activity.getLogicComponentId());
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

  @Override
  public List<PeerGroup> getPeerGroups(PeerGroupActivity activity) {
    return peerGroupDao.getListByActivity(activity);
  }

  @Override
  public List<StudentWork> getStudentWork(PeerGroup peerGroup) {
    return getStudentWork(peerGroup, peerGroup.getPeerGroupActivity().getNodeId(),
        peerGroup.getPeerGroupActivity().getComponentId());
  }

  @Override
  public List<StudentWork> getStudentWork(PeerGroup peerGroup, String nodeId, String componentId) {
    return studentWorkDao.getWorkForComponentByWorkgroups(peerGroup.getMembers(), nodeId,
        componentId);
  }

  @Override
  public List<StudentWork> getLatestStudentWork(PeerGroup peerGroup, String nodeId,
      String componentId) {
    List<StudentWork> allStudentWork = getStudentWork(peerGroup, nodeId, componentId);
    HashMap<Long, StudentWork> workgroupToStudentWork = new HashMap<Long, StudentWork>();
    for (StudentWork studentWork : allStudentWork) {
      workgroupToStudentWork.put(studentWork.getWorkgroup().getId(), studentWork);
    }
    return new ArrayList<StudentWork>(workgroupToStudentWork.values());
  }
}
