/**
 * Copyright (c) 2008-2017 Regents of the University of California (Regents).
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
package org.wise.portal.dao.workgroup.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

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
import org.wise.portal.dao.workgroup.WorkgroupDao;
import org.wise.portal.domain.group.impl.PersistentGroup;
import org.wise.portal.domain.run.Run;
import org.wise.portal.domain.run.impl.RunImpl;
import org.wise.portal.domain.user.User;
import org.wise.portal.domain.user.impl.UserImpl;
import org.wise.portal.domain.workgroup.Workgroup;
import org.wise.portal.domain.workgroup.impl.WorkgroupImpl;

/**
 * @author Hiroki Terashima
 */
@Repository
public class HibernateWorkgroupDao extends AbstractHibernateDao<Workgroup>
    implements WorkgroupDao<Workgroup> {

  @PersistenceContext
  private EntityManager entityManager;

  private CriteriaBuilder getCriteriaBuilder() {
    Session session = this.getHibernateTemplate().getSessionFactory().getCurrentSession();
    return session.getCriteriaBuilder();
  }

  private static final String FIND_ALL_QUERY = "from WorkgroupImpl";

  @Override
  protected Class<WorkgroupImpl> getDataObjectClass() {
    return WorkgroupImpl.class;
  }

  @Override
  protected String getFindAllQuery() {
    return FIND_ALL_QUERY;
  }

  @SuppressWarnings("unchecked")
  public List<Workgroup> getListByRunAndUser(Run run, User user) {
    CriteriaBuilder cb = getCriteriaBuilder();
    CriteriaQuery<WorkgroupImpl> cq = cb.createQuery(WorkgroupImpl.class);
    Root<UserImpl> userImplRoot = cq.from(UserImpl.class);
    Root<RunImpl> runImplRoot = cq.from(RunImpl.class);
    Root<WorkgroupImpl> workgroupImplRoot = cq.from(WorkgroupImpl.class);
    Root<PersistentGroup> persistentGroupRoot = cq.from(PersistentGroup.class);
    List<Predicate> predicates = new ArrayList<>();
    predicates.add(cb.equal(userImplRoot.get("id"), user.getId()));
    predicates.add(cb.equal(runImplRoot.get("id"), run.getId()));
    predicates.add(cb.equal(runImplRoot.get("id"), workgroupImplRoot.get("run")));
    predicates.add(cb.equal(workgroupImplRoot.get("group"), persistentGroupRoot.get("id")));
    predicates
        .add(cb.isMember(userImplRoot.get("id"), persistentGroupRoot.<Set<User>> get("members")));
    cq.select(workgroupImplRoot).where(predicates.toArray(new Predicate[predicates.size()]));
    TypedQuery<WorkgroupImpl> query = entityManager.createQuery(cq);
    List<WorkgroupImpl> runResultList = query.getResultList();
    return (List<Workgroup>) (Object) runResultList;
  }

  @SuppressWarnings("unchecked")
  public List<Workgroup> getListByUser(User user) {
    CriteriaBuilder cb = getCriteriaBuilder();
    CriteriaQuery<WorkgroupImpl> cq = cb.createQuery(WorkgroupImpl.class);
    Root<WorkgroupImpl> workgroupImplRoot = cq.from(WorkgroupImpl.class);
    Root<PersistentGroup> persistentGroupRoot = cq.from(PersistentGroup.class);
    Root<UserImpl> userImplRoot = cq.from(UserImpl.class);
    List<Predicate> predicates = new ArrayList<>();
    predicates.add(cb.equal(userImplRoot.get("id"), user.getId()));
    predicates.add(cb.equal(workgroupImplRoot.get("group"), persistentGroupRoot.get("id")));
    predicates
        .add(cb.isMember(userImplRoot.get("id"), persistentGroupRoot.<Set<User>> get("members")));
    cq.select(workgroupImplRoot).where(predicates.toArray(new Predicate[predicates.size()]));
    TypedQuery<WorkgroupImpl> query = entityManager.createQuery(cq);
    List<WorkgroupImpl> runResultList = query.getResultList();
    return (List<Workgroup>) (Object) runResultList;
  }

  @SuppressWarnings("unchecked")
  public List<Workgroup> getListByRun(Run run) {
    CriteriaBuilder cb = getCriteriaBuilder();
    CriteriaQuery<WorkgroupImpl> cq = cb.createQuery(WorkgroupImpl.class);
    Root<WorkgroupImpl> workgroupRoot = cq.from(WorkgroupImpl.class);
    cq.select(workgroupRoot).where(cb.equal(workgroupRoot.get("run").get("id"), run.getId()));
    TypedQuery<WorkgroupImpl> query = entityManager.createQuery(cq);
    List<WorkgroupImpl> workgroupResultList = query.getResultList();
    return (List<Workgroup>) (Object) workgroupResultList;
  }

}
