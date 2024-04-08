package org.wise.portal.presentation.web.filters;

import java.io.IOException;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.jwt.Jwt;
import org.springframework.security.jwt.JwtHelper;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.wise.portal.presentation.web.exception.MicrosoftUserNotFoundException;

import com.fasterxml.jackson.databind.ObjectMapper;

public class MicrosoftOpenIdConnectFilter extends AbstractOpenIdConnectFilter {

  public MicrosoftOpenIdConnectFilter(String defaultFilterProcessesUrl) {
    super(defaultFilterProcessesUrl);
  }

  @Override
  public Authentication attemptAuthentication(HttpServletRequest request,
      HttpServletResponse response) throws AuthenticationException, IOException, ServletException {
    saveRequestParams(request);
    OAuth2AccessToken accessToken = getAccessToken();
    final String idToken = accessToken.getAdditionalInformation().get("id_token").toString();
    String kid = JwtHelper.headers(idToken).get("kid");
    Jwt tokenDecoded = null;
    try {
      tokenDecoded = JwtHelper.decodeAndVerify(idToken, verifier(kid));
    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    final Map<String, String> authInfo = new ObjectMapper().readValue(tokenDecoded.getClaims(),
        Map.class);
    verifyClaims(authInfo);
    final UserDetails user = userDetailsService.loadUserByMicrosoftUserId(authInfo.get("sub"));
    invalidateAccessToken();
    if (user != null) {
      if (request.getAttribute("redirectUrl").toString().contains("join")) {
        response.sendRedirect("/join/microsoftUserAlreadyExists");
        return null;
      } else {
        return new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
      }
    } else {
      throw new MicrosoftUserNotFoundException("Microsoft user not found", authInfo);
    }
  }

  @Value("${microsoft.clientId:}")
  protected void setClientId(String clientId) {
    this.clientId = clientId;
  }

  @Value("${microsoft.issuer:}")
  protected void setIssuer(String issuer) {
    this.issuer = issuer;
  }

  @Value("${microsoft.jwkUrl:}")
  protected void setJwkUrl(String jwkUrl) {
    this.jwkUrl = jwkUrl;
  }

  @Autowired
  @Qualifier("microsoftOpenIdRestTemplate")
  protected void setOpenIdRestTemplate(OAuth2RestTemplate template) {
    this.openIdRestTemplate = template;
  }
}
