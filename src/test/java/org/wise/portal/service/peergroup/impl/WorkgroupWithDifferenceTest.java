package org.wise.portal.service.peergroup.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.easymock.EasyMockRunner;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wise.portal.domain.peergrouping.logic.DifferentIdeasLogic;
import org.wise.portal.domain.workgroup.Workgroup;
import org.wise.portal.domain.workgroup.impl.WorkgroupImpl;

@RunWith(EasyMockRunner.class)
public class WorkgroupWithDifferenceTest {

  WorkgroupWithDifference wwd1, wwd2, wwd3, wwd4;
  Workgroup workgroup = new WorkgroupImpl();

  @Test
  public void compareTo_AnyMode() {
    DifferentIdeasLogic logicAny = new DifferentIdeasLogic(
        "differentIdeas(\"node1\", \"componentX\", \"any\")");
    wwd1 = new WorkgroupWithDifference(workgroup, 1, logicAny);
    wwd2 = new WorkgroupWithDifference(workgroup, 2, logicAny);
    wwd3 = new WorkgroupWithDifference(workgroup, 1, logicAny);
    wwd4 = new WorkgroupWithDifference(workgroup, 0, logicAny);
    assertEquals(1, wwd1.compareTo(wwd4));
    assertEquals(-1, wwd4.compareTo(wwd1));
    int wwd1_compared_to_wwd2 = wwd1.compareTo(wwd2);
    assertTrue(wwd1_compared_to_wwd2 == -1 || wwd1_compared_to_wwd2 == 1);
    int wwd1_compared_to_wwds3 = wwd1.compareTo(wwd3);
    assertTrue(wwd1_compared_to_wwds3 == -1 || wwd1_compared_to_wwds3 == 1);
  }

  @Test
  public void compareTo_MaximizeMode() {
    DifferentIdeasLogic logicMaximize = new DifferentIdeasLogic(
        "differentIdeas(\"node1\", \"componentX\", \"maximize\")");
    wwd1 = new WorkgroupWithDifference(workgroup, 1, logicMaximize);
    wwd2 = new WorkgroupWithDifference(workgroup, 2, logicMaximize);
    wwd3 = new WorkgroupWithDifference(workgroup, 1, logicMaximize);
    wwd4 = new WorkgroupWithDifference(workgroup, 0, logicMaximize);
    assertEquals(1, wwd1.compareTo(wwd4));
    assertEquals(-1, wwd4.compareTo(wwd1));
    assertEquals(-1, wwd1.compareTo(wwd2));
    assertEquals(1, wwd2.compareTo(wwd1));
    int wwd1_compared_to_wwd3 = wwd1.compareTo(wwd3);
    assertTrue(wwd1_compared_to_wwd3 == -1 || wwd1_compared_to_wwd3 == 1);
  }
}
