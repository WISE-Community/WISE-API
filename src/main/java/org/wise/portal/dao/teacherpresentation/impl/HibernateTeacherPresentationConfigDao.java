package org.wise.portal.dao.teacherpresentation.impl;

import java.util.List;

import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;

import org.springframework.stereotype.Repository;
import org.wise.portal.dao.impl.AbstractHibernateDao;
import org.wise.portal.dao.teacherpresentation.TeacherPresentationConfigDao;
import org.wise.portal.domain.group.Group;
import org.wise.portal.domain.run.Run;
import org.wise.vle.domain.teacherpresentation.TeacherPresentationConfig;
import org.wise.vle.domain.teacherpresentation.TeacherPresentationReflectionAnswer;

@Repository("teacherPresentationConfigDao")
public class HibernateTeacherPresentationConfigDao
    extends AbstractHibernateDao<TeacherPresentationConfig>
    implements TeacherPresentationConfigDao<TeacherPresentationConfig> {

  @Override
  protected Class<? extends TeacherPresentationConfig> getDataObjectClass() {
    return TeacherPresentationConfig.class;
  }

  @Override
  public TeacherPresentationConfig getConfig(Run run, Group period, String nodeId,
      String componentId) {
    CriteriaBuilder cb = getCriteriaBuilder();
    CriteriaQuery<TeacherPresentationConfig> cq = cb.createQuery(TeacherPresentationConfig.class);
    Root<TeacherPresentationConfig> root = cq.from(TeacherPresentationConfig.class);
    cq.select(root).where(cb.equal(root.get("run"), run), cb.equal(root.get("period"), period),
        cb.equal(root.get("nodeId"), nodeId), cb.equal(root.get("componentId"), componentId));
    List<TeacherPresentationConfig> results = entityManager.createQuery(cq).getResultList();
    return results.isEmpty() ? null : results.get(0);
  }

  @Override
  public List<TeacherPresentationReflectionAnswer> getAnswers(TeacherPresentationConfig config) {
    CriteriaBuilder cb = getCriteriaBuilder();
    CriteriaQuery<TeacherPresentationReflectionAnswer> cq = cb
        .createQuery(TeacherPresentationReflectionAnswer.class);
    Root<TeacherPresentationReflectionAnswer> root = cq
        .from(TeacherPresentationReflectionAnswer.class);
    cq.select(root).where(cb.equal(root.get("teacherPresentationConfig"), config));
    return entityManager.createQuery(cq).getResultList();
  }

  @Override
  public TeacherPresentationReflectionAnswer getAnswer(TeacherPresentationConfig config,
      String questionId) {
    CriteriaBuilder cb = getCriteriaBuilder();
    CriteriaQuery<TeacherPresentationReflectionAnswer> cq = cb
        .createQuery(TeacherPresentationReflectionAnswer.class);
    Root<TeacherPresentationReflectionAnswer> root = cq
        .from(TeacherPresentationReflectionAnswer.class);
    cq.select(root).where(cb.equal(root.get("teacherPresentationConfig"), config),
        cb.equal(root.get("questionId"), questionId));
    TypedQuery<TeacherPresentationReflectionAnswer> query = entityManager.createQuery(cq);
    List<TeacherPresentationReflectionAnswer> results = query.getResultList();
    return results.isEmpty() ? null : results.get(0);
  }

  @Override
  public void saveAnswer(TeacherPresentationReflectionAnswer answer) {
    if (answer.getId() == null) {
      entityManager.persist(answer);
    } else {
      entityManager.merge(answer);
    }
  }
}
