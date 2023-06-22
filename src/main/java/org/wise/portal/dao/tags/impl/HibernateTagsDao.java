package org.wise.portal.dao.tags.impl;

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
import org.hibernate.proxy.HibernateProxyHelper;
import org.springframework.stereotype.Repository;
import org.wise.portal.dao.impl.AbstractHibernateDao;
import org.wise.portal.dao.tags.TagsDao;
import org.wise.portal.domain.run.Run;
import org.wise.portal.domain.run.impl.RunImpl;
import org.wise.portal.domain.tags.Tags;
import org.wise.portal.domain.tags.impl.TagsImpl;
import org.wise.portal.domain.user.User;

@Repository
public class HibernateTagsDao extends AbstractHibernateDao<Tags> implements TagsDao<Tags> {

  @PersistenceContext
  private EntityManager entityManager;

  private static final String FIND_ALL_QUERY = "from TagsImpl";

  @Override
  protected String getFindAllQuery() {
    return FIND_ALL_QUERY;
  }

  @Override
  protected Class<? extends Tags> getDataObjectClass() {
    return TagsImpl.class;
  }

  public Tags get(User user, Run run) {
    CriteriaBuilder cb = getCriteriaBuilder();
    CriteriaQuery<TagsImpl> cq = cb.createQuery(TagsImpl.class);
    Root<TagsImpl> tagsRoot = cq.from(TagsImpl.class);
    List<Predicate> predicates = new ArrayList<>();
    Class clazz = HibernateProxyHelper.getClassWithoutInitializingProxy(new RunImpl());
    String className = clazz.getCanonicalName();
    predicates.add(cb.equal(tagsRoot.get("user").get("id"), user.getId()));
    predicates
        .add(cb.equal(tagsRoot.get("targetObjectIdentity").get("aclTargetObjectId"), run.getId()));
    predicates.add(cb.equal(
        tagsRoot.get("targetObjectIdentity").get("aclTargetObject").get("classname"), className));
    cq.select(tagsRoot).where(predicates.toArray(new Predicate[predicates.size()]));
    TypedQuery<TagsImpl> query = entityManager.createQuery(cq);
    List<TagsImpl> tagsResultList = query.getResultList();
    return tagsResultList.isEmpty() ? null : tagsResultList.get(0);
  }

  private CriteriaBuilder getCriteriaBuilder() {
    Session session = this.getHibernateTemplate().getSessionFactory().getCurrentSession();
    return session.getCriteriaBuilder();
  }
}
