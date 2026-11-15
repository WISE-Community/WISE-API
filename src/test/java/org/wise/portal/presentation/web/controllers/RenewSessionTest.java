package org.wise.portal.presentation.web.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.easymock.EasyMock;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.switchuser.SwitchUserFilter;
import org.springframework.security.web.authentication.switchuser.SwitchUserGrantedAuthority;
import org.springframework.test.util.ReflectionTestUtils;
import org.wise.portal.domain.authentication.impl.StudentUserDetails;
import org.wise.portal.domain.portal.Portal;
import org.wise.portal.domain.portal.impl.PortalImpl;
import org.wise.portal.domain.user.User;
import org.wise.portal.service.authentication.UserDetailsService;
import org.wise.portal.service.portal.PortalService;
import org.wise.portal.service.user.UserService;

class RenewSessionTest {

  private static final GrantedAuthority STUDENT =
      new SimpleGrantedAuthority(UserDetailsService.STUDENT_ROLE);

  private Object previousUserService;

  private LoginController controller;

  private MockHttpServletResponse response;

  @BeforeEach
  void setUp() {
    previousUserService = ReflectionTestUtils.getField(ControllerUtil.class, "userService");
    controller = new LoginController();
    response = new MockHttpServletResponse();
  }

  @AfterEach
  void tearDown() {
    ReflectionTestUtils.setField(ControllerUtil.class, "userService", previousUserService);
    SecurityContextHolder.clearContext();
  }

  @Test
  void aStudentIsAskedToLeaveAClosedSite() throws Exception {
    renew(false, false);
    assertEquals("requestLogout", response.getContentAsString());
  }

  @Test
  void anAdministratorStaysInAClosedSite() throws Exception {
    renew(false, true);
    assertEquals("true", response.getContentAsString());
  }

  @Test
  void anAdministratorImpersonatingAStudentStaysInAClosedSite() throws Exception {
    renew(false, false, STUDENT, switchedFrom(UserDetailsService.ADMIN_ROLE));
    assertEquals("true", response.getContentAsString());
  }

  @Test
  void aResearcherImpersonatingAStudentIsAskedToLeaveAClosedSite() throws Exception {
    renew(false, false, STUDENT, switchedFrom(UserDetailsService.RESEARCHER_ROLE));
    assertEquals("requestLogout", response.getContentAsString());
  }

  private static GrantedAuthority switchedFrom(String role) {
    return new SwitchUserGrantedAuthority(SwitchUserFilter.ROLE_PREVIOUS_ADMINISTRATOR,
        new UsernamePasswordAuthenticationToken("original", "",
            List.of(new SimpleGrantedAuthority(role))));
  }

  @Test
  void anyoneStaysInAnOpenSite() throws Exception {
    renew(true, false);
    assertEquals("true", response.getContentAsString());
  }

  private void renew(boolean loginAllowed, boolean admin, GrantedAuthority... authorities)
      throws Exception {
    StudentUserDetails details = new StudentUserDetails();
    details.setUsername("someone");
    SecurityContextHolder.getContext().setAuthentication(
        new UsernamePasswordAuthenticationToken(details, "", List.of(authorities)));
    User user = EasyMock.createMock(User.class);
    EasyMock.expect(user.isAdmin()).andReturn(admin).anyTimes();
    UserService userService = EasyMock.createMock(UserService.class);
    EasyMock.expect(userService.retrieveUser(details)).andReturn(user);
    Portal portal = new PortalImpl();
    portal.setSettings("{isLoginAllowed:" + loginAllowed + "}");
    PortalService portalService = EasyMock.createMock(PortalService.class);
    EasyMock.expect(portalService.getById(1)).andReturn(portal);
    EasyMock.replay(user, userService, portalService);
    ReflectionTestUtils.setField(ControllerUtil.class, "userService", userService);
    ReflectionTestUtils.setField(controller, "portalService", portalService);

    controller.renewSession(response);
  }
}
