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
import java.util.HashSet;
import java.util.List;
import java.util.Random;
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
import org.wise.portal.domain.peergrouping.PeerGrouping;
import org.wise.portal.domain.run.Run;
import org.wise.portal.domain.workgroup.Workgroup;
import org.wise.portal.service.peergroup.PeerGroupCreationException;
import org.wise.portal.service.peergroup.PeerGroupingThresholdNotSatisfiedException;
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
  public PeerGroup getPeerGroup(Workgroup workgroup, PeerGrouping peerGrouping)
      throws JSONException, PeerGroupingThresholdNotSatisfiedException,
      PeerGroupCreationException {
    PeerGroup peerGroup = peerGroupDao.getByWorkgroupAndPeerGrouping(workgroup, peerGrouping);
    if (peerGrouping.getLogic().contains("manual")) {
      // use contains check instead of equals until custom logic is implemented for
      // backwards-compatibility
      return peerGroup;
    } else {
      return peerGroup != null ? peerGroup : this.createPeerGroup(workgroup, peerGrouping);
    }
  }

  private PeerGroup createPeerGroup(Workgroup workgroup, PeerGrouping peerGrouping)
      throws PeerGroupingThresholdNotSatisfiedException, PeerGroupCreationException {
    if (!isCompletionThresholdSatisfied(peerGrouping, workgroup)) {
      throw new PeerGroupingThresholdNotSatisfiedException();
    } else if (!peerGroupThresholdService.canCreatePeerGroup(peerGrouping, workgroup.getPeriod())) {
      throw new PeerGroupCreationException();
    } else {
      PeerGroup peerGroup = new PeerGroupImpl(peerGrouping, workgroup.getPeriod(),
          getPeerGroupMembers(workgroup, peerGrouping));
      this.peerGroupDao.save(peerGroup);
      return peerGroup;
    }
  }

  private boolean isCompletionThresholdSatisfied(PeerGrouping peerGrouping, Workgroup workgroup) {
    return hasWorkForPeerGroupingLogic(workgroup, peerGrouping) && peerGroupThresholdService
        .isCompletionThresholdSatisfied(peerGrouping, workgroup.getPeriod());
  }

  private boolean hasWorkForPeerGroupingLogic(Workgroup workgroup,
      PeerGrouping peerGrouping) {
    try {
      return studentWorkDao.getWorkForComponentByWorkgroup(workgroup, peerGrouping.getLogicNodeId(),
          peerGrouping.getLogicComponentId()).size() > 0;
    } catch (JSONException e) {
      return false;
    }
  }

  private Set<Workgroup> getPeerGroupMembers(Workgroup workgroup, PeerGrouping peerGrouping)
      throws PeerGroupCreationException {
    try {
      List<Workgroup> workgroupsInPeerGroup = peerGroupDao.getWorkgroupsInPeerGroup(peerGrouping,
          workgroup.getPeriod());
      Set<Workgroup> workgroupsNotInPeerGroupAndCompletedPeerGroupingLogic =
          getWorkgroupsNotInPeerGroupAndCompletedPeerGroupingLogic(
          peerGrouping, workgroup.getPeriod(), workgroupsInPeerGroup);
      if (isLastOnesLeftToPair(workgroup.getRun(), workgroup.getPeriod(), workgroupsInPeerGroup,
          workgroupsNotInPeerGroupAndCompletedPeerGroupingLogic,
          peerGrouping.getMaxMembershipCount())) {
        return workgroupsNotInPeerGroupAndCompletedPeerGroupingLogic;
      } else {
        return getWorkgroupsInPeerGroupUpToMaxMembership(workgroup, peerGrouping,
            workgroupsNotInPeerGroupAndCompletedPeerGroupingLogic);
      }
    } catch (JSONException e) {
      throw new PeerGroupCreationException();
    }
  }

  private boolean isLastOnesLeftToPair(Run run, Group period, List<Workgroup> workgroupsInPeerGroup,
      Set<Workgroup> workgroupsNotInPeerGroupAndCompletedPeerGroupingLogic,
      int maxMembershipCount) {
    List<Workgroup> workgroupsInPeriod = runDao.getWorkgroupsForRunAndPeriod(run.getId(),
        period.getId());
    workgroupsInPeriod.removeIf(workgroup -> workgroup.getMembers().size() == 0);
    int numWorkgroupsNotInPeerGroup = workgroupsInPeriod.size() - workgroupsInPeerGroup.size();
    int numWorkgroupsNotInPeerGroupAndCompletedPeerGroupingLogic =
        workgroupsNotInPeerGroupAndCompletedPeerGroupingLogic.size();
    return numWorkgroupsNotInPeerGroup == numWorkgroupsNotInPeerGroupAndCompletedPeerGroupingLogic
        && (numWorkgroupsNotInPeerGroupAndCompletedPeerGroupingLogic - maxMembershipCount) <= 1;
  }

  private Set<Workgroup> getWorkgroupsInPeerGroupUpToMaxMembership(Workgroup workgroup,
      PeerGrouping peerGrouping, Set<Workgroup> possibleMembers) throws JSONException {
    Set<Workgroup> members = new HashSet<Workgroup>();
    members.add(workgroup);
    possibleMembers.remove(workgroup);
    if (peerGrouping.getLogic().contains("random")) {
      // use contains check instead of equals until custom logic is implemented for
      // backwards-compatibility
      addMembersRandomly(peerGrouping, possibleMembers, members);
    } else {
      addMembersInOrder(peerGrouping, possibleMembers, members);
    }
    return members;
  }

  private void addMembersRandomly(PeerGrouping peerGrouping, Set<Workgroup> possibleMembers,
      Set<Workgroup> members) {
    List<Workgroup> possibleMembersList = new ArrayList<Workgroup>(possibleMembers);
    Random random = new Random();
    while (members.size() < peerGrouping.getMaxMembershipCount()) {
      int randomInt = random.nextInt(possibleMembersList.size());
      Workgroup randomWorkgroup = possibleMembersList.get(randomInt);
      members.add(randomWorkgroup);
      possibleMembersList.remove(randomWorkgroup);
    }
  }

  private void addMembersInOrder(PeerGrouping peerGrouping, Set<Workgroup> possibleMembers,
      Set<Workgroup> members) {
    for (Workgroup possibleMember : possibleMembers) {
      members.add(possibleMember);
      if (members.size() == peerGrouping.getMaxMembershipCount()) {
        break;
      }
    }
  }

  private Set<Workgroup> getWorkgroupsNotInPeerGroupAndCompletedPeerGroupingLogic(
      PeerGrouping peerGrouping, Group period, List<Workgroup> workgroupsInPeerGroup)
      throws JSONException {
    List<StudentWork> logicComponentStudentWork = getLogicComponentStudentWorkForPeriod(
        peerGrouping, period);
    Set<Workgroup> workgroupsNotInPeerGroup = new HashSet<Workgroup>();
    for (StudentWork work : logicComponentStudentWork) {
      if (!workgroupsInPeerGroup.contains(work.getWorkgroup())) {
        workgroupsNotInPeerGroup.add(work.getWorkgroup());
      }
    }
    return workgroupsNotInPeerGroup;
  }

  private List<StudentWork> getLogicComponentStudentWorkForPeriod(PeerGrouping peerGrouping,
      Group period) throws JSONException {
    List<StudentWork> logicComponentStudentWorkForPeriod = studentWorkDao
        .getWorkForComponentByPeriod(peerGrouping.getRun(), period, peerGrouping.getLogicNodeId(),
            peerGrouping.getLogicComponentId());
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
  public List<PeerGroup> getPeerGroups(PeerGrouping peerGrouping) {
    return peerGroupDao.getListByPeerGrouping(peerGrouping);
  }

  @Override
  public List<StudentWork> getStudentWork(PeerGroup peerGroup, String nodeId, String componentId) {
    return studentWorkDao.getStudentWork(peerGroup, nodeId, componentId);
  }

}
