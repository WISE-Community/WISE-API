/**
 * Copyright (c) 2007-2017 Regents of the University of California (Regents).
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
package org.wise.portal.presentation.web.controllers.teacher;

import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.HttpSessionRequiredException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;
import org.springframework.web.servlet.view.RedirectView;
import org.wise.portal.dao.ObjectNotFoundException;
import org.wise.portal.domain.authentication.Curriculumsubjects;
import org.wise.portal.domain.authentication.Schoollevel;
import org.wise.portal.domain.authentication.impl.ChangePasswordParameters;
import org.wise.portal.domain.authentication.impl.TeacherUserDetails;
import org.wise.portal.domain.user.User;
import org.wise.portal.presentation.validators.ChangePasswordParametersValidator;
import org.wise.portal.presentation.validators.TeacherAccountFormValidator;
import org.wise.portal.presentation.web.TeacherAccountForm;
import org.wise.portal.presentation.web.controllers.ControllerUtil;
import org.wise.portal.service.authentication.DuplicateUsernameException;
import org.wise.portal.service.mail.IMailFacade;
import org.wise.portal.service.user.UserService;

import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;
import java.util.Calendar;
import java.util.Locale;
import java.util.Properties;

/**
 * Controller for creating and updating SCORE teacher accounts
 *
 * @author Hiroki Terashima
 */
@Controller
@SessionAttributes({"teacherAccountForm", "changePasswordParameters"})
@RequestMapping(value = "/legacy/teacher")
public class TeacherAccountController {

  @Autowired
  protected Properties appProperties;

  @Autowired
  protected IMailFacade mailService;

  @Autowired
  protected MessageSource messageSource;

  @Autowired
  protected UserService userService;

  @Autowired
  protected TeacherAccountFormValidator teacherAccountFormValidator;

  @Autowired
  protected ChangePasswordParametersValidator changePasswordParametersValidator;

  /**
   * Called before the page is loaded to initialize values.
   * Adds the TeacherAccountForm object to the model.
   * This object will be filled out and submitted for creating
   * the new teacher
   * @param modelMap the model object that contains values for the page to use when rendering the view
   * @return the path of the view to display
   */
  @RequestMapping(value = "/join", method = RequestMethod.GET)
  public String initializeFormNewTeacher(ModelMap modelMap) throws Exception {
    TeacherAccountForm teacherAccountForm = new TeacherAccountForm();
    modelMap.addAttribute("teacherAccountForm", teacherAccountForm);
    populateModelMap(modelMap);
    return "teacher/join";
  }

  /**
   * Shows page where teacher can update account information
   * Switched user (e.g. admin/researcher logged in as this user) should not be able to view/modify
   * user account.
   */
  @RequestMapping(value = "/account", method = RequestMethod.GET)
  public String updateMyAccountPage(ModelMap modelMap) {
    if (ControllerUtil.isUserPreviousAdministrator()) {
      return "errors/accessdenied";
    } else {
      User signedInUser = ControllerUtil.getSignedInUser();
      if (signedInUser.isTeacher()) {
        TeacherUserDetails teacherUserDetails = (TeacherUserDetails) signedInUser.getUserDetails();
        TeacherAccountForm teacherAccountForm = new TeacherAccountForm(teacherUserDetails);
        modelMap.addAttribute("teacherAccountForm", teacherAccountForm);
        setChangePasswordParametersInModelMap(modelMap, signedInUser);
        populateModelMap(modelMap);
        return "teacher/account";
      }
      return "errors/accessdenied";
    }
  }

  /**
   * Populate the model map with objects the form requires
   * @param modelMap the model to populate
   * @return the model
   */
  protected ModelMap populateModelMap(ModelMap modelMap) {
    try {
      modelMap.put("schoollevels", Schoollevel.values());
      modelMap.put("curriculumsubjects",Curriculumsubjects.values());
      String supportedLocales = appProperties
          .getProperty("supportedLocales", "en,zh_TW,zh_CN,nl,he,ja,ko,es,pt,tr");
      modelMap.put("languages", supportedLocales.split(","));
    } catch (Exception e) {
      e.printStackTrace();
    }
    return modelMap;
  }

  private void setChangePasswordParametersInModelMap(ModelMap modelMap, User user) {
    ChangePasswordParameters params = new ChangePasswordParameters();
    params.setUser(user);
    modelMap.addAttribute("changePasswordParameters", params);
  }

