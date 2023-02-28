/**
 * Copyright (c) 2008-2022 Regents of the University of California (Regents).
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
package org.wise.portal.presentation.web.filters;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;
import org.wise.portal.dao.ObjectNotFoundException;
import org.wise.portal.domain.authentication.MutableUserDetails;
import org.wise.portal.domain.authentication.impl.StudentUserDetails;
import org.wise.portal.domain.authentication.impl.TeacherUserDetails;
import org.wise.portal.domain.portal.Portal;
import org.wise.portal.presentation.web.controllers.ControllerUtil;
import org.wise.portal.service.authentication.AuthorityNotFoundException;
import org.wise.portal.service.authentication.UserDetailsService;
import org.wise.portal.service.portal.PortalService;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Locale;

/**
 * @author Hiroki Terashima
 */
public class WISEAuthenticationSuccessHandler
    extends SavedRequestAwareAuthenticationSuccessHandler {

  @Autowired
  private UserDetailsService userDetailsService;

  @Autowired
  private PortalService portalService;

  @Autowired
  private Environment appProperties;

  @Override
  public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
      Authentication authentication) throws ServletException, IOException {
    MutableUserDetails userDetails = (MutableUserDetails) authentication.getPrincipal();
    boolean userIsAdmin = false;
    Locale locale = getLocale(request, userDetails);

    if (userDetails instanceof StudentUserDetails) {
      setDefaultTargetUrl(WISEAuthenticationProcessingFilter.STUDENT_DEFAULT_TARGET_PATH);
    } else if (userDetails instanceof TeacherUserDetails) {
      this.setDefaultTargetUrl(WISEAuthenticationProcessingFilter.TEACHER_DEFAULT_TARGET_PATH);
      GrantedAuthority researcherAuth = null;
      try {
        researcherAuth = userDetailsService.loadAuthorityByName(UserDetailsService.RESEARCHER_ROLE);
      } catch (AuthorityNotFoundException e) {
        e.printStackTrace();
      }
      Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();
      for (GrantedAuthority authority : authorities) {
        if (researcherAuth.equals(authority)) {
          setDefaultTargetUrl(WISEAuthenticationProcessingFilter.RESEARCHER_DEFAULT_TARGET_PATH);
        }
      }

      GrantedAuthority adminAuth = null;
      try {
        adminAuth = userDetailsService.loadAuthorityByName(UserDetailsService.ADMIN_ROLE);
      } catch (AuthorityNotFoundException e) {
        e.printStackTrace();
      }
      for (GrantedAuthority authority : authorities) {
        if (adminAuth.equals(authority)) {
          setDefaultTargetUrl(WISEAuthenticationProcessingFilter.ADMIN_DEFAULT_TARGET_PATH);
          userIsAdmin = true;
        }
      }
    }

    // if user is not admin and login is disallowed, log out user and redirect them to the "we are undergoing maintenance" page
    try {
      Portal portal = portalService.getById(new Integer(1));
      if (!userIsAdmin && !portal.isLoginAllowed()) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
          new SecurityContextLogoutHandler().logout(request, response, auth);
        }
        SecurityContextHolder.getContext().setAuthentication(null);
        String contextPath = request.getContextPath();
        response.sendRedirect(
            contextPath + WISEAuthenticationProcessingFilter.LOGIN_DISABLED_MESSGE_PAGE);
        return;
      }
    } catch (ObjectNotFoundException e) {
      // do nothing
    } catch (IOException ioe) {
      // do nothing
    }

    request.getSession().setAttribute(SessionLocaleResolver.LOCALE_SESSION_ATTRIBUTE_NAME, locale);
    userDetailsService.updateStatsOnSuccessfulLogin((MutableUserDetails) userDetails);
    String redirectUrl = (String) request.getAttribute("redirectUrl");
    if (redirectUrl != null) {
      handleRedirectRequest(redirectUrl, request, response, userDetails, locale);
    } else if (request.getServletPath().contains("google-login")) {
      handleGoogleLogin(request, response, userDetails, locale);
    }
    if (ControllerUtil.isUserPreviousAdministrator()) {
      response.sendRedirect(getUserHomeUrl(userDetails, locale));
    }
    //super.handle(request, response, authentication);
  }

  private void handleRedirectRequest(String redirectUrl, HttpServletRequest request,
      HttpServletResponse response, MutableUserDetails userDetails, Locale locale)
      throws IOException {
    if (redirectUrl.equals("/")) {
      response.sendRedirect(getUserHomeUrl(userDetails, locale));
    } else if (redirectUrl.startsWith("http")) {
      response.sendRedirect(redirectUrl);
    } else {
      response.sendRedirect(getHomeUrlWithLocale(locale) + redirectUrl);
    }
  }

  private void handleGoogleLogin(HttpServletRequest request, HttpServletResponse response,
      MutableUserDetails userDetails, Locale locale) throws IOException {
    String accessCode = (String) request.getAttribute("accessCode");
    String homeUrl = getUserHomeUrl(userDetails, locale);
    if (accessCode != null && !accessCode.equals("")) {
      response.sendRedirect(homeUrl + "?accessCode=" + accessCode);
    }
    response.sendRedirect(homeUrl);
  }

  private String getTeacherHomeUrl(Locale locale) {
    return getHomeUrlWithLocale(locale)
        + WISEAuthenticationProcessingFilter.TEACHER_DEFAULT_TARGET_PATH;
  }

  private String getStudentHomeUrl(Locale locale) {
    return getHomeUrlWithLocale(locale)
        + WISEAuthenticationProcessingFilter.STUDENT_DEFAULT_TARGET_PATH;
  }

  private String getHomeUrlWithLocale(Locale locale) {
    return appProperties.getProperty("wise.hostname") + getLocalePath(locale);
  }

  private String getUserHomeUrl(MutableUserDetails userDetails, Locale locale) {
    if (userDetails instanceof StudentUserDetails) {
      return getStudentHomeUrl(locale);
    } else {
      return getTeacherHomeUrl(locale);
    }
  }

  private String getLocalePath(Locale locale) {
    String localePath = "";
    if (locale.equals(Locale.ENGLISH)) {
      localePath = "";
    } else if (locale.equals(Locale.SIMPLIFIED_CHINESE)) {
      localePath = "/zh-Hans";
    } else if (locale.equals(Locale.TRADITIONAL_CHINESE)) {
      localePath = "/zh-Hant";
    } else if (locale.getLanguage().equals(new Locale("es").getLanguage())) {
      localePath = "/es";
    } else if (locale.equals(Locale.JAPANESE)) {
      localePath = "/ja";
    } else if (locale.getLanguage().equals(new Locale("tr").getLanguage())) {
      localePath = "/tr";
    }
    return localePath;
  }

  private Locale getLocale(HttpServletRequest request, MutableUserDetails userDetails) {
    Locale locale = null;
    String userLanguage = userDetails.getLanguage();
    if (userLanguage != null) {
      if (userLanguage.contains("_")) {
        String language = userLanguage.substring(0, userLanguage.indexOf("_"));
        String country = userLanguage.substring(userLanguage.indexOf("_") + 1);
        locale = new Locale(language, country);
      } else {
        locale = new Locale(userLanguage);
      }
    } else {
      // user default browser locale setting if user hasn't specified locale
      locale = request.getLocale();
    }
    return locale;
  }

}
