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
package org.wise.portal.dao.peergroup.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import jakarta.persistence.LockModeType;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

import org.springframework.stereotype.Repository;
import org.wise.portal.dao.impl.AbstractHibernateDao;
import org.wise.portal.dao.peergroup.PeerGroupDao;
import org.wise.portal.domain.group.Group;
import org.wise.portal.domain.peergroup.PeerGroup;
import org.wise.portal.domain.peergroup.impl.PeerGroupImpl;
import org.wise.portal.domain.peergrouping.PeerGrouping;
import org.wise.portal.domain.peergrouping.impl.PeerGroupingImpl;
import org.wise.portal.domain.run.Run;
import org.wise.portal.domain.workgroup.Workgroup;
import org.wise.portal.domain.workgroup.impl.WorkgroupImpl;

/**
 * @author Hiroki Terashima
 */
@Repository
public class HibernatePeerGroupDao extends AbstractHibernateDao<PeerGroup>
    implements PeerGroupDao<PeerGroup> {

  @Override
  protected Class<? extends PeerGroup> getDataObjectClass() {
    return PeerGroupImpl.class;
  }

  @Override
  public PeerGroup getByWorkgroupAndPeerGrouping(Workgroup workgroup, PeerGrouping peerGrouping) {
    CriteriaBuilder cb = getCriteriaBuilder();
    CriteriaQuery<PeerGroupImpl> cq = cb.createQuery(PeerGroupImpl.class);
    Root<PeerGroupImpl> peerGroupImplRoot = cq.from(PeerGroupImpl.class);
    Root<WorkgroupImpl> workgroupImplRoot = cq.from(WorkgroupImpl.class);
    List<Predicate> predicates = new ArrayList<>();
    predicates.add(cb.equal(workgroupImplRoot.get("id"), workgroup.getId()));
    predicates.add(cb.equal(peerGroupImplRoot.get("peerGrouping"), peerGrouping));
    predicates.add(cb.isMember(workgroup, peerGroupImplRoot.<Set<Workgroup>> get("members")));
    cq.select(peerGroupImplRoot).where(predicates.toArray(new Predicate[predicates.size()]));
    TypedQuery<PeerGroupImpl> query = entityManager.createQuery(cq);
    query.setLockMode(LockModeType.PESSIMISTIC_WRITE);
    return (PeerGroup) query.getResultStream().findFirst().orElse(null);
  }

  @Override
  public List<PeerGroup> getListByPeerGrouping(PeerGrouping peerGrouping) {
    return getListByTag(peerGrouping.getRun(), peerGrouping.getTag());
  }

  private List<PeerGroup> getListByTag(Run run, String tag) {
    CriteriaBuilder cb = getCriteriaBuilder();
    CriteriaQuery<PeerGroupImpl> cq = cb.createQuery(PeerGroupImpl.class);
    Root<PeerGroupImpl> peerGroupImplRoot = cq.from(PeerGroupImpl.class);
    Root<PeerGroupingImpl> peerGroupingImplRoot = cq.from(PeerGroupingImpl.class);
    List<Predicate> predicates = new ArrayList<>();
    predicates.add(cb.equal(peerGroupingImplRoot.get("run"), run));
    predicates.add(cb.equal(peerGroupingImplRoot.get("tag"), tag));
    predicates.add(cb.equal(peerGroupImplRoot.get("peerGrouping"), peerGroupingImplRoot));
    cq.select(peerGroupImplRoot).where(predicates.toArray(new Predicate[predicates.size()]));
    TypedQuery<PeerGroupImpl> query = entityManager.createQuery(cq);
    List<PeerGroupImpl> resultList = query.getResultList();
    return (List<PeerGroup>) (Object) resultList;
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<PeerGroup> getListByWorkgroup(Workgroup workgroup) {
    CriteriaBuilder cb = getCriteriaBuilder();
    CriteriaQuery<PeerGroupImpl> cq = cb.createQuery(PeerGroupImpl.class);
    Root<PeerGroupImpl> peerGroupImplRoot = cq.from(PeerGroupImpl.class);
    Root<WorkgroupImpl> workgroupImplRoot = cq.from(WorkgroupImpl.class);
    List<Predicate> predicates = new ArrayList<>();
    predicates.add(cb.equal(workgroupImplRoot.get("id"), workgroup.getId()));
    predicates.add(cb.isMember(workgroup, peerGroupImplRoot.<Set<Workgroup>> get("members")));
    cq.select(peerGroupImplRoot).where(predicates.toArray(new Predicate[predicates.size()]));
    TypedQuery<PeerGroupImpl> query = entityManager.createQuery(cq);
    List<PeerGroupImpl> resultList = query.getResultList();
    return (List<PeerGroup>) (Object) resultList;
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<Workgroup> getWorkgroupsInPeerGroup(PeerGrouping peerGrouping, Group period) {
    CriteriaBuilder cb = getCriteriaBuilder();
    CriteriaQuery<WorkgroupImpl> cq = cb.createQuery(WorkgroupImpl.class);
    Root<PeerGroupImpl> peerGroupImplRoot = cq.from(PeerGroupImpl.class);
    Join<PeerGroupImpl, WorkgroupImpl> membersJoin = peerGroupImplRoot.join("members");
    List<Predicate> predicates = new ArrayList<>();
    predicates.add(cb.equal(peerGroupImplRoot.get("peerGrouping"), peerGrouping));
    predicates.add(cb.equal(membersJoin.get("period"), period));
    cq.select(membersJoin).where(predicates.toArray(new Predicate[predicates.size()]));
    TypedQuery<WorkgroupImpl> query = entityManager.createQuery(cq);
    List<WorkgroupImpl> resultList = query.getResultList();
    return (List<Workgroup>) (Object) resultList;
  }
}
