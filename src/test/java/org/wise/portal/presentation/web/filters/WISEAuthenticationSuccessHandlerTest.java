package org.wise.portal.presentation.web.filters;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.easymock.EasyMock;
import org.json.JSONObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;
import org.wise.portal.domain.authentication.MutableUserDetails;
import org.wise.portal.domain.authentication.impl.StudentUserDetails;
import org.wise.portal.domain.portal.Portal;
import org.wise.portal.domain.portal.impl.PortalImpl;
import org.wise.portal.service.authentication.UserDetailsService;
import org.wise.portal.service.portal.PortalService;

class WISEAuthenticationSuccessHandlerTest {

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void signingInToAClosedSite_shouldAnswerLoginDisabled() throws Exception {
    Portal portal = new PortalImpl();
    portal.setSettings("{isLoginAllowed:false}");
    PortalService portalService = EasyMock.createMock(PortalService.class);
    EasyMock.expect(portalService.getById(1)).andReturn(portal);
    UserDetailsService userDetailsService = EasyMock.createMock(UserDetailsService.class);
    EasyMock.replay(portalService, userDetailsService);
    WISEAuthenticationSuccessHandler handler = new WISEAuthenticationSuccessHandler();
    ReflectionTestUtils.setField(handler, "portalService", portalService);
    ReflectionTestUtils.setField(handler, "userDetailsService", userDetailsService);
    ReflectionTestUtils.setField(handler, "appProperties",
        new MockEnvironment().withProperty("wise.hostname", "http://localhost"));
    MutableUserDetails student = new StudentUserDetails();
    student.setUsername("student");
    student.setLanguage("en");
    UsernamePasswordAuthenticationToken authentication =
        new UsernamePasswordAuthenticationToken(student, "", List.of());
    SecurityContextHolder.getContext().setAuthentication(authentication);
    MockHttpServletRequest request = new MockHttpServletRequest("POST",
        "/api/j_acegi_security_check");
    MockHttpServletResponse response = new MockHttpServletResponse();

    handler.onAuthenticationSuccess(request, response, authentication);

    assertNull(response.getRedirectedUrl());
    JSONObject body = new JSONObject(response.getContentAsString());
    assertEquals("error", body.getString("status"));
    assertTrue(body.getBoolean("isLoginDisabled"));
    assertNull(SecurityContextHolder.getContext().getAuthentication());
    EasyMock.verify(portalService, userDetailsService);
  }
}
