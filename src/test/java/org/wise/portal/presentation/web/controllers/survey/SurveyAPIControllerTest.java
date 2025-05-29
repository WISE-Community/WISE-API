package org.wise.portal.presentation.web.controllers.survey;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.easymock.EasyMockExtension;
import org.easymock.Mock;
import org.easymock.TestSubject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.wise.portal.domain.run.Run;
import org.wise.portal.domain.run.impl.RunImpl;
import org.wise.portal.domain.workgroup.Workgroup;
import org.wise.portal.domain.workgroup.impl.WorkgroupImpl;
import org.wise.portal.service.run.RunService;
import org.wise.portal.service.workgroup.WorkgroupService;

@ExtendWith(EasyMockExtension.class)
public class SurveyAPIControllerTest {
  private Run run;
  private TestingAuthenticationToken authority;
  private SecurityContext securityContext;

  @TestSubject
  SurveyAPIController surveyAPIController = new SurveyAPIController();

  @Mock
  HttpServletResponse httpServletResponse;

  @Mock
  HttpServletRequest httpServletRequest;

  @Mock
  RunService runService;

  @Mock
  WorkgroupService workgroupService;

  @BeforeEach
  public void setUp() {
    run = new RunImpl();
    run.setId(1L);
  }

  SecurityContext getSecurityContext(String role, String authorityName) {
    authority = new TestingAuthenticationToken(role,
        new GrantedAuthority[] { new SimpleGrantedAuthority(authorityName) });
    authority.setAuthenticated(true);
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
  public void launchSurveyRun_AlreadySignedIn_RedirectLogOutPage() throws Exception {
    httpServletResponse.sendRedirect("/survey/logout");
    expectLastCall();
    replay(httpServletResponse);
    run.setIsSurvey(true);
    expect(runService.retrieveRunByRuncode("dog1234")).andReturn(run);
    replay(runService);
    SecurityContextHolder.setContext(getSecurityContext("student", "ROLE_STUDENT"));
    surveyAPIController.launchSurveyRun("dog1234-1", httpServletResponse, httpServletRequest);
    verify(httpServletResponse);
    verify(runService);
  }

  @Test
  public void launchSurveyRun_OverWorkgroupLimit_RedirectWorkgroupLimitPage() throws Exception {
    httpServletResponse.sendRedirect("/survey/workgroupLimitReached");
    expectLastCall();
    replay(httpServletResponse);
    run.setIsSurvey(false);
    expect(runService.retrieveRunByRuncode("dog1234")).andReturn(run);
    replay(runService);
    SecurityContextHolder.setContext(getSecurityContext("anonymousUser", "ROLE_ANONYMOUS"));
    List<Workgroup> workgroups = new ArrayList<Workgroup>();
    for (int i = 0; i < 1005; i++) {
      workgroups.add(new WorkgroupImpl());
    }
    expect(workgroupService.getWorkgroupsForRun(run)).andReturn(workgroups);
    replay(workgroupService);
    surveyAPIController.launchSurveyRun("dog1234-1", httpServletResponse, httpServletRequest);
    verify(httpServletResponse);
    verify(runService);
    verify(workgroupService);
  }

  @Test
  public void launchSurveyRun_NoIssues_RedirectUnit() throws Exception {
    httpServletResponse.sendRedirect("/student/unit/1");
    expectLastCall();
    replay(httpServletResponse);
    run.setIsSurvey(false);
    expect(runService.retrieveRunByRuncode("dog1234")).andReturn(run);
    replay(runService);
    SecurityContextHolder.setContext(getSecurityContext("anonymousUser", "ROLE_ANONYMOUS"));
    expect(workgroupService.getWorkgroupsForRun(run)).andReturn(new ArrayList<Workgroup>());
    replay(workgroupService);
    surveyAPIController.launchSurveyRun("dog1234-1", httpServletResponse, httpServletRequest);
    verify(httpServletResponse);
    verify(runService);
    verify(workgroupService);
  }
}
