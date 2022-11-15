package org.wise.portal.service.peergroup.impl;

import org.wise.portal.domain.peergrouping.logic.DifferentKIScoresLogic;
import org.wise.portal.domain.peergrouping.logic.DifferentKIScoresLogicMode;
import org.wise.portal.domain.workgroup.Workgroup;
import lombok.Getter;

@Getter
public class WorkgroupWithDifferentScore
    implements Comparable<WorkgroupWithDifferentScore>, WorkgroupLogicComparable {
  private DifferentKIScoresLogic logic;
  private int scoreDifference;
  private Workgroup workgroup;

  public WorkgroupWithDifferentScore(Workgroup workgroup, int scoreDifference,
      DifferentKIScoresLogic logic) {
    this.logic = logic;
    this.workgroup = workgroup;
    this.scoreDifference = scoreDifference;
  }

  public int compareTo(WorkgroupWithDifferentScore o) {
    return this.logic.getMode().equals(DifferentKIScoresLogicMode.ANY) ? compareToAnyMode(o)
      : compareToMaximizeMode(o);
  }

  private int compareToAnyMode(WorkgroupWithDifferentScore o) {
    if (this.scoreDifference == 0) {
      return -1;
    } else if (o.scoreDifference == 0) {
      return 1;
    } else {
      return randomInt();
    }
  }

  private int compareToMaximizeMode(WorkgroupWithDifferentScore o) {
    return this.scoreDifference == o.scoreDifference ? randomInt()
      : this.scoreDifference - o.scoreDifference;
  }

  private int randomInt() {
    return (Math.random() <= 0.5) ? -1 : 1;
  }
}
