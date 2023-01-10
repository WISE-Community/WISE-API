package org.wise.portal.service.peergroup.impl;

import org.wise.portal.domain.peergrouping.logic.AbstractPairingLogic;
import org.wise.portal.domain.peergrouping.logic.LogicMode;
import org.wise.portal.domain.workgroup.Workgroup;

import lombok.Getter;

@Getter
public class WorkgroupWithDifference
    implements Comparable<WorkgroupWithDifference>, WorkgroupLogicComparable {

  AbstractPairingLogic logic;
  int difference;
  Workgroup workgroup;

  public WorkgroupWithDifference(Workgroup workgroup, int difference, AbstractPairingLogic logic) {
    this.logic = logic;
    this.workgroup = workgroup;
    this.difference = difference;
  }

  public int compareTo(WorkgroupWithDifference o) {
    return this.logic.getMode().equals(LogicMode.ANY) ? compareToAnyMode(o)
      : compareToMaximizeMode(o);
  }

  private int compareToAnyMode(WorkgroupWithDifference o) {
    if (this.difference == 0) {
      return -1;
    } else if (o.difference == 0) {
      return 1;
    } else {
      return randomInt();
    }
  }

  private int compareToMaximizeMode(WorkgroupWithDifference o) {
    return this.difference == o.difference ? randomInt() : this.difference - o.difference;
  }

  private int randomInt() {
    return (Math.random() <= 0.5) ? -1 : 1;
  }
}
