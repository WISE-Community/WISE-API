/**
 * Copyright (c) 2008-2019 Regents of the University of California (Regents).
 * Created by WISE, Graduate School of Education, University of California, Berkeley.
 *
 * This software is distributed under the GNU General Public License, v3,
 * or (at your option) any later version.
 *
 * Permission is hereby granted, without written agreement and without license
 * or royalty fees, to use, copy, modify, and distribute this software and its
 * documentation for any purpose, provided that the above copyright notice and
 * the following two paragraphs appear in all copies of this software.
 *
 * REGENTS SPECIFICALLY DISCLAIMS ANY WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE. THE SOFTWARE AND ACCOMPANYING DOCUMENTATION, IF ANY, PROVIDED
 * HEREUNDER IS PROVIDED "AS IS". REGENTS HAS NO OBLIGATION TO PROVIDE
 * MAINTENANCE, SUPPORT, UPDATES, ENHANCEMENTS, OR MODIFICATIONS.
 *
 * IN NO EVENT SHALL REGENTS BE LIABLE TO ANY PARTY FOR DIRECT, INDIRECT,
 * SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES, INCLUDING LOST PROFITS,
 * ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
 * REGENTS HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.wise.portal.spring.impl;

import java.util.ArrayList;
import java.util.List;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSessionListener;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.boot.web.servlet.ServletListenerRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.orm.hibernate5.support.OpenSessionInViewFilter;
import org.springframework.security.access.AccessDecisionVoter;
import org.springframework.security.access.event.LoggerListener;
import org.springframework.security.access.vote.ConsensusBased;
import org.springframework.security.access.vote.RoleVoter;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.ExceptionTranslationFilter;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.security.web.servletapi.SecurityContextHolderAwareRequestFilter;
import org.springframework.security.web.session.HttpSessionEventPublisher;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.session.Session;
import org.wise.portal.presentation.web.filters.MicrosoftAuthenticationFailureHandler;
import org.wise.portal.presentation.web.filters.OAuth2AuthenticationFailureHandler;
import org.wise.portal.presentation.web.filters.OAuth2AuthenticationSuccessHandler;
import org.wise.portal.presentation.web.filters.WISEAuthenticationFailureHandler;
import org.wise.portal.presentation.web.filters.WISEAuthenticationProcessingFilter;
import org.wise.portal.presentation.web.filters.WISEAuthenticationSuccessHandler;
import org.wise.portal.presentation.web.filters.WISESwitchUserFilter;
import org.wise.portal.presentation.web.listeners.WISESessionListener;
import org.wise.portal.service.authentication.UserDetailsService;

@Configuration
@EnableWebSecurity(debug = true)
@Order(SecurityProperties.BASIC_AUTH_ORDER - 10)
public class WebSecurityConfig {

  @Autowired
  private UserDetailsService userDetailsService;

  // @Autowired
  // private AuthenticationManager authenticationManager;

  @Bean
  public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig)
      throws Exception {
    return authConfig.getAuthenticationManager();
  }

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http,
      AuthenticationManager authenticationManager) throws Exception {
    http.csrf(csrf -> csrf.disable())
        .securityContext(securityContext -> securityContext.requireExplicitSave(false))
        .sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
        .addFilterAfter(openSessionInViewFilter(), SecurityContextHolderAwareRequestFilter.class)
        .addFilterAfter(authenticationProcessingFilter(authenticationManager),
            SecurityContextHolderAwareRequestFilter.class)
        .authorizeHttpRequests(
            auth -> auth.requestMatchers(new AntPathRequestMatcher("/api/login/impersonate"))
                .hasAnyRole("ADMINISTRATOR", "RESEARCHER")
                .requestMatchers(new AntPathRequestMatcher("/admin/**"))
                .hasAnyRole("ADMINISTRATOR", "RESEARCHER")
                .requestMatchers(new AntPathRequestMatcher("/author/**")).hasAnyRole("TEACHER")
                .requestMatchers(new AntPathRequestMatcher("/project/notifyAuthor*/**"))
                .hasAnyRole("TEACHER")
                .requestMatchers(new AntPathRequestMatcher("/student/account/info"))
                .hasAnyRole("TEACHER").requestMatchers(new AntPathRequestMatcher("/student/**"))
                .hasAnyRole("STUDENT").requestMatchers(new AntPathRequestMatcher("/studentStatus"))
                .hasAnyRole("TEACHER", "STUDENT")
                .requestMatchers(new AntPathRequestMatcher("/oauth2/**")).permitAll()
                .requestMatchers(new AntPathRequestMatcher("/login/oauth2/**")).permitAll()
                .requestMatchers(new AntPathRequestMatcher("/api/google-login")).permitAll()
                .requestMatchers(new AntPathRequestMatcher("/api/*/register")).permitAll()
                .requestMatchers(new AntPathRequestMatcher("/api/teacher/**")).hasAnyRole("TEACHER")
                .requestMatchers(new AntPathRequestMatcher("/sso/discourse"))
                .hasAnyRole("TEACHER", "STUDENT")
                .requestMatchers(new AntPathRequestMatcher("/api/user/tags")).hasAnyRole("TEACHER")
                .requestMatchers(new AntPathRequestMatcher("/api/user/tag/**"))
                .hasAnyRole("TEACHER").requestMatchers(new AntPathRequestMatcher("/api/debug/**"))
                .permitAll().requestMatchers(new AntPathRequestMatcher("/api/user/info"))
                .permitAll().requestMatchers(new AntPathRequestMatcher("/api/user/config"))
                .permitAll().requestMatchers(new AntPathRequestMatcher("/login")).permitAll()
                .requestMatchers(new AntPathRequestMatcher("/")).permitAll().anyRequest()
                .authenticated())
        .formLogin(form -> form.loginPage("/login").permitAll())
        .oauth2Login(oauth2 -> oauth2.loginPage("/login")
            .successHandler(oauth2AuthenticationSuccessHandler())
            .failureHandler(oauth2AuthenticationFailureHandler()))
        .logout(logout -> logout.addLogoutHandler(wiseLogoutHandler())
            .logoutRequestMatcher(new AntPathRequestMatcher("/api/logout"))
            .logoutSuccessHandler((request, response, authentication) -> response
                .setStatus(HttpServletResponse.SC_OK)))
        .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()));

    return http.build();
  }

  @Bean
  public WISEAuthenticationProcessingFilter authenticationProcessingFilter(
      AuthenticationManager authenticationManager) {
    WISEAuthenticationProcessingFilter filter = new WISEAuthenticationProcessingFilter();
    filter.setAuthenticationManager(authenticationManager);
    filter.setAuthenticationSuccessHandler(authSuccessHandler());
    filter.setAuthenticationFailureHandler(authFailureHandler());
    filter.setFilterProcessesUrl("/api/j_acegi_security_check");
    return filter;
  }

  @Bean
  public OpenSessionInViewFilter openSessionInViewFilter() {
    return new OpenSessionInViewFilter();
  }

  @Bean
  public OAuth2AuthenticationSuccessHandler oauth2AuthenticationSuccessHandler() {
    return new OAuth2AuthenticationSuccessHandler();
  }

  @Bean
  public OAuth2AuthenticationFailureHandler oauth2AuthenticationFailureHandler() {
    return new OAuth2AuthenticationFailureHandler();
  }

  @Bean
  public LoggerListener loggerListener() {
    return new LoggerListener();
  }

  @Bean
  public LoginUrlAuthenticationEntryPoint authenticationEntryPoint() {
    return new LoginUrlAuthenticationEntryPoint("/login");
  }

  @Bean
  public ExceptionTranslationFilter exceptionTranslationFilter() {
    return new ExceptionTranslationFilter(authenticationEntryPoint());
  }

  @Bean
  public RoleVoter roleVoter() {
    return new RoleVoter();
  }

  @Bean
  public LogoutFilter logoutFilter() {
    LogoutHandler[] handlers = new LogoutHandler[] { new SecurityContextLogoutHandler() };
    return new LogoutFilter("/", handlers);
  }

  @Bean
  public HttpSessionEventPublisher httpSessionEventPublisher() {
    return new HttpSessionEventPublisher();
  }

  @Bean
  public ServletListenerRegistrationBean<HttpSessionListener> sessionListener() {
    return new ServletListenerRegistrationBean<HttpSessionListener>(new WISESessionListener());
  }

  @Bean
  public AuthenticationSuccessHandler authSuccessHandler() {
    WISEAuthenticationSuccessHandler handler = new WISEAuthenticationSuccessHandler();
    handler.setDefaultTargetUrl("/student");
    return handler;
  }

  @Bean
  public AuthenticationFailureHandler authFailureHandler() {
    WISEAuthenticationFailureHandler handler = new WISEAuthenticationFailureHandler();
    handler.setAuthenticationFailureUrl("/login?failed=true");
    return handler;
  }

  @Bean
  public AuthenticationFailureHandler microsoftAuthFailureHandler() {
    return new MicrosoftAuthenticationFailureHandler();
  }

  @Bean
  public ConsensusBased urlAccessDecisionManager() {
    List<AccessDecisionVoter<? extends Object>> decisionVoters = new ArrayList<>();
    decisionVoters.add(roleVoter());
    ConsensusBased manager = new ConsensusBased(decisionVoters);
    manager.setAllowIfAllAbstainDecisions(false);
    return manager;
  }

  @Bean
  public WISESwitchUserFilter switchUserProcessingFilter() {
    WISESwitchUserFilter filter = new WISESwitchUserFilter();
    filter.setUserDetailsService(userDetailsService);
    filter.setSwitchUserUrl("/api/login/impersonate");
    filter.setExitUserUrl("/api/logout/impersonate");
    filter.setSuccessHandler(authSuccessHandler());
    return filter;
  }

  @Bean
  public WISELogoutHandler<Session> wiseLogoutHandler() {
    return new WISELogoutHandler<Session>();
  }
}
