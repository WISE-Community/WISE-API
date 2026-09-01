package org.wise.portal.presentation.web.exception;

import org.springframework.security.core.AuthenticationException;

public class TeacherVerificationException extends AuthenticationException {
  private static final long serialVersionUID = 1L;

  public TeacherVerificationException(String msg) {
    super(msg);
  }
}