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
public class WorkgroupWithDifferentIdeasTest {

  WorkgroupWithDifferentIdeas wwdi1, wwdi2, wwdi3, wwdi4;
  Workgroup workgroup = new WorkgroupImpl();

  @Test
  public void compareTo_AnyMode() {
    DifferentIdeasLogic logicAny = new DifferentIdeasLogic(
        "differentIdeas(\"node1\", \"componentX\", \"any\")");
    wwdi1 = new WorkgroupWithDifferentIdeas(workgroup, 1, logicAny);
    wwdi2 = new WorkgroupWithDifferentIdeas(workgroup, 2, logicAny);
    wwdi3 = new WorkgroupWithDifferentIdeas(workgroup, 1, logicAny);
    wwdi4 = new WorkgroupWithDifferentIdeas(workgroup, 0, logicAny);
    assertEquals(1, wwdi1.compareTo(wwdi4));
    assertEquals(-1, wwdi4.compareTo(wwdi1));
    int wwdi1_compared_to_wwdi2 = wwdi1.compareTo(wwdi2);
    assertTrue(wwdi1_compared_to_wwdi2 == -1 || wwdi1_compared_to_wwdi2 == 1);
    int wwdi1_compared_to_wwds3 = wwdi1.compareTo(wwdi3);
    assertTrue(wwdi1_compared_to_wwds3 == -1 || wwdi1_compared_to_wwds3 == 1);
  }

  @Test
  public void compareTo_MaximizeMode() {
    DifferentIdeasLogic logicMaximize = new DifferentIdeasLogic(
        "differentIdeas(\"node1\", \"componentX\", \"maximize\")");
    wwdi1 = new WorkgroupWithDifferentIdeas(workgroup, 1, logicMaximize);
    wwdi2 = new WorkgroupWithDifferentIdeas(workgroup, 2, logicMaximize);
    wwdi3 = new WorkgroupWithDifferentIdeas(workgroup, 1, logicMaximize);
    wwdi4 = new WorkgroupWithDifferentIdeas(workgroup, 0, logicMaximize);
    assertEquals(1, wwdi1.compareTo(wwdi4));
    assertEquals(-1, wwdi4.compareTo(wwdi1));
    assertEquals(-1, wwdi1.compareTo(wwdi2));
    assertEquals(1, wwdi2.compareTo(wwdi1));
    int wwdi1_compared_to_wwdi3 = wwdi1.compareTo(wwdi3);
    assertTrue(wwdi1_compared_to_wwdi3 == -1 || wwdi1_compared_to_wwdi3 == 1);
  }
}
