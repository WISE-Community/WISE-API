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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import lombok.Getter;

/**
 * ProjectNode stores a list of <code>ProjectComponent</code> and is used to organize the structure
 * of a Project's content.
 * @author Hiroki Terashima
 */
public class ProjectNode {

  JSONObject nodeJSON;

  @Getter
  String id;

  public ProjectNode(JSONObject nodeJSON) throws JSONException {
    this.nodeJSON = nodeJSON;
    this.id = nodeJSON.getString("id");
  }

  public ProjectComponent getComponent(String componentId) throws JSONException {
    JSONArray components = nodeJSON.getJSONArray("components");
    for (int c = 0; c < components.length(); c++) {
      JSONObject component = components.getJSONObject(c);
      if (component.getString("id").equals(componentId)) {
        return new ProjectComponent(new ProjectNode(this.nodeJSON), component);
      }
    }
    return null;
  }
}
