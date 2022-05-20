/**
 * Copyright (c) 2008-2021 Regents of the University of California (Regents).
 * Created by WISE, Graduate School of Education, University of California, Berkeley.
 *
 * This software is distributed under the GNU General Public License, v3,
 * or (at your option) any later version.
 *
 * Permission is hereby granted, without written agreement and without license
 * or royalty fees, to use, copy, modify, and distribute this software and its
 * documentation for any purpose, provided that the above copyright notice and
 * the following two paragraphs appear in all copies of this software.
 *
 * REGENTS SPECIFICALLY DISCLAIMS ANY WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE. THE SOFTWARE AND ACCOMPANYING DOCUMENTATION, IF ANY, PROVIDED
 * HEREUNDER IS PROVIDED "AS IS". REGENTS HAS NO OBLIGATION TO PROVIDE
 * MAINTENANCE, SUPPORT, UPDATES, ENHANCEMENTS, OR MODIFICATIONS.
 *
 * IN NO EVENT SHALL REGENTS BE LIABLE TO ANY PARTY FOR DIRECT, INDIRECT,
 * SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES, INCLUDING LOST PROFITS,
 * ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
 * REGENTS HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.wise.portal.domain.project.impl;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * A class that stores the entire content for a project.
 * @author Hiroki Terashima
 */
public class ProjectContent {

  JSONObject projectJSON;

  public ProjectContent(JSONObject projectJSON) {
    this.projectJSON = projectJSON;
  }

  public ProjectNode getNode(String nodeId) throws JSONException {
    JSONArray nodes = this.projectJSON.getJSONArray("nodes");
    for (int i = 0; i < nodes.length(); i++) {
      JSONObject node = nodes.getJSONObject(i);
      if (node.getString("id").equals(nodeId)) {
        return new ProjectNode(node);
      }
    }
    return null;
  }

  public ProjectComponent getComponent(String nodeId, String componentId) throws JSONException {
    ProjectNode node = getNode(nodeId);
    return node != null ? node.getComponent(componentId) : null;
  }

  public JSONArray getPeerGroupings() {
    return this.projectJSON.optJSONArray("peerGroupings");
  }

  public List<ProjectComponent> getComponents() throws JSONException {
    List<ProjectComponent> projectComponents = new ArrayList<ProjectComponent>();
    JSONArray nodes = this.projectJSON.getJSONArray("nodes");
    for (int n = 0; n < nodes.length(); n++) {
      JSONObject node = nodes.getJSONObject(n);
      if (node.getString("type").equals("node")) {
        ProjectNode projectNode = new ProjectNode(node);
        JSONArray components = node.getJSONArray("components");
        for (int c = 0; c < components.length(); c++) {
          JSONObject component = components.getJSONObject(c);
          projectComponents.add(new ProjectComponent(projectNode, component));
        }
      }
    }
    return projectComponents;
  }
}
