package org.wise.portal.presentation.web.exception;

import org.springframework.security.core.AuthenticationException;

public class RecaptchaVerificationException extends AuthenticationException {
  private static final long serialVersionUID = 1L;

  public RecaptchaVerificationException(String msg) {
    super(msg);
  }
}
