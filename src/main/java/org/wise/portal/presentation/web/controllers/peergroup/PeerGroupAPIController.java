/**
 * Copyright (c) 2008-2022 Regents of the University of California (Regents).
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
package org.wise.portal.presentation.web.controllers.peergroup;

import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.wise.portal.dao.ObjectNotFoundException;
import org.wise.portal.domain.peergroup.PeerGroup;
import org.wise.portal.domain.peergroupactivity.PeerGroupActivity;
import org.wise.portal.domain.run.Run;
import org.wise.portal.domain.user.User;
import org.wise.portal.domain.workgroup.Workgroup;
import org.wise.portal.service.peergroup.PeerGroupActivityThresholdNotSatisfiedException;
import org.wise.portal.service.peergroup.PeerGroupCreationException;
import org.wise.portal.service.peergroup.PeerGroupService;
import org.wise.portal.service.peergroupactivity.PeerGroupActivityNotFoundException;
import org.wise.portal.service.peergroupactivity.PeerGroupActivityService;
import org.wise.portal.service.run.RunService;
import org.wise.portal.service.user.UserService;
import org.wise.portal.service.workgroup.WorkgroupService;

@RestController
@Secured("ROLE_USER")
@RequestMapping("/api/peer-group")
public class PeerGroupAPIController {

  @Autowired
  private RunService runService;

  @Autowired
  private UserService userService;

  @Autowired
  private WorkgroupService workgroupService;

  @Autowired
  private PeerGroupService peerGroupService;

  @Autowired
  private PeerGroupActivityService peerGroupActivityService;

  @GetMapping("/{runId}/{workgroupId}/{peerGroupActivityTag}")
  PeerGroup getPeerGroup(@PathVariable Long runId, @PathVariable Long workgroupId,
      @PathVariable String peerGroupActivityTag, Authentication auth)
      throws JSONException, ObjectNotFoundException, PeerGroupActivityNotFoundException,
      PeerGroupCreationException, PeerGroupActivityThresholdNotSatisfiedException {
    Run run = runService.retrieveById(runId);
    Workgroup workgroup = workgroupService.retrieveById(workgroupId);
    User user = userService.retrieveUserByUsername(auth.getName());
    if (workgroupService.isUserInWorkgroupForRun(user, run, workgroup)) {
      return getPeerGroup(run, peerGroupActivityTag, workgroup);
    } else {
      throw new AccessDeniedException("Not permitted");
    }
  }

  @GetMapping("/{runId}/{workgroupId}/{nodeId}/{componentId}")
  PeerGroup getPeerGroup(@PathVariable Long runId, @PathVariable Long workgroupId,
      @PathVariable String nodeId, @PathVariable String componentId, Authentication auth)
      throws JSONException, ObjectNotFoundException, PeerGroupActivityNotFoundException,
      PeerGroupCreationException, PeerGroupActivityThresholdNotSatisfiedException {
    Run run = runService.retrieveById(runId);
    Workgroup workgroup = workgroupService.retrieveById(workgroupId);
    User user = userService.retrieveUserByUsername(auth.getName());
    if (workgroupService.isUserInWorkgroupForRun(user, run, workgroup)) {
      return getPeerGroup(run, nodeId, componentId, workgroup);
    } else {
      throw new AccessDeniedException("Not permitted");
    }
  }

  private PeerGroup getPeerGroup(Run run, String peerGroupActivityTag, Workgroup workgroup)
      throws JSONException, PeerGroupActivityNotFoundException, PeerGroupCreationException,
      PeerGroupActivityThresholdNotSatisfiedException {
    PeerGroupActivity activity = peerGroupActivityService.getByTag(run, peerGroupActivityTag);
    return peerGroupService.getPeerGroup(workgroup, activity);
  }

  private PeerGroup getPeerGroup(Run run, String nodeId, String componentId, Workgroup workgroup)
      throws JSONException, PeerGroupActivityNotFoundException, PeerGroupCreationException,
      PeerGroupActivityThresholdNotSatisfiedException {
    PeerGroupActivity activity = peerGroupActivityService.getByComponent(run, nodeId, componentId);
    return peerGroupService.getPeerGroup(workgroup, activity);
  }
}
