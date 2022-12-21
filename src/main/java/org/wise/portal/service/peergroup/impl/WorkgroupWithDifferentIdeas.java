package org.wise.portal.service.peergroup.impl;

import org.wise.portal.domain.peergrouping.logic.DifferentIdeasLogic;
import org.wise.portal.domain.peergrouping.logic.DifferentIdeasLogicMode;
import org.wise.portal.domain.workgroup.Workgroup;

import lombok.Getter;

@Getter
public class WorkgroupWithDifferentIdeas
    implements Comparable<WorkgroupWithDifferentIdeas>, WorkgroupLogicComparable {
  private DifferentIdeasLogic logic;
  private Workgroup workgroup;
  private int numDifferentIdeas;

  public WorkgroupWithDifferentIdeas(Workgroup workgroup, int numDifferentIdeas,
      DifferentIdeasLogic logic) {
    this.logic = logic;
    this.workgroup = workgroup;
    this.numDifferentIdeas = numDifferentIdeas;
  }

  @Override
  public int compareTo(WorkgroupWithDifferentIdeas o) {
    return this.logic.getMode().equals(DifferentIdeasLogicMode.ANY) ? compareToAnyMode(o)
      : compareToMaximizeMode(o);
  }

  private int compareToAnyMode(WorkgroupWithDifferentIdeas o) {
    if (this.numDifferentIdeas == 0) {
      return -1;
    } else if (o.numDifferentIdeas == 0) {
      return 1;
    } else {
      return randomInt();
    }
  }

  private int compareToMaximizeMode(WorkgroupWithDifferentIdeas o) {
    return this.numDifferentIdeas == o.numDifferentIdeas ? randomInt()
      : this.numDifferentIdeas - o.numDifferentIdeas;
  }

  private int randomInt() {
    return (Math.random() <= 0.5) ? -1 : 1;
  }
}
