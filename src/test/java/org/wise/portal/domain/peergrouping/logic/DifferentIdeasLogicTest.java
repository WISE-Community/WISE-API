package org.wise.portal.domain.peergrouping.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.easymock.EasyMockRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(EasyMockRunner.class)
public class DifferentIdeasLogicTest {

  private DifferentIdeasLogic logic;
  private String logicString = "differentIdeas(\"node1\", \"componentX\")";

  @Before
  public void setup() {
    this.logic = new DifferentIdeasLogic(logicString);
  }

  @Test
  public void getComponentId() {
    assertEquals("componentX", logic.getComponentId());
  }

  @Test
  public void getNodeId() {
    assertEquals("node1", logic.getNodeId());
  }
}
