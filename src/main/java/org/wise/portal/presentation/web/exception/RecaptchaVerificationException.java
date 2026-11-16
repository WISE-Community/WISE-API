package org.wise.portal.presentation.web.exception;

import java.io.Serial;
import org.springframework.security.core.AuthenticationException;

public class RecaptchaVerificationException extends AuthenticationException {

  @Serial
  private static final long serialVersionUID = 1L;

  public RecaptchaVerificationException(String msg) {
    super(msg);
  }
}
