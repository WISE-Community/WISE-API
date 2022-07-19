package org.wise.portal.presentation.web.controllers.admin;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.wise.portal.domain.user.User;
import org.wise.portal.service.user.UserService;

@Controller
@Secured("ROLE_ADMINISTRATOR")
@RequestMapping("/admin/account/show-recent-users")
public class ShowRecentUsersController {

  @Autowired
  private UserService userService;

  @GetMapping
  protected String show(@RequestParam String duration, ModelMap modelMap) {
    List<User> studentsWhoLoggedInSince = new ArrayList<User>();
    List<User> teachersWhoLoggedInSince = new ArrayList<User>();
    if (duration.equals("today")) {
      studentsWhoLoggedInSince = userService.retrieveStudentUsersWhoLoggedInToday();
      teachersWhoLoggedInSince = userService.retrieveTeacherUsersWhoLoggedInToday();
    } else if (duration.equals("thisWeek")) {
      studentsWhoLoggedInSince = userService.retrieveStudentUsersWhoLoggedInThisWeek();
      teachersWhoLoggedInSince = userService.retrieveTeacherUsersWhoLoggedInThisWeek();
    } else if (duration.equals("thisMonth")) {
      studentsWhoLoggedInSince = userService.retrieveStudentUsersWhoLoggedInThisMonth();
      teachersWhoLoggedInSince = userService.retrieveTeacherUsersWhoLoggedInThisMonth();
    } else if (duration.equals("thisYear")) {
      studentsWhoLoggedInSince = userService.retrieveStudentUsersWhoLoggedInThisYear();
      teachersWhoLoggedInSince = userService.retrieveTeacherUsersWhoLoggedInThisYear();
    }
    modelMap.put("studentsWhoLoggedInSince", studentsWhoLoggedInSince);
    modelMap.put("teachersWhoLoggedInSince", teachersWhoLoggedInSince);
    return "admin/account/manageusers";
  }
}
