package org.wise.portal.presentation.web.filters;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Component;
import org.wise.portal.domain.user.User;
import org.wise.portal.service.user.UserService;

import java.io.IOException;

/**
 * Handles successful OAuth2/OpenID Connect authentication.
 * Looks up the WISE user by their Google or Microsoft user ID and creates a proper authentication
 * with WISE UserDetails (Teacher or Student).
 */
@Component
public class OAuth2AuthenticationSuccessHandler implements AuthenticationSuccessHandler {

  @Autowired
  private UserService userService;

  @Override
  public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
      Authentication authentication) throws IOException, ServletException {

    String registrationId = null;
    if (authentication instanceof OAuth2AuthenticationToken) {
      registrationId = ((OAuth2AuthenticationToken) authentication)
          .getAuthorizedClientRegistrationId();
    }

    String userId = null;
    String name = "";
    String email = "";
    if (authentication.getPrincipal() instanceof OidcUser) {
      OidcUser oidcUser = (OidcUser) authentication.getPrincipal();
      userId = oidcUser.getSubject();
      name = oidcUser.getFullName();
      email = oidcUser.getEmail();
    } else if (authentication.getPrincipal() instanceof OAuth2User) {
      OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
      userId = oauth2User.getName();
    }

    if (userId != null) {
      boolean isMicrosoft = "microsoft".equalsIgnoreCase(registrationId);
      String notFoundRedirect = isMicrosoft ? "/join?microsoftUserNotFound=true"
        : "/join?googleUserNotFound=true";
      HttpSession session = request.getSession(false);
      if (session != null && isMicrosoft) {
        String redirectUrl = (String) session.getAttribute("SAVED_REDIRECT_URL");
        if (redirectUrl != null && redirectUrl.startsWith("/join")) {
          notFoundRedirect = redirectUrl + ";mID=" + userId + ";name=" + name + ";email=" + email;
        }
      }


      try {
        User user = isMicrosoft ? userService.retrieveUserByMicrosoftUserId(userId)
          : userService.retrieveUserByGoogleUserId(userId);

        if (user != null) {
          saveUserToSession(request, user);
          if (user.isStudent()) {
            response.sendRedirect("/student");
          } else {
            response.sendRedirect("/teacher");
          }
          return;
        } else {
          invalidateSession(request);
          response.sendRedirect(notFoundRedirect);
          return;
        }
      } catch (Exception e) {
        invalidateSession(request);
        response.sendRedirect(notFoundRedirect);
        return;
      }
    }

    response.sendRedirect("/");
  }

  private void saveUserToSession(HttpServletRequest request, User user) {
    UserDetails userDetails = user.getUserDetails();
    UsernamePasswordAuthenticationToken wiseAuth = new UsernamePasswordAuthenticationToken(
        userDetails, null, userDetails.getAuthorities());
    SecurityContext context = SecurityContextHolder.createEmptyContext();
    context.setAuthentication(wiseAuth);
    SecurityContextHolder.setContext(context);
    HttpSession session = request.getSession(true);
    session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, context);
  }

  private void invalidateSession(HttpServletRequest request) {
    HttpSession session = request.getSession(false);
    if (session != null) {
      session.invalidate();
    }
  }
}
