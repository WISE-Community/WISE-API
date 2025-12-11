package org.wise.portal.presentation.web.controllers.survey;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.isA;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpServletRequest;

import org.easymock.EasyMockExtension;
import org.easymock.Mock;
import org.easymock.TestSubject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.wise.portal.domain.authentication.impl.StudentUserDetails;
import org.wise.portal.domain.group.Group;
import org.wise.portal.domain.group.impl.PersistentGroup;
import org.wise.portal.domain.project.impl.Projectcode;
import org.wise.portal.domain.run.Run;
import org.wise.portal.domain.run.impl.RunImpl;
import org.wise.portal.domain.user.User;
import org.wise.portal.domain.user.impl.UserImpl;
import org.wise.portal.domain.workgroup.Workgroup;
import org.wise.portal.domain.workgroup.impl.WorkgroupImpl;
import org.wise.portal.service.authentication.UserDetailsService;
import org.wise.portal.service.run.RunService;
import org.wise.portal.service.student.StudentService;
import org.wise.portal.service.user.UserService;
import org.wise.portal.service.workgroup.WorkgroupService;

@ExtendWith(EasyMockExtension.class)
public class SurveyAPIControllerTest {
  private Run run;
  private TestingAuthenticationToken authority;
  private SecurityContext securityContext;
  private UserImpl studentUser = new UserImpl();
  private StudentUserDetails studentUserDetails = new StudentUserDetails();
  List<Workgroup> workgroups;

  @TestSubject
  SurveyAPIController surveyAPIController = new SurveyAPIController();

  @Mock
  AuthenticationManager authenticationManager;

  @Mock
  HttpServletResponse httpServletResponse;

  @Mock
  HttpServletRequest httpServletRequest;

  @Mock
  HttpSession httpSession;

  @Mock
  RunService runService;

  @Mock
  StudentService studentService;

  @Mock
  UserService userService;

  @Mock
  UserDetailsService userDetailsService;

  @Mock
  WorkgroupService workgroupService;

  private static final String[] periodnames = { "1", "2", "3", "6", "9", "10", "sunflower" };

  @BeforeEach
  public void setUp() {
    run = new RunImpl();
    run.setId(1L);
    run.setIsSurvey(true);
    run.setStarttime(new Date(System.currentTimeMillis() - 3600 * 1000));
    Set<Group> periods = new TreeSet<Group>();
    for (String periodname : periodnames) {
      Group period = new PersistentGroup();
      period.setName(periodname);
      if (periodname.equals("1")) {
        period.addMember(studentUser);
      }
      periods.add(period);
    }
    run.setPeriods(periods);
    workgroups = new ArrayList<Workgroup>();
  }

  SecurityContext getSecurityContext(Object principal, String authorityName) {
    authority = new TestingAuthenticationToken(principal,
        new GrantedAuthority[] { new SimpleGrantedAuthority(authorityName) });
    authority.setAuthenticated(true);
    authority.setDetails(new StudentUserDetails());
    securityContext = new SecurityContextImpl();
    securityContext.setAuthentication(authority);
    return securityContext;
  }

  @Test
  public void launchSurveyRun_NotASurvey_RedirectHomePage() throws Exception {
    httpServletResponse.sendRedirect("/");
    expectLastCall();
    replay(httpServletResponse);
    run.setIsSurvey(false);
    expect(runService.retrieveRunByRuncode("dog1234")).andReturn(run);
    replay(runService);
    surveyAPIController.launchSurveyRun("dog1234-1", httpServletResponse, httpServletRequest);
    verify(httpServletResponse);
    verify(runService);
  }

  @Test
  public void launchSurveyRun_RunInFuture_RedirectHomePage() throws Exception {
    httpServletResponse.sendRedirect("/");
    expectLastCall();
    replay(httpServletResponse);
    run.setStarttime(new Date(System.currentTimeMillis() + 3600 * 1000)); // Future start time
    expect(runService.retrieveRunByRuncode("dog1234")).andReturn(run);
    replay(runService);
    surveyAPIController.launchSurveyRun("dog1234-1", httpServletResponse, httpServletRequest);
    verify(httpServletResponse);
    verify(runService);
  }

