package org.wise.portal.presentation.web.filters;

import java.net.URL;
import java.security.interfaces.RSAPublicKey;
import java.util.Date;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.jwt.crypto.sign.RsaVerifier;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.exceptions.OAuth2Exception;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.wise.portal.service.authentication.UserDetailsService;

import com.auth0.jwk.Jwk;
import com.auth0.jwk.JwkProvider;
import com.auth0.jwk.UrlJwkProvider;

public abstract class AbstractOpenIdConnectFilter extends AbstractAuthenticationProcessingFilter {

  protected String clientId;
  protected String issuer;
  protected String jwkUrl;
  protected OAuth2RestTemplate openIdRestTemplate;

  @Autowired
  protected UserDetailsService userDetailsService;

  protected AbstractOpenIdConnectFilter(String defaultFilterProcessesUrl) {
    super(defaultFilterProcessesUrl);
    setAuthenticationManager(new NoopAuthenticationManager());
  }

  protected OAuth2AccessToken getAccessToken() {
    OAuth2AccessToken accessToken;
    try {
      accessToken = openIdRestTemplate.getAccessToken();
    } catch (final OAuth2Exception e) {
      throw new BadCredentialsException("Could not obtain access token", e);
    }
    return accessToken;
  }

  protected void saveRequestParams(HttpServletRequest request) {
    saveRequestParameter(request, "accessCode");
    saveRequestParameter(request, "redirectUrl");
  }

  protected void saveRequestParameter(HttpServletRequest request, String parameterName) {
    String parameterValue = request.getParameter(parameterName);
    String parameterFromState = (String) openIdRestTemplate.getOAuth2ClientContext()
        .removePreservedState(parameterName);
    openIdRestTemplate.getOAuth2ClientContext().setPreservedState(parameterName, parameterValue);
    request.setAttribute(parameterName, parameterFromState);
  }

  protected void verifyClaims(Map claims) {
    int exp = (int) claims.get("exp");
    Date expireDate = new Date(exp * 1000L);
    Date now = new Date();
    if (expireDate.before(now) || !claims.get("iss").equals(issuer)
        || !claims.get("aud").equals(clientId)) {
      throw new RuntimeException("Invalid claims");
    }
  }

  protected RsaVerifier verifier(String kid) throws Exception {
    JwkProvider provider = new UrlJwkProvider(new URL(jwkUrl));
    Jwk jwk = provider.get(kid);
    return new RsaVerifier((RSAPublicKey) jwk.getPublicKey());
  }

  protected void invalidateAccessToken() {
    openIdRestTemplate.getOAuth2ClientContext().setAccessToken((OAuth2AccessToken) null);
  }

  protected abstract void setClientId(String clientId);

  protected abstract void setIssuer(String issuer);

  protected abstract void setJwkUrl(String jwkUrl);

  protected abstract void setOpenIdRestTemplate(OAuth2RestTemplate template);
}
