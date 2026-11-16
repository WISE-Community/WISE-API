package org.wise.portal.dao.teacherpresentation.impl;

import java.util.List;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;

import org.springframework.stereotype.Repository;
import org.wise.portal.dao.impl.AbstractHibernateDao;
import org.wise.portal.dao.teacherpresentation.TeacherPresentationConfigDao;
import org.wise.portal.domain.group.Group;
import org.wise.portal.domain.run.Run;
import org.wise.vle.domain.teacherpresentation.TeacherPresentationConfig;

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
}
