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

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.RequestBuilder;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

/**
 * Exercises the administrator-only boundary around user and account management through the real
 * Spring Security filter chain, so that it cannot regress. The boundary is enforced by two layers
 * that both run in the chain: the {@link WebSecurityConfig} URL rules and the controllers'
 * {@code @Secured} method security. Where both layers guard a path these tests assert the
 * effective decision (is the request denied?) rather than isolating a single layer. Controller
 * unit tests instantiate the controller directly and never go through the security proxy, so they
 * cannot cover this.
 *
 * The whole web context is started because the authorization rules are only wired up as part of
 * it. That context connects to Redis on startup (session repository and a message listener), so a
 * Redis container is provided the same way the data store integration tests do.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
public class WebSecurityConfigAuthorizationTest {

  @Container
  static GenericContainer<?> redisContainer =
      new GenericContainer<>(DockerImageName.parse("redis:7-alpine")).withExposedPorts(6379);

  @DynamicPropertySource
  static void redisProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.redis.host", redisContainer::getHost);
    registry.add("spring.redis.port", redisContainer::getFirstMappedPort);
  }

  @Autowired
  private MockMvc mockMvc;

  @Test
  public void researcher_userAndAccountManagement_shouldBeForbidden() throws Exception {
    mockMvc.perform(get("/admin/account/show-all-users").with(user("researcher").roles("RESEARCHER")))
        .andExpect(status().isForbidden());
  }

  @Test
  public void researcher_trailingSlashOnExactAdminPath_shouldBeForbidden() throws Exception {
    // The trailing-slash form of an exact administrator path must stay denied to researchers.
    // Both the mvcMatchers URL rule and AdminUtilsController's @Secured enforce this, so a pass
    // confirms the endpoint is denied, not specifically which layer denied it.
    mockMvc.perform(get("/admin/mergeProjectMetadata/").with(user("researcher").roles("RESEARCHER")))
        .andExpect(status().isForbidden());
  }

  @Test
  public void administrator_userAndAccountManagement_shouldBeAuthorized() throws Exception {
    // Positive counterpart to researcher_userAndAccountManagement_shouldBeForbidden on the same
    // path: the rule restricts by role rather than blocking the path outright.
    assertAuthorized(get("/admin/account/show-all-users").with(user("admin").roles("ADMINISTRATOR")));
  }

  @Test
  public void researcher_nonAccountAdminEndpoint_shouldStayAuthorized() throws Exception {
    // Researchers keep access to the rest of /admin/**. RunStatisticsController has no method
    // security, so this exercises the URL rule directly rather than a method-security backstop.
    assertAuthorized(get("/admin/run/stats").with(user("researcher").roles("RESEARCHER")));
  }

  /**
   * Asserts the request passes authorization. An authorization denial short-circuits with a 403
   * response before the handler runs, so reaching the handler means the request was authorized.
   * The handler may then return any status or fail, because the test authenticates with a mock
   * principal rather than a persisted WISE user; such a downstream failure still counts as
   * authorized. A propagated {@link AccessDeniedException} is the one exception that does mean a
   * denial, so it is re-thrown rather than swallowed.
   */
  private void assertAuthorized(RequestBuilder request) throws Exception {
    MvcResult result;
    try {
      result = mockMvc.perform(request).andReturn();
    } catch (Exception e) {
      if (isAccessDenied(e)) {
        throw e;
      }
      return;
    }
    assertNotEquals(HttpStatus.FORBIDDEN.value(), result.getResponse().getStatus(),
        "Expected the request to be authorized, but it was forbidden (403).");
  }

  private static boolean isAccessDenied(Throwable throwable) {
    for (Throwable cause = throwable; cause != null; cause = cause.getCause()) {
      if (cause instanceof AccessDeniedException) {
        return true;
      }
    }
    return false;
  }
}
