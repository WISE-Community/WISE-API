package org.wise.portal.service.peergrouping.logic.impl;

import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.stereotype.Service;
import org.wise.portal.domain.peergrouping.PeerGrouping;
import org.wise.portal.domain.peergrouping.logic.DifferentKIScoreLogic;
import org.wise.portal.domain.workgroup.Workgroup;
import org.wise.portal.service.peergroup.impl.WorkgroupLogicComparable;
import org.wise.portal.service.peergroup.impl.WorkgroupWithDifferentScore;
import org.wise.vle.domain.annotation.wise5.Annotation;

@Service
public class DifferentKIScoreLogicServiceImpl extends PeerGroupAnnotationLogicServiceImpl {

  DifferentKIScoreLogic getLogic(PeerGrouping peerGrouping) {
    return new DifferentKIScoreLogic(peerGrouping.getLogic());
  }

  TreeSet<WorkgroupLogicComparable> getPossibleMembersInOrder(Set<Workgroup> possibleMembers,
      Workgroup workgroup, PeerGrouping peerGrouping) {
    Map<Workgroup, Annotation> workgroupToAnnotation = getWorkgroupToAnnotation(peerGrouping,
        workgroup.getPeriod());
    int workgroupScore = getKIScore(workgroup, workgroupToAnnotation);
    return getWorkgroupsWithScoreDifferences(possibleMembers, workgroupScore, workgroupToAnnotation,
        new DifferentKIScoreLogic(peerGrouping.getLogic()));
  }

  private int getKIScore(Workgroup workgroup, Map<Workgroup, Annotation> workgroupToAnnotation) {
    return workgroupToAnnotation.containsKey(workgroup)
      ? getKIScore(workgroupToAnnotation.get(workgroup)) : 1;
  }

  private int getKIScore(Annotation annotation) {
    try {
      JSONObject dataJson = new JSONObject(annotation.getData());
      JSONArray scores = dataJson.getJSONArray("scores");
      for (int i = 0; i < scores.length(); i++) {
        JSONObject score = scores.getJSONObject(i);
        if (score.getString("id").equals("ki")) {
          return score.getInt("score");
        }
      }
      return 1;
    } catch (JSONException e) {
      return 1;
    }
  }

  private TreeSet<WorkgroupLogicComparable> getWorkgroupsWithScoreDifferences(
      Set<Workgroup> possibleMembers, int workgroupScore,
      Map<Workgroup, Annotation> workgroupToAnnotation, DifferentKIScoreLogic logic) {
    TreeSet<WorkgroupLogicComparable> workgroups = new TreeSet<WorkgroupLogicComparable>();
    for (Workgroup possibleMember : possibleMembers) {
      if (workgroupToAnnotation.containsKey(possibleMember)) {
        int possibleMemberScore = getKIScore(possibleMember, workgroupToAnnotation);
        int scoreDifference = Math.abs(workgroupScore - possibleMemberScore);
        workgroups.add(new WorkgroupWithDifferentScore(possibleMember, scoreDifference, logic));
      }
    }
    return workgroups;
  }
}
