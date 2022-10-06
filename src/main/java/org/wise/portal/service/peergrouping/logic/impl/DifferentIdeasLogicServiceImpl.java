package org.wise.portal.service.peergrouping.logic.impl;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

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
    List<Annotation> annotations = annotationDao
        .getAnnotations(peerGrouping.getRun(), logic.getNodeId(), logic.getComponentId()).stream()
        .filter(annotation -> annotation.getPeriod().equals(period)
            && annotation.getType().equals("autoScore"))
        .collect(Collectors.toList());
    return annotations;
  }

  private boolean hasWorkgroupAnnotation(Workgroup workgroup, List<Annotation> annotations) {
    return getWorkgroupAnnotation(workgroup, annotations).isPresent();
  }

  private Optional<Annotation> getWorkgroupAnnotation(Workgroup workgroup,
      List<Annotation> annotations) {
    return annotations.stream().filter(annotation -> annotation.getToWorkgroup().equals(workgroup))
        .findFirst();
  }

  private boolean hasEnoughUnpairedMembersIdeasAnnotation(PeerGrouping peerGrouping,
      List<Annotation> annotations, Set<Workgroup> workgroupsNotInPeerGroup) {
    return workgroupsNotInPeerGroup.stream()
        .filter(workgroup -> hasWorkgroupAnnotation(workgroup, annotations)).count() >= 2;
  }

  Set<Workgroup> groupMembersUpToMaxMembership(Workgroup workgroup, PeerGrouping peerGrouping,
      Set<Workgroup> possibleMembers) {
    Set<Workgroup> members = new HashSet<Workgroup>();
    members.add(workgroup);
    possibleMembers.remove(workgroup);
    TreeSet<WorkgroupWithDifferentIdeas> workgroupsWithDifferentIdeas = getWorkgroupsWithDifferentIdeas(
        possibleMembers, workgroup, peerGrouping);
    Iterator<WorkgroupWithDifferentIdeas> iterator = workgroupsWithDifferentIdeas.iterator();
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
    Optional<Annotation> workgroupAnnotation = getWorkgroupAnnotation(workgroup, annotations);
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
