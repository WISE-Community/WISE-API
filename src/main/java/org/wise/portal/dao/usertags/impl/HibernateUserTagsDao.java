package org.wise.portal.dao.usertags.impl;

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
import org.wise.portal.dao.usertags.UserTagsDao;
import org.wise.portal.domain.user.User;
import org.wise.portal.domain.usertag.UserTag;
import org.wise.portal.domain.usertag.impl.UserTagImpl;

@Repository
public class HibernateUserTagsDao extends AbstractHibernateDao<UserTag>
    implements UserTagsDao<UserTag> {

  @PersistenceContext
  private EntityManager entityManager;

  private static final String FIND_ALL_QUERY = "from TagsImpl";

  @Override
  protected String getFindAllQuery() {
    return FIND_ALL_QUERY;
  }

  @Override
  protected Class<? extends UserTag> getDataObjectClass() {
    return UserTag.class;
  }

  public UserTag get(Long tagId) {
    CriteriaBuilder cb = getCriteriaBuilder();
    CriteriaQuery<UserTagImpl> cq = cb.createQuery(UserTagImpl.class);
    Root<UserTagImpl> tagsRoot = cq.from(UserTagImpl.class);
    List<Predicate> predicates = new ArrayList<>();
    predicates.add(cb.equal(tagsRoot.get("id"), tagId));
    cq.select(tagsRoot).where(predicates.toArray(new Predicate[predicates.size()]));
    TypedQuery<UserTagImpl> query = entityManager.createQuery(cq);
    List<UserTagImpl> tagsResultList = query.getResultList();
    return tagsResultList.isEmpty() ? null : tagsResultList.get(0);
  }

  @SuppressWarnings("unchecked")
  public List<UserTag> get(User user) {
    CriteriaBuilder cb = getCriteriaBuilder();
    CriteriaQuery<UserTagImpl> cq = cb.createQuery(UserTagImpl.class);
    Root<UserTagImpl> tagsRoot = cq.from(UserTagImpl.class);
    List<Predicate> predicates = new ArrayList<>();
    predicates.add(cb.equal(tagsRoot.get("user").get("id"), user.getId()));
    cq.select(tagsRoot).where(predicates.toArray(new Predicate[predicates.size()]));
    TypedQuery<UserTagImpl> query = entityManager.createQuery(cq);
    List<UserTagImpl> userTagsResult = query.getResultList();
    return (List<UserTag>) (Object) userTagsResult;
  }

  public UserTag get(User user, String text) {
    CriteriaBuilder cb = getCriteriaBuilder();
    CriteriaQuery<UserTagImpl> cq = cb.createQuery(UserTagImpl.class);
    Root<UserTagImpl> tagsRoot = cq.from(UserTagImpl.class);
    List<Predicate> predicates = new ArrayList<>();
    predicates.add(cb.equal(tagsRoot.get("user").get("id"), user.getId()));
    predicates.add(cb.equal(cb.lower(tagsRoot.get("text")), text.toLowerCase()));
    cq.select(tagsRoot).where(predicates.toArray(new Predicate[predicates.size()]));
    TypedQuery<UserTagImpl> query = entityManager.createQuery(cq);
    List<UserTagImpl> tagsResultList = query.getResultList();
    return tagsResultList.isEmpty() ? null : tagsResultList.get(0);
  }

  private CriteriaBuilder getCriteriaBuilder() {
    Session session = this.getHibernateTemplate().getSessionFactory().getCurrentSession();
    return session.getCriteriaBuilder();
  }
}
