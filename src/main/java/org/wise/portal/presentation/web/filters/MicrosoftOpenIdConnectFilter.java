package org.wise.portal.presentation.web.filters;

import java.io.IOException;
import java.net.URL;
import java.security.interfaces.RSAPublicKey;
import java.util.Date;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.jwt.Jwt;
import org.springframework.security.jwt.JwtHelper;
import org.springframework.security.jwt.crypto.sign.RsaVerifier;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.exceptions.OAuth2Exception;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.wise.portal.presentation.web.exception.MicrosoftUserNotFoundException;
import org.wise.portal.service.authentication.UserDetailsService;

import com.auth0.jwk.Jwk;
import com.auth0.jwk.JwkProvider;
import com.auth0.jwk.UrlJwkProvider;
import com.fasterxml.jackson.databind.ObjectMapper;

public class MicrosoftOpenIdConnectFilter extends AbstractAuthenticationProcessingFilter {

  @Value("${microsoft.clientId:}")
  private String microsoftClientId;

  @Value("${microsoft.issuer:}")
  private String microsoftIssuer;

  @Value("${microsoft.jwkUrl:}")
  private String microsoftJwkUrl;

  @Autowired
  @Qualifier("microsoftOpenIdRestTemplate")
  private OAuth2RestTemplate microsoftOpenIdRestTemplate;

  @Autowired
  private UserDetailsService userDetailsService;

  public MicrosoftOpenIdConnectFilter(String defaultFilterProcessesUrl) {
    super(defaultFilterProcessesUrl);
    setAuthenticationManager(new NoopAuthenticationManager());
  }

  @Override
  public Authentication attemptAuthentication(HttpServletRequest request,
      HttpServletResponse response) throws AuthenticationException, IOException, ServletException {
    saveRequestParameter(request, "redirectUrl");
    OAuth2AccessToken accessToken;
    try {
      accessToken = microsoftOpenIdRestTemplate.getAccessToken();
    } catch (final OAuth2Exception e) {
      throw new BadCredentialsException("Could not obtain access token", e);
    }
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
    String microsoftUserId = authInfo.get("sub");
    final UserDetails user = userDetailsService.loadUserByMicrosoftUserId(microsoftUserId);
    invalidateAccessToken();
    if (user != null) {
      return new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
    } else {
      throw new MicrosoftUserNotFoundException("Microsoft user not found", authInfo);
    }
  }

  private void saveRequestParameter(HttpServletRequest request, String parameterName) {
    String parameterValue = request.getParameter(parameterName);
    String parameterFromState = (String) microsoftOpenIdRestTemplate.getOAuth2ClientContext()
        .removePreservedState(parameterName);
    microsoftOpenIdRestTemplate.getOAuth2ClientContext().setPreservedState(parameterName,
        parameterValue);
    request.setAttribute(parameterName, parameterFromState);
  }

  private void verifyClaims(Map claims) {
    int exp = (int) claims.get("exp");
    Date expireDate = new Date(exp * 1000L);
    Date now = new Date();
    if (expireDate.before(now) || !claims.get("iss").equals(microsoftIssuer)
        || !claims.get("aud").equals(microsoftClientId)) {
      throw new RuntimeException("Invalid claims");
    }
  }

  private RsaVerifier verifier(String kid) throws Exception {
    JwkProvider provider = new UrlJwkProvider(new URL(microsoftJwkUrl));
    Jwk jwk = provider.get(kid);
    return new RsaVerifier((RSAPublicKey) jwk.getPublicKey());
  }

  private void invalidateAccessToken() {
    microsoftOpenIdRestTemplate.getOAuth2ClientContext().setAccessToken((OAuth2AccessToken) null);
  }
}
