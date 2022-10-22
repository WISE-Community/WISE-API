package org.wise.portal.service.peergrouping.logic.impl;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.collections4.SetUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.stereotype.Service;
import org.wise.portal.domain.peergrouping.PeerGrouping;
import org.wise.portal.domain.peergrouping.logic.DifferentIdeasLogic;
import org.wise.portal.domain.workgroup.Workgroup;
import org.wise.portal.service.peergroup.impl.WorkgroupLogicComparable;
import org.wise.portal.service.peergroup.impl.WorkgroupWithDifferentIdeas;
import org.wise.vle.domain.annotation.wise5.Annotation;

@Service
public class DifferentIdeasLogicServiceImpl extends PeerGroupAnnotationLogicServiceImpl {

  DifferentIdeasLogic getLogic(PeerGrouping peerGrouping) {
    return new DifferentIdeasLogic(peerGrouping.getLogic());
  }

  TreeSet<WorkgroupLogicComparable> getPossibleMembersInOrder(Set<Workgroup> possibleMembers,
      Workgroup workgroup, PeerGrouping peerGrouping) {
    Map<Workgroup, Annotation> workgroupToAnnotation = getWorkgroupToAnnotation(peerGrouping,
        workgroup.getPeriod());
    Set<String> workgroupIdeas = getDetectedIdeas(workgroup, workgroupToAnnotation);
    return getWorkgroupsWithDifferentIdeas(possibleMembers, workgroupIdeas, workgroupToAnnotation);
  }

  private Set<String> getDetectedIdeas(Workgroup workgroup,
      Map<Workgroup, Annotation> workgroupToAnnotation) {
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

  private TreeSet<WorkgroupLogicComparable> getWorkgroupsWithDifferentIdeas(
      Set<Workgroup> possibleMembers, Set<String> workgroupIdeas,
      Map<Workgroup, Annotation> workgroupToAnnotation) {
    TreeSet<WorkgroupLogicComparable> workgroups = new TreeSet<WorkgroupLogicComparable>();
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
