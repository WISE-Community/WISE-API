package org.wise.portal.service.peergrouping.logic.impl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.collections4.SetUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.wise.portal.dao.annotation.wise5.AnnotationDao;
import org.wise.portal.domain.group.Group;
import org.wise.portal.domain.peergrouping.PeerGrouping;
import org.wise.portal.domain.peergrouping.logic.DifferentIdeasLogic;
import org.wise.portal.domain.workgroup.Workgroup;
import org.wise.portal.service.peergroup.impl.WorkgroupWithDifferentIdeas;
import org.wise.vle.domain.annotation.wise5.Annotation;

@Service
public class DifferentIdeasLogicServiceImpl extends PeerGroupLogicServiceImpl {

  @Autowired
  private AnnotationDao<Annotation> annotationDao;

  boolean canCreatePeerGroup(Workgroup workgroup, Set<Workgroup> workgroupsNotInAPeerGroup,
      PeerGrouping peerGrouping) {
    HashMap<Workgroup, Annotation> workgroupToAnnotation = getWorkgroupToAnnotation(peerGrouping,
        workgroup.getPeriod());
    return workgroupToAnnotation.containsKey(workgroup) && hasEnoughUnpairedMembersIdeasAnnotation(
        peerGrouping, workgroupToAnnotation, workgroupsNotInAPeerGroup);
  }

  private HashMap<Workgroup, Annotation> getWorkgroupToAnnotation(PeerGrouping peerGrouping,
      Group period) {
    HashMap<Workgroup, Annotation> workgroupToAnnotation = new HashMap<Workgroup, Annotation>();
    for (Annotation annotation : getAutoScoreAnnotations(peerGrouping, period)) {
      Workgroup workgroup = annotation.getToWorkgroup();
      workgroupToAnnotation.put(workgroup, annotation);
    }
    return workgroupToAnnotation;
  }

  private List<Annotation> getAutoScoreAnnotations(PeerGrouping peerGrouping, Group period) {
    DifferentIdeasLogic logic = new DifferentIdeasLogic(peerGrouping.getLogic());
    return annotationDao.getAnnotationsByParams(null, peerGrouping.getRun(), period, null, null,
        logic.getNodeId(), logic.getComponentId(), null, null, null, "autoScore");
  }

  private boolean hasEnoughUnpairedMembersIdeasAnnotation(PeerGrouping peerGrouping,
      HashMap<Workgroup, Annotation> workgroupToAnnotation,
      Set<Workgroup> workgroupsNotInAPeerGroup) {
    return workgroupsNotInAPeerGroup.stream()
        .filter(workgroup -> workgroupToAnnotation.containsKey(workgroup))
        .count() >= peerGrouping.getMaxMembershipCount();
  }

  Set<Workgroup> groupMembersUpToMaxMembership(Workgroup workgroup, PeerGrouping peerGrouping,
      Set<Workgroup> possibleMembers) {
    Set<Workgroup> members = new HashSet<Workgroup>();
    members.add(workgroup);
    possibleMembers.remove(workgroup);
    TreeSet<WorkgroupWithDifferentIdeas> workgroupsWithDifferentIdeas = getWorkgroupsWithDifferentIdeas(
        possibleMembers, workgroup, peerGrouping);
    Iterator<WorkgroupWithDifferentIdeas> iterator = workgroupsWithDifferentIdeas
        .descendingIterator();
    while (members.size() < peerGrouping.getMaxMembershipCount()) {
      members.add(iterator.next().getWorkgroup());
    }
    return members;
  }

  private TreeSet<WorkgroupWithDifferentIdeas> getWorkgroupsWithDifferentIdeas(
      Set<Workgroup> possibleMembers, Workgroup workgroup, PeerGrouping peerGrouping) {
    HashMap<Workgroup, Annotation> workgroupToAnnotation = getWorkgroupToAnnotation(peerGrouping,
        workgroup.getPeriod());
    Set<String> workgroupIdeas = getDetectedIdeas(workgroup, workgroupToAnnotation);
    return getWorkgroupsWithDifferentIdeas(possibleMembers, workgroupIdeas, workgroupToAnnotation);
  }

  private Set<String> getDetectedIdeas(Workgroup workgroup,
      HashMap<Workgroup, Annotation> workgroupToAnnotation) {
    return workgroupToAnnotation.containsKey(workgroup)
      ? getDetectedIdeas(workgroupToAnnotation.get(workgroup)) : new HashSet<String>();
  }

  private Set<String> getDetectedIdeas(Annotation annotation) {
    try {
      JSONObject dataJson = new JSONObject(annotation.getData());
      JSONArray ideas = dataJson.getJSONArray("ideas");
      Set<String> detectedIdeas = new HashSet<String>();
      for (int i = 0; i < ideas.length(); i++) {
        JSONObject idea = ideas.getJSONObject(i);
        if (idea.getBoolean("detected")) {
          detectedIdeas.add(idea.getString("name"));
        }
      }
      return detectedIdeas;
    } catch (JSONException e) {
      return new HashSet<String>();
    }
  }

  private TreeSet<WorkgroupWithDifferentIdeas> getWorkgroupsWithDifferentIdeas(
      Set<Workgroup> possibleMembers, Set<String> workgroupIdeas,
      HashMap<Workgroup, Annotation> workgroupToAnnotation) {
    TreeSet<WorkgroupWithDifferentIdeas> workgroups = new TreeSet<WorkgroupWithDifferentIdeas>();
    for (Workgroup possibleMember : possibleMembers) {
      if (workgroupToAnnotation.containsKey(possibleMember)) {
        Set<String> possibleMemberIdeas = getDetectedIdeas(possibleMember, workgroupToAnnotation);
        Set<String> differentIdeas = SetUtils.disjunction(workgroupIdeas, possibleMemberIdeas);
        workgroups.add(new WorkgroupWithDifferentIdeas(possibleMember, differentIdeas.size()));
      }
    }
    return workgroups;
  }
}
