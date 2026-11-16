package org.wise.portal.dao.annotation.wise5.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

import org.springframework.stereotype.Repository;
import org.wise.portal.dao.annotation.wise5.AnnotationDao;
import org.wise.portal.dao.impl.AbstractHibernateDao;
import org.wise.portal.domain.group.Group;
import org.wise.portal.domain.group.impl.PersistentGroup;
import org.wise.portal.domain.run.Run;
import org.wise.portal.domain.run.impl.RunImpl;
import org.wise.portal.domain.workgroup.Workgroup;
import org.wise.vle.domain.annotation.wise5.Annotation;
import org.wise.vle.domain.work.NotebookItem;
import org.wise.vle.domain.work.StudentWork;

@Repository("wise5AnnotationDao")
public class HibernateAnnotationDao extends AbstractHibernateDao<Annotation>
    implements AnnotationDao<Annotation> {

  @Override
  protected Class<? extends Annotation> getDataObjectClass() {
    return null;
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<Annotation> getAnnotationsByParams(Integer id, Run run, Group period,
      Workgroup fromWorkgroup, Workgroup toWorkgroup, String nodeId, String componentId,
      StudentWork studentWork, String localNotebookItemId, NotebookItem notebookItem, String type) {
    CriteriaBuilder cb = getCriteriaBuilder();
    CriteriaQuery<Annotation> cq = cb.createQuery(Annotation.class);
    Root<Annotation> annotationRoot = cq.from(Annotation.class);
    List<Predicate> predicates = new ArrayList<>();
    if (id != null) {
      predicates.add(cb.equal(annotationRoot.get("id"), id));
    }
    if (run != null) {
      predicates.add(cb.equal(annotationRoot.get("run"), run));
    }
    if (period != null) {
      predicates.add(cb.equal(annotationRoot.get("period"), period));
    }
    if (fromWorkgroup != null) {
      predicates.add(cb.equal(annotationRoot.get("fromWorkgroup"), fromWorkgroup));
    }
    if (toWorkgroup != null) {
      predicates.add(cb.equal(annotationRoot.get("toWorkgroup"), toWorkgroup));
    }
    if (nodeId != null) {
      predicates.add(cb.equal(annotationRoot.get("nodeId"), nodeId));
    }
    if (componentId != null) {
      predicates.add(cb.equal(annotationRoot.get("componentId"), componentId));
    }
    if (studentWork != null) {
      predicates.add(cb.equal(annotationRoot.get("studentWork"), studentWork));
    }
    if (notebookItem != null) {
      predicates.add(cb.equal(annotationRoot.get("notebookItem"), notebookItem));
    }
    if (localNotebookItemId != null) {
      predicates.add(cb.equal(annotationRoot.get("localNotebookItemId"), localNotebookItemId));
    }
    if (type != null) {
      predicates.add(cb.equal(annotationRoot.get("type"), type));
    }
    cq.select(annotationRoot).where(predicates.toArray(new Predicate[predicates.size()]));
    TypedQuery<Annotation> query = entityManager.createQuery(cq);
    return (List<Annotation>) (Object) query.getResultList();
  }

  public List<Annotation> getAnnotations(Run run, String nodeId, String componentId) {
    return getAnnotations(run, null, nodeId, componentId);
  }

  public List<Annotation> getAnnotations(Run run, Group period, String nodeId, String componentId) {
    CriteriaBuilder cb = getCriteriaBuilder();
    CriteriaQuery<Annotation> cq = cb.createQuery(Annotation.class);
    Root<Annotation> annotationRoot = cq.from(Annotation.class);
    Root<RunImpl> runImplRoot = cq.from(RunImpl.class);
    List<Predicate> predicates = new ArrayList<>();
    predicates.add(cb.equal(runImplRoot.get("id"), run.getId()));
    predicates.add(cb.equal(annotationRoot.get("run"), runImplRoot));
    predicates.add(cb.equal(annotationRoot.get("nodeId"), nodeId));
    predicates.add(cb.equal(annotationRoot.get("componentId"), componentId));
    if (period != null) {
      Root<PersistentGroup> periodRoot = cq.from(PersistentGroup.class);
      predicates.add(cb.equal(periodRoot.get("id"), period.getId()));
      predicates.add(cb.equal(annotationRoot.get("period"), periodRoot));
    }
    cq.select(annotationRoot).where(predicates.toArray(new Predicate[predicates.size()]));
    TypedQuery<Annotation> query = entityManager.createQuery(cq);
    return (List<Annotation>) (Object) query.getResultList();
  }

  public List<Annotation> getAnnotationsToWorkgroups(Set<Workgroup> workgroups, String nodeId,
      String componentId) {
    CriteriaBuilder cb = getCriteriaBuilder();
    CriteriaQuery<Annotation> cq = cb.createQuery(Annotation.class);
    Root<Annotation> annotationRoot = cq.from(Annotation.class);
    List<Predicate> predicates = new ArrayList<Predicate>();
    predicates.add(cb.in(annotationRoot.get("toWorkgroup")).value(workgroups));
    predicates.add(cb.equal(annotationRoot.get("nodeId"), nodeId));
    predicates.add(cb.equal(annotationRoot.get("componentId"), componentId));
    cq.select(annotationRoot).where(predicates.toArray(new Predicate[predicates.size()]))
        .orderBy(cb.asc(annotationRoot.get("serverSaveTime")));
    TypedQuery<Annotation> query = entityManager.createQuery(cq);
    return (List<Annotation>) query.getResultList();
  }
}
