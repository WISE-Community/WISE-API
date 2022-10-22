package org.wise.portal.domain.peergrouping.logic;

import static org.junit.Assert.assertEquals;

import org.easymock.EasyMockRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(EasyMockRunner.class)
public class DifferentKIScoreLogicTest {

  private DifferentKIScoreLogic logicBasic, logicWithMode;
  private String logicStringBasic = "differentKIScore(\"node1\", \"componentX\")";
  private String logicStringWithMode = "differentKIScore(\"node1\", \"componentX\", \"maximize\")";

  @Before
  public void setup() {
    this.logicBasic = new DifferentKIScoreLogic(logicStringBasic);
    this.logicWithMode = new DifferentKIScoreLogic(logicStringWithMode);
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
    assertEquals(DifferentKIScoreLogicMode.ANY, logicBasic.getMode());
    assertEquals(DifferentKIScoreLogicMode.MAXIMIZE, logicWithMode.getMode());
  }
}
