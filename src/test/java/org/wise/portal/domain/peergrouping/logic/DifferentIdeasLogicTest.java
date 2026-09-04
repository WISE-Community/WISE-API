package org.wise.portal.domain.peergrouping.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;


import org.easymock.EasyMockExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(EasyMockExtension.class)
public class DifferentIdeasLogicTest {

  private DifferentIdeasLogic logic;
  private String logicString = "differentIdeas(\"node1\", \"componentX\")";

  @BeforeEach
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
