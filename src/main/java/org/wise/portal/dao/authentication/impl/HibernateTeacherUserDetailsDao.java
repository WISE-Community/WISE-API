package org.wise.portal.dao.authentication.impl;

import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.wise.portal.dao.authentication.TeacherUserDetailsDao;
import org.wise.portal.dao.impl.AbstractHibernateDao;
import org.wise.portal.domain.authentication.impl.TeacherUserDetails;

public class HibernateTeacherUserDetailsDao extends AbstractHibernateDao<TeacherUserDetails> 
    implements TeacherUserDetailsDao {

  @Override
  public boolean hasVerificationCode(String verificationCode) {
    CriteriaBuilder cb = getCriteriaBuilder();
    CriteriaQuery<Long> cq = cb.createQuery(Long.class);
    Root<TeacherUserDetails> teacherUserDetailsRoot = cq.from(TeacherUserDetails.class);
    cq.select(cb.count(teacherUserDetailsRoot))
        .where(cb.equal(teacherUserDetailsRoot.get("verificationCode"), verificationCode));
    TypedQuery<Long> query = entityManager.createQuery(cq);
    Long count = query.getSingleResult();
    return count > 0;
  }

  @Override
  protected Class<? extends TeacherUserDetails> getDataObjectClass() {
    return TeacherUserDetails.class;
  }
}
