package org.wise.portal.presentation.web.exception;

import java.io.Serial;

public class TagAlreadyExistsException extends MessageCodeException {

  @Serial
  private static final long serialVersionUID = 1L;

  public TagAlreadyExistsException() {
    super("tagAlreadyExists");
  }
}