  /**
   * Creates a new teacher user and saves to data store
   * @param accountForm the model object that contains values for the page to use when rendering the view
   * @param bindingResult the object used for validation in which errors will be stored
   * @param request the http request object
   * @param modelMap the object that contains values to be displayed on the page
   * @return the path of the view to display
   */
  @RequestMapping(value = "/join", method = RequestMethod.POST)
  protected String createNewTeacher(
      @ModelAttribute("teacherAccountForm") TeacherAccountForm accountForm,
      BindingResult bindingResult,
      HttpServletRequest request,
      ModelMap modelMap) {
    TeacherUserDetails userDetails = (TeacherUserDetails) accountForm.getUserDetails();
    userDetails.setSignupdate(Calendar.getInstance().getTime());
    teacherAccountFormValidator.validate(accountForm, bindingResult);
    if (bindingResult.hasErrors()) {
      populateModelMap(modelMap);
      return "teacher/join";
    }

    try {
      userDetails.setDisplayname(userDetails.getFirstname() + " " + userDetails.getLastname());
      userDetails.setEmailValid(true);
      userDetails.setLanguage(appProperties.getProperty("defaultLocale", "en"));
      User createdUser = this.userService.createUser(userDetails);
      NewAccountEmailService newAccountEmailService = new NewAccountEmailService(createdUser, request.getLocale(), request);
      Thread thread = new Thread(newAccountEmailService);
      thread.start();
      modelMap.addAttribute("username", userDetails.getUsername());
      modelMap.addAttribute("displayname", userDetails.getDisplayname());
      return "teacher/joinsuccess";
    } catch (DuplicateUsernameException e) {
      bindingResult.rejectValue("username", "error.duplicate-username", new Object[] { userDetails.getUsername() }, "Duplicate Username.");
      populateModelMap(modelMap);
      return "teacher/join";
    }
  }

  /**
   * Updates an existing teacher record
   * @param accountForm the model object that contains values for the page to use when rendering the view
   * @param bindingResult the object used for validation in which errors will be stored
   * @param request the http request object
   * @param modelMap the object that contains values to be displayed on the page
   * @return the path of the view to display
   */
  @RequestMapping(value = "/account", method = RequestMethod.POST)
  protected String updateExistingTeacher(
      @ModelAttribute("teacherAccountForm") TeacherAccountForm accountForm,
      BindingResult bindingResult,
      HttpServletRequest request,
      ModelMap modelMap) {
    TeacherUserDetails userDetails = (TeacherUserDetails) accountForm.getUserDetails();
    teacherAccountFormValidator.validate(accountForm, bindingResult);
    if (bindingResult.hasErrors()) {
      populateModelMap(modelMap);
      return "teacher/account";
    }

    User user = userService.retrieveUserByUsername(userDetails.getUsername());
    updateTeacherUserDetails(user, userDetails);
    updateUserLocaleInSession(user, request);
    userService.updateUser(user);
    request.getSession().setAttribute(User.CURRENT_USER_SESSION_KEY, user);
    setChangePasswordParametersInModelMap(modelMap, user);
    modelMap.put("accountInfoSavedMessage", "Changes saved!");
    populateModelMap(modelMap);
    return "teacher/account";
  }

  /**
   * Updates an existing teacher record
   * @param bindingResult the object used for validation in which errors will be stored
   * @param request the http request object
   * @param modelMap the object that contains values to be displayed on the page
   * @return the path of the view to display
   */
  @RequestMapping(value = "/account/password", method = RequestMethod.POST)
  protected String updateExistingTeacherPassword(
      @ModelAttribute("changePasswordParameters") ChangePasswordParameters params,
      BindingResult bindingResult,
      HttpServletRequest request,
      ModelMap modelMap) throws ObjectNotFoundException {
    changePasswordParametersValidator.validate(params, bindingResult);
    if (bindingResult.hasErrors()) {
      populateModelMap(modelMap);
      return "teacher/account";
    } else {
      User user = userService.retrieveById(params.getUser().getId());
      userService.updateUserPassword(user, params.getPasswd1());
      request.getSession().setAttribute(User.CURRENT_USER_SESSION_KEY, user);
      setChangePasswordParametersInModelMap(modelMap, user);
      modelMap.put("passwordSavedMessage", "Password changes saved!");
      populateModelMap(modelMap);
      return "teacher/account";
    }
  }

  private void updateTeacherUserDetails(User user, TeacherUserDetails newUserDetails) {
    TeacherUserDetails teacherUserDetails = (TeacherUserDetails) user.getUserDetails();
    teacherUserDetails.setCity(newUserDetails.getCity());
    teacherUserDetails.setCountry(newUserDetails.getCountry());
    teacherUserDetails.setCurriculumsubjects(newUserDetails.getCurriculumsubjects());
    teacherUserDetails.setEmailAddress(newUserDetails.getEmailAddress());
    teacherUserDetails.setSchoollevel(newUserDetails.getSchoollevel());
    teacherUserDetails.setSchoolname(newUserDetails.getSchoolname());
    teacherUserDetails.setState(newUserDetails.getState());
    teacherUserDetails.setDisplayname(newUserDetails.getDisplayname());
    teacherUserDetails.setEmailValid(true);

    if ("default".equals(newUserDetails.getLanguage())) {
      teacherUserDetails.setLanguage(null);
    } else {
      teacherUserDetails.setLanguage(newUserDetails.getLanguage());
    }
  }

