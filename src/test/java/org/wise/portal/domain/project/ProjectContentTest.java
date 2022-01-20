package org.wise.portal.domain.project;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Set;

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
              "{\"id\":\"c2\", \"peerGroupActivityTag\":\"tag1\"}]}]}";

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

  @Test
  public void getPeerGroupActivityTags_ReturnTags() throws JSONException {
    Set<String> tags = content.getPeerGroupActivityTags();
    assertEquals(1, tags.size());
    assertTrue(tags.contains("tag1"));
  }
}
