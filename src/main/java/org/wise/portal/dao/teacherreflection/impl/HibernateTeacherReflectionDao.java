package org.wise.portal.dao.teacherreflection.impl;

import java.util.List;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;

import org.springframework.stereotype.Repository;
import org.wise.portal.dao.impl.AbstractHibernateDao;
import org.wise.portal.dao.teacherreflection.TeacherReflectionDao;
import org.wise.portal.domain.group.Group;
import org.wise.portal.domain.run.Run;
import org.wise.vle.domain.teacherreflection.TeacherReflection;

@Repository("teacherReflectionDao")
public class HibernateTeacherReflectionDao extends AbstractHibernateDao<TeacherReflection>
    implements TeacherReflectionDao<TeacherReflection> {

  @Override
  protected Class<? extends TeacherReflection> getDataObjectClass() {
    return TeacherReflection.class;
  }

  @Override
  public List<TeacherReflection> getQuestions(Run run, Group period, String nodeId,
      String componentId) {
    CriteriaBuilder cb = getCriteriaBuilder();
    CriteriaQuery<TeacherReflection> cq = cb.createQuery(TeacherReflection.class);
    Root<TeacherReflection> root = cq.from(TeacherReflection.class);
    cq.select(root).where(cb.equal(root.get("run"), run), cb.equal(root.get("period"), period),
        cb.equal(root.get("nodeId"), nodeId), cb.equal(root.get("componentId"), componentId))
        .orderBy(cb.asc(root.get("id")));
    return entityManager.createQuery(cq).getResultList();
  }

  @Override
  public TeacherReflection getQuestion(Run run, Group period, String nodeId, String componentId,
      String questionId) {
    CriteriaBuilder cb = getCriteriaBuilder();
    CriteriaQuery<TeacherReflection> cq = cb.createQuery(TeacherReflection.class);
    Root<TeacherReflection> root = cq.from(TeacherReflection.class);
    cq.select(root).where(cb.equal(root.get("run"), run), cb.equal(root.get("period"), period),
        cb.equal(root.get("nodeId"), nodeId), cb.equal(root.get("componentId"), componentId),
        cb.equal(root.get("questionId"), questionId));
    List<TeacherReflection> results = entityManager.createQuery(cq).getResultList();
    return results.isEmpty() ? null : results.get(0);
  }
}
