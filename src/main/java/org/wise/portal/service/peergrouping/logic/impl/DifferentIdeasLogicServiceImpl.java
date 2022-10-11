package org.wise.portal.service.peergrouping.logic.impl;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
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

  boolean canCreatePeerGroup(Workgroup workgroup, Set<Workgroup> workgroupsNotInPeerGroup,
      PeerGrouping peerGrouping) {
    List<Annotation> annotations = getIdeaDetectedAnnotations(peerGrouping, workgroup.getPeriod());
    return hasWorkgroupAnnotation(workgroup, annotations)
        && hasEnoughUnpairedMembersIdeasAnnotation(peerGrouping, annotations,
            workgroupsNotInPeerGroup);
  }

  private List<Annotation> getIdeaDetectedAnnotations(PeerGrouping peerGrouping, Group period) {
    DifferentIdeasLogic logic = new DifferentIdeasLogic(peerGrouping.getLogic());
    return annotationDao.getAnnotationsByParams(null, peerGrouping.getRun(), period, null, null,
        logic.getNodeId(), logic.getComponentId(), null, null, null, "autoScore");
  }

  private boolean hasWorkgroupAnnotation(Workgroup workgroup, List<Annotation> annotations) {
    return getLatestWorkgroupAnnotation(workgroup, annotations).isPresent();
  }

  private Optional<Annotation> getLatestWorkgroupAnnotation(Workgroup workgroup,
      List<Annotation> annotations) {
    for (int i = annotations.size() - 1; i >= 0; i--) {
      Annotation annotation = annotations.get(i);
      if (annotation.getToWorkgroup().equals(workgroup)) {
        return Optional.of(annotation);
      }
    }
    return Optional.empty();
  }

  private boolean hasEnoughUnpairedMembersIdeasAnnotation(PeerGrouping peerGrouping,
      List<Annotation> annotations, Set<Workgroup> workgroupsNotInPeerGroup) {
    return workgroupsNotInPeerGroup.stream()
        .filter(workgroup -> hasWorkgroupAnnotation(workgroup, annotations))
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
    List<Annotation> annotations = getIdeaDetectedAnnotations(peerGrouping, workgroup.getPeriod());
    Set<String> workgroupIdeas = getDetectedIdeas(workgroup, annotations);
    return getWorkgroupsWithDifferentIdeas(possibleMembers, workgroupIdeas, annotations);
  }

  private Set<String> getDetectedIdeas(Workgroup workgroup, List<Annotation> annotations) {
    Optional<Annotation> workgroupAnnotation = getLatestWorkgroupAnnotation(workgroup, annotations);
    return workgroupAnnotation.isPresent() ? getDetectedIdeas(workgroupAnnotation.get())
      : new HashSet<String>();
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
      Set<Workgroup> possibleMembers, Set<String> workgroupIdeas, List<Annotation> annotations) {
    TreeSet<WorkgroupWithDifferentIdeas> workgroups = new TreeSet<WorkgroupWithDifferentIdeas>();
    for (Workgroup possibleMember : possibleMembers) {
      if (hasWorkgroupAnnotation(possibleMember, annotations)) {
        Set<String> possibleMemberIdeas = getDetectedIdeas(possibleMember, annotations);
        Set<String> differentIdeas = SetUtils.disjunction(workgroupIdeas, possibleMemberIdeas);
        workgroups.add(new WorkgroupWithDifferentIdeas(possibleMember, differentIdeas.size()));
      }
    }
    return workgroups;
  }
}
