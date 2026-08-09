package org.wise.portal.service.authentication;

public class DuplicateVerificationCodeException extends Exception {

  private static final long serialVersionUID = 1L;

  private String message;

  public DuplicateVerificationCodeException(String verificationCode) {
    message = "Verification code:" + verificationCode + " already in use.";
  }

  public String getMessage() {
    return message;
  }
}
