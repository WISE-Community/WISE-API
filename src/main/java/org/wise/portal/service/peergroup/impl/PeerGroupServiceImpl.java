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
import java.util.Random;
import java.util.Set;

import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.wise.portal.dao.ObjectNotFoundException;
import org.wise.portal.dao.peergroup.PeerGroupDao;
import org.wise.portal.dao.run.RunDao;
import org.wise.portal.dao.work.StudentWorkDao;
import org.wise.portal.domain.peergroup.PeerGroup;
import org.wise.portal.domain.peergroup.impl.PeerGroupImpl;
import org.wise.portal.domain.peergrouping.PeerGrouping;
import org.wise.portal.domain.run.Run;
import org.wise.portal.domain.workgroup.Workgroup;
import org.wise.portal.service.peergroup.PeerGroupCreationException;
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
      throws JSONException, PeerGroupCreationException {
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
      throws PeerGroupCreationException {
    if (!peerGroupThresholdService.canCreatePeerGroup(peerGrouping, workgroup.getPeriod())) {
      return null;
    } else {
      PeerGroup peerGroup = new PeerGroupImpl(peerGrouping, workgroup.getPeriod(),
          getPeerGroupMembers(workgroup, peerGrouping));
      this.peerGroupDao.save(peerGroup);
      return peerGroup;
    }
  }

  private Set<Workgroup> getPeerGroupMembers(Workgroup workgroup, PeerGrouping peerGrouping)
      throws PeerGroupCreationException {
    try {
      List<Workgroup> workgroupsInPeriod = runDao.getWorkgroupsForRunAndPeriod(
          workgroup.getRun().getId(), workgroup.getPeriod().getId());
      workgroupsInPeriod.removeIf(workgroupInPeriod -> workgroupInPeriod.getMembers().size() == 0);
      List<Workgroup> workgroupsInPeerGroup = peerGroupDao.getWorkgroupsInPeerGroup(peerGrouping,
          workgroup.getPeriod());
      Set<Workgroup> workgroupsNotInPeerGroup = getWorkgroupsNotInPeerGroup(workgroupsInPeriod,
          workgroupsInPeerGroup);
      if (isLastOnesLeftToPair(workgroupsInPeerGroup, workgroupsInPeriod,
          peerGrouping.getMaxMembershipCount())) {
        return workgroupsNotInPeerGroup;
      } else {
        return getWorkgroupsInPeerGroupUpToMaxMembership(workgroup, peerGrouping,
            workgroupsNotInPeerGroup);
      }
    } catch (JSONException e) {
      throw new PeerGroupCreationException();
    }
  }

  private boolean isLastOnesLeftToPair(List<Workgroup> workgroupsInPeerGroup,
      List<Workgroup> workgroupsInPeriod, int maxMembershipCount) {
    int numWorkgroupsNotInPeerGroup = workgroupsInPeriod.size() - workgroupsInPeerGroup.size();
    return (numWorkgroupsNotInPeerGroup - maxMembershipCount) <= 1;
  }

  private Set<Workgroup> getWorkgroupsInPeerGroupUpToMaxMembership(Workgroup workgroup,
      PeerGrouping peerGrouping, Set<Workgroup> possibleMembers) throws JSONException {
    Set<Workgroup> members = new HashSet<Workgroup>();
    members.add(workgroup);
    possibleMembers.remove(workgroup);
    addMembersRandomly(peerGrouping, possibleMembers, members);
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

  private Set<Workgroup> getWorkgroupsNotInPeerGroup(List<Workgroup> workgroupsInPeriod,
      List<Workgroup> workgroupsInPeerGroup) throws JSONException {
    Set<Workgroup> workgroupsNotInPeerGroup = new HashSet<Workgroup>();
    for (Workgroup workgroup : workgroupsInPeriod) {
      if (!workgroupsInPeerGroup.contains(workgroup)) {
        workgroupsNotInPeerGroup.add(workgroup);
      }
    }
    return workgroupsNotInPeerGroup;
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
