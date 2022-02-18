package org.wise.portal.service.vle.wise5;

import java.util.List;
import java.util.Set;

import org.wise.portal.domain.workgroup.Workgroup;
import org.wise.vle.domain.work.StudentWork;

public interface StudentWorkService {
  List<StudentWork> getStudentWork(Set<Workgroup> workgroups, String nodeId, String componentId);

  List<StudentWork> getLatestStudentWork(Set<Workgroup> workgroups, String nodeId,
      String componentId);
}
