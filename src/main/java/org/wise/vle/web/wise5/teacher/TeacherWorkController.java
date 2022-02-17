package org.wise.vle.web.wise5.teacher;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.wise.portal.service.vle.wise5.TeacherWorkService;
import org.wise.portal.service.work.BroadcastStudentWorkService;
import org.wise.vle.domain.work.StudentWork;

@Secured("ROLE_TEACHER")
@RestController
public class TeacherWorkController {

  @Autowired
  TeacherWorkService teacherWorkService;

  @Autowired
  BroadcastStudentWorkService broadcastStudentWorkService;

  @PostMapping("/api/teacher/run/{runId}/work")
  public StudentWork save(@RequestBody StudentWork studentWork) {
    StudentWork savedStudentWork = teacherWorkService.save(studentWork);
    broadcastStudentWorkService.broadcastToClassroom(studentWork);
    return savedStudentWork;
  }
}
