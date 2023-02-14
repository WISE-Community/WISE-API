package org.wise.portal.service.vle.wise5;

import java.util.List;
import java.util.Set;

import org.wise.portal.domain.workgroup.Workgroup;
import org.wise.vle.domain.annotation.wise5.Annotation;

public interface AnnotationService {

  /**
   * Get latest annotations of a certain type on a component for a set of workgroups
   */
  List<Annotation> getLatest(Set<Workgroup> workgroups, String nodeId, String componentId,
      String type);

  List<Annotation> getAnnotationsToWorkgroups(Set<Workgroup> workgroups, String nodeId,
      String componentId);
}
