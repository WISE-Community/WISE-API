package org.wise.vle.web.wise5.teacher;

import java.nio.file.AccessDeniedException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.wise.portal.domain.user.User;
import org.wise.portal.service.user.UserService;
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

  @Autowired
  UserService userService;

  @PostMapping("/api/teacher/run/{runId}/work")
  public StudentWork save(@RequestBody StudentWork studentWork, Authentication auth)
      throws AccessDeniedException {
    User user = userService.retrieveUserByUsername(auth.getName());
    if (studentWork.getRun().isTeacherAssociatedToThisRun(user)) {
      StudentWork savedStudentWork = teacherWorkService.save(studentWork);
      broadcastStudentWorkService.broadcastToClassroom(studentWork);
      return savedStudentWork;
    } else {
      throw new AccessDeniedException("Not permitted");
    }
  }
}
