package org.wise.portal.service.peergroup.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.easymock.EasyMockRunner;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wise.portal.domain.peergrouping.logic.DifferentKIScoresLogic;
import org.wise.portal.domain.workgroup.Workgroup;
import org.wise.portal.domain.workgroup.impl.WorkgroupImpl;

@RunWith(EasyMockRunner.class)
public class WorkgroupWithDifferentScoreTest {

  WorkgroupWithDifferentScore wwds1, wwds2, wwds3, wwds4;
  Workgroup workgroup = new WorkgroupImpl();

  @Test
  public void compareTo_AnyMode() {
    DifferentKIScoresLogic logicAny = new DifferentKIScoresLogic(
        "differentKIScores(\"node1\", \"componentX\", \"any\")");
    wwds1 = new WorkgroupWithDifferentScore(workgroup, 1, logicAny);
    wwds2 = new WorkgroupWithDifferentScore(workgroup, 2, logicAny);
    wwds3 = new WorkgroupWithDifferentScore(workgroup, 1, logicAny);
    wwds4 = new WorkgroupWithDifferentScore(workgroup, 0, logicAny);
    assertEquals(1, wwds1.compareTo(wwds4));
    assertEquals(-1, wwds4.compareTo(wwds1));
    int wwds1_compared_to_wwds2 = wwds1.compareTo(wwds2);
    assertTrue(wwds1_compared_to_wwds2 == -1 || wwds1_compared_to_wwds2 == 1);
    int wwds1_compared_to_wwds3 = wwds1.compareTo(wwds3);
    assertTrue(wwds1_compared_to_wwds3 == -1 || wwds1_compared_to_wwds3 == 1);
  }

  @Test
  public void compareTo_MaximizeMode() {
    DifferentKIScoresLogic logicMaximize = new DifferentKIScoresLogic(
        "differentKIScores(\"node1\", \"componentX\", \"maximize\")");
    wwds1 = new WorkgroupWithDifferentScore(workgroup, 1, logicMaximize);
    wwds2 = new WorkgroupWithDifferentScore(workgroup, 2, logicMaximize);
    wwds3 = new WorkgroupWithDifferentScore(workgroup, 1, logicMaximize);
    wwds4 = new WorkgroupWithDifferentScore(workgroup, 0, logicMaximize);
    assertEquals(1, wwds1.compareTo(wwds4));
    assertEquals(-1, wwds4.compareTo(wwds1));
    assertEquals(-1, wwds1.compareTo(wwds2));
    assertEquals(1, wwds2.compareTo(wwds1));
    int wwds1_compared_to_wwds3 = wwds1.compareTo(wwds3);
    assertTrue(wwds1_compared_to_wwds3 == -1 || wwds1_compared_to_wwds3 == 1);
  }
}
