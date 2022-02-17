package org.wise.portal.service.work;

import org.wise.vle.domain.work.StudentWork;

public interface BroadcastStudentWorkService {

  void broadcastToClassroom(StudentWork studentWork);

  void broadcastToTeacher(StudentWork studentWork);
}
