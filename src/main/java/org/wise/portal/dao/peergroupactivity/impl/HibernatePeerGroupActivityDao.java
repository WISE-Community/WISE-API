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
package org.wise.portal.dao.peergroupactivity.impl;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.hibernate.Session;
import org.springframework.stereotype.Repository;
import org.wise.portal.dao.impl.AbstractHibernateDao;
import org.wise.portal.dao.peergroupactivity.PeerGroupActivityDao;
import org.wise.portal.domain.peergroupactivity.PeerGroupActivity;
import org.wise.portal.domain.peergroupactivity.impl.PeerGroupActivityImpl;
import org.wise.portal.domain.run.Run;

/**
 * @author Hiroki Terashima
 */
@Repository
public class HibernatePeerGroupActivityDao extends AbstractHibernateDao<PeerGroupActivity>
    implements PeerGroupActivityDao<PeerGroupActivity> {

  @PersistenceContext
  private EntityManager entityManager;

  @Override
  protected String getFindAllQuery() {
    return "from PeerGroupActivityImpl";
  }

  @Override
  protected Class<? extends PeerGroupActivity> getDataObjectClass() {
    return PeerGroupActivityImpl.class;
  }

  @Override
  public PeerGroupActivity getByComponent(Run run, String nodeId, String componentId) {
    CriteriaBuilder cb = getCriteriaBuilder();
    CriteriaQuery<PeerGroupActivityImpl> cq = cb.createQuery(PeerGroupActivityImpl.class);
    Root<PeerGroupActivityImpl> peerGroupActivityImplRoot = cq.from(PeerGroupActivityImpl.class);
    List<Predicate> predicates = new ArrayList<>();
    predicates.add(cb.equal(peerGroupActivityImplRoot.get("run"), run.getId()));
    predicates.add(cb.equal(peerGroupActivityImplRoot.get("nodeId"), nodeId));
    predicates.add(cb.equal(peerGroupActivityImplRoot.get("componentId"), componentId));
    cq.select(peerGroupActivityImplRoot).where(predicates.toArray(new Predicate[predicates.size()]));
    TypedQuery<PeerGroupActivityImpl> query = entityManager.createQuery(cq);
    return (PeerGroupActivityImpl) query.getResultStream().findFirst().orElse(null);
  }

  @Override
  public PeerGroupActivity getByTag(Run run, String tag) {
    CriteriaBuilder cb = getCriteriaBuilder();
    CriteriaQuery<PeerGroupActivityImpl> cq = cb.createQuery(PeerGroupActivityImpl.class);
    Root<PeerGroupActivityImpl> peerGroupActivityImplRoot = cq.from(PeerGroupActivityImpl.class);
    List<Predicate> predicates = new ArrayList<>();
    predicates.add(cb.equal(peerGroupActivityImplRoot.get("run"), run.getId()));
    predicates.add(cb.equal(peerGroupActivityImplRoot.get("tag"), tag));
    cq.select(peerGroupActivityImplRoot).where(predicates.toArray(new Predicate[predicates.size()]));
    TypedQuery<PeerGroupActivityImpl> query = entityManager.createQuery(cq);
    return (PeerGroupActivityImpl) query.getResultStream().findFirst().orElse(null);
  }

  private CriteriaBuilder getCriteriaBuilder() {
    Session session = this.getHibernateTemplate().getSessionFactory().getCurrentSession();
    return session.getCriteriaBuilder();
  }
}
