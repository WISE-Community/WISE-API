package org.wise.portal.presentation.web.controllers.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.wise.portal.service.session.SessionService;

@Controller
@Secured("ROLE_ADMINISTRATOR")
@RequestMapping("/admin/account/show-online-users")
public class ShowOnlineUsersController {

  @Autowired
  private SessionService sessionService;

  @GetMapping
  protected String show(ModelMap modelMap) {
    modelMap.put("loggedInStudentUsernames", sessionService.getLoggedInStudents());
    modelMap.put("loggedInTeacherUsernames", sessionService.getLoggedInTeachers());
    return "admin/account/manageusers";
  }
}
