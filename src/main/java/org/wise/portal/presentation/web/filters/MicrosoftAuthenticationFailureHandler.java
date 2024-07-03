package org.wise.portal.presentation.web.filters;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.transaction.annotation.Transactional;
import org.wise.portal.presentation.web.exception.MicrosoftUserNotFoundException;

public class MicrosoftAuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {

  @Override
  @Transactional
  public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
      AuthenticationException exception) throws IOException, ServletException {
    if (request.getAttribute("redirectUrl").toString().contains("join")) {
      String microsoftId = ((MicrosoftUserNotFoundException) exception).getMicrosoftId();
      String name = ((MicrosoftUserNotFoundException) exception).getName();
      String email = ((MicrosoftUserNotFoundException) exception).getEmail();
      response.sendRedirect(request.getAttribute("redirectUrl").toString() + ";mID=" + microsoftId
          + ";name=" + name + ";email=" + email);
    } else {
      response.sendRedirect("/join?microsoftUserNotFound=true");
    }
  }
}
