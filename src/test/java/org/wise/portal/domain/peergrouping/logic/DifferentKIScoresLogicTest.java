package org.wise.portal.domain.peergrouping.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;


import org.easymock.EasyMockExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(EasyMockExtension.class)
public class DifferentKIScoresLogicTest {

  private DifferentKIScoresLogic logicBasic, logicWithMode;
  private String logicStringBasic = "differentKIScores(\"node1\", \"componentX\")";
  private String logicStringWithMode = "differentKIScores(\"node1\", \"componentX\", \"maximize\")";

  @BeforeEach
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
    assertEquals(LogicMode.ANY, logicBasic.getMode());
    assertEquals(LogicMode.MAXIMIZE, logicWithMode.getMode());
  }
}
