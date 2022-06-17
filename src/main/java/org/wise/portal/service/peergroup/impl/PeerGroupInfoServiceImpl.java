package org.wise.portal.service.peergroup.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.wise.portal.dao.ObjectNotFoundException;
import org.wise.portal.domain.peergroup.PeerGroup;
import org.wise.portal.domain.peergrouping.PeerGrouping;
import org.wise.portal.domain.run.Run;
import org.wise.portal.domain.workgroup.Workgroup;
import org.wise.portal.service.peergroup.PeerGroupInfoService;
import org.wise.portal.service.peergroup.PeerGroupService;
import org.wise.portal.service.run.RunService;

/**
 * @author Hiroki Terashima
 */
@Service
public class PeerGroupInfoServiceImpl implements PeerGroupInfoService {

  @Autowired
  private PeerGroupService peerGroupService;

  @Autowired
  private RunService runService;

  @Override
  public Map<String, Object> getPeerGroupInfo(PeerGrouping peerGrouping) {
    Map<String, Object> peerGroupInfo = new HashMap<String, Object>();
    List<PeerGroup> peerGroups = peerGroupService.getPeerGroups(peerGrouping);
    peerGroupInfo.put("peerGroups", peerGroups);
    peerGroupInfo.put("workgroupsNotInPeerGroup",
        getWorkgroupsNotInPeerGroup(peerGrouping.getRun(), peerGroups));
    return peerGroupInfo;
  }

  private List<Workgroup> getWorkgroupsNotInPeerGroup(Run run, List<PeerGroup> peerGroups) {
    Set<Workgroup> workgroupsInPeerGroup = getWorkgroupsInPeerGroup(peerGroups);
    List<Workgroup> workgroupsNotInPeerGroups = new ArrayList<Workgroup>();
    try {
      for (Workgroup workgroupInRun : runService.getWorkgroups(run.getId())) {
        if (isActiveStudentWorkgroup(workgroupInRun) &&
            !workgroupsInPeerGroup.contains(workgroupInRun)) {
          workgroupsNotInPeerGroups.add(workgroupInRun);
        }
      }
    } catch (ObjectNotFoundException e) {
    }
    return workgroupsNotInPeerGroups;
  }

  private Set<Workgroup> getWorkgroupsInPeerGroup(List<PeerGroup> peerGroups) {
    Set<Workgroup> workgroupsInPeerGroup  = new HashSet<Workgroup>();
    for (PeerGroup peerGroup : peerGroups) {
      workgroupsInPeerGroup.addAll(peerGroup.getMembers());
    }
    return workgroupsInPeerGroup;
  }

  private boolean isActiveStudentWorkgroup(Workgroup workgroup) {
    return workgroup.getPeriod() != null && workgroup.isStudentWorkgroup();
  }
}
