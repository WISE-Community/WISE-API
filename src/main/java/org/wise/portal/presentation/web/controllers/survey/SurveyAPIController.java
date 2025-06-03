package org.wise.portal.presentation.web.controllers.survey;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.wise.portal.dao.ObjectNotFoundException;
import org.wise.portal.domain.PeriodNotFoundException;
import org.wise.portal.domain.RunHasEndedException;
import org.wise.portal.domain.StudentUserAlreadyAssociatedWithRunException;
import org.wise.portal.domain.authentication.Gender;
import org.wise.portal.domain.authentication.impl.StudentUserDetails;
import org.wise.portal.domain.project.impl.Projectcode;
import org.wise.portal.domain.run.Run;
import org.wise.portal.domain.user.User;
import org.wise.portal.service.authentication.AuthorityNotFoundException;
import org.wise.portal.service.authentication.DuplicateUsernameException;
import org.wise.portal.service.authentication.UserDetailsService;
import org.wise.portal.service.run.RunService;
import org.wise.portal.service.student.StudentService;
import org.wise.portal.service.user.UserService;
import org.wise.portal.service.workgroup.WorkgroupService;

import java.io.IOException;
import java.util.Date;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@RestController
@RequestMapping("/run-survey")
public class SurveyAPIController {
  @Autowired
  private AuthenticationManager authenticationManager;

  @Autowired
  private RunService runService;

  @Autowired
  private StudentService studentService;

  @Autowired
  private UserDetailsService userDetailsService;

  @Autowired
  private UserService userService;

  @Autowired
  private WorkgroupService workgroupService;

  @GetMapping("/{code}")
  public void launchSurveyRun(@PathVariable String code, HttpServletResponse response,
      HttpServletRequest request) throws AuthorityNotFoundException, IOException,
      DuplicateUsernameException, ObjectNotFoundException, PeriodNotFoundException,
      StudentUserAlreadyAssociatedWithRunException, RunHasEndedException {
    Projectcode projectCode = new Projectcode(code);
    Run run = runService.retrieveRunByRuncode(projectCode.getRuncode());
    if (run.isSurvey() && isActive(run)) {
      handleSurveyLaunched(response, request, run, projectCode);
    } else {
      sendRedirect(response, "/");
    }
  }

  private boolean isActive(Run run) {
    Date now = new Date();
    Date endTime = run.getEndtime();
    return run.getStarttime().before(now) && (endTime == null || endTime.after(now));
  }

  private void handleSurveyLaunched(HttpServletResponse response, HttpServletRequest request,
      Run run, Projectcode projectCode) throws AuthorityNotFoundException, IOException,
      DuplicateUsernameException, ObjectNotFoundException, PeriodNotFoundException,
      StudentUserAlreadyAssociatedWithRunException, RunHasEndedException {

    Object principal = getSecurityContextHolderPrincipal();
    if (principal instanceof StudentUserDetails
        && isStudentAssociatedWithRun(run, (StudentUserDetails) principal)) {
      sendRedirect(response, "/student/unit/" + run.getId());
      return;
    } else {
      SecurityContextHolder.getContext().setAuthentication(null);
    }
    if (underWorkgroupLimit(run)) {
      String password = RandomStringUtils.randomAlphanumeric(10);
      User user = this.createNewStudentAccount(request.getLocale(), password);
      loginStudent(request, user, password);
      studentService.addStudentToRun(user, projectCode);
      sendRedirect(response, "/student/unit/" + run.getId());
    } else {
      sendRedirect(response, "/survey/workgroupLimitReached");
    }
  }

  private void sendRedirect(HttpServletResponse response, String redirectUrl) throws IOException {
    response.sendRedirect(redirectUrl);
  }

  private Object getSecurityContextHolderPrincipal() {
    return SecurityContextHolder.getContext().getAuthentication().getPrincipal();
  }

  private boolean isStudentAssociatedWithRun(Run run, StudentUserDetails principal) {
    return run.isStudentAssociatedToThisRun(userService.retrieveStudentById((principal).getId()));
  }

  private boolean underWorkgroupLimit(Run run) {
    return workgroupService.getWorkgroupsForRun(run).size() <= 1000;
  }

  private void loginStudent(HttpServletRequest request, User user, String password) {
    UsernamePasswordAuthenticationToken authReq = new UsernamePasswordAuthenticationToken(
        user.getUserDetails().getUsername(), password);
    Authentication auth = authenticationManager.authenticate(authReq);
    SecurityContext sc = SecurityContextHolder.getContext();
    sc.setAuthentication(auth);
    HttpSession session = request.getSession(true);
    session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, sc);
  }

  private User createNewStudentAccount(Locale locale, String password)
      throws AuthorityNotFoundException, DuplicateUsernameException {
    StudentUserDetails sud = new StudentUserDetails();
    sud.setFirstname("survey_student_" + RandomStringUtils.randomAlphanumeric(10));
    sud.setLastname(RandomStringUtils.randomAlphanumeric(10));
    sud.setBirthday(new Date());
    sud.setPassword(password);
    sud.setGender(Gender.UNSPECIFIED);
    sud.setEmailAddress("null@null.com");
    sud.setLanguage(locale.getLanguage());
    sud.setNumberOfLogins(1);
    sud.setLastLoginTime(new Date());

    User user = userService.createUser(sud);
    user.getUserDetails().addAuthority(
        userDetailsService.loadAuthorityByName(UserDetailsService.SURVEY_STUDENT_ROLE));
    return user;
  }
}
