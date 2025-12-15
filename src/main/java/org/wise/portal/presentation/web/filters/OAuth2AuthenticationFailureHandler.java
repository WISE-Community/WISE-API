package org.wise.portal.presentation.web.filters;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Handles failed OAuth2/OpenID Connect authentication attempts.
 * Redirects to an error page or login page with error message.
 */
@Component
public class OAuth2AuthenticationFailureHandler implements AuthenticationFailureHandler {

	@Override
	public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
	    AuthenticationException exception) throws IOException, ServletException {

		// Log the error for debugging
		System.err.println("OAuth2 authentication failed: " + exception.getMessage());
		exception.printStackTrace();

		// Redirect to login page with error parameter
		response.sendRedirect("/login?error=oauth2&message="
		    + java.net.URLEncoder.encode(exception.getMessage(), "UTF-8"));
	}
}
