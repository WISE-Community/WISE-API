package org.wise.portal.presentation.web.exception;

public class MessageCodeException extends Exception {
  private static final long serialVersionUID = 1L;
  protected String messageCode;

  public MessageCodeException(String messageCode) {
    this.messageCode = messageCode;
  }

  public String getMessageCode() {
    return this.messageCode;
  }
}
