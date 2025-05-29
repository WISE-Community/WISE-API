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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

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

    Projectcode projectCode = new Projectcode(code.replaceAll("\\+\\+", " "));
    Run run = runService.retrieveRunByRuncode(projectCode.getRuncode());
    if (run.getIsSurvey()) {
      handleSurveyLaunched(response, request, run, projectCode);
    } else {
      response.sendRedirect("/");
    }
  }

  private void handleSurveyLaunched(HttpServletResponse response, HttpServletRequest request,
      Run run, Projectcode projectCode) throws AuthorityNotFoundException, IOException,
      DuplicateUsernameException, ObjectNotFoundException, PeriodNotFoundException,
      StudentUserAlreadyAssociatedWithRunException, RunHasEndedException {
    if (userAlreadySignedIn()) {
      response.sendRedirect("/survey/logout");
    } else if (underWorkgroupLimit(run)) {
      User user = this.createNewStudentAccount();
      loginStudent(request, user);
      studentService.addStudentToRun(user, projectCode);
      response.sendRedirect("/student/unit/" + run.getId());
    } else {
      response.sendRedirect("/survey/workgroupLimitReached");
    }
  }

  private Boolean userAlreadySignedIn() {
    return !SecurityContextHolder.getContext().getAuthentication().getPrincipal()
        .equals("anonymousUser");
  }

  private Boolean underWorkgroupLimit(Run run) {
    return workgroupService.getWorkgroupsForRun(run).size() <= 1000;
  }

  private void loginStudent(HttpServletRequest request, User user) {
    UsernamePasswordAuthenticationToken authReq = new UsernamePasswordAuthenticationToken(
        user.getUserDetails().getUsername(), "null");
    Authentication auth = authenticationManager.authenticate(authReq);
    SecurityContext sc = SecurityContextHolder.getContext();
    sc.setAuthentication(auth);
    HttpSession session = request.getSession(true);
    session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, sc);
  }

  private User createNewStudentAccount()
      throws AuthorityNotFoundException, DuplicateUsernameException {
    StudentUserDetails sud = new StudentUserDetails();
    sud.setFirstname("survey_student");
    sud.setLastname(Integer.toString((int) Math.ceil(Math.random() * 10000)));
    sud.setBirthday(new Date());
    sud.setPassword("null");
    sud.setGender(Gender.UNSPECIFIED);
    sud.setEmailAddress("null@null.com");
    sud.setLanguage("null");

    User user = userService.createUser(sud);
    user.getUserDetails().addAuthority(
        userDetailsService.loadAuthorityByName(UserDetailsService.SURVEY_STUDENT_ROLE));

    return user;
  }
}
