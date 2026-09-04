package org.wise.portal.presentation.web.filters;

import static org.easymock.EasyMock.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.easymock.EasyMockExtension;
import org.easymock.Mock;
import org.easymock.TestSubject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.wise.portal.domain.authentication.impl.PersistentUserDetails;
import org.wise.portal.domain.user.User;
import org.wise.portal.service.user.UserService;

@ExtendWith(EasyMockExtension.class)
public class OAuth2AuthenticationSuccessHandlerTest {

  @TestSubject
  private OAuth2AuthenticationSuccessHandler handler = new OAuth2AuthenticationSuccessHandler();

  @Mock
  private UserService userService;

  @Mock
  private User studentUser;

  @Mock
  private User teacherUser;

  @Mock
  private PersistentUserDetails userDetails;

  @Test
  public void testOnAuthenticationSuccess_GoogleStudent() throws Exception {
    MockHttpServletRequest request = new MockHttpServletRequest();
    MockHttpServletResponse response = new MockHttpServletResponse();

    Map<String, Object> claims = new HashMap<>();
    claims.put("sub", "google-123");
    OidcIdToken idToken = new OidcIdToken("token", null, null, claims);
    OidcUser oidcUser = new DefaultOidcUser(Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")), idToken);

    OAuth2AuthenticationToken token = new OAuth2AuthenticationToken(oidcUser, oidcUser.getAuthorities(), "google");

    expect(userService.retrieveUserByGoogleUserId("google-123")).andReturn(studentUser);
    expect(studentUser.getUserDetails()).andReturn(userDetails);
    expect(userDetails.getAuthorities()).andReturn(Collections.emptyList());
    expect(studentUser.isStudent()).andReturn(true);
    replay(userService, studentUser, userDetails);

    handler.onAuthenticationSuccess(request, response, token);

    assertEquals("/student", response.getRedirectedUrl());
    verify(userService, studentUser, userDetails);
  }

  @Test
  public void testOnAuthenticationSuccess_MicrosoftTeacher() throws Exception {
    MockHttpServletRequest request = new MockHttpServletRequest();
    MockHttpServletResponse response = new MockHttpServletResponse();

    Map<String, Object> claims = new HashMap<>();
    claims.put("sub", "ms-456");
    OidcIdToken idToken = new OidcIdToken("token", null, null, claims);
    OidcUser oidcUser = new DefaultOidcUser(Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")), idToken);

    OAuth2AuthenticationToken token = new OAuth2AuthenticationToken(oidcUser, oidcUser.getAuthorities(), "microsoft");

    expect(userService.retrieveUserByMicrosoftUserId("ms-456")).andReturn(teacherUser);
    expect(teacherUser.getUserDetails()).andReturn(userDetails);
    expect(userDetails.getAuthorities()).andReturn(Collections.emptyList());
    expect(teacherUser.isStudent()).andReturn(false);
    replay(userService, teacherUser, userDetails);

    handler.onAuthenticationSuccess(request, response, token);

    assertEquals("/teacher", response.getRedirectedUrl());
    verify(userService, teacherUser, userDetails);
  }

  @Test
  public void testOnAuthenticationSuccess_MicrosoftUserNotFound() throws Exception {
    MockHttpServletRequest request = new MockHttpServletRequest();
    MockHttpServletResponse response = new MockHttpServletResponse();

    Map<String, Object> claims = new HashMap<>();
    claims.put("sub", "ms-unknown");
    OidcIdToken idToken = new OidcIdToken("token", null, null, claims);
    OidcUser oidcUser = new DefaultOidcUser(Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")), idToken);

    OAuth2AuthenticationToken token = new OAuth2AuthenticationToken(oidcUser, oidcUser.getAuthorities(), "microsoft");

    expect(userService.retrieveUserByMicrosoftUserId("ms-unknown")).andReturn(null);
    replay(userService);

    handler.onAuthenticationSuccess(request, response, token);

    assertEquals("/join?microsoftUserNotFound=true", response.getRedirectedUrl());
    verify(userService);
  }

  @Test
  public void testOnAuthenticationSuccess_GoogleUserNotFound() throws Exception {
    MockHttpServletRequest request = new MockHttpServletRequest();
    MockHttpServletResponse response = new MockHttpServletResponse();

    Map<String, Object> claims = new HashMap<>();
    claims.put("sub", "google-unknown");
    OidcIdToken idToken = new OidcIdToken("token", null, null, claims);
    OidcUser oidcUser = new DefaultOidcUser(Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")), idToken);

    OAuth2AuthenticationToken token = new OAuth2AuthenticationToken(oidcUser, oidcUser.getAuthorities(), "google");

    expect(userService.retrieveUserByGoogleUserId("google-unknown")).andReturn(null);
    replay(userService);

    handler.onAuthenticationSuccess(request, response, token);

    assertEquals("/join?googleUserNotFound=true", response.getRedirectedUrl());
    verify(userService);
  }
}
