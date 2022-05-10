package org.wise.portal.domain.project;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.easymock.EasyMockRunner;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wise.portal.domain.project.impl.ProjectContent;

@RunWith(EasyMockRunner.class)
public class ProjectContentTest {

  ProjectContent content;

  final String PROJECT_1 = "{\"nodes\":[" +
      "{\"id\":\"node1\"," +
          "\"components\":[" +
              "{\"id\":\"c1\"}," +
              "{\"id\":\"c2\", \"peerGroupingTag\":\"tag1\"}]}]}";

  @Before
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
