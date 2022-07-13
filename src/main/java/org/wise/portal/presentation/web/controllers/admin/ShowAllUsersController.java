package org.wise.portal.presentation.web.controllers.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.wise.portal.service.authentication.UserDetailsService;

@Controller
@RequestMapping("/admin/account/show-all-users")
public class ShowAllUsersController {

  @Autowired
  private UserDetailsService userDetailsService;

  @GetMapping
  protected String show(@RequestParam String userType, ModelMap modelMap) {
    if (userType.equals("student")) {
      modelMap.put("students", this.userDetailsService.retrieveAllStudentUsernames());
    } else if (userType.equals("teacher")) {
      modelMap.put("teachers", this.userDetailsService.retrieveAllTeacherUsernames());
    }
    return "admin/account/manageusers";
  }
}
