package org.wise.portal.service.vle.wise5.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.wise.portal.dao.work.StudentWorkDao;
import org.wise.portal.domain.workgroup.Workgroup;
import org.wise.portal.service.vle.wise5.StudentWorkService;
import org.wise.vle.domain.work.StudentWork;

@Service
public class StudentWorkServiceImpl implements StudentWorkService {

  @Autowired
  private StudentWorkDao<StudentWork> studentWorkDao;

  @Override
  public List<StudentWork> getStudentWork(Set<Workgroup> workgroups, String nodeId,
      String componentId) {
    return studentWorkDao.getStudentWork(workgroups, nodeId, componentId);
  }

  @Override
  public List<StudentWork> getLatestStudentWork(Set<Workgroup> workgroups, String nodeId,
      String componentId) {
    List<StudentWork> allStudentWork = getStudentWork(workgroups, nodeId, componentId);
    HashMap<Long, StudentWork> workgroupToStudentWork = new HashMap<Long, StudentWork>();
    for (StudentWork studentWork : allStudentWork) {
      workgroupToStudentWork.put(studentWork.getWorkgroup().getId(), studentWork);
    }
    return new ArrayList<StudentWork>(workgroupToStudentWork.values());
  }
}
