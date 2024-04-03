package org.wise.portal.presentation.web.exception;

import java.util.Map;

import org.springframework.security.core.AuthenticationException;

public class MicrosoftUserNotFoundException extends AuthenticationException {
  private static final long serialVersionUID = 1L;
  private Map<String, String> authInfo;

  public MicrosoftUserNotFoundException(String msg, Map<String, String> authInfo) {
    super(msg);
    this.authInfo = authInfo;
  }

  public String getEmail() {
    return authInfo.get("email");
  }

  public String getMicrosoftId() {
    return authInfo.get("sub");
  }

  public String getName() {
    return authInfo.get("name");
  }
}
