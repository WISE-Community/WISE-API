package org.wise.portal.presentation.web.exception;

public class TagAlreadyExistsException extends MessageCodeException {
  private static final long serialVersionUID = 1L;

  public TagAlreadyExistsException() {
    super("tagAlreadyExists");
  }
}
