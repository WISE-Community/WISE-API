package org.wise.portal.domain.peergrouping.logic;

import static org.junit.Assert.assertEquals;

import org.easymock.EasyMockRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(EasyMockRunner.class)
public class DifferentKIScoresLogicTest {

  private DifferentKIScoresLogic logicBasic, logicWithMode;
  private String logicStringBasic = "differentKIScores(\"node1\", \"componentX\")";
  private String logicStringWithMode = "differentKIScores(\"node1\", \"componentX\", \"maximize\")";

  @Before
  public void setup() {
    this.logicBasic = new DifferentKIScoresLogic(logicStringBasic);
    this.logicWithMode = new DifferentKIScoresLogic(logicStringWithMode);
  }

  @Test
  public void getComponentId() {
    assertEquals("componentX", logicBasic.getComponentId());
    assertEquals("componentX", logicWithMode.getComponentId());
  }

  @Test
  public void getNodeId() {
    assertEquals("node1", logicBasic.getNodeId());
    assertEquals("node1", logicWithMode.getNodeId());
  }

  @Test
  public void getMode() {
    assertEquals(DifferentKIScoresLogicMode.ANY, logicBasic.getMode());
    assertEquals(DifferentKIScoresLogicMode.MAXIMIZE, logicWithMode.getMode());
  }
}
