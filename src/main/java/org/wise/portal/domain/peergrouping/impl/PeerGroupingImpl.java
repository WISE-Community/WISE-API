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
package org.wise.portal.domain.peergrouping.impl;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import com.fasterxml.jackson.annotation.JsonIgnore;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.wise.portal.domain.peergrouping.PeerGrouping;
import org.wise.portal.domain.run.Run;
import org.wise.portal.domain.run.impl.RunImpl;

import lombok.Getter;
import lombok.Setter;

/**
 * @author Hiroki Terashima
 */
@Entity
@Table(name = "peer_groupings",
    indexes = { @Index(columnList = "runId", name = "peerGroupingsRunIdIndex") },
    uniqueConstraints = { @UniqueConstraint(columnNames = { "runId", "tag" }) })
@Getter
@Setter
public class PeerGroupingImpl implements PeerGrouping {

  private static final long serialVersionUID = 1L;

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long id = null;

  @ManyToOne(targetEntity = RunImpl.class, cascade = {
      CascadeType.PERSIST }, fetch = FetchType.LAZY)
  @JoinColumn(name = "runId", nullable = false)
  @JsonIgnore
  private Run run;

  @Column(length = 30, nullable = false)
  private String logic = "manual";

  @Column(length = 30, nullable = false)
  private String tag;

  @Column
  private int logicThresholdCount;

  @Column
  private int logicThresholdPercent;

  @Column
  private int maxMembershipCount = 2;

  public PeerGroupingImpl() {
  }

  public PeerGroupingImpl(Run run, String tag, String logic, Integer logicThresholdCount,
      Integer logicThresholdPercent, Integer maxMembershipCount) {
    this.run = run;
    this.tag = tag;
    this.logic = logic;
    this.logicThresholdCount = logicThresholdCount;
    this.logicThresholdPercent = logicThresholdPercent;
    this.maxMembershipCount = maxMembershipCount;
  }

  public String getLogicNodeId() throws JSONException {
    return getFirstLogicJSON().getString("nodeId");
  }

  public String getLogicComponentId() throws JSONException {
    return getFirstLogicJSON().getString("componentId");
  }

  private JSONObject getFirstLogicJSON() throws JSONException {
    return new JSONArray(this.logic).getJSONObject(0);
  }
}
