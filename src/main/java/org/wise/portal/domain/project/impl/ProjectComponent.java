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
 * ProjectComponent defines activities that students work on, like MultipleChoice, OpenResponse,
 * etc.
 */
public class ProjectComponent {

  JSONObject componentJSON;

  @Getter
  String id;

  @Getter
  ProjectNode node;

  @Getter
  String type;

  public ProjectComponent(ProjectNode node, JSONObject componentJSON) throws JSONException {
    this.node = node;
    this.componentJSON = componentJSON;
    this.id = componentJSON.getString("id");
    this.type = componentJSON.getString("type");
  }

  public String getString(String key) throws JSONException {
    return this.componentJSON.getString(key);
  }

  public boolean hasField(String key) {
    return this.componentJSON.has(key);
  }

  public boolean getBoolean(String key) throws JSONException {
    return this.componentJSON.getBoolean(key);
  }

  public int getInt(String key) throws JSONException {
    return this.componentJSON.getInt(key);
  }

  public String getPeerGroupingTag() throws JSONException {
    return this.componentJSON.getString("peerGroupingTag");
  }

  public JSONArray getJSONArray(String key) throws JSONException {
    return this.componentJSON.getJSONArray(key);
  }

  public JSONObject getJSONObject(String key) throws JSONException {
    return this.componentJSON.getJSONObject(key);
  }
}
