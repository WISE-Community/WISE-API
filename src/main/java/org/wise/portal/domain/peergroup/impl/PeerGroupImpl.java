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
package org.wise.portal.domain.peergroup.impl;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import org.wise.portal.domain.group.Group;
import org.wise.portal.domain.group.impl.PersistentGroup;
import org.wise.portal.domain.peergroup.PeerGroup;
import org.wise.portal.domain.peergrouping.PeerGrouping;
import org.wise.portal.domain.peergrouping.impl.PeerGroupingImpl;
import org.wise.portal.domain.user.User;
import org.wise.portal.domain.workgroup.Workgroup;
import org.wise.portal.domain.workgroup.impl.WorkgroupImpl;

import lombok.Getter;
import lombok.Setter;

/**
 * WISE implementation of peer group
 * @author Hiroki Terashima
 */
@Entity
@Table(name = "peer_groups")
@Getter
@Setter
public class PeerGroupImpl implements PeerGroup {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long id = null;

  @ManyToOne(targetEntity = PeerGroupingImpl.class, cascade = { CascadeType.PERSIST },
      fetch = FetchType.LAZY)
  @JoinColumn(name = "peerGroupingId", nullable = false)
  @JsonIgnore
  private PeerGrouping peerGrouping;

  @ManyToMany(targetEntity = WorkgroupImpl.class)
  @JoinTable(name = "peer_groups_related_to_workgroups",
      joinColumns = { @JoinColumn(name = "peer_group_fk", nullable = false)},
      inverseJoinColumns = @JoinColumn(name = "workgroup_fk", nullable = false))
  private Set<Workgroup> members = new HashSet<Workgroup>();

  @OneToOne(targetEntity = PersistentGroup.class, fetch = FetchType.LAZY)
  @JoinColumn(name = "periodId")
  @JsonIgnore
  private Group period;

  public PeerGroupImpl() {}

  public PeerGroupImpl(PeerGrouping peerGrouping, Group period, Set<Workgroup> members) {
    this.peerGrouping = peerGrouping;
    this.period = period;
    this.members = members;
  }

  public void addMember(Workgroup workgroup) {
    this.members.add(workgroup);
  }

  public void removeMember(Workgroup workgroup) {
    this.members.remove(workgroup);
  }

  @Override
  public boolean isMember(User user) {
    for (Workgroup workgroup : members) {
      if (workgroup.getMembers().contains(user)) {
        return true;
      }
    }
    return false;
  }

  @JsonProperty
  public Long getPeriodId() {
    return period.getId();
  }
}