  private void updateUserLocaleInSession(User user, HttpServletRequest request) {
    Locale locale;
    String userLanguage = user.getUserDetails().getLanguage();
    if (userLanguage != null) {
      if (userLanguage.contains("_")) {
        String language = userLanguage.substring(0, userLanguage.indexOf("_"));
        String country = userLanguage.substring(userLanguage.indexOf("_")+1);
        locale = new Locale(language, country);
      } else {
        locale = new Locale(userLanguage);
      }
    } else {
      locale = request.getLocale();
    }
    request.getSession().setAttribute(SessionLocaleResolver.LOCALE_SESSION_ATTRIBUTE_NAME, locale);
  }

  @ExceptionHandler(HttpSessionRequiredException.class)
  public ModelAndView handleSessionExpired(HttpServletRequest request) {
    String contextPath = request.getContextPath();
    ModelAndView mav = new ModelAndView();
    if (isOnRegisterNewTeacherPage(request)) {
      mav.setView(new RedirectView(contextPath + "/teacher/join"));
    } else {
      mav.setView(new RedirectView(contextPath + "/index.html"));
    }
    return mav;
  }

  public boolean isOnRegisterNewTeacherPage(HttpServletRequest request) {
    String domain = ControllerUtil.getBaseUrlString(request);
    String domainWithPort = domain + ":" + request.getLocalPort();
    String referrer = request.getHeader("referer");
    String contextPath = request.getContextPath();
    String registerUrl = contextPath + "/teacher/join";

    return referrer != null &&
        (referrer.contains(domain + registerUrl) || referrer.contains(domainWithPort + registerUrl));
  }

  class NewAccountEmailService implements Runnable {
    private User newUser;
    private Locale locale;
    private HttpServletRequest request;

    public NewAccountEmailService(User newUser, Locale locale, HttpServletRequest request) {
      this.newUser = newUser;
      this.locale = locale;
      this.request = request;
    }

    public void run() {
      String sendEmailEnabledStr = appProperties.getProperty("send_email_enabled", "false");
      Boolean sendEmailEnabled = Boolean.valueOf(sendEmailEnabledStr);
      if (!sendEmailEnabled) {
        return;
      } else {
        this.sendEmail(this.request);
      }
    }

    /**
     * Sends a welcome email to the new user with WISE resources.
     */
    private void sendEmail(HttpServletRequest request) {
      TeacherUserDetails newUserDetails = (TeacherUserDetails) newUser.getUserDetails();
      String userUsername = newUserDetails.getUsername();
      String userEmailAddress[] = {newUserDetails.getEmailAddress()};
      String[] recipients = (String[]) ArrayUtils.addAll(userEmailAddress,
          appProperties.getProperty("uber_admin").split(","));
      String defaultSubject = messageSource.getMessage("presentation.web.controllers.teacher.registerTeacherController.welcomeTeacherEmailSubject", null, Locale.US);
      String subject = messageSource.getMessage("presentation.web.controllers.teacher.registerTeacherController.welcomeTeacherEmailSubject", null, defaultSubject, this.locale);
      String portalString = ControllerUtil.getPortalUrlString(request);
      String gettingStartedUrl = portalString + "/pages/gettingstarted.html";
      String defaultBody = messageSource.getMessage("presentation.web.controllers.teacher.registerTeacherController.welcomeTeacherEmailBody", new Object[] {userUsername,gettingStartedUrl}, Locale.US);
      String message = messageSource.getMessage("presentation.web.controllers.teacher.registerTeacherController.welcomeTeacherEmailBody", new Object[] {userUsername,gettingStartedUrl}, defaultBody, this.locale);

      if (appProperties.containsKey("discourse_url")) {
        String discourseURL = appProperties.getProperty("discourse_url");
        if (discourseURL != null && !discourseURL.isEmpty()) {
          // if this WISE instance uses discourse for teacher community, append link to it in the P.S. section of the email
          String defaultPS = messageSource.getMessage("teacherEmailPSCommunity", new Object[] {discourseURL}, Locale.US);
          String pS = messageSource.getMessage("teacherEmailPSCommunity", new Object[] {discourseURL}, defaultPS, this.locale);
          message += "\n\n"+pS;
        }
      }
      String fromEmail = appProperties.getProperty("portalemailaddress");
      try {
        mailService.postMail(recipients, subject, message, fromEmail);
      } catch (MessagingException e) {
        e.printStackTrace();
      }
    }
  }
}