  @Test
  public void launchSurveyRun_RunInPast_RedirectHomePage() throws Exception {
    httpServletResponse.sendRedirect("/");
    expectLastCall();
    replay(httpServletResponse);
    run.setEndtime(new Date(System.currentTimeMillis() - 3600 * 1000)); // Past end time
    expect(runService.retrieveRunByRuncode("dog1234")).andReturn(run);
    replay(runService);
    surveyAPIController.launchSurveyRun("dog1234-1", httpServletResponse, httpServletRequest);
    verify(httpServletResponse);
    verify(runService);
  }

  @Test
  public void launchSurveyRun_AlreadyAssociatedWithRun_RedirectUnit() throws Exception {
    httpServletResponse.sendRedirect("/student/unit/1");
    expectLastCall();
    replay(httpServletResponse);
    expect(runService.retrieveRunByRuncode("dog1234")).andReturn(run);
    replay(runService);
    expect(userService.retrieveStudentById(null)).andReturn(studentUser);
    replay(userService);
    SecurityContextHolder.setContext(getSecurityContext(studentUserDetails, "ROLE_STUDENT"));
    surveyAPIController.launchSurveyRun("dog1234-1", httpServletResponse, httpServletRequest);
    verify(httpServletResponse, runService, userService);
  }

  @Test
  public void launchSurveyRun_OverWorkgroupLimit_RedirectWorkgroupLimitPage() throws Exception {
    httpServletResponse.sendRedirect("/survey/workgroupLimitReached");
    expectLastCall();
    replay(httpServletResponse);
    expect(userService.retrieveStudentById(1L)).andReturn(new UserImpl());
    expect(runService.retrieveRunByRuncode("dog1234")).andReturn(run);
    replay(runService);
    SecurityContextHolder.setContext(getSecurityContext("anonymousUser", "ROLE_ANONYMOUS"));
    for (int i = 0; i < 1005; i++) {
      workgroups.add(new WorkgroupImpl());
    }
    expect(workgroupService.getWorkgroupsForRun(run)).andReturn(workgroups);
    replay(workgroupService);
    surveyAPIController.launchSurveyRun("dog1234-1", httpServletResponse, httpServletRequest);
    verify(httpServletResponse, runService, workgroupService);
  }

  @Test
  public void launchSurveyRun_NoIssues_RedirectUnit() throws Exception {
    httpServletResponse.sendRedirect("/student/unit/1");
    expectLastCall();
    replay(httpServletResponse);
    expect(runService.retrieveRunByRuncode("dog1234")).andReturn(run);
    replay(runService);
    studentUser.setUserDetails(studentUserDetails);
    expect(userService.createUser(studentUserDetails)).andReturn(studentUser);
    replay(userService);
    SecurityContextHolder.setContext(getSecurityContext("anonymousUser", "ROLE_ANONYMOUS"));
    expect(workgroupService.getWorkgroupsForRun(run)).andReturn(new ArrayList<Workgroup>());
    replay(workgroupService);
    expect(userDetailsService.loadAuthorityByName("ROLE_SURVEY_STUDENT")).andReturn(null);
    replay(userDetailsService);
    expect(httpServletRequest.getLocale()).andReturn(new Locale("en"));
    expect(httpServletRequest.getSession(true)).andReturn(httpSession);
    replay(httpServletRequest);
    studentService.addStudentToRun(isA(User.class), isA(Projectcode.class));
    expectLastCall();
    replay(studentService);
    surveyAPIController.launchSurveyRun("dog1234-1", httpServletResponse, httpServletRequest);
    verify(httpServletResponse, runService, workgroupService, studentService);
  }
}
