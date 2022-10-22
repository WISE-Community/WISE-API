package org.wise.portal.service.peergrouping.logic.impl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.springframework.beans.factory.annotation.Autowired;
import org.wise.portal.dao.annotation.wise5.AnnotationDao;
import org.wise.portal.domain.group.Group;
import org.wise.portal.domain.peergrouping.PeerGrouping;
import org.wise.portal.domain.peergrouping.logic.AbstractPairingLogic;
import org.wise.portal.domain.workgroup.Workgroup;
import org.wise.portal.service.peergroup.impl.WorkgroupLogicComparable;
import org.wise.vle.domain.annotation.wise5.Annotation;

public abstract class PeerGroupAnnotationLogicServiceImpl extends PeerGroupLogicServiceImpl {

  @Autowired
  AnnotationDao<Annotation> annotationDao;

  abstract AbstractPairingLogic getLogic(PeerGrouping peerGrouping);

  boolean canCreatePeerGroup(Workgroup workgroup, Set<Workgroup> workgroupsNotInAPeerGroup,
      PeerGrouping peerGrouping) {
    Map<Workgroup, Annotation> workgroupToAnnotation = getWorkgroupToAnnotation(peerGrouping,
        workgroup.getPeriod());
    return workgroupToAnnotation.containsKey(workgroup)
        && hasEnoughUnpairedMembers(peerGrouping, workgroupToAnnotation, workgroupsNotInAPeerGroup);
  }

  protected boolean hasEnoughUnpairedMembers(PeerGrouping peerGrouping,
      Map<Workgroup, Annotation> workgroupToAnnotation, Set<Workgroup> workgroupsNotInAPeerGroup) {
    return workgroupsNotInAPeerGroup.stream()
        .filter(workgroup -> workgroupToAnnotation.containsKey(workgroup))
        .count() >= peerGrouping.getMaxMembershipCount();
  }

  protected Map<Workgroup, Annotation> getWorkgroupToAnnotation(PeerGrouping peerGrouping,
      Group period) {
    Map<Workgroup, Annotation> workgroupToAnnotation = new HashMap<Workgroup, Annotation>();
    for (Annotation annotation : getAutoScoreAnnotations(peerGrouping, period)) {
      Workgroup workgroup = annotation.getToWorkgroup();
      workgroupToAnnotation.put(workgroup, annotation);
    }
    return workgroupToAnnotation;
  }

  private List<Annotation> getAutoScoreAnnotations(PeerGrouping peerGrouping, Group period) {
    AbstractPairingLogic logic = this.getLogic(peerGrouping);
    return annotationDao.getAnnotationsByParams(null, peerGrouping.getRun(), period, null, null,
        logic.getNodeId(), logic.getComponentId(), null, null, null, "autoScore");
  }

  Set<Workgroup> groupMembersUpToMaxMembership(Workgroup workgroup, PeerGrouping peerGrouping,
      Set<Workgroup> possibleMembers) {
    Set<Workgroup> members = new HashSet<Workgroup>();
    members.add(workgroup);
    possibleMembers.remove(workgroup);
    TreeSet<WorkgroupLogicComparable> possibleMembersInOrder = getPossibleMembersInOrder(
        possibleMembers, workgroup, peerGrouping);
    Iterator<WorkgroupLogicComparable> iterator = possibleMembersInOrder.descendingIterator();
    while (members.size() < peerGrouping.getMaxMembershipCount()) {
      members.add(iterator.next().getWorkgroup());
    }
    return members;
  }

  abstract TreeSet<WorkgroupLogicComparable> getPossibleMembersInOrder(
      Set<Workgroup> possibleMembers, Workgroup workgroup, PeerGrouping peerGrouping);
}
