package org.wise.portal.presentation.web.filters;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

public class LoginRedirectUrlParameterFilter implements Filter {
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String redirectUrl = httpRequest.getParameter("redirectUrl");

        // If the parameter exists, cache it in the session
        if (redirectUrl != null) {
            HttpSession session = httpRequest.getSession(true);
            session.setAttribute("SAVED_REDIRECT_URL", redirectUrl);
        }

        chain.doFilter(request, response);
    }
}
