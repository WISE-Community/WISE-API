package org.wise.portal.presentation.web.exception;

import java.io.Serial;

public class InvalidNameException extends MessageCodeException {

  @Serial
  private static final long serialVersionUID = 1L;

  public InvalidNameException(String messageCode) {
    super(messageCode);
  }
}
