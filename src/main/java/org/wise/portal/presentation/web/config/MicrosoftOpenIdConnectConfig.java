package org.wise.portal.presentation.web.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.OAuth2ClientContext;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.resource.OAuth2ProtectedResourceDetails;
import org.springframework.security.oauth2.client.token.grant.code.AuthorizationCodeResourceDetails;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableOAuth2Client;

import java.util.Arrays;

@Configuration
@EnableOAuth2Client
public class MicrosoftOpenIdConnectConfig {

  @Value("${microsoft.accessTokenUri:}")
  private String accessTokenUri;

  @Value("${microsoft.clientId:}")
  private String clientId;

  @Value("${microsoft.clientSecret:}")
  private String clientSecret;

  @Value("${microsoft.redirectUri:}")
  private String redirectUri;

  @Value("${microsoft.userAuthorizationUri:}")
  private String userAuthorizationUri;

  @Bean("microsoftOAuth2ProtectedResourceDetails")
  public OAuth2ProtectedResourceDetails microsoftOpenId() {
    final AuthorizationCodeResourceDetails details = new AuthorizationCodeResourceDetails();
    details.setClientId(clientId);
    details.setClientSecret(clientSecret);
    details.setAccessTokenUri(accessTokenUri);
    details.setUserAuthorizationUri(userAuthorizationUri);
    details.setScope(Arrays.asList("openid", "email", "profile"));
    details.setPreEstablishedRedirectUri(redirectUri);
    details.setUseCurrentUri(false);
    return details;
  }

  @Bean("microsoftOpenIdRestTemplate")
  public OAuth2RestTemplate microsoftOpenIdRestTemplate(final OAuth2ClientContext clientContext) {
    final OAuth2RestTemplate template = new OAuth2RestTemplate(microsoftOpenId(), clientContext);
    return template;
  }
}
