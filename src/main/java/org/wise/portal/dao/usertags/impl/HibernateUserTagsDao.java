package org.wise.portal.dao.usertags.impl;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

import org.springframework.stereotype.Repository;
import org.wise.portal.dao.impl.AbstractHibernateDao;
import org.wise.portal.dao.usertags.UserTagsDao;
import org.wise.portal.domain.user.User;
import org.wise.portal.domain.usertag.UserTag;
import org.wise.portal.domain.usertag.impl.UserTagImpl;

@Repository
public class HibernateUserTagsDao extends AbstractHibernateDao<UserTag>
    implements UserTagsDao<UserTag> {

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
}
