package org.wise.portal.domain.project;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.easymock.EasyMockExtension;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.wise.portal.domain.project.impl.ProjectContent;

@ExtendWith(EasyMockExtension.class)
public class ProjectContentTest {

  ProjectContent content;

  final String PROJECT_1 = """
      {"nodes":[\
      {"id":"node1",\
      "components":[\
      {"id":"c1", "type":"HTML"},\
      {"id":"c2", "type":"PeerChat", "peerGroupingTag":"tag1"}]}]}\
      """;

  @BeforeEach
  public void setup() throws JSONException {
    content = new ProjectContent(new JSONObject(PROJECT_1));
  }

  @Test
  public void getNode_NodeDoesNotExist_ReturnNull() throws JSONException {
    assertNull(content.getNode("node_id_not_exists"));
  }

  @Test
  public void getNode_NodeExists_ReturnNode() throws JSONException {
    assertNotNull(content.getNode("node1"));
  }

  @Test
  public void getComponent_ComponentDoesNotExist_ReturnNull() throws JSONException {
    assertNull(content.getComponent("node1", "component_id_not_exists"));
  }

  @Test
  public void getComponent_ComponentExists_ReturnComponent() throws JSONException {
    assertNotNull(content.getComponent("node1", "c1"));
  }
}
