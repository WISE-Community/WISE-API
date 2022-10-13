package org.wise.portal.service.vle.wise5.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.wise.portal.dao.annotation.wise5.AnnotationDao;
import org.wise.portal.domain.workgroup.Workgroup;
import org.wise.portal.service.vle.wise5.AnnotationService;
import org.wise.vle.domain.annotation.wise5.Annotation;

@Service
public class AnnotationServiceImpl implements AnnotationService {

  @Autowired
  private AnnotationDao<Annotation> annotationDao;

  @Override
  public List<Annotation> getLatest(Set<Workgroup> workgroups, String nodeId, String componentId,
      String type) {
    List<Annotation> annotations = annotationDao.getAnnotationsToWorkgroups(workgroups, nodeId,
        componentId);
    HashMap<Long, Annotation> workgroupToAnnotation = new HashMap<Long, Annotation>();
    for (Annotation annotation : annotations) {
      if (annotation.getType().equals(type)) {
        workgroupToAnnotation.put(annotation.getToWorkgroup().getId(), annotation);
      }
    }
    return new ArrayList<Annotation>(workgroupToAnnotation.values());
  }
}
