package org.wise.vle.domain.webservice.crater;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.Test;

public class CRaterIdeaTest extends CRaterResponseSubItemTest {

  @Test
  public void constructor_ShouldDetectIdea() throws Exception {
    CRaterIdea idea = new CRaterIdea(getNode(
        "<idea name=\"idea1\" detected=\"1\" character_offsets=\"[]\" />"));
    assertEquals("idea1", idea.getName());
    assertTrue(idea.isDetected());
  }
}
