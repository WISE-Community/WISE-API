package org.wise.portal.presentation.web.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.security.oauth2.client.DefaultOAuth2ClientContext;
import org.springframework.security.oauth2.client.OAuth2ClientContext;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.resource.OAuth2ProtectedResourceDetails;
import org.springframework.security.oauth2.client.token.AccessTokenRequest;
import org.springframework.security.oauth2.client.token.DefaultAccessTokenRequest;
import org.springframework.security.oauth2.client.token.grant.code.AuthorizationCodeResourceDetails;

import java.util.Arrays;

@Configuration
public class GoogleOpenIdConnectConfig {

  @Value("${google.clientId:}")
  private String googleClientId;

  @Value("${google.clientSecret:}")
  private String googleClientSecret;

  @Value("${google.accessTokenUri:}")
  private String googleAccessTokenUri;

  @Value("${google.userAuthorizationUri:}")
  private String googleUserAuthorizationUri;

  @Value("${google.redirectUri:}")
  private String googleRedirectUri;

  @Bean
  public OAuth2ProtectedResourceDetails googleOpenId() {
    final AuthorizationCodeResourceDetails details = new AuthorizationCodeResourceDetails();
    details.setClientId(googleClientId);
    details.setClientSecret(googleClientSecret);
    details.setAccessTokenUri(googleAccessTokenUri);
    details.setUserAuthorizationUri(googleUserAuthorizationUri);
    details.setScope(Arrays.asList("openid", "email"));
    details.setPreEstablishedRedirectUri(googleRedirectUri);
    details.setUseCurrentUri(false);
    return details;
  }

  @Bean
  @Scope(value = "session", proxyMode = ScopedProxyMode.INTERFACES)
  public OAuth2ClientContext oauth2ClientContext() {
    return new DefaultOAuth2ClientContext(new DefaultAccessTokenRequest());
  }

  @Bean
  @Scope(value = "session", proxyMode = ScopedProxyMode.TARGET_CLASS)
  public OAuth2RestTemplate googleOpenIdRestTemplate(
      @Qualifier("oauth2ClientContext") OAuth2ClientContext clientContext) {
    final OAuth2RestTemplate template = new OAuth2RestTemplate(googleOpenId(), clientContext);
    return template;
  }

}
