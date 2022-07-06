/**
 * Copyright (c) 2007-2018 Regents of the University of California (Regents).
 * Created by WISE, Graduate School of Education, University of California, Berkeley.
 *
 * This software is distributed under the GNU General Public License, v3,
 * or (at your option) any later version.
 *
 * Permission is hereby granted, without written agreement and without license
 * or royalty fees, to use, copy, modify, and distribute this software and its
 * documentation for any purpose, provided that the above copyright notice and
 * the following two paragraphs appear in all copies of this software.
 *
 * REGENTS SPECIFICALLY DISCLAIMS ANY WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE. THE SOFTWARE AND ACCOMPANYING DOCUMENTATION, IF ANY, PROVIDED
 * HEREUNDER IS PROVIDED "AS IS". REGENTS HAS NO OBLIGATION TO PROVIDE
 * MAINTENANCE, SUPPORT, UPDATES, ENHANCEMENTS, OR MODIFICATIONS.
 *
 * IN NO EVENT SHALL REGENTS BE LIABLE TO ANY PARTY FOR DIRECT, INDIRECT,
 * SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES, INCLUDING LOST PROFITS,
 * ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
 * REGENTS HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.wise.portal.presentation.web.controllers.admin;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.wise.portal.domain.user.User;
import org.wise.portal.service.authentication.UserDetailsService;
import org.wise.portal.service.user.UserService;

/**
 * @author Sally Ahn
 */
@Controller
@RequestMapping("/admin/account/manageusers")
public class ViewAllUsersController{

  @Autowired
  private UserService userService;

  @Autowired
  private UserDetailsService userDetailsService;

  protected static final String TEACHERS = "teachers";

  protected static final String STUDENTS = "students";

  protected static final String STUDENT = "student";

  protected static final String TEACHER = "teacher";

  protected static final String OTHER = "other";

  protected static final String USER_TYPE = "userType";

  private static final String USERNAMES = "usernames";

  @GetMapping
  protected String showUsers(HttpServletRequest request, ModelMap modelMap) throws Exception {
    String onlyShowUsersWhoLoggedIn = request.getParameter("onlyShowUsersWhoLoggedIn");
    if (onlyShowUsersWhoLoggedIn != null) {
      List<User> studentsWhoLoggedInSince = new ArrayList<User>();
      List<User> teachersWhoLoggedInSince = new ArrayList<User>();
      if ("today".equals(onlyShowUsersWhoLoggedIn)) {
        studentsWhoLoggedInSince =
            userService.retrieveStudentUsersWhoLoggedInToday();
        teachersWhoLoggedInSince =
            userService.retrieveTeacherUsersWhoLoggedInToday();
      } else if ("thisWeek".equals(onlyShowUsersWhoLoggedIn)) {
        studentsWhoLoggedInSince =
            userService.retrieveStudentUsersWhoLoggedInThisWeek();
        teachersWhoLoggedInSince =
            userService.retrieveTeacherUsersWhoLoggedInThisWeek();
      } else if ("thisMonth".equals(onlyShowUsersWhoLoggedIn)) {
        studentsWhoLoggedInSince =
            userService.retrieveStudentUsersWhoLoggedInThisMonth();
        teachersWhoLoggedInSince =
            userService.retrieveTeacherUsersWhoLoggedInThisMonth();
      } else if ("thisYear".equals(onlyShowUsersWhoLoggedIn)) {
        studentsWhoLoggedInSince =
            userService.retrieveStudentUsersWhoLoggedInThisYear();
        teachersWhoLoggedInSince =
            userService.retrieveTeacherUsersWhoLoggedInThisYear();
      } else {
        studentsWhoLoggedInSince =
            userService.retrieveStudentUsersWhoLoggedInSinceYesterday();
        teachersWhoLoggedInSince =
            userService.retrieveTeacherUsersWhoLoggedInSinceYesterday();
      }
      modelMap.put("studentsWhoLoggedInSince", studentsWhoLoggedInSince);
      modelMap.put("teachersWhoLoggedInSince", teachersWhoLoggedInSince);
    } else {
      String userType = request.getParameter(USER_TYPE);
      if (userType == null) {
        List<String> allUsernames = this.userService.retrieveAllUsernames();
        modelMap.put(USERNAMES, allUsernames);
      } else if (userType.equals(STUDENT)) {
        List<String> usernames = this.userDetailsService.retrieveAllStudentUsernames();
        modelMap.put(STUDENTS, usernames);
      } else if (userType.equals(TEACHER)) {
        List<String> usernames = this.userDetailsService.retrieveAllTeacherUsernames();
        modelMap.put(TEACHERS, usernames);
      }
    }
    return "admin/account/manageusers";
  }
}
