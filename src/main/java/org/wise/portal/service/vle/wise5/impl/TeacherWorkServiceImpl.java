package org.wise.portal.service.vle.wise5.impl;

import java.sql.Timestamp;
import java.util.Calendar;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.wise.portal.dao.work.StudentWorkDao;
import org.wise.portal.service.vle.wise5.TeacherWorkService;
import org.wise.vle.domain.work.StudentWork;

@Service
public class TeacherWorkServiceImpl implements TeacherWorkService {

  @Autowired
  StudentWorkDao<StudentWork> studentWorkDao;

  public StudentWork save(StudentWork studentWork) {
    setServerSaveTime(studentWork);
    studentWorkDao.save(studentWork);
    return studentWork;
  }

  private void setServerSaveTime(StudentWork studentWork) {
    Calendar now = Calendar.getInstance();
    Timestamp serverSaveTimestamp = new Timestamp(now.getTimeInMillis());
    studentWork.setServerSaveTime(serverSaveTimestamp);
  }
}
