package org.wise.portal.service.peergroup.impl;

import org.wise.portal.domain.workgroup.Workgroup;

import lombok.Getter;

@Getter
public class WorkgroupWithDifferentIdeas implements Comparable<WorkgroupWithDifferentIdeas> {
  private Workgroup workgroup;
  private int numDifferentIdeas;

  public WorkgroupWithDifferentIdeas(Workgroup workgroup, int numDifferentIdeas) {
    this.workgroup = workgroup;
    this.numDifferentIdeas = numDifferentIdeas;
  }

  @Override
  public int compareTo(WorkgroupWithDifferentIdeas o) {
    if (this.numDifferentIdeas > o.numDifferentIdeas) {
      return -1;
    } else if (this.numDifferentIdeas < o.numDifferentIdeas) {
      return 1;
    } else {
      return 0;
    }
  }
}
