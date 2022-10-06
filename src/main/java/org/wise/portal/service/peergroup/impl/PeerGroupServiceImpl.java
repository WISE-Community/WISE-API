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
import org.wise.portal.dao.ObjectNotFoundException;
import org.wise.portal.dao.peergroup.PeerGroupDao;
import org.wise.portal.dao.work.StudentWorkDao;
import org.wise.portal.domain.peergroup.PeerGroup;
import org.wise.portal.domain.peergrouping.PeerGrouping;
import org.wise.portal.domain.workgroup.Workgroup;
import org.wise.portal.service.peergroup.PeerGroupCreationException;
import org.wise.portal.service.peergroup.PeerGroupService;
import org.wise.portal.service.peergroup.PeerGroupThresholdService;
import org.wise.portal.service.peergrouping.logic.impl.DifferentIdeasLogicServiceImpl;
import org.wise.portal.service.peergrouping.logic.impl.RandomLogicServiceImpl;
import org.wise.vle.domain.work.StudentWork;

@Service
public class PeerGroupServiceImpl implements PeerGroupService {

  @Autowired
  private DifferentIdeasLogicServiceImpl differentIdeasLogicService;

  @Autowired
  private PeerGroupDao<PeerGroup> peerGroupDao;

  @Autowired
  private PeerGroupThresholdService peerGroupThresholdService;

  @Autowired
  private RandomLogicServiceImpl randomLogicService;

  @Autowired
  private StudentWorkDao<StudentWork> studentWorkDao;

  public PeerGroup getById(Long id) throws ObjectNotFoundException {
    return peerGroupDao.getById(id);
  }

  public PeerGroup getPeerGroup(Workgroup workgroup, PeerGrouping peerGrouping)
      throws PeerGroupCreationException {
    PeerGroup peerGroup = peerGroupDao.getByWorkgroupAndPeerGrouping(workgroup, peerGrouping);
    if (peerGrouping.getLogic().equals("manual")) {
      return peerGroup;
    } else {
      return peerGroup != null ? peerGroup : createPeerGroup(workgroup, peerGrouping);
    }
  }

  private PeerGroup createPeerGroup(Workgroup workgroup, PeerGrouping peerGrouping)
      throws PeerGroupCreationException {
    if (peerGroupThresholdService.isThresholdSatisfied(peerGrouping, workgroup.getPeriod())) {
      return peerGrouping.getLogic().equals("random")
        ? randomLogicService.createPeerGroup(workgroup, peerGrouping)
        : differentIdeasLogicService.createPeerGroup(workgroup, peerGrouping);
    }
    return null;
  }

  public List<PeerGroup> getPeerGroups(PeerGrouping peerGrouping) {
    return peerGroupDao.getListByPeerGrouping(peerGrouping);
  }

  public List<StudentWork> getStudentWork(PeerGroup peerGroup, String nodeId, String componentId) {
    return studentWorkDao.getStudentWork(peerGroup, nodeId, componentId);
  }
}
