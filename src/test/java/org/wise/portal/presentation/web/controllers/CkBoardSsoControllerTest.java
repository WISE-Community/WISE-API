package org.wise.portal.presentation.web.controllers;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;

import org.easymock.EasyMockRunner;
import org.easymock.TestSubject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.web.servlet.view.RedirectView;

@RunWith(EasyMockRunner.class)
public class CkBoardSsoControllerTest extends APIControllerTest {

  @TestSubject
  private CkBoardSsoController ckBoardSsoController = new CkBoardSsoController();

  @Test
  public void ckBoardSsoLogin() throws Exception {
    expect(appProperties.getProperty("ck_board_url")).andReturn("http://localhost:4201");
    expect(appProperties.getProperty("ck_board_sso_secret_key")).andReturn("VV5jf6rXt4zrbBuH");
    expect(userService.retrieveUserByUsername(teacherAuth.getName())).andReturn(teacher1);
    replay(appProperties, userService);
    String payload = "bm9uY2U9ZTFiYzIwM2YtYTE0My00ODgzLWI1OGQtODJjNzgyN2JjNjU5";
    String sig = "b98379e2e6d3e86a03e281f8d96bf7335d1ffc1670b69bb79f23e1cbfc20e676";
    String redirectUrl = "/dashboard";
    ckBoardSsoController.init();
    RedirectView ckBoardSsoLoginRedirect = ckBoardSsoController.ckBoardSsoLogin(payload, sig,
        redirectUrl, teacherAuth);
    String expectedckBoardSsoLoginRedirectUrl = "http://localhost:4201/sso/login/bm9uY2U9ZTFiYzIwM2"
        + "YtYTE0My00ODgzLWI1OGQtODJjNzgyN2JjNjU5JnVzZXItaWQ9OTQyMTAmdXNlcm5hbWU9U3F1aWR3YXJkVGVudG"
        + "FjbGVzJnJvbGU9dGVhY2hlciZyZWRpcmVjdC11cmw9L2Rhc2hib2FyZA%3D%3D/f842d22f6cedb8541297337eb"
        + "c27b115d93f6fcc6cab4989a78bd2cb96d141ea";
    assertEquals(expectedckBoardSsoLoginRedirectUrl, ckBoardSsoLoginRedirect.getUrl());
    verify(appProperties, userService);
  }
}
