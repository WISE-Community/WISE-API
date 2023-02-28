/**
 * Copyright (c) 2008-2017 Regents of the University of California (Regents).
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

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.session.FindByIndexNameSessionRepository;
import org.springframework.session.Session;
import org.springframework.session.security.SpringSessionBackedSessionRegistry;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;
import org.wise.portal.domain.admin.DailyAdminJob;
import org.wise.portal.domain.portal.Portal;
import org.wise.portal.domain.user.User;
import org.wise.portal.service.authentication.UserDetailsService;
import org.wise.portal.service.portal.PortalService;
import org.wise.portal.service.session.SessionService;

/**
 * Controller for Admin index page
 * @author Hiroki Terashima
 */
@Controller
public class AdminIndexController<S extends Session> {

  @Autowired
  private PortalService portalService;

  @Autowired
  private Environment appProperties;

  @Autowired
  private DailyAdminJob adminJob;

  @Autowired
  private FindByIndexNameSessionRepository<S> sessionRepository;

  @Autowired
  protected SessionService sessionService;

  @Autowired
  private UserDetailsService userDetailsService;

  @GetMapping("/admin")
  protected ModelAndView showAdminHome(HttpServletRequest request) throws Exception {
    ModelAndView modelAndView = new ModelAndView("admin/index");
    this.removeExpiredUserSessions();

    Integer portalId = 1;
    Portal portal = portalService.getById(portalId);
    modelAndView.addObject("portal", portal);
    modelAndView.addObject("isBatchCreateUserAccountsEnabled",
        Boolean.valueOf(appProperties.getProperty("isBatchCreateUserAccountsEnabled", "false")));
    modelAndView.addObject("numCurrentlyLoggedInUsers", sessionService.getNumberSignedInUsers());

    Calendar todayZeroHour = Calendar.getInstance();
    todayZeroHour.set(Calendar.HOUR_OF_DAY, 0);
    todayZeroHour.set(Calendar.MINUTE, 0);
    todayZeroHour.set(Calendar.SECOND, 0);
    todayZeroHour.set(Calendar.MILLISECOND, 0);
    Date dateMin = todayZeroHour.getTime();

    Date dateMax = new Date(Calendar.getInstance().getTimeInMillis());
    adminJob.setYesterday(dateMin);
    adminJob.setToday(dateMax);

    List<User> studentsWhoLoggedInToday = adminJob
        .findUsersWhoLoggedInSinceYesterday("studentUserDetails");
    List<User> teachersWhoLoggedInToday = adminJob
        .findUsersWhoLoggedInSinceYesterday("teacherUserDetails");
    if (studentsWhoLoggedInToday != null && teachersWhoLoggedInToday != null) {
      modelAndView.addObject("numUsersWhoLoggedInToday",
          studentsWhoLoggedInToday.size() + teachersWhoLoggedInToday.size());
    } else {
      modelAndView.addObject("numUsersWhoLoggedInToday", 0);
    }
    return modelAndView;
  }

  private void removeExpiredUserSessions() {
    Set<String> loggedInUsernames = sessionService.getLoggedInStudents();
    loggedInUsernames.addAll(sessionService.getLoggedInTeachers());
    SpringSessionBackedSessionRegistry<S> sessionRegistry = new SpringSessionBackedSessionRegistry<>(
        this.sessionRepository);
    for (String loggedInUsername : loggedInUsernames) {
      UserDetails loggedInUserDetails = userDetailsService.loadUserByUsername(loggedInUsername);
      List<SessionInformation> sessions = sessionRegistry.getAllSessions(loggedInUserDetails,
          false);
      if (sessions.size() == 0) {
        sessionService.removeUser(loggedInUserDetails);
      }
    }
  }
}
